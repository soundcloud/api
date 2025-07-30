package main

import (
	"bytes"
	"io"
	"mime/multipart"
	"strings"
)

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
