package main

import (
	"context"
	"errors"
	"net/http"
	"reflect"
	"testing"

	"github.com/soundcloud/gokit/clients/authenticator"
)

type fakeAuthenticatorClient struct {
	fn func(ctx context.Context, token string, headers http.Header) (*authenticator.SessionResponse, error, int)
}

func (f *fakeAuthenticatorClient) GetSessionByToken(ctx context.Context, token string, headers http.Header) (*authenticator.SessionResponse, error, int) {
	return f.fn(ctx, token, headers)
}

type fakeGatekeeperClient struct {
	fn func(ctx context.Context, userUrn string) (*[]string, error)
}

func (f *fakeGatekeeperClient) GetFeatures(ctx context.Context, userUrn string) (*[]string, error) {
	return f.fn(ctx, userUrn)
}

func TestParseTokenFromAuthorization(t *testing.T) {
	tests := []struct {
		name        string
		input       string
		expected    string
		expectError bool
	}{
		{
			name:        "valid OAuth token",
			input:       "OAuth 1-123-456-abc123def456",
			expected:    "1-123-456-abc123def456",
			expectError: false,
		},
		{
			name:        "valid Bearer token",
			input:       "Bearer 1-123-456-abc123def456",
			expected:    "1-123-456-abc123def456",
			expectError: false,
		},
		{
			name:        "valid JWT token",
			input:       "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
			expected:    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
			expectError: false,
		},
		{
			name:        "empty string",
			input:       "",
			expected:    "",
			expectError: true,
		},
		{
			name:        "no space separator",
			input:       "Bearer",
			expected:    "",
			expectError: true,
		},
		{
			name:        "too many parts",
			input:       "Bearer token extra",
			expected:    "",
			expectError: true,
		},
		{
			name:        "no scheme",
			input:       "token123",
			expected:    "",
			expectError: true,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result, err := ParseTokenFromAuthorization(tt.input)

			if tt.expectError {
				if err == nil {
					t.Errorf("ParseTokenFromAuthorization(%q) expected error, got nil", tt.input)
				}
				if err != nil && err.Error() != "splitting Authorization header should result in 2 parts" {
					t.Errorf("ParseTokenFromAuthorization(%q) expected specific error message, got %q", tt.input, err.Error())
				}
			} else {
				if err != nil {
					t.Errorf("ParseTokenFromAuthorization(%q) unexpected error: %v", tt.input, err)
				}
				if result != tt.expected {
					t.Errorf("ParseTokenFromAuthorization(%q) = %q, want %q", tt.input, result, tt.expected)
				}
			}
		})
	}
}

func TestMaskToken(t *testing.T) {
	tests := []struct {
		name     string
		input    string
		expected string
	}{
		{
			name:     "short token",
			input:    "short",
			expected: "****",
		},
		{
			name:     "exactly 20 chars",
			input:    "12345678901234567890",
			expected: "****",
		},
		{
			name:     "long token",
			input:    "12345678901234567890abc123def456ghi789",
			expected: "12345678901234567890**********789",
		},
		{
			name:     "empty token",
			input:    "",
			expected: "****",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := maskToken(tt.input)
			if result != tt.expected {
				t.Errorf("maskToken(%q) = %q, want %q", tt.input, result, tt.expected)
			}
		})
	}
}

