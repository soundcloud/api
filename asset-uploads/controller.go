package main

import (
	"fmt"
	"io/ioutil"
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
		authHeaderFormat = "OAuth %s"
		emptyResponse    = ""
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

	res, err := c.service.createTrack(&createTrackRequest{
		body:     http.MaxBytesReader(w, r.Body, c.maxRequestBytes),
		boundary: boundary,
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
		http.Error(w, emptyResponse, http.StatusServiceUnavailable)
		return
	}

	// If the request contained auth information, propagate it by setting
	// the `Authorization` header. This overwrites any existing value.
	if res.auth.Len() > 0 {
		r.Header.Set("Authorization", fmt.Sprintf(authHeaderFormat, res.auth.String()))
	}

	r.ContentLength = int64(res.body.Len())
	r.Body = ioutil.NopCloser(res.body)

	c.proxy.ServeHTTP(w, r)
}
