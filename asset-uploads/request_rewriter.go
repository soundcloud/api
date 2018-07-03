package main

import (
	"bytes"
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"mime"
	"mime/multipart"
	"net/http"
	"strings"
)

type requestRewriter struct{}

func (rw *requestRewriter) run(r *http.Request, w storageWriter) error {
	s := &rewriterState{request: r, storage: w}

	f := stateProcessHeaders
	for f != nil {
		log.Printf("%v\n", s)
		f = f(s)
	}

	return nil
}

type rewriterState struct {
	request *http.Request

	storage storageWriter

	boundary string
	reader   *multipart.Reader
	part     *multipart.Part

	buffer *bytes.Buffer
	writer *multipart.Writer
}

type rewriterStateFunc func(*rewriterState) rewriterStateFunc

const (
	authorizationHeader        = "Authorization"
	authorizationHeaderFormat  = "OAuth %s"
	authorizationTokenMaxBytes = 128 // Max length of an OAuth token. TODO: Verify
)

// stateProcessHeaders TODO
func stateProcessHeaders(s *rewriterState) rewriterStateFunc {
	mt, params, err := mime.ParseMediaType(s.request.Header.Get("Content-Type"))
	if err != nil {
		// Unable to partse Content-Type.
		return nil
	}

	if !strings.HasPrefix(mt, "multipart/form-data") {
		// Not a multipart form upload.
		return nil
	}

	b, ok := params["boundary"]
	if !ok || b == "" {
		// Unable to determine the multipart boundary.
		return nil
	}

	s.boundary = b

	// Processing the request body.
	return stateProcessBody
}

// stateProcessBody allocates resources for processing the request body.
// TODO: Could attempt reading a part here and fail early/within this context.
func stateProcessBody(s *rewriterState) rewriterStateFunc {
	// Prepare a reader for the incoming request.
	// TODO? Attempt to read the first part to verify multipart-ness?
	s.reader = multipart.NewReader(s.request.Body, s.boundary)

	// Prepare a writer and in-memory buffer to rewrite the incoming request.
	// TODO: Maybe ensure that in memory request is <= max size?
	s.buffer = &bytes.Buffer{}
	s.writer = multipart.NewWriter(s.buffer)
	s.writer.SetBoundary(s.boundary)

	return stateNextPart
}

// stateNextPart TODO
func stateNextPart(s *rewriterState) rewriterStateFunc {
	s.part = nil

	part, err := s.reader.NextPart()
	if err != nil {
		if err == io.EOF {
			// We finished reading all parts, we won't be writing more.
			s.writer.Close()

			return stateReplaceBody
		}

		return nil
	}

	s.part = part

	// File parts need to be handled specially.
	if part.FileName() != "" {
		switch part.FormName() {
		case "track[artwork_data]":
			return stateProcessPart
		case "track[asset_data]":
			return stateProcessTrackAssetPart
		default:
			// Unsupported type of file part.
			return nil
		}
	}

	// The oauth token is handled specically.
	if part.FormName() == "oauth_token" {
		return stateProcessAuthTokenPart
	}

	return stateProcessPart
}

// stateProcessPart TODO
func stateProcessPart(s *rewriterState) rewriterStateFunc {
	w, err := s.writer.CreatePart(s.part.Header)
	if err != nil {
		return nil
	}

	if _, err := io.Copy(w, s.part); err != nil {
		return nil
	}

	return stateNextPart
}

// stateProcessTrackAssetPart TODO
func stateProcessTrackAssetPart(s *rewriterState) rewriterStateFunc {
	// TODO: Figure out how to obtain a key
	if err := s.storage.Store("process-track-foo", s.part); err != nil {
		return nil
	}

	return stateNextPart
}

// stateProcessAuthToken TODO
func stateProcessAuthTokenPart(s *rewriterState) rewriterStateFunc {
	buf := bytes.Buffer{}

	// Read limited bytes from the part.
	// TODO: Do we need to check sanity differently?
	lr := &io.LimitedReader{R: s.part, N: authorizationTokenMaxBytes}
	if _, err := buf.ReadFrom(lr); err != nil {
		return nil
	}

	// Add authorization header with the obtained token.
	s.request.Header.Set(authorizationHeader,
		fmt.Sprintf(authorizationHeaderFormat, buf.Bytes()))

	return stateNextPart
}

// stateReplaceBody TODO
func stateReplaceBody(s *rewriterState) rewriterStateFunc {
	s.request.Body = ioutil.NopCloser(s.buffer)
	s.request.ContentLength = int64(s.buffer.Len())

	return nil
}
