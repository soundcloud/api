package main

import (
	"bytes"
	"context"
	"errors"
	"fmt"
	"github.com/soundcloud/gokit/clients/authenticator"
	"io"
	"log"
	"mime/multipart"
	"net/http"
	"reflect"
	"strings"
)

type authenticatorAPI interface {
	GetSessionByToken(ctx context.Context, token string, headers http.Header) (*authenticator.SessionResponse, error, int)
}

type gatekeeperAPI interface {
	GetFeatures(ctx context.Context, user string) (*[]string, error)
}

type SessionHeaders struct {
	User     string `header:"Sc-User"`
	Agent    string `header:"Sc-Agent"`
	Scopes   string `header:"Sc-Oauth-Scopes"`
	Features string `header:"Sc-User-Features"`
}

func (s service) extractAuthToken(p *multipart.Part) (*bytes.Buffer, error) {
	const (
		maxTokenBytes = 1024
	)

	// Because the token is extracted to be propagated outside of the request
	// body, we're restricting its maximum length.
	lr := io.LimitReader(p, maxTokenBytes)

	// Exceeding maxTokenBytes is currently not an error condition.
	// We'll use what fits into maxTokenBytes.
	buffer := &bytes.Buffer{}
	if _, err := buffer.ReadFrom(lr); err != nil {
		return nil, err
	}

	return buffer, nil
}

func maskToken(token string) string {
	if len(token) <= 20 {
		return "****"
	}
	return strings.Join([]string{token[:20], strings.Repeat("*", 10), token[len(token)-3:]}, "")
}

func (s service) GetSessionWithFeatures(ctx context.Context, authorizationHeader string) (*EnrichedSessionResponse, error) {
	if authorizationHeader == "" {
		return nil, authorizationError{errors.New("no authorization header in request")}
	}
	session, err := s.GetSessionByToken(ctx, authorizationHeader)
	if err != nil {
		return nil, authorizationError{err}
	}

	user := session.Urn
	if user == "" { // shouldn't be the case, anonymous session cannot be return since we don't pass client_id in GetSessionByToken
		log.Printf("user urn is not present in session, session urn: %s", session.Session)
		return nil, authorizationError{errors.New("anonymous sessions are not supported")}
	}

	res, errFeatures := s.gatekeeperClient.GetFeatures(ctx, user)
	if errFeatures != nil {
		log.Printf("call to gatekeeperClient failed: %v", errFeatures)
		return nil, authorizationError{fmt.Errorf("call to gatekeeperClient failed: %w", errFeatures)}
	}

	enrichedSession := &EnrichedSessionResponse{SessionResponse: *session}
	if res != nil {
		enrichedSession.Features = *res
	}

	return enrichedSession, nil
}

type EnrichedSessionResponse struct {
	authenticator.SessionResponse
	Features []string
}

func (s service) GetSessionByToken(ctx context.Context, authHeader string) (*authenticator.SessionResponse, error) {
	token, err := ParseTokenFromAuthorization(authHeader)
	if err != nil {
		log.Printf("error while parsing token from Authorization header")
		return nil, fmt.Errorf("error while parsing token from Authorization header: %w", err)
	}
	session, err, status := s.authenticatorClient.GetSessionByToken(ctx, token, http.Header{})
	if err != nil {
		return nil, err
	}
	if status == 400 || status == 401 {
		return nil, errors.New("authenticator GET session endpoint returned 400 or 401")
	}
	return session, nil
}

// ParseTokenFromAuthorization stripes out token from header value, auth header looks like
// OAuth {AUTH_VERSION}-{CREDENTIAL_ID}-{USER_ID}-{TOKEN}
// or Bearer {AUTH_VERSION}-{CREDENTIAL_ID}-{USER_ID}-{TOKEN}
// or OAuth {JWT}
// or Bearer {JWT}
func ParseTokenFromAuthorization(authHeader string) (string, error) {
	splitHeader := strings.Split(authHeader, " ")
	if len(splitHeader) != 2 {
		return "", errors.New("splitting Authorization header should result in 2 parts")
	}
	return splitHeader[1], nil
}

func CreateSessionHeaders(session *EnrichedSessionResponse) map[string]string {
	// Create the session data and write it to the headers
	sessionData := SessionHeaders{
		User:     session.Urn,
		Agent:    session.ClientApplication,
		Scopes:   session.Scope,
		Features: strings.Join(session.Features, ","),
	}
	return marshalRequestHeaders(sessionData)
}

func marshalRequestHeaders(h SessionHeaders) map[string]string {
	fields := reflect.VisibleFields(reflect.TypeOf(h))
	values := reflect.ValueOf(h)
	result := make(map[string]string)
	for i, v := range fields {
		if val := values.Field(i).String(); val != "" {
			if key := v.Tag.Get("header"); key != "" {
				result[key] = val
			}
		}
	}
	return result
}
