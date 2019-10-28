package main

import (
	"bytes"
	"fmt"
	"io"
	"io/ioutil"
	"mime/multipart"
	"net/http"

	"github.com/prometheus/client_golang/prometheus"
)

var (
	requestUploadMethod = prometheus.NewCounterVec(
		prometheus.CounterOpts{
			Name: "track_upload_method_total",
			Help: "Count of how often each method to upload a track has been chosen",
		},
		[]string{"method"},
	)
)

func init() {
	prometheus.MustRegister(requestUploadMethod)
	// We know all possible label values, may as well initialize them.
	requestUploadMethod.WithLabelValues("passthrough").Add(0.0)
	requestUploadMethod.WithLabelValues("s3").Add(0.0)
}

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
	// Unless the feature flag header is set, return the original request.
	if feature := r.Header.Get("X-Track-Asset-Uploads"); feature == "false" {
		requestUploadMethod.WithLabelValues("passthrough").Inc()
		return r, nil
	}
	requestUploadMethod.WithLabelValues("s3").Inc()

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

func (s service) rewriteTrackPart(p *multipart.Part, w *multipart.Writer, header http.Header) error {
	if p.FormName() == "track[asset_data]" {
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

func (s service) rewriteGenericPart(p *multipart.Part, w *multipart.Writer, header http.Header) error {
	switch p.FormName() {
	case "oauth_token":
		token, err := s.extractAuthToken(p)
		if err != nil {
			return err
		}

		if token.Len() > 0 {
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

	r.ContentLength = int64(body.Len())
	r.Body = ioutil.NopCloser(body)

	return r
}

func (s service) extractAuthToken(p *multipart.Part) (*bytes.Buffer, error) {
	const (
		maxTokenBytes = 64
	)

	// Because the token is extracted to be propagated outside of the request
	// body, we're restricting its maximum length.
	lr := io.LimitReader(p, maxTokenBytes)

	// Exceeding maxTokenBytes is currently not an error condition.
	// We'll use what fits into maxTokenBytes.
	buffer := &bytes.Buffer{}
	if _, err := buffer.ReadFrom(lr); err != nil {
		return nil, err
	}

	return buffer, nil
}

func (s service) uploadTrackAssetData(p *multipart.Part, w *multipart.Writer) (*uploadTrackResponse, error) {
	o, err := w.CreateFormField("track[original_filename]")
	if err != nil {
		return nil, err
	}

	filename := p.FileName()
	if _, err := o.Write([]byte(filename)); err != nil {
		return nil, err
	}

	res, err := s.upload.uploadTrack(&uploadTrackRequest{data: p})
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

	if _, err := io.Copy(dst, p); err != nil {
		return err
	}

	return nil
}

// Ensure that service implements serviceAPI.
var _ serviceAPI = (*service)(nil)