func TestGetSessionByToken(t *testing.T) {
	tests := []struct {
		name           string
		authHeader     string
		mockResponse   *authenticator.SessionResponse
		mockError      error
		mockStatus     int
		expectedError  string
		expectedResult *authenticator.SessionResponse
	}{
		{
			name:       "successful session retrieval",
			authHeader: "Bearer valid-token",
			mockResponse: &authenticator.SessionResponse{
				Urn:               "soundcloud:users:123",
				ClientApplication: "mobile-app",
				Scope:             "non-expiring",
			},
			mockError:     nil,
			mockStatus:    200,
			expectedError: "",
			expectedResult: &authenticator.SessionResponse{
				Urn:               "soundcloud:users:123",
				ClientApplication: "mobile-app",
				Scope:             "non-expiring",
			},
		},
		{
			name:           "empty auth header",
			authHeader:     "",
			mockResponse:   nil,
			mockError:      nil,
			mockStatus:     200,
			expectedError:  "error while parsing token from Authorization header: splitting Authorization header should result in 2 parts",
			expectedResult: nil,
		},
		{
			name:           "invalid auth header format",
			authHeader:     "invalid",
			mockResponse:   nil,
			mockError:      nil,
			mockStatus:     200,
			expectedError:  "error while parsing token from Authorization header: splitting Authorization header should result in 2 parts",
			expectedResult: nil,
		},
		{
			name:           "authenticator client error",
			authHeader:     "Bearer valid-token",
			mockResponse:   nil,
			mockError:      errors.New("network error"),
			mockStatus:     500,
			expectedError:  "network error",
			expectedResult: nil,
		},
		{
			name:           "401 unauthorized status",
			authHeader:     "Bearer invalid-token",
			mockResponse:   nil,
			mockError:      nil,
			mockStatus:     401,
			expectedError:  "authenticator GET session endpoint returned 400 or 401",
			expectedResult: nil,
		},
		{
			name:           "400 bad request status",
			authHeader:     "Bearer malformed-token",
			mockResponse:   nil,
			mockError:      nil,
			mockStatus:     400,
			expectedError:  "authenticator GET session endpoint returned 400 or 401",
			expectedResult: nil,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			service := &service{
				authenticatorClient: &fakeAuthenticatorClient{
					fn: func(ctx context.Context, token string, headers http.Header) (*authenticator.SessionResponse, error, int) {
						return tt.mockResponse, tt.mockError, tt.mockStatus
					},
				},
			}

			result, err := service.GetSessionByToken(context.Background(), tt.authHeader)

			if tt.expectedError != "" {
				if err == nil || err.Error() != tt.expectedError {
					t.Errorf("Expected error %q, got %v", tt.expectedError, err)
				}
			} else {
				if err != nil {
					t.Errorf("Unexpected error: %v", err)
				}
			}

			if !reflect.DeepEqual(result, tt.expectedResult) {
				t.Errorf("Expected result %+v, got %+v", tt.expectedResult, result)
			}
		})
	}
}

func TestGetSessionWithFeatures(t *testing.T) {
	tests := []struct {
		name             string
		authHeader       string
		sessionResponse  *authenticator.SessionResponse
		sessionError     error
		sessionStatus    int
		featuresResponse *[]string
		featuresError    error
		expectedError    string
		expectedFeatures []string
	}{
		{
			name:       "successful session with features",
			authHeader: "Bearer valid-token",
			sessionResponse: &authenticator.SessionResponse{
				Urn:               "soundcloud:users:123",
				ClientApplication: "mobile-app",
				Scope:             "non-expiring",
			},
			sessionError:     nil,
			sessionStatus:    200,
			featuresResponse: &[]string{"feature1", "feature2"},
			featuresError:    nil,
			expectedError:    "",
			expectedFeatures: []string{"feature1", "feature2"},
		},
		{
			name:             "empty authorization header",
			authHeader:       "",
			sessionResponse:  nil,
			sessionError:     nil,
			sessionStatus:    200,
			featuresResponse: nil,
			featuresError:    nil,
			expectedError:    "authorization error: no authorization header in request",
			expectedFeatures: nil,
		},
		{
			name:       "session error",
			authHeader: "Bearer invalid-token",
			sessionResponse: &authenticator.SessionResponse{
				Urn:               "soundcloud:users:123",
				ClientApplication: "mobile-app",
				Scope:             "non-expiring",
			},
			sessionError:     errors.New("session fetch failed"),
			sessionStatus:    401,
			featuresResponse: nil,
			featuresError:    nil,
			expectedError:    "authorization error: session fetch failed",
			expectedFeatures: nil,
		},
		{
			name:       "empty user urn",
			authHeader: "Bearer valid-token",
			sessionResponse: &authenticator.SessionResponse{
				Urn:               "",
				ClientApplication: "mobile-app",
				Scope:             "non-expiring",
			},
			sessionError:     nil,
			sessionStatus:    200,
			featuresResponse: nil,
			featuresError:    nil,
			expectedError:    "authorization error: anonymous sessions are not supported",
			expectedFeatures: nil,
		},
		{
			name:       "gatekeeper client error",
			authHeader: "Bearer valid-token",
			sessionResponse: &authenticator.SessionResponse{
				Urn:               "soundcloud:users:123",
				ClientApplication: "mobile-app",
				Scope:             "non-expiring",
			},
			sessionError:     nil,
			sessionStatus:    200,
			featuresResponse: nil,
			featuresError:    errors.New("gatekeeper unavailable"),
			expectedError:    "authorization error: call to gatekeeperClient failed: gatekeeper unavailable",
			expectedFeatures: nil,
		},
		{
			name:       "nil features response",
			authHeader: "Bearer valid-token",
			sessionResponse: &authenticator.SessionResponse{
				Urn:               "soundcloud:users:123",
				ClientApplication: "mobile-app",
				Scope:             "non-expiring",
			},
			sessionError:     nil,
			sessionStatus:    200,
			featuresResponse: nil,
			featuresError:    nil,
			expectedError:    "",
			expectedFeatures: nil,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			service := &service{
				authenticatorClient: &fakeAuthenticatorClient{
					fn: func(ctx context.Context, token string, headers http.Header) (*authenticator.SessionResponse, error, int) {
						return tt.sessionResponse, tt.sessionError, tt.sessionStatus
					},
				},
				gatekeeperClient: &fakeGatekeeperClient{
					fn: func(ctx context.Context, userUrn string) (*[]string, error) {
						return tt.featuresResponse, tt.featuresError
					},
				},
			}

			result, err := service.GetSessionWithFeatures(context.Background(), tt.authHeader)

			if tt.expectedError != "" {
				if err == nil || err.Error() != tt.expectedError {
					t.Errorf("Expected error %q, got %v", tt.expectedError, err)
				}
				return
			}

			if err != nil {
				t.Errorf("Unexpected error: %v", err)
				return
			}

			if result == nil {
				t.Error("Expected non-nil result")
				return
			}

			if !reflect.DeepEqual(result.Features, tt.expectedFeatures) {
				t.Errorf("Expected features %+v, got %+v", tt.expectedFeatures, result.Features)
			}

			if result.SessionResponse.Urn != tt.sessionResponse.Urn {
				t.Errorf("Expected urn %q, got %q", tt.sessionResponse.Urn, result.SessionResponse.Urn)
			}
		})
	}
}

