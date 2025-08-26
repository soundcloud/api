package main

import (
	"log"
	"net/http"
	"os"
	"strings"

	"github.com/soundcloud/gokit/v2/instrumenthttp"
	"github.com/streadway/handy/report"
)

type Middleware func(http.Handler) http.Handler

var defaultLatencyBuckets = []float64{0.001, 0.002, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.5, 1, 2, 5, 10, 20, 50}

func httpHandler(path string, handler http.Handler) http.Handler {
	return register(path)(logger()(handler))
}

// Log data about the request
func logger() Middleware {
	return func(handler http.Handler) http.Handler {
		return report.JSON(os.Stdout, handler)
	}
}

// Instrument the request
func register(path string) Middleware {
	return func(handler http.Handler) http.Handler {
		return instrumenthttp.Middleware(
			instrumenthttp.MiddlewareOpts{
				Path:           path,
				LatencyBuckets: defaultLatencyBuckets,
			},
			handler,
		)
	}
}

// Handle errors during proxying. We want to treat errors caused by too-large
// request bodies specially and return the appropriate response code.
func handleProxyError(w http.ResponseWriter, r *http.Request, err error) {
	if err == nil {
		panic("trying to handle nil error")
	}
	// There's currently no nice way of differentiating `request too large`
	// as enforced by the http.MaxBytesReader from other errors.
	// https://github.com/golang/go/issues/30715
	const (
		maxBytesReaderError = "http: request body too large"
		emptyResponse       = ""
	)

	// Doing a substring match here because the service may batch errors.
	if strings.Contains(err.Error(), maxBytesReaderError) {
		http.Error(w, emptyResponse, http.StatusRequestEntityTooLarge)
		return
	}

	log.Printf("http: proxy error (overriden): %v", err)

	// Assume we did something wrong and default to 500.
	status := http.StatusInternalServerError

	if _, ok := err.(clientError); ok {
		status = http.StatusBadRequest
	} else if _, ok := err.(fileNameValidationError); ok {
		status = http.StatusUnprocessableEntity
	} else if _, ok := err.(authorizationError); ok {
		status = http.StatusUnauthorized
	}
	http.Error(w, emptyResponse, status)
}
