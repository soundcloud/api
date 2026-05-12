package main

import (
	"bytes"
	"fmt"
	"io"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/soundcloud/gokit/v2/clients/authenticator"
)

type trackCoordinatorMock struct {
	path                string
	incomingRequestBody *bytes.Buffer
	incomingHeaders     http.Header
	responseCode        int
	responseBody        string
}

func (t *trackCoordinatorMock) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	t.path = r.URL.Path
	t.incomingHeaders = r.Header

	t.incomingRequestBody = &bytes.Buffer{}
	if _, err := io.Copy(t.incomingRequestBody, r.Body); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
	}

	w.WriteHeader(t.responseCode)
	if _, err := w.Write([]byte(t.responseBody)); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
	}
}

func TestCreateUserPolicy_SuccessScenario(t *testing.T) {
	stubResponse := `{"uid":"someUid","ignoredField":"ignoredValue"}`
	m := &trackCoordinatorMock{responseCode: http.StatusCreated, responseBody: stubResponse}
	srv := httptest.NewServer(m)
	client := trackCoordinatorClient{http.DefaultClient, srv.URL}

	defer srv.Close()

	inputSessionResponse := &EnrichedSessionResponse{
		SessionResponse: authenticator.SessionResponse{Urn: "soundcloud:users:1"},
		Features:        []string{"feature1", "feature2"},
	}

	uid, err := client.createUserPolicy("test.mp3", 12345, inputSessionResponse)
	if err != nil {
		t.Errorf("Unexpected error. Got %s", err)
	}

	if want, got := "someUid", uid; want != got {
		t.Errorf("unexpected uid value returned. Want: %s; got: %s", want, got)
	}

	if want, got := "/user/upload-policy", m.path; want != got {
		t.Errorf("wrong path used. Want: %s ; Got: %s", want, got)
	}

	expectedHeaders := map[string]string{"Content-Type": "application/json", "Sc-System": "api-public-assets", "Sc-User-Features": "feature1,feature2", "Sc-User": "soundcloud:users:1"}

	for k, v := range expectedHeaders {
		if want, got := v, m.incomingHeaders.Get(k); want != got {
			t.Errorf("Expected header not present/with wrong value. Want: %s-%s ; Got: %s-%s", k, want, k, got)
		}
	}

	if want, got := `{"filename":"test.mp3","filesize":12345}`, m.incomingRequestBody.String(); want != got {
		t.Errorf("error in request body. Want: %v ; Got: %v", want, got)
	}

}

func TestCreateUserPolicy_FailureScenarios(t *testing.T) {
	tests := [...]struct {
		name                 string
		inputSessionResponse *EnrichedSessionResponse
		responseCode         int
		responseBody         string
		expectedError        error
	}{
		0: {
			name:                 "for the case track-coordinator returns a 403, produce an appropriate error",
			inputSessionResponse: &EnrichedSessionResponse{},
			responseCode:         http.StatusForbidden,
			responseBody:         `{"error": "exceeded upload quota"}`,
			expectedError:        authorizationError{fmt.Errorf("not permitted to perform upload for user ")},
		},
		1: {
			name:                 "for the case track-coordinator returns a 400, produce an appropriate error",
			inputSessionResponse: &EnrichedSessionResponse{},
			responseCode:         http.StatusBadRequest,
			responseBody:         `{"error": "file is too big"}`,
			expectedError:        clientError{fmt.Errorf("failed to generate upload id for user ")},
		},
		2: {
			name: "for the case track-coordinator returns a successful response missing a uid json field, produce an appropriate error",
			inputSessionResponse: &EnrichedSessionResponse{
				SessionResponse: authenticator.SessionResponse{Urn: "soundcloud:users:1"},
			},
			responseCode:  http.StatusCreated,
			responseBody:  `{"missingUid":"valueMissing"}`,
			expectedError: fmt.Errorf("failed to generate upload id for user soundcloud:users:1"),
		},
	}

	for _, tt := range tests {
		m := &trackCoordinatorMock{responseCode: tt.responseCode, responseBody: tt.responseBody}
		srv := httptest.NewServer(m)
		client := trackCoordinatorClient{http.DefaultClient, srv.URL}

		defer srv.Close()

		_, err := client.createUserPolicy("test.mp3", 12345, tt.inputSessionResponse)
		if err == nil {
			t.Errorf("Missing error. Want: %s ", tt.expectedError)
		}

		if want, got := tt.expectedError.Error(), err.Error(); want != got {
			t.Errorf("Unexpected error. Want: %s ; Got %s", tt.expectedError, err)
		}
	}
}

func TestTriggerTranscodings_SuccessScenario(t *testing.T) {
	stubResponse := `{"ignoredField":"ignoredValue"}`
	m := &trackCoordinatorMock{responseCode: http.StatusCreated, responseBody: stubResponse}
	srv := httptest.NewServer(m)
	client := trackCoordinatorClient{http.DefaultClient, srv.URL}

	defer srv.Close()

	err := client.triggerTranscodings("someUid")
	if err != nil {
		t.Errorf("Unexpected error. Got %s", err)
	}

	if want, got := "/transcodings", m.path; want != got {
		t.Errorf("wrong path used. Want: %s ; Got: %s", want, got)
	}

	expectedHeaders := map[string]string{"Content-Type": "application/json", "Sc-System": "api-public-assets"}

	for k, v := range expectedHeaders {
		if want, got := v, m.incomingHeaders.Get(k); want != got {
			t.Errorf("Expected header not present/with wrong value. Want: %s-%s ; Got: %s-%s", k, want, k, got)
		}
	}

	if want, got := `{"uid":"someUid"}`, m.incomingRequestBody.String(); want != got {
		t.Errorf("error in request body. Want: %v ; Got: %v", want, got)
	}
}

func TestTriggerTranscodings_FailureScenarios(t *testing.T) {
	tests := [...]struct {
		name          string
		responseCode  int
		responseBody  string
		expectedError error
	}{
		0: {
			name:          "for the case track-coordinator returns anything other than 201, produce an appropriate error where response body provided",
			responseCode:  http.StatusInternalServerError,
			responseBody:  `{"error": "something went wrong"}`,
			expectedError: fmt.Errorf(`failed to trigger transcoding for uid: someUid; status: 500; resp: {"error": "something went wrong"};`),
		},
		1: {
			name:          "for the case track-coordinator returns anything other than 201, produce an appropriate error where no response body provided",
			responseCode:  http.StatusInternalServerError,
			expectedError: fmt.Errorf(`failed to trigger transcoding for uid: someUid; status: 500; resp: ;`),
		},
	}

	for _, tt := range tests {
		m := &trackCoordinatorMock{responseCode: tt.responseCode, responseBody: tt.responseBody}
		srv := httptest.NewServer(m)
		client := trackCoordinatorClient{http.DefaultClient, srv.URL}

		defer srv.Close()

		err := client.triggerTranscodings("someUid")
		if err == nil {
			t.Errorf("Missing error. Want: %s ", tt.expectedError)
		}

		if want, got := tt.expectedError.Error(), err.Error(); want != got {
			t.Errorf("Unexpected error. Want: %s ; Got %s", tt.expectedError, err)
		}
	}
}
