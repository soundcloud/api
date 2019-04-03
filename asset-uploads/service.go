package main

import (
	"bytes"
	"io"
	"io/ioutil"
	"mime/multipart"
	"net/http"
)

type serviceAPI interface {
	createTrack(*createTrackRequest) (*createTrackResponse, error)
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

func (s service) createTrack(r *createTrackRequest) (*createTrackResponse, error) {
	reader := multipart.NewReader(r.request.Body, r.boundary)

	body := &bytes.Buffer{}
	writer := multipart.NewWriter(body)
	if err := writer.SetBoundary(r.boundary); err != nil {
		return nil, err
	}

	auth, err := s.rewrite(reader, writer)
	if err != nil {
		return nil, err
	}

	if err := writer.Close(); err != nil {
		return nil, err
	}

	req := s.modifyRequest(r.request, auth, body)
	return &createTrackResponse{request: req}, nil
}

func (s service) rewrite(src *multipart.Reader, dst *multipart.Writer) (*bytes.Buffer, error) {
	auth := &bytes.Buffer{}

	for {
		part, err := src.NextPart()
		if err != nil {
			if err == io.EOF {
				return auth, nil
			}

			return nil, err
		}

		switch part.FormName() {
		case "oauth_token":
			if err := s.extractAuthToken(part, auth); err != nil {
				return nil, err
			}
		case "track[asset_data]":
			if err := s.storeTrackAssetData(part, s.upload, dst); err != nil {
				return nil, err
			}
		default:
			if err := s.copyPart(part, dst); err != nil {
				return nil, err
			}
		}
	}
}

func (s service) modifyRequest(r *http.Request, auth *bytes.Buffer, body *bytes.Buffer) *http.Request {
	// If the request contained auth information, propagate it by setting
	// the `Authorization` header. This overwrites any existing value.
	if auth.Len() > 0 {
		r.Header.Set("Authorization", "OAuth "+auth.String())
	}

	r.ContentLength = int64(body.Len())
	r.Body = ioutil.NopCloser(body)

	return r
}

func (s service) extractAuthToken(src *multipart.Part, dst *bytes.Buffer) error {
	const (
		maxTokenBytes = 64
	)

	// Because the token is extracted to be propagated outside of the request
	// body, we're restricting its maximum length.
	lr := io.LimitReader(src, maxTokenBytes)

	// Exceeding maxTokenBytes is currently not an error condition.
	// We'll use what fits into maxTokenBytes.
	if _, err := dst.ReadFrom(lr); err != nil {
		return err
	}

	return nil
}

func (s service) storeTrackAssetData(src *multipart.Part, uploader uploaderAPI, w *multipart.Writer) error {
	o, err := w.CreateFormField("track[original_filename]")
	if err != nil {
		return err
	}

	filename := src.FileName()
	if _, err := o.Write([]byte(filename)); err != nil {
		return err
	}

	res, err := uploader.uploadTrack(&uploadTrackRequest{data: src})
	if err != nil {
		return err
	}

	u, err := w.CreateFormField("track[uid]")
	if err != nil {
		return err
	}

	if _, err := u.Write([]byte(res.uid)); err != nil {
		return err
	}

	return nil
}

func (s service) copyPart(src *multipart.Part, w *multipart.Writer) error {
	dst, err := w.CreatePart(src.Header)
	if err != nil {
		return err
	}

	if _, err := io.Copy(dst, src); err != nil {
		return err
	}

	return nil
}

// Ensure that service implements serviceAPI.
var _ serviceAPI = (*service)(nil)
