package main

import (
	"bytes"
	"errors"
	"io/ioutil"
	"testing"
)

const (
	crlf = "\r\n"
)

type fakeUploader struct {
	uploaderAPI
	fn func(*uploadTrackRequest) (*uploadTrackResponse, error)
}

func (f fakeUploader) uploadTrack(r *uploadTrackRequest) (*uploadTrackResponse, error) { return f.fn(r) }

func TestValidMultipart(t *testing.T) {
	body := []byte(
		"--------------------------7570ceb7c872df7a" +
			crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
			crlf + "" +
			crlf + "My Track" +
			crlf + "--------------------------7570ceb7c872df7a" +
			crlf + "Content-Disposition: form-data; name=\"track[genre]\"" +
			crlf + "" +
			crlf + "HipHop" +
			crlf + "--------------------------7570ceb7c872df7a--" +
			crlf)

	service := &service{}

	res, err := service.createTrack(&createTrackRequest{
		body:     bytes.NewReader(body),
		boundary: "------------------------7570ceb7c872df7a",
	})
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	if !bytes.Equal(body, res.body.Bytes()) {
		t.Error("Expected request to be unmodified")
	}
}

func TestInvalidMultipart(t *testing.T) {
	service := &service{}

	_, err := service.createTrack(&createTrackRequest{
		body:     bytes.NewReader([]byte("Not mime multipart data")),
		boundary: "some-boundary",
	})

	if err == nil {
		t.Error("Expected to fail parsing malformed multipart data")
	}
}

func TestExtractAuthToken(t *testing.T) {
	const (
		fiveBytes   = "012345"
		twentyBytes = "01234567890123456789"
		largeToken  = twentyBytes + twentyBytes + twentyBytes + fiveBytes
	)

	tests := [...]struct {
		auth []byte
		body []byte
	}{
		0: {
			auth: []byte{},
			body: []byte(
				"--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Tack" +
					crlf + "--------------------------becf7c3b48144d16--" +
					crlf),
		},
		1: {
			auth: []byte("some-token"),
			body: []byte(
				"--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"oauth_token\"" +
					crlf + "" +
					crlf + "some-token" +
					crlf + "--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Tack" +
					crlf + "--------------------------becf7c3b48144d16--" +
					crlf),
		},
		2: {
			auth: []byte(largeToken[:64]),
			body: []byte(
				"--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"oauth_token\"" +
					crlf + "" +
					crlf + largeToken +
					crlf + "--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Tack" +
					crlf + "--------------------------becf7c3b48144d16--" +
					crlf),
		},
	}

	for _, tt := range tests {
		service := &service{}

		got, err := service.createTrack(&createTrackRequest{
			body:     bytes.NewReader(tt.body),
			boundary: "------------------------becf7c3b48144d16",
		})
		if err != nil {
			t.Fatalf("Expected no error, got: %v", err)
		}

		want := struct {
			auth []byte
			body []byte
		}{
			auth: tt.auth,
			body: []byte(
				"--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Tack" +
					crlf + "--------------------------becf7c3b48144d16--" +
					crlf),
		}

		if !bytes.Equal(want.auth, got.auth.Bytes()) {
			t.Errorf("Expected auth to be %v, got %v", string(want.auth), got.auth.String())
		}

		if !bytes.Equal(want.body, got.body.Bytes()) {
			t.Errorf("Expected body to be %v, got %v", string(want.body), got.body.String())
		}
	}
}

func TestStoreTrackAssetData(t *testing.T) {
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

	tests := [...]struct {
		body []byte
		fn   func(*uploadTrackRequest) (*uploadTrackResponse, error)
	}{
		0: {
			body: []byte(
				"--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[original_filename]\"" +
					crlf + "" +
					crlf + "my_track.wav" +
					crlf + "--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[uid]\"" +
					crlf + "" +
					crlf + "12345" +
					crlf + "--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Track" +
					crlf + "--------------------------6808b4f61ea0e5a2--" +
					crlf),
			fn: func(r *uploadTrackRequest) (*uploadTrackResponse, error) {
				// Echo the track data (without whitespace) as the track uid
				data, _ := ioutil.ReadAll(r.data)
				return &uploadTrackResponse{uid: string(bytes.TrimSpace(data))}, nil
			},
		},
		1: {
			body: []byte{},
			fn: func(r *uploadTrackRequest) (*uploadTrackResponse, error) {
				return nil, errors.New("Upload failed")
			},
		},
	}

	for _, want := range tests {
		service := &service{
			upload: &fakeUploader{fn: want.fn},
		}

		got, err := service.createTrack(&createTrackRequest{
			body:     bytes.NewReader(body),
			boundary: "------------------------6808b4f61ea0e5a2",
		})

		if len(want.body) != 0 && !bytes.Equal(want.body, got.body.Bytes()) {
			t.Errorf("Expected body to be %v, got %v", string(want.body), got.body.String())
		}

		if len(want.body) == 0 && err == nil {
			t.Error("Expected failed upload error")
		}
	}
}
