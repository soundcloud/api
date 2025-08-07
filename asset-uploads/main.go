package main

import (
	"flag"
	"fmt"
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"
	"os"
	"strconv"
	"time"

	_ "net/http/pprof"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/credentials"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
	"github.com/aws/aws-sdk-go/service/s3/s3manager"
	"github.com/gorilla/mux"
	"github.com/prometheus/client_golang/prometheus/promhttp"
	"github.com/soundcloud/gokit/dnssrv"
	"github.com/soundcloud/gokit/httpserver"
	"github.com/soundcloud/gokit/instrumenthttp"
)

func main() {
	bytes, err := strconv.ParseInt(os.Getenv("MAX_REQUEST_BYTES"), 10, 64)
	if err != nil {
		log.Fatal(err)
	}
	var (
		addr      = flag.String("addr", ":80", "Listen address")
		adminAddr = flag.String("admin-addr", ":5000", "Listen address admin server")

		mediaServiceAddr = flag.String("media-service-addr", os.Getenv("MEDIA_SERVICE_ADDRESS"), "media service address")
		apiGatewayAddr = flag.String("apiGatewayAddr", os.Getenv("API_PUBLIC_GATEWAY_ADDRESS"), "API Public Gateway service address")

		awsKey    = flag.String("aws-key", os.Getenv("AWS_ACCESS_KEY_ID"), "AWS access key ID")
		awsSecret = flag.String("aws-secret", os.Getenv("AWS_SECRET_ACCESS_KEY"), "AWS secret access key")
		s3Bucket  = flag.String("s3-bucket", os.Getenv("AWS_S3_BUCKET"), "AWS S3 bucket")
		s3Region  = flag.String("s3-region", os.Getenv("AWS_S3_REGION"), "AWS S3 region")

		maxRequestBytes = flag.Int64("max-request-bytes", bytes, "Max request size in bytes")
	)
	flag.Parse()

	s3cli := &http.Client{
		Transport: instrumenthttp.Tripperware(
			"S3",
			instrumenthttp.TripperwareOpts{},
			http.DefaultTransport,
		),
	}

	s3 := s3.New(
		session.Must(
			session.NewSession(&aws.Config{
				Credentials: credentials.NewStaticCredentials(
					*awsKey,
					*awsSecret,
					"", // The session token is optional.
				),
				HTTPClient: s3cli,
				Region:     aws.String(*s3Region),
			}),
		),
	)

	mediacli := &http.Client{
		Transport: instrumenthttp.Tripperware(
			"MEDIA_SERVICE",
			instrumenthttp.TripperwareOpts{},
			dnssrv.DefaultTransport,
		),
	}

	mediaService := &mediaServiceClient{
		client: mediacli,
		host:   *mediaServiceAddr,
	}

	service := &service{
		upload: &uploader{
			mediaService:   mediaService,
			s3Uploader:     s3manager.NewUploaderWithClient(s3),
			s3Bucket:       *s3Bucket,
			s3KeyGenerator: generateS3Key,
		},
	}

	apiPublic, err := initializeReverseProxy(*apiGatewayAddr, http.DefaultTransport)
	if err != nil {
		log.Fatal(err)
	}

	controller := &controller{
		maxRequestBytes: *maxRequestBytes,
		proxy:           apiPublic,
		service:         service,
	}

	go func(a string) {
		http.Handle("/metrics", promhttp.Handler())

		log.Println("Admin server listening on:", a)
		if err := http.ListenAndServe(a, nil); err != nil {
			log.Fatal(err)
		}
	}(*adminAddr)

	router := mux.NewRouter()
	router.HandleFunc("/-/health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("OK"))
	})

	tracksHandler := controller.tracks()

	router.Handle("/tracks/{id:[0-9]+}", httpHandler("/tracks/:id", tracksHandler))
	router.Handle("/tracks/{id:[0-9]+}.json", httpHandler("/tracks/:id.json", tracksHandler))
	router.Handle("/tracks.json", httpHandler("/tracks.json", tracksHandler))
	router.Handle("/tracks.json/", httpHandler("/tracks.json/", tracksHandler))
	router.Handle("/tracks", httpHandler("/tracks", tracksHandler))
	router.Handle("/tracks/", httpHandler("/tracks/", tracksHandler))

	router.Handle("/v1/tracks", httpHandler("/v1/tracks", tracksHandler))
	router.Handle("/v1/tracks/", httpHandler("/v1/tracks/", tracksHandler))
	router.Handle("/v1/tracks.json", httpHandler("/v1/tracks.json", tracksHandler))
	router.Handle("/v1/tracks.json/", httpHandler("/v1/tracks.json/", tracksHandler))

	router.Handle("/users/{userId:[0-9]+}/tracks", httpHandler("/users/:userid/tracks", tracksHandler))
	router.Handle("/users/{userId:[0-9]+}/tracks/", httpHandler("/users/:userid/tracks/", tracksHandler))

	router.Handle("/me/tracks", httpHandler("/me/tracks", tracksHandler))
	router.Handle("/me/tracks.json", httpHandler("/me/tracks.json", tracksHandler))

	router.PathPrefix("/").Handler(httpHandler("generic", controller.generic()))

	server := httpserver.Graceful{
		Config: http.Server{
			Addr:    *addr,
			Handler: router,
		},
		CloseTimeout: 60 * time.Second,
		DrainTimeout: 30 * time.Second,
	}

	log.Println("Server listening on:", *addr)
	if err := server.ListenAndServe(); err != nil {
		log.Fatal(err)
	}
}

func initializeReverseProxy(addr string, transport http.RoundTripper) (*httputil.ReverseProxy, error) {
	// Parse the address URL
	url, err := url.Parse(addr)
	if err != nil {
		log.Fatal(err)
		return nil, fmt.Errorf("failed to parse URL: %w", err)
	}

	// Initialize the ReverseProxy for the rewritten target URL
	proxy := &httputil.ReverseProxy{
		Director: func(req *http.Request) {
			req.URL.Scheme = url.Scheme
			req.URL.Host = url.Host
			req.URL.Path = "/tracks-after-upload" // Rewrite the path here
			req.Host = url.Host
		},
		Transport:    http.DefaultTransport, // Or use your desired transport
		ErrorHandler: handleProxyError,
	}
	proxy.Transport = instrumenthttp.Tripperware(
		"API_PUBLIC",
		instrumenthttp.TripperwareOpts{},
		transport,
	)
	proxy.ErrorHandler = handleProxyError

	// Return the initialized proxy
	return proxy, nil
}