func TestCreateSessionHeaders(t *testing.T) {
	tests := []struct {
		name     string
		session  *EnrichedSessionResponse
		expected map[string]string
	}{
		{
			name: "complete session data",
			session: &EnrichedSessionResponse{
				SessionResponse: authenticator.SessionResponse{
					Urn:               "soundcloud:users:123",
					ClientApplication: "mobile-app",
					Scope:             "non-expiring",
				},
				Features: []string{"feature1", "feature2", "feature3"},
			},
			expected: map[string]string{
				"Sc-User":          "soundcloud:users:123",
				"Sc-Agent":         "mobile-app",
				"Sc-Oauth-Scopes":  "non-expiring",
				"Sc-User-Features": "feature1,feature2,feature3",
			},
		},
		{
			name: "session with empty features",
			session: &EnrichedSessionResponse{
				SessionResponse: authenticator.SessionResponse{
					Urn:               "soundcloud:users:456",
					ClientApplication: "web-app",
					Scope:             "read",
				},
				Features: []string{},
			},
			expected: map[string]string{
				"Sc-User":         "soundcloud:users:456",
				"Sc-Agent":        "web-app",
				"Sc-Oauth-Scopes": "read",
			},
		},
		{
			name: "session with nil features",
			session: &EnrichedSessionResponse{
				SessionResponse: authenticator.SessionResponse{
					Urn:               "soundcloud:users:789",
					ClientApplication: "desktop-app",
					Scope:             "write",
				},
				Features: nil,
			},
			expected: map[string]string{
				"Sc-User":         "soundcloud:users:789",
				"Sc-Agent":        "desktop-app",
				"Sc-Oauth-Scopes": "write",
			},
		},
		{
			name: "minimal session data",
			session: &EnrichedSessionResponse{
				SessionResponse: authenticator.SessionResponse{
					Urn: "soundcloud:users:999",
				},
				Features: []string{"single-feature"},
			},
			expected: map[string]string{
				"Sc-User":          "soundcloud:users:999",
				"Sc-User-Features": "single-feature",
			},
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := CreateSessionHeaders(tt.session)

			if !reflect.DeepEqual(result, tt.expected) {
				t.Errorf("Expected headers %+v, got %+v", tt.expected, result)
			}
		})
	}
}

func TestMarshalRequestHeaders(t *testing.T) {
	tests := []struct {
		name     string
		headers  SessionHeaders
		expected map[string]string
	}{
		{
			name: "all fields populated",
			headers: SessionHeaders{
				User:     "soundcloud:users:123",
				Agent:    "mobile-app",
				Scopes:   "non-expiring",
				Features: "feature1,feature2",
			},
			expected: map[string]string{
				"Sc-User":          "soundcloud:users:123",
				"Sc-Agent":         "mobile-app",
				"Sc-Oauth-Scopes":  "non-expiring",
				"Sc-User-Features": "feature1,feature2",
			},
		},
		{
			name: "partial fields populated",
			headers: SessionHeaders{
				User:     "soundcloud:users:456",
				Agent:    "web-app",
				Features: "",
			},
			expected: map[string]string{
				"Sc-User":  "soundcloud:users:456",
				"Sc-Agent": "web-app",
			},
		},
		{
			name:     "empty headers",
			headers:  SessionHeaders{},
			expected: map[string]string{},
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := marshalRequestHeaders(tt.headers)

			if !reflect.DeepEqual(result, tt.expected) {
				t.Errorf("Expected headers %+v, got %+v", tt.expected, result)
			}
		})
	}
}
