package main

import (
	"mime"
	"net/http"
	"net/http/httputil"
	"strings"
)

type controller struct {
	maxRequestBytes int64
	publicHostname  string
	proxy           *httputil.ReverseProxy
	service         serviceAPI
}

type svcDispatch func(string, *http.Request) (*http.Request, error)

func (c controller) generic(w http.ResponseWriter, r *http.Request) {
	c.dispatch(w, r, c.svcGeneric)
}

func (c controller) tracks(w http.ResponseWriter, r *http.Request) {
	c.dispatch(w, r, c.svcTracks)
}

func (c controller) svcGeneric(b string, r *http.Request) (*http.Request, error) {
	res, err := c.service.generic(&genericRequest{
		boundary: b,
		request:  r,
	})
	if err != nil {
		return nil, err
	}

	return res.request, nil
}

func (c controller) svcTracks(b string, r *http.Request) (*http.Request, error) {
	res, err := c.service.createTrack(&createTrackRequest{
		boundary: b,
		request:  r,
	})
	if err != nil {
		return nil, err
	}

	return res.request, nil
}

func (c controller) dispatch(w http.ResponseWriter, r *http.Request, svc svcDispatch) {
	const (
		emptyResponse = ""
	)

	method := r.Method
	if method != http.MethodPost && method != http.MethodPut {
		http.Error(w, emptyResponse, http.StatusNotFound)
		return
	}

	if host := r.Host; host == "" || host != c.publicHostname {
		http.Error(w, emptyResponse, http.StatusNotFound)
		return
	}

	mt, params, err := mime.ParseMediaType(r.Header.Get("Content-Type"))
	if err != nil {
		http.Error(w, emptyResponse, http.StatusBadRequest)
		return
	}

	if !strings.HasPrefix(mt, "multipart/form-data") {
		http.Error(w, emptyResponse, http.StatusBadRequest)
		return
	}

	boundary, ok := params["boundary"]
	if !ok {
		http.Error(w, emptyResponse, http.StatusBadRequest)
		return
	}

	if r.ContentLength > c.maxRequestBytes {
		http.Error(w, emptyResponse, http.StatusRequestEntityTooLarge)
		return
	}

	r.Body = http.MaxBytesReader(w, r.Body, c.maxRequestBytes)

	rr, err := svc(boundary, r)
	if err != nil {
		// There's currently no nice way of differentiating `request too large`
		// as enforced by the http.MaxBytesReader from other errors.
		// https://github.com/golang/go/issues/30715
		const (
			maxBytesReaderError = "http: request body too large"
		)

		// Doing a substring match here because the service may batch errors.
		if strings.Contains(err.Error(), maxBytesReaderError) {
			http.Error(w, emptyResponse, http.StatusRequestEntityTooLarge)
			return
		}

		// Assume that all other errors are client-retryable.
		http.Error(w, emptyResponse, http.StatusServiceUnavailable)
		return
	}

	c.proxy.ServeHTTP(w, rr)
}
