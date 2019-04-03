package main

import (
	"mime"
	"net/http"
	"net/http/httputil"
	"strings"
)

type controller struct {
	maxRequestBytes int64
	proxy           *httputil.ReverseProxy
	service         serviceAPI
}

func (c controller) tracks(w http.ResponseWriter, r *http.Request) {
	const (
		emptyResponse = ""
	)

	method := r.Method
	if method != http.MethodPost && method != http.MethodPut {
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

	r.Body = http.MaxBytesReader(w, r.Body, c.maxRequestBytes)

	res, err := c.service.createTrack(&createTrackRequest{
		boundary: boundary,
		request:  r,
	})
	if err != nil {
		// There's currently no nice way of differentiating `request too large`
		// as enforced by the http.MaxBytesReader from other errors.
		// https://github.com/golang/go/issues/30715
		const (
			maxBytesReaderError = "http: request body too large"
		)

		if err.Error() == maxBytesReaderError {
			http.Error(w, emptyResponse, http.StatusRequestEntityTooLarge)
			return
		}

		// Assume that all other errors are client-retryable.
		http.Error(w, err.Error(), http.StatusServiceUnavailable)
		return
	}

	c.proxy.ServeHTTP(w, res.request)
}
