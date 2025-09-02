package main

import (
	"crypto/rand"
	"encoding/base64"
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"net/http"
	"strings"
)

type UploadPolicy struct {
	Uid string `json:"uid"`
	Url string `json:"url"`
}

type TranscodingResponse struct {
	Status string `json:"status"`
}

func main() {
	var listenAddr = flag.String("listenAddr", ":9090", "Listen address")
	flag.Parse()

	http.HandleFunc("/-/health", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("OK"))
	}))

	http.HandleFunc("/user/upload-policy", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		log.Println(r)

		features := r.Header.Get("Sc-User-Features")
		if !strings.Contains(features, "unlimited-uploads") {
			http.Error(w, fmt.Sprint("uploads are not enabled for user"), http.StatusForbidden)
		}

		b := make([]byte, 12)
		_, err := rand.Read(b)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		uid := base64.URLEncoding.EncodeToString(b)[0:12]
		response := UploadPolicy{
			Uid: uid,
			Url: "ignored",
		}

		w.WriteHeader(http.StatusCreated)
		if err := json.NewEncoder(w).Encode(response); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
	}))

	http.HandleFunc("/transcodings", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		log.Println(r)

		response := TranscodingResponse{
			Status: "ignored",
		}

		w.WriteHeader(http.StatusCreated)
		if err := json.NewEncoder(w).Encode(response); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
	}))

	http.ListenAndServe(*listenAddr, nil)
}
