package main

import (
	"bytes"
	"errors"
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"mime"
	"mime/multipart"
	"net/http"
	"strings"
)

type requestRewriter struct {
	storage    storage
	transcoder transcoder
}

func (rw *requestRewriter) run(r *http.Request) error {
	s := &rewriterState{
		request:    r,
		storage:    rw.storage,
		transcoder: rw.transcoder,
	}

	f := stateProcessHeaders
	for f != nil {
		//		log.Printf("%v\n", s)

		f = f(s)
	}

	return s.err
}

type rewriterState struct {
	request *http.Request

	storage    storage
	transcoder transcoder

	boundary string
	reader   *multipart.Reader
	part     *multipart.Part

	buffer *bytes.Buffer
	writer *multipart.Writer

	err error
}

type rewriterStateFunc func(*rewriterState) rewriterStateFunc

const (
	authorizationHeader        = "Authorization"
	authorizationHeaderFormat  = "OAuth %s"
	authorizationTokenMaxBytes = 128 // Max length of an OAuth token. TODO: Verify
)

// stateProcessHeaders TODO
func stateProcessHeaders(s *rewriterState) rewriterStateFunc {
	var (
		errUnknownContentType  = errors.New("Unknown Content-Type")
		errNotMultiPart        = errors.New("Not a multipart form upload")
		errNoMultipartBoundary = errors.New("No multipart boundary")
	)

	mt, params, err := mime.ParseMediaType(s.request.Header.Get("Content-Type"))
	if err != nil {
		s.err = errUnknownContentType
		return nil
	}

	if !strings.HasPrefix(mt, "multipart/form-data") {
		s.err = errNotMultiPart
		return nil
	}

	b, ok := params["boundary"]
	if !ok || b == "" {
		s.err = errNoMultipartBoundary
		return nil
	}
	s.boundary = b

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
		s.err = fmt.Errorf("Failed to read next part: %s", err)
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
			s.err = fmt.Errorf("Unsupported file part: %s", part.FormName())
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
		s.err = fmt.Errorf("Failed to create form part: %s", err)
		return nil
	}

	if _, err := io.Copy(w, s.part); err != nil {
		s.err = fmt.Errorf("Failed to copy part: %s", err)
		return nil
	}

	return stateNextPart
}

// stateProcessTrackAssetPart TODO
// https://github.com/soundcloud/soundcloud/blob/c447c7da50505835bee166e5a19f2a292ba0d212/app/services/tracks_service.rb#L203
func stateProcessTrackAssetPart(s *rewriterState) rewriterStateFunc {
	uid, err := s.storage.store(s.part)

	if err != nil {
		s.err = fmt.Errorf("Failed to store part: %s", err)
		return nil
	}

	// Add the track[original_filename] form field.
	w, err := s.writer.CreateFormField("track[original_filename]")
	if err != nil {
		s.err = fmt.Errorf("Failed to create track[original_filename]: %s", err)
		return nil
	}

	filename := s.part.FileName()
	if _, err := w.Write([]byte(filename)); err != nil {
		s.err = fmt.Errorf("Failed to write track[original_filename]: %s", err)
		return nil
	}

	log.Printf("Stored %s as %s", filename, uid)

	// Add the track[uid] form field.
	w, err = s.writer.CreateFormField("track[uid]")
	if err != nil {
		s.err = fmt.Errorf("Failed to create track[uid]: %s", err)
		return nil
	}

	if _, err := w.Write([]byte(uid)); err != nil {
		s.err = fmt.Errorf("Failed to write track[uid]: %s", err)
		return nil
	}

	log.Printf("Wrote uid=%s, filename=%s to request.", uid, filename)

	success, err := s.transcoder.transcode(uid)

	if err != nil {
		s.err = fmt.Errorf("Failed to trigger transcoding for Track<uid = \"%s\">", uid)
		return nil
	}

	log.Printf("Transcoding success: %t", success)

	return stateNextPart
}

// stateProcessAuthToken TODO
func stateProcessAuthTokenPart(s *rewriterState) rewriterStateFunc {
	buf := bytes.Buffer{}

	// Read limited bytes from the part.
	// TODO: Do we need to check sanity differently?
	lr := &io.LimitedReader{R: s.part, N: authorizationTokenMaxBytes}
	if _, err := buf.ReadFrom(lr); err != nil {
		s.err = fmt.Errorf("Failed to read auth token part: %s", err)
		return nil
	}

	// Add authorization header with the obtained token.
	s.request.Header.Set(authorizationHeader, fmt.Sprintf(authorizationHeaderFormat, buf.Bytes()))

	return stateNextPart
}

// stateReplaceBody TODO
func stateReplaceBody(s *rewriterState) rewriterStateFunc {
	s.request.Body = ioutil.NopCloser(s.buffer)
	s.request.ContentLength = int64(s.buffer.Len())

	return nil
}
