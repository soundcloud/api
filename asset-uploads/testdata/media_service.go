package main

import (
	"crypto/rand"
	"encoding/base64"
	"flag"
	"fmt"
	"log"
	"net/http"
)

const (
	responseFormat = "{\"uid\":\"%s\"}"
	uidBytes       = 12
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

	http.HandleFunc("/uid", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		log.Println(r)

		if r.Method != "POST" {
			http.Error(w, fmt.Sprintf("Unsupported HTTP method: %s", r.Method), http.StatusBadRequest)
			return
		}

		b := make([]byte, uidBytes)

		_, err := rand.Read(b)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		uid := base64.URLEncoding.EncodeToString(b)

		w.WriteHeader(http.StatusCreated)
		w.Write([]byte(fmt.Sprintf(responseFormat, uid[0:uidBytes])))
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
