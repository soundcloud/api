package main

import (
	"flag"
	"fmt"
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"
	"os"

	_ "net/http/pprof"

	"github.com/prometheus/client_golang/prometheus/promhttp"
)

func main() {
	const (
		envAWSAccessKeyID = "AWS_ACCESS_KEY_ID"
		envAWSSecretKey   = "AWS_SECRET_ACCESS_KEY"
		envS3Bucket       = "AWS_S3_BUCKET"
		envS3Region       = "AWS_S3_REGION"
	)

	var (
		addr      = flag.String("addr", ":8080", "Listen address")
		adminAddr = flag.String("admin-addr", ":8081", "Listen address admin server")

		moshimoshiAddr = flag.String("moshimoshiAddr", "localhost:9091", "MoshiMoshi service address")
		rawTargetURL   = flag.String("targetURL", "http://localhost:9000/", "Target URL")

		awsKey    = flag.String("aws-key", os.Getenv(envAWSAccessKeyID), "AWS access key ID")
		awsSecret = flag.String("aws-secret", os.Getenv(envAWSSecretKey), "AWS secret access key")
		s3Bucket  = flag.String("s3-bucket", os.Getenv(envS3Bucket), "AWS S3 bucket")
		s3Region  = flag.String("s3-region", os.Getenv(envS3Region), "AWS S3 region")
	)
	flag.Parse()

	// TODO: Use service discovery and add a custom roundtripper.
	targetURL, err := url.Parse(*rawTargetURL)
	if err != nil {
		log.Fatalf("Failed to parse target URL: %s", *rawTargetURL)
	}

	moshi := &moshimoshiClient{
		host:       *moshimoshiAddr,
		httpClient: http.DefaultClient,
	}

	s3, err := newS3Storage(*awsKey, *awsSecret, *s3Region, *s3Bucket, moshi)
	if err != nil {
		log.Fatalf("Failed to initialize S3 storage: %v", err)
	}

	// Filter/transform the incoming request through a middleware stack.
	mw := []middleware{}
	mw = append(mw, logRequestMiddleware)
	mw = append(mw, filterRequestMiddleware)
	mw = append(mw, limitRequestSizeMiddleware)
	mw = append(mw, rewriteRequestMiddleware(s3))

	// Proxy all requests that make it through filters/transforms.
	proxy := httputil.NewSingleHostReverseProxy(targetURL)

	mux := http.NewServeMux()
	mux.Handle("/", middlewareHandler(mw, proxy))

	go func(a string) {
		http.Handle("/metrics", promhttp.Handler())

		log.Println(fmt.Sprintf("Listening on %s", a))
		log.Fatal(http.ListenAndServe(a, nil))
	}(*adminAddr)

	log.Println(fmt.Sprintf("Listening on %s", *addr))
	log.Fatal(http.ListenAndServe(*addr, mux))
}
