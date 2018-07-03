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
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		// TODO: Limit request method -> PUT/POST
		// TODO: Limit path/resource -> /tracks? Verify
		next.ServeHTTP(w, r)
	})
}

// limitRequestSizeMiddleware TODO
func limitRequestSizeMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		// TODO: http.MaxBytesReader?
		next.ServeHTTP(w, r)
	})
}

// rewriteRequestMiddleware TODO
func rewriteRequestMiddleware(sw storageWriter) middleware {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			rw := &requestRewriter{}

			if err := rw.run(r, sw); err != nil {
				// TODO? Switch on kind of error
				http.Error(w, err.Error(), http.StatusInternalServerError)
				return
			}

			next.ServeHTTP(w, r)
		})
	}
}
