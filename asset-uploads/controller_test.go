package main

import (
	"bytes"
	"errors"
	"io"
	"io/ioutil"
	"net/http"
	"net/http/httptest"
	"net/http/httputil"
	"net/url"
	"testing"
)

type fakeService struct {
	serviceAPI
	fn func(*createTrackRequest) (*createTrackResponse, error)
}

func (f fakeService) createTrack(r *createTrackRequest) (*createTrackResponse, error) { return f.fn(r) }

func TestTracksAllowedMethods(t *testing.T) {
	tests := [...]struct {
		method string
		status int
	}{
		0: {http.MethodGet, http.StatusNotFound},
		1: {http.MethodHead, http.StatusNotFound},
		2: {http.MethodPatch, http.StatusNotFound},
		3: {http.MethodDelete, http.StatusNotFound},
		4: {http.MethodConnect, http.StatusNotFound},
		5: {http.MethodOptions, http.StatusNotFound},
		6: {http.MethodTrace, http.StatusNotFound},
	}

	for _, tt := range tests {
		controller := &controller{}

		res := httptest.NewRecorder()
		req := httptest.NewRequest(tt.method, "/", nil)

		controller.tracks(res, req)

		if want, got := tt.status, res.Result().StatusCode; want != got {
			t.Errorf("Expected %v to return %v, was: %v", tt.method, want, got)
		}
	}
}

func TestTracksRequiresContentType(t *testing.T) {
	tests := [...]struct {
		method      string
		contentType string
		status      int
	}{
		0: {http.MethodPost, "application/json", http.StatusBadRequest},
		1: {http.MethodPut, "multipart/form-data", http.StatusBadRequest},
		2: {http.MethodPost, "", http.StatusBadRequest},
	}

	for _, tt := range tests {
		controller := &controller{}

		res := httptest.NewRecorder()
		req := httptest.NewRequest(tt.method, "/", nil)
		req.Header.Set("Content-Type", tt.contentType)

		controller.tracks(res, req)

		if want, got := tt.status, res.Result().StatusCode; want != got {
			t.Errorf("Expected %v %v to return %v, was: %v", tt.method, tt.contentType, want, got)
		}
	}
}

func TestExceedsMaxContentLength(t *testing.T) {
	randomBody := []byte{97, 97, 98, 101, 105, 110, 115, 115, 116}

	controller := &controller{maxRequestBytes: int64(len(randomBody)) - 1}

	res := httptest.NewRecorder()
	req := httptest.NewRequest(http.MethodPost, "/", bytes.NewReader(randomBody))
	req.Header.Set("Content-Type", "multipart/form-data; boundary=some-boundary")
	controller.tracks(res, req)

	if want, got := http.StatusRequestEntityTooLarge, res.Result().StatusCode; want != got {
		t.Errorf("Expected request to return %v, was: %v", want, got)
	}
}

func TestTracksLimitsRequestSize(t *testing.T) {
	randomBody := []byte{97, 97, 98, 101, 105, 110, 115, 115, 116}

	controller := &controller{
		maxRequestBytes: int64(len(randomBody) - 1),
		service: &fakeService{
			fn: func(r *createTrackRequest) (*createTrackResponse, error) {
				if _, err := ioutil.ReadAll(r.request.Body); err != nil {
					return nil, err
				}

				panic("Expecting an error return")
			},
		},
	}

	res := httptest.NewRecorder()
	req := httptest.NewRequest("POST", "/", bytes.NewReader(randomBody))
	req.Header.Set("Content-Type", "multipart/form-data; boundary=some-boundary")
	req.ContentLength = -1 // Create a request that won't get filtered on Content-Length.

	controller.tracks(res, req)

	result := res.Result()
	if want, got := http.StatusRequestEntityTooLarge, result.StatusCode; want != got {
		t.Errorf("Expected request to return %v, got %v", want, got)
	}
}

func TestPassesMultipartBoundary(t *testing.T) {
	controller := &controller{
		service: &fakeService{
			fn: func(r *createTrackRequest) (*createTrackResponse, error) {
				if want, got := "the-boundary", r.boundary; want != got {
					t.Errorf("Expected multipart boundary to be %v, got %v", want, got)
				}

				return nil, errors.New("This failed")
			},
		},
	}

	res := httptest.NewRecorder()
	req := httptest.NewRequest("POST", "/", nil)
	req.Header.Set("Content-Type", "multipart/form-data; boundary=the-boundary")

	controller.tracks(res, req)
}

func TestFailingServiceCall(t *testing.T) {
	controller := &controller{
		service: &fakeService{
			fn: func(r *createTrackRequest) (*createTrackResponse, error) {
				return nil, errors.New("This failed")
			},
		},
	}

	res := httptest.NewRecorder()
	req := httptest.NewRequest("POST", "/", nil)
	req.Header.Set("Content-Type", "multipart/form-data; boundary=some-boundary")

	controller.tracks(res, req)

	if want, got := http.StatusServiceUnavailable, res.Result().StatusCode; want != got {
		t.Errorf("Expected request to return %v, got %v", want, got)
	}
}

func TestSuccessfulServiceCall(t *testing.T) {
	server := httptest.NewServer(
		http.HandlerFunc(
			func(w http.ResponseWriter, r *http.Request) {
				w.WriteHeader(http.StatusCreated)
				_, _ = io.Copy(w, r.Body)
			},
		),
	)
	defer server.Close()

	url, _ := url.Parse(server.URL)
	serverProxy := httputil.NewSingleHostReverseProxy(url)

	body := []byte("test123")

	controller := &controller{
		maxRequestBytes: int64(len(body)),
		service: &fakeService{
			fn: func(r *createTrackRequest) (*createTrackResponse, error) {
				return &createTrackResponse{request: r.request}, nil
			},
		},
		proxy: serverProxy,
	}

	res := httptest.NewRecorder()
	req := httptest.NewRequest("POST", "/", bytes.NewReader(body))
	req.Header.Set("Content-Type", "multipart/form-data; boundary=some-boundary")

	controller.tracks(res, req)

	result := res.Result()
	if want, got := http.StatusCreated, result.StatusCode; want != got {
		t.Errorf("Expected status to be %v, got %v", want, got)
	}

	b, _ := ioutil.ReadAll(result.Body)
	if want, got := body, b; !bytes.Equal(want, got) {
		t.Errorf("Expected response to be %s, got %s", want, got)
	}
}
