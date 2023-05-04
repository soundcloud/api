// build: +integration
package main

import (
	"bytes"
	"io"
	"io/ioutil"
	"net/http"
	"net/http/httptest"
	"net/http/httputil"
	"net/url"
	"testing"

	"github.com/aws/aws-sdk-go/service/s3/s3manager"
	"github.com/aws/aws-sdk-go/service/s3/s3manager/s3manageriface"
)

type fakeS3Manager struct {
	s3manageriface.UploaderAPI
}

func (f fakeS3Manager) Upload(i *s3manager.UploadInput, opts ...func(*s3manager.Uploader)) (*s3manager.UploadOutput, error) {
	return &s3manager.UploadOutput{}, nil
}

type fakeMediaServiceClient struct {
	mediaServiceClientAPI
	uid string
}

func (f fakeMediaServiceClient) createTranscoding(string, string) (string, error) { return f.uid, nil }

func TestControllerServiceS3Integration(t *testing.T) {
	uploader := &uploader{
		mediaService: &fakeMediaServiceClient{uid: "testUid"},
		s3Bucket:     "test-bucket",
		s3Uploader:   &fakeS3Manager{},
		s3KeyGenerator: func() string {
			return "public-api/foobar"
		},
	}

	service := &service{
		upload: uploader,
	}

	server := httptest.NewServer(http.HandlerFunc(
		func(w http.ResponseWriter, r *http.Request) {
			w.WriteHeader(http.StatusCreated)
			_, _ = io.Copy(w, r.Body)
		},
	))

	url, _ := url.Parse(server.URL)
	proxy := httputil.NewSingleHostReverseProxy(url)

	controller := &controller{
		maxRequestBytes: 1024,
		service:         service,
		proxy:           proxy,
	}

	body := []byte(
		"--------------------------6808b4f61ea0e5a2" +
			crlf + "Content-Disposition: form-data; name=\"track[asset_data]\"; filename=\"my_track.wav\"" +
			crlf + "Content-Type: application/octet-stream" +
			crlf + "" +
			crlf + "12345" +
			crlf + "" +
			crlf + "--------------------------6808b4f61ea0e5a2" +
			crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
			crlf + "" +
			crlf + "My Track" +
			crlf + "--------------------------6808b4f61ea0e5a2--" +
			crlf)

	longFileName := "0I9vCc8sKj4LzHxG7mRfN6YlPnQb2JtV1a3qOEUZDXTWgwA5pSyBhFeMuTr1aB5cD3eF7gH2iJ8kL0mN4oP6qR9sT5uV3wX1yZ7bA9dC2fE4hG6jI8lK0nMjK5b9X1cLx2dF0tP6yNzH8aVr4qE7sGhIuO3mWpAeRiSvTgDQwUoZfCnMl0I9vCc8sKj4LzHxG7mRfN6YlPnQb2JtV1a3qOEUZDXTWgwA5pSyBhFeMuTr1aB5cD3eF7gH2iJ8kL0mN4oP6qR9sT5uV3wX1yZ7bA9dC2fE4hG6jI8lK0nMjK5"
	fileNameTooLong := []byte(
		"--------------------------6808b4f61ea0e5a2" +
			crlf + "Content-Disposition: form-data; name=\"track[asset_data]\"; filename=\"" + longFileName + ".wav\"" +
			crlf + "Content-Type: application/octet-stream" +
			crlf + "" +
			crlf + "12345" +
			crlf + "" +
			crlf + "--------------------------6808b4f61ea0e5a2" +
			crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
			crlf + "" +
			crlf + "My Track" +
			crlf + "--------------------------6808b4f61ea0e5a2--" +
			crlf)

	res := httptest.NewRecorder()
	req := httptest.NewRequest("POST", "/tracks", bytes.NewReader(body))
	req.Host = "api.sc.local"
	req.Header.Set("Content-Type", "multipart/form-data; boundary=------------------------6808b4f61ea0e5a2")

	controller.tracks().ServeHTTP(res, req)

	result := res.Result()
	if want, got := http.StatusCreated, result.StatusCode; want != got {
		t.Errorf("Expected response status to be %v, got %v", want, got)
	}

	expected := []byte(
		"--------------------------6808b4f61ea0e5a2" +
			crlf + "Content-Disposition: form-data; name=\"track[original_filename]\"" +
			crlf + "" +
			crlf + "my_track.wav" +
			crlf + "--------------------------6808b4f61ea0e5a2" +
			crlf + "Content-Disposition: form-data; name=\"track[uid]\"" +
			crlf + "" +
			crlf + "testUid" +
			crlf + "--------------------------6808b4f61ea0e5a2" +
			crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
			crlf + "" +
			crlf + "My Track" +
			crlf + "--------------------------6808b4f61ea0e5a2--" +
			crlf)
	if want, got := int64(len(expected)), result.ContentLength; want != got {
		t.Errorf("Expected content length to be %d, got %d", want, got)
	}

	bs, _ := ioutil.ReadAll(result.Body)
	if want, got := expected, bs; !bytes.Equal(want, got) {
		t.Errorf("Expected response body to be %s, got %s", want, got)
	}

	newRes := httptest.NewRecorder()
	newReq := httptest.NewRequest("POST", "/tracks", bytes.NewReader(fileNameTooLong))
	newReq.Host = "api.sc.local"
	newReq.Header.Set("Content-Type", "multipart/form-data; boundary=------------------------6808b4f61ea0e5a2")

	controller.tracks().ServeHTTP(newRes, newReq)

	newResult := newRes.Result()
	if want, got := http.StatusUnprocessableEntity, newResult.StatusCode; want != got {
		t.Errorf("Expected response status to be %v, got %v", want, got)
	}
}
