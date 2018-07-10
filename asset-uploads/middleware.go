package main

import (
	"log"
	"net/http"
)

// middleware describes a chained (wrapped) http.Handler.
type middleware func(http.Handler) http.Handler

// middlewareHandler composes the given middlewares with the http.Handler.
func middlewareHandler(mw []middleware, next http.Handler) http.Handler {
	h := next

	// Chain middlewares in reverse order of execution.
	for i := len(mw) - 1; i >= 0; i-- {
		h = mw[i](h)
	}

	return h
}

// logRequestMiddleware TODO
func logRequestMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		// TODO: Maybe use structured logging -> streadway/handy?
		log.Printf("%v\n", r)

		next.ServeHTTP(w, r)
	})
}

// filterRequestMiddleware TODO
func filterRequestMiddleware(next http.Handler) http.Handler {
	const (
		// TODO: Not sure if we need to give reasons.
		notFoundMessage = ""
	)

	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != "POST" && r.Method != "PUT" {
			http.Error(w, notFoundMessage, http.StatusNotFound)
			return
		}

		// TODO: Limit path/resource -> /tracks? Verify
		next.ServeHTTP(w, r)
	})
}

// limitRequestSizeMiddleware TODO
func limitRequestSizeMiddleware(maxBytes int64) middleware {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			r.Body = http.MaxBytesReader(w, r.Body, maxBytes)

			next.ServeHTTP(w, r)
		})
	}
}

// rewriteRequestMiddleware TODO
func rewriteRequestMiddleware(s storage) middleware {
	rw := &requestRewriter{storage: s}

	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			if err := rw.run(r); err != nil {
				log.Println(err)

				// TODO? Switch on kind of error (server v client)
				http.Error(w, err.Error(), http.StatusInternalServerError)
				return
			}

			next.ServeHTTP(w, r)
		})
	}
}
