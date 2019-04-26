package main

import (
	"flag"
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"
	"os"
	"time"

	_ "net/http/pprof"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/credentials"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
	"github.com/aws/aws-sdk-go/service/s3/s3manager"
	"github.com/prometheus/client_golang/prometheus/promhttp"
	"github.com/soundcloud/gokit/dnssrv"
	"github.com/soundcloud/gokit/httpserver"
)

func main() {
	var (
		addr      = flag.String("addr", ":80", "Listen address")
		adminAddr = flag.String("admin-addr", ":5000", "Listen address admin server")

		moshiAddr     = flag.String("moshimoshi-addr", os.Getenv("MOSHIMOSHI_ADDRESS"), "MoshiMoshi service address")
		stranglerAddr = flag.String("strangler-addr", os.Getenv("PUBLIC_API_STRANGLER_ADDRESS"), "Public API strangler service address")

		awsKey    = flag.String("aws-key", os.Getenv("AWS_ACCESS_KEY_ID"), "AWS access key ID")
		awsSecret = flag.String("aws-secret", os.Getenv("AWS_SECRET_ACCESS_KEY"), "AWS secret access key")
		s3Bucket  = flag.String("s3-bucket", os.Getenv("AWS_S3_BUCKET"), "AWS S3 bucket")
		s3Region  = flag.String("s3-region", os.Getenv("AWS_S3_REGION"), "AWS S3 region")

		maxRequestBytes = flag.Int64("max-request-bytes", 5<<(10*2), "Max request size in bytes")
	)
	flag.Parse()

	s3cli := &http.Client{
		Transport: serviceTransport{
			name:      "S3",
			transport: http.DefaultTransport,
		},
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

	moshicli := &http.Client{
		Transport: serviceTransport{
			name:      "MOSHIMOSHI",
			transport: dnssrv.DefaultTransport,
		},
	}
	moshi := &moshimoshiClient{
		client: moshicli,
		host:   *moshiAddr,
	}

	service := &service{
		upload: &uploader{
			moshimoshi: moshi,
			s3Uploader: s3manager.NewUploaderWithClient(s3),
			s3Bucket:   *s3Bucket,
		},
	}

	stranglerURL, err := url.Parse("http://" + *stranglerAddr)
	if err != nil {
		log.Fatal(err)
	}
	strangler := httputil.NewSingleHostReverseProxy(stranglerURL)
	strangler.Transport = serviceTransport{
		name:      "PUBLIC_API_STRANGLER",
		transport: dnssrv.DefaultTransport,
	}

	controller := &controller{
		maxRequestBytes: *maxRequestBytes,
		proxy:           strangler,
		service:         service,
	}

	go func(a string) {
		http.Handle("/metrics", promhttp.Handler())

		log.Println("Server listening on:", a)
		if err := http.ListenAndServe(a, nil); err != nil {
			log.Fatal(err)
		}
	}(*adminAddr)

	mux := http.NewServeMux()
	mux.HandleFunc("/-/health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("OK"))
	})
	mux.HandleFunc("/tracks", httpHandler("/tracks", controller.tracks))

	server := httpserver.Graceful{
		Config: http.Server{
			Addr:    *addr,
			Handler: mux,
		},
		CloseTimeout: 60 * time.Second,
		DrainTimeout: 30 * time.Second,
	}

	log.Println("Admin server listening on:", *addr)
	if err := server.ListenAndServe(); err != nil {
		log.Fatal(err)
	}
}
