package main

import (
	"flag"
	"fmt"
	"log"
	"net/http"
	"net/url"
	"os"

	_ "net/http/pprof"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/credentials"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
	"github.com/aws/aws-sdk-go/service/s3/s3manager"
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

		moshiAddr    = flag.String("moshimoshiAddr", "localhost:9091", "MoshiMoshi service address")
		rawTargetURL = flag.String("targetURL", "http://localhost:9000/", "Target URL")

		awsKey    = flag.String("aws-key", os.Getenv(envAWSAccessKeyID), "AWS access key ID")
		awsSecret = flag.String("aws-secret", os.Getenv(envAWSSecretKey), "AWS secret access key")
		s3Bucket  = flag.String("s3-bucket", os.Getenv(envS3Bucket), "AWS S3 bucket")
		s3Region  = flag.String("s3-region", os.Getenv(envS3Region), "AWS S3 region")

		maxRequestBytes = flag.Int64("max-request-bytes", 500<<(10*2), "Max request size in bytes")
	)
	flag.Parse()

	// TODO: Use service discovery and add a custom roundtripper.
	targetURL, err := url.Parse(*rawTargetURL)
	if err != nil {
		log.Fatalf("Failed to parse target URL: %s", *rawTargetURL)
	}

	s3cli := httpClient("S3")
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

	moshicli := httpClient("MOSHIMOSHI")
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

	proxy := httpProxy("PUBLIC_API_STRANGLER", targetURL)
	controller := &controller{
		maxRequestBytes: *maxRequestBytes,
		proxy:           proxy,
		service:         service,
	}

	go func(a string) {
		http.Handle("/metrics", promhttp.Handler())

		log.Println(fmt.Sprintf("Listening on %s", a))
		log.Fatal(http.ListenAndServe(a, nil))
	}(*adminAddr)

	mux := http.NewServeMux()
	mux.HandleFunc("/-/health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("OK"))
	})
	mux.HandleFunc("/tracks", httpHandler("/tracks", controller.tracks))

	log.Println(fmt.Sprintf("Listening on %s", *addr))
	log.Fatal(http.ListenAndServe(*addr, mux))
}
