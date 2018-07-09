package main

import (
	"fmt"
	"io/ioutil"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
)

func TestMiddlewareHandler(t *testing.T) {
	const (
		header = "Handler"
		n      = 7 // arbitrary
	)

	// Build a chain of n middlewares. Each of them appends its header id.
	mw := []middleware{}
	for i := 0; i < n; i++ {
		handlerID := fmt.Sprintf("%d", i)
		mw = append(mw, func(next http.Handler) http.Handler {
			return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
				w.Header().Add(header, handlerID)
				next.ServeHTTP(w, r)
			})
		})
	}

	handler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
	})

	server := httptest.NewServer(middlewareHandler(mw, handler))
	defer server.Close()

	r, _ := http.Get(server.URL)

	// Check that the handler was invoked.
	if r.StatusCode != http.StatusOK {
		t.Errorf("Expected %d got %d", http.StatusOK, r.StatusCode)
	}

	// Check that handlers were applied in order.
	for i := 0; i < n; i++ {
		want := fmt.Sprintf("%d", i)
		if have := r.Header[header][i]; have != want {
			t.Errorf("Expected %v got %v", want, have)
		}
	}
}

func TestLimitRequestSizeMiddleware(t *testing.T) {
	const maxBytes = 4

	handler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if _, err := ioutil.ReadAll(r.Body); err != nil {
			http.Error(w, err.Error(), http.StatusBadRequest)
			return
		}
		w.WriteHeader(http.StatusOK)
	})

	server := httptest.NewServer(
		middlewareHandler(
			[]middleware{limitRequestSizeMiddleware(maxBytes)},
			handler,
		),
	)
	defer server.Close()

	// Check that all request bodies > maxBytes fail.
	examples := map[string]int{
		"tes":    http.StatusOK,
		"test":   http.StatusOK,
		"test1":  http.StatusBadRequest,
		"test12": http.StatusBadRequest,
	}

	for body, want := range examples {
		r, _ := http.Post(server.URL, "text/plain", strings.NewReader(body))
		if have := r.StatusCode; have != want {
			t.Errorf("Expected %d got %d", want, have)
		}
	}
}
