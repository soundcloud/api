package main

import (
	"log"
	"mime"
	"net/http"
	"net/http/httputil"
	"strings"

	"github.com/prometheus/client_golang/prometheus"
)

type controller struct {
	maxRequestBytes int64
	publicHostname  string
	proxy           *httputil.ReverseProxy
	service         serviceAPI
}

var (
	requestsMisdirected = prometheus.NewCounterVec(
		prometheus.CounterOpts{
			Name: "incoming_http_requests_misdirected_total",
			Help: "Count of requests rejected because this component does not handle them",
		},
		// Not adding a path label here because it is too complex to normalize public-api request paths. --MR
		[]string{"method", "client", "reason"},
	)
)

func init() {
	prometheus.MustRegister(requestsMisdirected)
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
		client := labelClient(r.Header.Get(http.CanonicalHeaderKey("Sc-System")))
		log.Printf("rejected misdirected request due to unhandled method: %s %s (%s)", method, r.RequestURI, client)
		requestsMisdirected.WithLabelValues(method, client, "unhandled method").Inc()
		// Using "misdirected" because we do support other methods – just not on this component (i.e. there is a routing issue somewhere).
		w.Header().Set("Cache-Control", "no-store")
		http.Error(w, emptyResponse, http.StatusMisdirectedRequest)
		return
	}

	client := labelClient(r.Header.Get(http.CanonicalHeaderKey("Sc-System")))

	if host := r.Host; host == "" || host != c.publicHostname {
		log.Printf("rejected misdirected request due to unexpected hostname: %s %s (%s)", method, r.RequestURI, client)
		requestsMisdirected.WithLabelValues(method, client, "unexpected hostname").Inc()
		http.Error(w, emptyResponse, http.StatusMisdirectedRequest)
		return
	}

	mt, params, err := mime.ParseMediaType(r.Header.Get("Content-Type"))
	if err != nil {
		log.Printf("rejected request due to missing content type: %s %s (%s)", method, r.RequestURI, client)
		requestsMisdirected.WithLabelValues(method, client, "no content type").Inc()
		http.Error(w, emptyResponse, http.StatusMisdirectedRequest)
		return
	}

	if !strings.HasPrefix(mt, "multipart/form-data") {
		log.Printf("rejected request due to unexpected content type: %s %s (%s)", method, r.RequestURI, client)
		requestsMisdirected.WithLabelValues(method, client, "unexpected content type").Inc()
		http.Error(w, emptyResponse, http.StatusMisdirectedRequest)
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
		handleProxyError(w, rr, err)
		return
	}

	c.proxy.ServeHTTP(w, rr)
}
