package main

import (
	"bytes"
	"context"
	"errors"
	"fmt"
	"io"
	"log"
	"mime/multipart"
	"net/http"
	"net/textproto"
	"regexp"
	"strings"
)

type serviceAPI interface {
	createTrack(*createTrackRequest) (*createTrackResponse, error)
	generic(*genericRequest) (*genericResponse, error)
}

type service struct {
	upload              uploaderAPI
	gatekeeperClient    gatekeeperAPI
	authenticatorClient authenticatorAPI
}

type createTrackRequest struct {
	boundary string
	request  *http.Request
}

type createTrackResponse struct {
	request *http.Request
}

type genericRequest struct {
	boundary string
	request  *http.Request
}

type genericResponse struct {
	request *http.Request
}

type multiPartRequest struct {
	request  *http.Request
	boundary string
}

type rewriter struct {
	ctx                context.Context
	authHeader         string
	additionalHeaders  http.Header
	body               *bytes.Buffer
	writer             *multipart.Writer
	uploadTrackRequest *uploadTrackRequest
}

var /* const */ forbiddenTrackFields = []string{
	"uid",
	"original_filename",
	"replacing_uid",
	"replacing_original_filename",
}

var /* const */ forbiddenFieldPattern = regexp.MustCompile(
	fmt.Sprintf(`track\[(%s)\]`, strings.Join(forbiddenTrackFields, "|")),
)

func (s service) createTrack(r *createTrackRequest) (*createTrackResponse, error) {
	rewriter, err := s.configureRewriter(multiPartRequest{r.request, r.boundary})
	if err != nil || rewriter == nil {
		return nil, err
	}

	if rewriter.uploadTrackRequest != nil {
		if err := s.uploadTrackWithFields(rewriter); err != nil {
			_ = rewriter.writer.Close()
			return nil, err
		}
	}

	req, err := rewriter.rewrite(r.request)
	if err != nil {
		return nil, err
	}

	return &createTrackResponse{request: req}, nil
}

func (s service) generic(r *genericRequest) (*genericResponse, error) {
	rewriter, err := s.configureRewriter(multiPartRequest{r.request, r.boundary})
	if err != nil {
		return nil, err
	}

	req, err := rewriter.rewrite(r.request)
	if err != nil {
		return nil, err
	}

	return &genericResponse{request: req}, nil
}

func (s service) configureRewriter(r multiPartRequest) (*rewriter, error) {
	if r.request == nil || r.request.Header == nil || r.request.Body == nil {
		return nil, clientError{errors.New("invalid http request")}
	}

	body := &bytes.Buffer{}
	reader := multipart.NewReader(r.request.Body, r.boundary)
	writer := multipart.NewWriter(body)

	if err := writer.SetBoundary(r.boundary); err != nil {
		return nil, clientError{cause: err}
	}

	rewriter := &rewriter{
		ctx:               r.request.Context(),
		authHeader:        r.request.Header.Get("Authorization"),
		additionalHeaders: http.Header{},
		body:              body,
		writer:            writer,
	}

	for {
		p, err := reader.NextPart()
		if err != nil {
			if err == io.EOF {
				break // Processed all parts.
			}
			_ = writer.Close()
			return nil, clientError{cause: err}
		}

		//iterate through parts
		if err := s.processPart(p, rewriter); err != nil {
			_ = writer.Close()
			return nil, err
		}
	}

	return rewriter, nil
}

func (r rewriter) rewrite(req *http.Request) (*http.Request, error) {
	if err := r.writer.Close(); err != nil {
		return nil, err
	}
	// for debugging AUTH-2326
	if authorizationHeader := r.additionalHeaders.Get("Authorization"); authorizationHeader != "" {
		log.Printf("in modifyRequest Authorization %s", maskToken(authorizationHeader))
	}

	for h := range r.additionalHeaders {
		req.Header.Set(h, r.additionalHeaders.Get(h))
	}

	req.ContentLength = int64(r.body.Len())
	req.Body = io.NopCloser(r.body)

	return req, nil
}

