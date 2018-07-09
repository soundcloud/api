// +build main1

package main

import (
	"flag"
	"fmt"
	"log"
	"net/http"
	"net/http/httputil"
)

func main() {
	var (
		listenAddr = flag.String("listenAddr", ":9090", "Listen address")
	)
	flag.Parse()

	http.HandleFunc("/", http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		log.Println(r)

		dump, err := httputil.DumpRequest(r, true)
		if err != nil {
			http.Error(w, fmt.Sprint(err), http.StatusInternalServerError)
			return
		}

		w.Write(dump)
	}))

	http.ListenAndServe(*listenAddr, nil)
}
