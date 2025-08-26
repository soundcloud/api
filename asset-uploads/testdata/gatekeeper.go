package main

import (
	"encoding/json"
	"flag"
	"log"
	"net/http"
)

func main() {
	var listenAddr = flag.String("listenAddr", ":9090", "Listen address")
	flag.Parse()

	mux := http.NewServeMux()

	mux.HandleFunc("/-/health", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("OK"))
	}))

	mux.HandleFunc("/users/{user}/features", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		log.Println(r)

		var features = []string{"unlimited-uploads,other-feature"}
		if r.PathValue("user") == "soundcloud:users:no-features-user" {
			features = []string{}
		}

		w.WriteHeader(http.StatusOK)
		if err := json.NewEncoder(w).Encode(features); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
	}))

	http.Handle("/", mux)
	http.ListenAndServe(*listenAddr, nil)
}
