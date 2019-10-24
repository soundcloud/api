package main

import (
	"log"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/prometheus/client_golang/prometheus"
)

var (
	latencyBuckets = []float64{0.001, 0.002, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.5, 1, 2, 5, 10, 20, 50}

	incomingLatency = prometheus.NewHistogramVec(
		prometheus.HistogramOpts{
			Name:    "incoming_http_request_latency_seconds",
			Help:    "A histogram of the response latency for HTTP requests to this instance",
			Buckets: latencyBuckets,
		},
		[]string{"method", "path", "client"},
	)

	incomingRequests = prometheus.NewCounterVec(
		prometheus.CounterOpts{
			Name: "incoming_http_requests_total",
			Help: "A counter for the total number of HTTP requests",
		},
		[]string{"method", "path", "status", "statusClass", "client"},
	)

	outgoingLatency = prometheus.NewHistogramVec(
		prometheus.HistogramOpts{
			Name:    "outgoing_http_request_latency_seconds",
			Help:    "A histogram of the response latency for HTTP requests to other serivces",
			Buckets: latencyBuckets,
		},
		[]string{"service", "method"},
	)

	outgoingRequests = prometheus.NewCounterVec(
		prometheus.CounterOpts{
			Name: "outgoing_http_requests_total",
			Help: "A counter for the total number of HTTP requests made to other services",
		},
		[]string{"service", "method", "status", "statusClass"},
	)
)

func init() {
	prometheus.MustRegister(incomingLatency)
	prometheus.MustRegister(incomingRequests)
	prometheus.MustRegister(outgoingLatency)
	prometheus.MustRegister(outgoingRequests)
}

type httpEndpointRecorder struct {
	http.ResponseWriter
	responseCode int
}

func (e *httpEndpointRecorder) WriteHeader(code int) {
	e.responseCode = code
	e.ResponseWriter.WriteHeader(code)
}

func httpHandler(path string, handler http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var (
			er    = &httpEndpointRecorder{w, 0}
			start = time.Now()
		)

		defer func() {
			var (
				client      = labelClient(r.Header.Get(http.CanonicalHeaderKey("Sc-System")))
				duration    = time.Since(start)
				method      = r.Method
				status      = labelStatusCode(er.responseCode)
				statusClass = labelStatusClass(er.responseCode)
				uri         = r.URL.String()
			)

			incomingLatency.WithLabelValues(method, path, client).Observe(duration.Seconds())
			incomingRequests.WithLabelValues(method, path, status, statusClass, client).Inc()

			log.Printf("[%d ms] %s %s -> %s (%s) (path=%s)", duration/time.Millisecond, method, uri, status, client, path)
		}()

		handler(er, r)
	}
}

// Source: https://github.com/soundcloud/hocuspocus/blob/7cf839237ff35d0b81cf797d4833a37eecaf40b5/cmd/api/http.go#L211
type serviceTransport struct {
	name      string
	transport http.RoundTripper
}

func (t serviceTransport) RoundTrip(req *http.Request) (*http.Response, error) {
	var (
		start    = time.Now()
		method   = req.Method
		uri      = req.URL.String()
		res, err = t.transport.RoundTrip(req)
		duration = time.Since(start)
	)

	var (
		// In JVMKit - this is when a client closes the connection before a complete response
		status      = "499"
		statusClass = "4xx"
	)
	if err == nil {
		status = labelStatusCode(res.StatusCode)
		statusClass = labelStatusClass(res.StatusCode)
	}

	outgoingLatency.WithLabelValues(t.name, req.Method).Observe(duration.Seconds())
	outgoingRequests.WithLabelValues(t.name, req.Method, status, statusClass).Inc()

	log.Printf("[%d ms] %s %s -> %s (%s)", duration/time.Millisecond, method, uri, status, t.name)

	return res, err
}

func labelClient(name string) string {
	if name == "" {
		return "unknown"
	}

	return name
}

func labelStatusClass(status int) string {
	switch {
	case status < 200:
		return "1xx"
	case status < 300:
		return "2xx"
	case status < 400:
		return "3xx"
	case status < 500:
		return "4xx"
	default:
		return "5xx"
	}
}

func labelStatusCode(status int) string {
	return strconv.Itoa(status)
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

	// Unless we have a clientError, then respond with 400.
	if _, ok := err.(clientError); ok {
		status = http.StatusBadRequest
	}

	http.Error(w, emptyResponse, status)
}
