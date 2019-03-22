package main

import (
	"bytes"
	"io"
	"mime/multipart"
)

type serviceAPI interface {
	createTrack(*createTrackRequest) (*createTrackResponse, error)
}

type service struct {
	upload uploaderAPI
}

type createTrackRequest struct {
	body     io.Reader
	boundary string
}

type createTrackResponse struct {
	auth *bytes.Buffer
	body *bytes.Buffer
}

func (s service) createTrack(req *createTrackRequest) (*createTrackResponse, error) {
	auth := &bytes.Buffer{}
	body := &bytes.Buffer{}

	reader := multipart.NewReader(req.body, req.boundary)

	writer := multipart.NewWriter(body)
	writer.SetBoundary(req.boundary)
	defer writer.Close()

	for {
		part, err := reader.NextPart()
		if err != nil {
			if err == io.EOF {
				return &createTrackResponse{auth, body}, nil
			}

			return nil, err
		}

		switch part.FormName() {
		case "oauth_token":
			if err := extractAuthToken(part, auth); err != nil {
				return nil, err
			}
		case "track[asset_data]":
			if err := storeTrackAssetData(part, s.upload, writer); err != nil {
				return nil, err
			}
		default:
			if err := copyPart(part, writer); err != nil {
				return nil, err
			}
		}
	}
}

func extractAuthToken(src *multipart.Part, dst *bytes.Buffer) error {
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

func storeTrackAssetData(src *multipart.Part, uploader uploaderAPI, w *multipart.Writer) error {
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

func copyPart(src *multipart.Part, w *multipart.Writer) error {
	dst, err := w.CreatePart(src.Header)
	if err != nil {
		return err
	}

	if _, err := io.Copy(dst, src); err != nil {
		return err
	}

	return nil
}
