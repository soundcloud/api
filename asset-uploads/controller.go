package main

import (
	"log"
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

type svcDispatch func(string, *http.Request) (*http.Request, error)

func (c controller) generic() http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		c.dispatch(w, r, c.svcGeneric)
	})
}

func (c controller) tracks() http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		c.dispatch(w, r, c.svcTracks)
	})
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
		client := labelClient(r.Header.Get(("Sc-System")))
		log.Printf("rejected misdirected request due to unhandled method: %s %s (%s)", method, r.RequestURI, client)
		// Using "misdirected" because we do support other methods – just not on this component (i.e. there is a routing issue somewhere).
		w.Header().Set("Cache-Control", "no-store")
		http.Error(w, emptyResponse, http.StatusMisdirectedRequest)
		return
	}

	client := labelClient(r.Header.Get(("Sc-System")))

	mt, params, err := mime.ParseMediaType(r.Header.Get("Content-Type"))
	if err != nil {
		log.Printf("rejected request due to missing content type: %s %s (%s) err: %v", method, r.RequestURI, client, err)
		http.Error(w, emptyResponse, http.StatusMisdirectedRequest)
		return
	}

	// special case: we do expect `multipart/mixed` requests to be redirected to this service,
	// but we no longer support them. instead we explicitly reject them as "bad request".
	if strings.HasPrefix(mt, "multipart/mixed") {
		log.Printf("bad request: content-type multipart/mixed is not supported: %s %s (%s)", method, r.RequestURI, client)
		http.Error(w, emptyResponse, http.StatusBadRequest)
		return
	}

	if !strings.HasPrefix(mt, "multipart/form-data") {
		log.Printf("rejected request due to unexpected content type: %s %s (%s) header: %s", method, r.RequestURI, client, r.Header.Get("Content-Type"))
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

	log.Printf("Rewritten URL Path: %s", r.URL.Path)

	rr, err := svc(boundary, r)
	if err != nil {
		handleProxyError(w, rr, err)
		return
	}

	c.proxy.ServeHTTP(w, rr)
}

func labelClient(name string) string {
	if name == "" {
		return "unknown"
	}

	return name
}
