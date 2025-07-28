package main

import (
	"bytes"
	"fmt"
	"io"
	"log"
	"mime/multipart"
	"net/http"
	"regexp"
	"strings"
)

type serviceAPI interface {
	createTrack(*createTrackRequest) (*createTrackResponse, error)
	generic(*genericRequest) (*genericResponse, error)
}

type service struct {
	upload uploaderAPI
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

type rewritePartFn func(*multipart.Part, *multipart.Writer, http.Header) error

type clientError struct {
	cause error
}

func (e clientError) Error() string {
	return fmt.Sprintf("client error: %s", e.cause)
}

type fileNameValidationError struct{}

func (e fileNameValidationError) Error() string {
	return fmt.Sprintf("file name validation error")
}

func (s service) createTrack(r *createTrackRequest) (*createTrackResponse, error) {
	req, err := s.rewriteMultipartRequest(r.request, r.boundary, s.rewriteTrackPart)
	if err != nil {
		return nil, err
	}

	return &createTrackResponse{request: req}, nil
}

func (s service) generic(r *genericRequest) (*genericResponse, error) {
	req, err := s.rewriteMultipartRequest(r.request, r.boundary, s.rewriteGenericPart)
	if err != nil {
		return nil, err
	}

	return &genericResponse{request: req}, nil
}

func (s service) rewriteMultipartRequest(r *http.Request, boundary string, fn rewritePartFn) (*http.Request, error) {
	header := http.Header{}
	body := &bytes.Buffer{}

	reader := multipart.NewReader(r.Body, boundary)
	writer := multipart.NewWriter(body)

	if err := writer.SetBoundary(boundary); err != nil {
		return nil, clientError{cause: err}
	}

	for {

		p, err := reader.NextPart()
		if err != nil {
			if err == io.EOF {
				// Processed all parts.
				break
			}
			_ = writer.Close()
			return nil, clientError{cause: err}
		}

		if err := s.validateFormName(p); err != nil {
			_ = writer.Close()
			return nil, err
		}

		// for debug: AUTH-2326
		if p.FormName() == "oauth_token" {
			if r.Header != nil {
				if r.Header.Get("Authorization") != "" {
					log.Printf("oauth_token part comes with Authorization %s header", maskToken(r.Header.Get("Authorization")))
				} else {
					log.Printf("oauth_token part comes without Authorization header")
				}
			}
		}

		if err := fn(p, writer, header); err != nil {
			_ = writer.Close()
			return nil, err
		}
	}

	if err := writer.Close(); err != nil {
		return nil, err
	}

	return s.modifyRequest(r, header, body), nil
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

func (s service) validateFormName(p *multipart.Part) error {
	if forbiddenFieldPattern.MatchString(p.FormName()) {
		return clientError{}
	}
	return nil
}

func (s service) rewriteTrackPart(p *multipart.Part, w *multipart.Writer, header http.Header) error {

	if isTrackUpload(p) {
		upload, err := s.uploadTrackAssetData(p, w)
		if err != nil {
			return err
		}
		header.Add("X-Track-Asset-Location", upload.location)
		header.Add("X-Track-Asset-Md5", upload.md5)

		return nil
	}

	return s.rewriteGenericPart(p, w, header)
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

func (s service) rewriteGenericPart(p *multipart.Part, w *multipart.Writer, header http.Header) error {
	switch p.FormName() {
	case "oauth_token":
		token, err := s.extractAuthToken(p)
		if err != nil {
			return err
		}
		if token.Len() > 0 {
			// for debug: AUTH-2326
			log.Printf("setting Authorization header as OAuth %s", maskToken(token.String()))
			header.Add("Authorization", "OAuth "+token.String())
		}

	default:
		if err := s.copyPart(p, w); err != nil {
			return err
		}
	}

	return nil
}

func (s service) modifyRequest(r *http.Request, header http.Header, body *bytes.Buffer) *http.Request {
	// Apply any additional headers to the original request.
	// This overwrites any potentially existing headers.
	for h := range header {
		r.Header.Set(h, header.Get(h))
	}
	// for debugging AUTH-2326
	if authorizationHeader := header.Get("Authorization"); authorizationHeader != "" {
		log.Printf("in modifyRequest Authorization %s", maskToken(authorizationHeader))
	}

	r.ContentLength = int64(body.Len())
	r.Body = io.NopCloser(body)

	return r
}

func maskToken(token string) string {
	if len(token) <= 20 {
		return "****"
	}
	return token[:20] + strings.Repeat("*", 10) + token[len(token)-3:]
}

// AUTH-2326 helper struct for logging
type authTokenLogger struct {
	total uint64
}

func (p *authTokenLogger) Write(b []byte) (int, error) {
	if len(b) > 0 {
		log.Printf("auth token before limit: %s, number of bytes %d", maskToken(string(b)), len(b))
		return len(b), nil
	} else {
		return 0, nil
	}
}

type artworkDataLogger struct {
	total uint64
}

func (p *artworkDataLogger) Write(b []byte) (int, error) {
	if len(b) > 8 {
		log.Printf("track[artwork_data] bytes start: %x | end: %x, number of bytes %d", b[:8], b[len(b)-8:], len(b))
		return len(b), nil
	} else {
		return 0, nil
	}
}

func (s service) extractAuthToken(p *multipart.Part) (*bytes.Buffer, error) {
	const (
		maxTokenBytes = 1024
	)

	// for debugging AUTH-2326
	teeReader := io.TeeReader(p, &authTokenLogger{})

	// Because the token is extracted to be propagated outside of the request
	// body, we're restricting its maximum length.
	lr := io.LimitReader(teeReader, maxTokenBytes)

	// Exceeding maxTokenBytes is currently not an error condition.
	// We'll use what fits into maxTokenBytes.

	log.Println("starting to read auth token")

	buffer := &bytes.Buffer{}
	if readBytesN, err := buffer.ReadFrom(lr); err != nil {
		return nil, err
	} else {
		log.Printf("auth token after limit: %s, number of bytes %d", maskToken(buffer.String()), readBytesN)
	}

	return buffer, nil
}

func (s service) uploadTrackAssetData(p *multipart.Part, w *multipart.Writer) (*uploadTrackResponse, error) {
	o, err := w.CreateFormField("track[original_filename]")
	if err != nil {
		return nil, err
	}

	filename := p.FileName()
	if len(filename) > 255 {
		return nil, fileNameValidationError{}
	}

	if _, err := o.Write([]byte(filename)); err != nil {
		return nil, err
	}

	res, err := s.upload.uploadTrack(&uploadTrackRequest{
		data:     p,
		filename: filename,
	})
	if err != nil {
		return nil, err
	}

	u, err := w.CreateFormField("track[uid]")
	if err != nil {
		return nil, err
	}

	if _, err := u.Write([]byte(res.uid)); err != nil {
		return nil, err
	}

	return res, nil
}

func (s service) copyPart(p *multipart.Part, w *multipart.Writer) error {
	dst, err := w.CreatePart(p.Header)
	if err != nil {
		return err
	}

	// for debug: AUTH-2326
	if p.FormName() == "track[artwork_data]" {
		log.Printf("copyPart track[artwork_data]")
		teeReader := io.TeeReader(p, &artworkDataLogger{})
		if _, err := io.Copy(dst, teeReader); err != nil {
			return err
		}
	} else {
		if _, err := io.Copy(dst, p); err != nil {
			return err
		}
	}

	return nil
}

// Ensure that service implements serviceAPI.
var _ serviceAPI = (*service)(nil)