func (s service) processPart(p *multipart.Part, r *rewriter) error {
	if err := validateFormName(p); err != nil {
		return err
	}

	switch p.FormName() {
	case "oauth_token":
		return s.extractOAuth(p, r) // do not propagate oauth_token
	default:
		if isTrackUpload(p) {
			u, err := s.copyToUploadTrackRequest(p)
			if err != nil {
				return err
			}
			r.uploadTrackRequest = u
			return nil // do not propagate track[asset_data]
		} else {
			return copyMultipart(p.Header, r.writer, p)
		}
	}
}

func (s service) extractOAuth(p *multipart.Part, r *rewriter) error {
	// for debug: AUTH-2326
	if r.authHeader != "" {
		log.Printf("oauth_token part comes with Authorization %s header", maskToken(r.authHeader))
	} else {
		log.Printf("oauth_token part comes without Authorization header")
	}

	token, err := s.extractAuthToken(p)
	if err != nil {
		return err
	}
	if token.Len() > 0 {
		r.authHeader = fmt.Sprintf("OAuth %s", token.String())
		r.additionalHeaders.Add("Authorization", r.authHeader)
		// for debug: AUTH-2326
		log.Printf("setting Authorization header as OAuth %s", maskToken(r.authHeader))
	}
	return nil
}

func (s service) copyToUploadTrackRequest(p *multipart.Part) (*uploadTrackRequest, error) {
	filename := p.FileName()
	if len(filename) > 255 {
		return nil, fileNameValidationError{}
	}

	buf := &bytes.Buffer{}
	bytes, err := io.Copy(buf, p)
	if err != nil {
		return nil, err
	}

	u := &uploadTrackRequest{
		data:     buf,
		filename: filename,
		fileSize: bytes,
	}

	return u, nil

}

func (s service) uploadTrackWithFields(r *rewriter) error {
	resp, err := s.GetSessionWithFeatures(r.ctx, r.authHeader)
	if err != nil {
		return err
	}

	r.uploadTrackRequest.session = resp
	upload, err := s.uploadTrackAssetData(r.uploadTrackRequest, r.writer)
	if err != nil {
		return err
	}
	r.additionalHeaders.Add("X-Track-Asset-Location", upload.location)
	r.additionalHeaders.Add("X-Track-Asset-Md5", upload.md5)

	return nil
}

func (s service) uploadTrackAssetData(u *uploadTrackRequest, w *multipart.Writer) (*uploadTrackResponse, error) {
	res, err := s.upload.uploadTrack(u)
	if err != nil {
		return nil, err
	}

	if err := addField("track[original_filename]", u.filename, w); err != nil {
		return nil, err
	}

	if err := addField("track[uid]", res.uid, w); err != nil {
		return nil, err
	}

	return res, nil
}

func validateFormName(p *multipart.Part) error {
	if forbiddenFieldPattern.MatchString(p.FormName()) {
		return clientError{}
	}
	return nil
}

func isTrackUpload(p *multipart.Part) bool {
	if p.FormName() == "track[asset_data]" {
		return true
	}

	// SoundCloud's Desktop Sharing Kit doesn't escape filenames correctly,
	// which can result in an empty FormName here. Check the raw header value
	// to see if this part contains track asset data.
	if p.FormName() == "" {
		header := p.Header.Values("Content-Disposition")
		for _, val := range header {
			if strings.Contains(val, `name="track[asset_data]"`) {
				return true
			}
		}
	}
	return false
}

func copyMultipart(h textproto.MIMEHeader, w *multipart.Writer, src io.Reader) error {
	dst, err := w.CreatePart(h)
	if err != nil {
		return err
	}

	if _, err := io.Copy(dst, src); err != nil {
		return err
	}

	return nil
}

func addField(name string, value string, w *multipart.Writer) error {
	ioWriter, err := w.CreateFormField(name)
	if err != nil {
		return err
	}

	if _, err := ioWriter.Write([]byte(value)); err != nil {
		return err
	}

	return nil
}

// Ensure that service implements serviceAPI.
var _ serviceAPI = (*service)(nil)
