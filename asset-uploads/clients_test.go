package main

import (
	"fmt"
	"testing"
)

func TestTranscodingRequestPayload(t *testing.T) {
	uid := "uvwxyz"
	want := fmt.Sprintf(`{"transcoding":{"uid":"%s"}}`, uid)

	payload, err := transcodingRequestPayload(uid)
	if err != nil {
		t.Fatalf("expected generating payload not to fail, got: %v", err)
	}

	got := string(payload)
	if want != got {
		t.Errorf("expected transcoding request payload:\n%v\n\ngot:\n%v", want, got)
	}
}
