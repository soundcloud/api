package main

import (
	"flag"
	"fmt"
	"log"
	"net/http"
)

func main() {
	var (
		listenAddr = flag.String("listenAddr", ":9090", "Listen address")
	)
	flag.Parse()

	http.HandleFunc("/-/health", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("OK"))
	}))

	http.HandleFunc("/transcode", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		log.Println(r)

		if r.Method != "POST" {
			http.Error(w, fmt.Sprintf("Unsupported HTTP method: %s", r.Method), http.StatusBadRequest)
			return
		}

		w.WriteHeader(http.StatusAccepted)
		w.Write([]byte("{\"status\": \"queued\"}"))
	}))

	http.ListenAndServe(*listenAddr, nil)
}
