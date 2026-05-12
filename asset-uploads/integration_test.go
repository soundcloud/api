// build: +integration
package main

import (
	"bytes"
	"context"
	"errors"
	"fmt"
	"io"
	"net/http"
	"net/http/httptest"
	"net/http/httputil"
	"net/url"
	"testing"

	"github.com/aws/aws-sdk-go/service/s3/s3manager"
	"github.com/aws/aws-sdk-go/service/s3/s3manager/s3manageriface"
	"github.com/soundcloud/gokit/v2/clients/authenticator"
)

type fakeS3Manager struct {
	s3manageriface.UploaderAPI
}

func (f fakeS3Manager) Upload(i *s3manager.UploadInput, opts ...func(*s3manager.Uploader)) (*s3manager.UploadOutput, error) {
	return &s3manager.UploadOutput{}, nil
}

type alwaysSucceedTrackCoordinator struct{ uid string }

func (a alwaysSucceedTrackCoordinator) createUserPolicy(filename string, fileSize int64, session *EnrichedSessionResponse) (string, error) {
	return a.uid, nil
}

func (a alwaysSucceedTrackCoordinator) triggerTranscodings(string) error { return nil }

type alwaysAllowedAuthenticator struct{}

func (a alwaysAllowedAuthenticator) GetSessionByToken(ctx context.Context, token string, headers http.Header) (*authenticator.SessionResponse, error, int) {
	sessionResponse := authenticator.SessionResponse{
		CacheKey:          "someCacheKey",
		ClientApplication: "soundcloud:clients:1",
		Scope:             "stub-scope",
		Session:           "stub-session",
		Urn:               "soundcloud:users:1",
	}
	return &sessionResponse, nil, 200
}

type noFeaturesGatekeeper struct{}

func (g noFeaturesGatekeeper) GetFeatures(ctx context.Context, user string) (*[]string, error) {
	return &[]string{}, nil
}

func TestControllerServiceS3Integration(t *testing.T) {
	uploader := &uploader{
		trackCoordinator: &alwaysSucceedTrackCoordinator{uid: "testUid"},
		s3Bucket:         "test-bucket",
		s3Uploader:       &fakeS3Manager{},
	}

	service := &service{
		authenticatorClient: alwaysAllowedAuthenticator{},
		gatekeeperClient:    noFeaturesGatekeeper{},
		upload:              uploader,
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

	longFileName := "0I9vCc8sKj4LzHxG7mRfN6YlPnQb2JtV1a3qOEUZDXTWgwA5pSyBhFeMuTr1aB5cD3eF7gH2iJ8kL0mN4oP6qR9sT5uV3wX1yZ7bA9dC2fE4hG6jI8lK0nMjK5b9X1cLx2dF0tP6yNzH8aVr4qE7sGhIuO3mWpAeRiSvTgDQwUoZfCnMl0I9vCc8sKj4LzHxG7mRfN6YlPnQb2JtV1a3qOEUZDXTWgwA5pSyBhFeMuTr1aB5cD3eF7gH2iJ8kL0mN4oP6qR9sT5uV3wX1yZ7bA9dC2fE4hG6jI8lK0nMjK5"

	tests := [...]struct {
		body       []byte
		statusCode int
		result     []byte
	}{
		0: {
			body: []byte(
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
					crlf),
			statusCode: http.StatusCreated,
			result: []byte(
				"--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Track" +
					crlf + "--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[original_filename]\"" +
					crlf + "" +
					crlf + "my_track.wav" +
					crlf + "--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[uid]\"" +
					crlf + "" +
					crlf + "testUid" +
					crlf + "--------------------------6808b4f61ea0e5a2--" +
					crlf),
		},
		1: {
			body: []byte(
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
					crlf),
			statusCode: http.StatusUnprocessableEntity,
			result:     nil,
		},
		2: {
			body: []byte(
				crlf + "--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[uid]\"" +
					crlf + "" +
					crlf + "user-provided-uid" +
					crlf + "--------------------------6808b4f61ea0e5a2--" +
					crlf),
			statusCode: http.StatusBadRequest,
			result:     nil,
		},
	}

	for _, tt := range tests {
		res := httptest.NewRecorder()
		req := httptest.NewRequest("POST", "/tracks", bytes.NewReader(tt.body))
		req.Host = "api.sc.local"
		req.Header.Set("Authorization", "OAuth some-token")
		req.Header.Set("Content-Type", "multipart/form-data; boundary=------------------------6808b4f61ea0e5a2")

		controller.tracks().ServeHTTP(res, req)

		result := res.Result()
		if want, got := tt.statusCode, result.StatusCode; want != got {
			t.Errorf("Expected response status to be %v, got %v", want, got)
		}

		expectedLength := -1
		if tt.result != nil {
			expectedLength = len(tt.result)
		}

		if want, got := int64(expectedLength), result.ContentLength; want != got {
			t.Errorf("Expected content length to be %d, got %d", want, got)
		}

		if expectedLength >= 0 {
			bs, _ := io.ReadAll(result.Body)
			if want, got := tt.result, bs; !bytes.Equal(want, got) {
				t.Errorf("Expected response body to be %s, got %s", want, got)
			}
		}
	}

}

func TestAuthorizationErrorConversion(t *testing.T) {
	tests := []struct {
		name           string
		error          error
		expectedStatus int
		description    string
	}{
		{
			name:           "direct authorizationError (no auth header)",
			error:          authorizationError{errors.New("no authorization header in request")},
			expectedStatus: http.StatusUnauthorized,
			description:    "Direct authorizationError for missing header should convert to 401",
		},
		{
			name:           "direct authorizationError (gatekeeper failure)",
			error:          authorizationError{fmt.Errorf("call to gatekeeperClient failed: %w", errors.New("gatekeeper unavailable"))},
			expectedStatus: http.StatusUnauthorized,
			description:    "Direct authorizationError with wrapped cause should convert to 401",
		},
		{
			name:           "clientError",
			error:          clientError{errors.New("bad request")},
			expectedStatus: http.StatusBadRequest,
			description:    "clientError should convert to 400",
		},
		{
			name:           "fileNameValidationError",
			error:          fileNameValidationError{},
			expectedStatus: http.StatusUnprocessableEntity,
			description:    "fileNameValidationError should convert to 422",
		},
		{
			name:           "generic error (parse token)",
			error:          fmt.Errorf("error while parsing token from Authorization header: %w", errors.New("splitting Authorization header should result in 2 parts")),
			expectedStatus: http.StatusInternalServerError,
			description:    "Generic parse error should default to 500",
		},
		{
			name:           "authenticator client error",
			error:          errors.New("authenticator GET session endpoint returned 400 or 401"),
			expectedStatus: http.StatusInternalServerError,
			description:    "Authenticator client error should default to 500",
		},
		{
			name:           "generic network error",
			error:          errors.New("network timeout"),
			expectedStatus: http.StatusInternalServerError,
			description:    "Generic error should default to 500",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			// Create a test response recorder
			w := httptest.NewRecorder()
			r := httptest.NewRequest("POST", "/test", nil)

			handleProxyError(w, r, tt.error)

			// Check the status code
			if w.Code != tt.expectedStatus {
				t.Errorf("%s: expected status %d, got %d", tt.description, tt.expectedStatus, w.Code)
			}

			// Verify that the response body is empty (as per the function implementation)
			body := w.Body.String()
			if body != "\n" {
				t.Errorf("%s: expected empty response body, got %q", tt.description, body)
			}
		})
	}
}
