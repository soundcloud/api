package main

import (
	"flag"
	"fmt"
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"

	_ "net/http/pprof"
)

func main() {
	var (
		adminListenAddr = flag.String("adminListenAddr", ":8081", "Listen address admin server")
		listenAddr      = flag.String("listenAddr", ":8080", "Listen address")
		rawTargetURL    = flag.String("targetURL", "http://localhost:9000/", "Target URL")
	)
	flag.Parse()

	// TODO: Use service discovery and add a custom roundtripper.
	targetURL, err := url.Parse(*rawTargetURL)
	if err != nil {
		log.Fatalf("Failed to parse target URL: %s", *rawTargetURL)
	}

	// Filter/transform the incoming request through a middleware stack.
	mw := []middleware{}
	mw = append(mw, logRequestMiddleware)
	mw = append(mw, filterRequestMiddleware)
	mw = append(mw, limitRequestSizeMiddleware)
	mw = append(mw, rewriteRequestMiddleware(&fileSystemWriter{baseDir: "/tmp"}))

	// Proxy all requests that make it through filters/transforms.
	proxy := httputil.NewSingleHostReverseProxy(targetURL)

	mux := http.NewServeMux()
	mux.Handle("/", middlewareHandler(mw, proxy))

	go func(addr string) { log.Fatal(http.ListenAndServe(addr, nil)) }(*adminListenAddr)

	log.Println(fmt.Sprintf("Listening on %s", *listenAddr))
	log.Fatal(http.ListenAndServe(*listenAddr, mux))
}
