package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"log"
	"net/http"
)

type SessionResponse struct {
	CacheKey          string `json:"cache_key"`
	ClientApplication string `json:"client_application"`
	Scope             string `json:"scope"`
	Session           string `json:"session"`
	ExpiresAt         string `json:"expires_at`
	Urn               string `json:"urn"`
}

func main() {
	var listenAddr = flag.String("listenAddr", ":9090", "Listen address")
	flag.Parse()

	http.HandleFunc("/-/health", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("OK"))
	}))

	http.HandleFunc("/sessions", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		log.Println(r)

		token := r.URL.Query().Get("auth_token")
		if token == "bad-token" {
			http.Error(w, fmt.Sprintf("Invalid OAuth token: %s", token), http.StatusBadRequest)
		}

		var user = "1"
		if token == "valid-token-no-features-user" {
			user = "no-features-user"
		}

		response := SessionResponse{
			CacheKey:          "stub-cache-key",
			ClientApplication: "soundcloud:clients:1",
			Scope:             "stub-scope",
			Session:           "stub-session",
			Urn:               fmt.Sprintf("soundcloud:users:%s", user),
		}

		if err := json.NewEncoder(w).Encode(response); err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
	}))

	http.ListenAndServe(*listenAddr, nil)
}
