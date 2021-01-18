package main

import (
	"fmt"
	"testing"
)

func TestTranscodingRequestPayload(t *testing.T) {
	key := "public-api/uvwxyz"
	filename := "Так закалялась сталь.mp3"
	want := fmt.Sprintf(`{"key":"%s","priority":"manual","filename":"%s"}`, key, filename)

	payload, err := transcodingRequestPayload(key, filename)
	if err != nil {
		t.Fatalf("expected generating payload not to fail, got: %v", err)
	}

	got := string(payload)
	if want != got {
		t.Errorf("expected transcoding request payload:\n%v\n\ngot:\n%v", want, got)
	}
}
