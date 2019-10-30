package main

import (
	"bytes"
	"errors"
	"io/ioutil"
	"net/http"
	"net/http/httptest"
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

	req := &createTrackRequest{
		boundary: "------------------------7570ceb7c872df7a",
		request: func() *http.Request {
			r := httptest.NewRequest(http.MethodPost, "/", bytes.NewReader(body))
			r.Header.Set("X-Track-Asset-Uploads", "true")
			return r
		}(),
	}

	res, err := service.createTrack(req)
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	bs, _ := ioutil.ReadAll(res.request.Body)
	if !bytes.Equal(body, bs) {
		t.Errorf("Expected request to be unmodified %s, %s", body, bs)
	}
}

func TestUnescapedFilename(t *testing.T) {
	body := []byte(
		"--------------------------6808b4f61ea0e5a2" +
			crlf + `Content-Disposition: form-data; name="track[asset_data]"; filename="my "track.wav""` +
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

	var uploaded bool
	service := &service{
		upload: &fakeUploader{fn: func(*uploadTrackRequest) (*uploadTrackResponse, error) {
			uploaded = true
			return &uploadTrackResponse{}, nil
		}},
	}

	req := &createTrackRequest{
		boundary: "------------------------6808b4f61ea0e5a2",
		request: func() *http.Request {
			r := httptest.NewRequest(http.MethodPost, "/", bytes.NewReader(body))
			return r
		}(),
	}

	res, err := service.createTrack(req)
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	if !uploaded {
		t.Errorf("Expected an upload, got none")
	}

	bs, _ := ioutil.ReadAll(res.request.Body)
	if bytes.Contains(bs, []byte("track[asset_data]")) {
		t.Errorf("Expected request request to be modified %s", bs)
	}
}

func TestInvalidMultipart(t *testing.T) {
	tests := [...]struct {
		body     []byte
		boundary string
	}{
		0: {
			[]byte("invalid multipart data"),
			"valid-boundary",
		},
		1: {
			[]byte(
				"--------------------------7570ceb7c872df7a" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Track" +
					crlf + "--------------------------7570ceb7c872df7a--" +
					crlf),
			"", // invalid empty boundary
		},
	}

	for _, tt := range tests {
		service := &service{}

		_, err := service.createTrack(&createTrackRequest{
			boundary: tt.boundary,
			request: func() *http.Request {
				r := httptest.NewRequest(http.MethodPost, "/", bytes.NewReader(tt.body))
				r.Header.Set("X-Track-Asset-Uploads", "true")
				return r
			}(),
		})

		if err == nil {
			t.Error("Expected to fail parsing malformed multipart data")
		}
		if _, ok := err.(clientError); !ok {
			t.Errorf("Expected clientError, got: %s", err)
		}
	}
}

func TestExtractAuthToken(t *testing.T) {
	const (
		fiveBytes   = "012345"
		twentyBytes = "01234567890123456789"
		largeToken  = twentyBytes + twentyBytes + twentyBytes + fiveBytes
	)

	tests := [...]struct {
		auth string
		body []byte
	}{
		0: {
			auth: "",
			body: []byte(
				"--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Tack" +
					crlf + "--------------------------becf7c3b48144d16--" +
					crlf),
		},
		1: {
			auth: "OAuth some-token",
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
			auth: "OAuth " + largeToken[:64],
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

		res, err := service.createTrack(&createTrackRequest{
			boundary: "------------------------becf7c3b48144d16",
			request: func() *http.Request {
				r := httptest.NewRequest(http.MethodPost, "/", bytes.NewReader(tt.body))
				r.Header.Set("X-Track-Asset-Uploads", "true")
				return r
			}(),
		})
		if err != nil {
			t.Fatalf("Expected no error, got: %v", err)
		}

		want := struct {
			auth string
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

		if got := res.request.Header.Get("Authorization"); want.auth != got {
			t.Errorf("Expected Authorization header to be %s, got %s", want.auth, got)
		}

		if got, _ := ioutil.ReadAll(res.request.Body); !bytes.Equal(want.body, got) {
			t.Errorf("Expected body to be %s, got %s", want.body, got)
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
			crlf + "Content-Disposition: form-data; name=\"track[artwork_data]\"; filename=\"my_track.jpg\"" +
			crlf + "Content-Type: application/octet-stream" +
			crlf + "" +
			crlf + "<JPEG data; won't be modified>" +
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
					crlf + "Content-Disposition: form-data; name=\"track[artwork_data]\"; filename=\"my_track.jpg\"" +
					crlf + "Content-Type: application/octet-stream" +
					crlf + "" +
					crlf + "<JPEG data; won't be modified>" +
					crlf + "" +
					crlf + "--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Track" +
					crlf + "--------------------------6808b4f61ea0e5a2--" +
					crlf),
			fn: func(r *uploadTrackRequest) (*uploadTrackResponse, error) {
				// Echo the track data (without whitespace) as the track uid
				data, _ := ioutil.ReadAll(r.data)

				return &uploadTrackResponse{
					uid: string(bytes.TrimSpace(data)),
				}, nil
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

		res, err := service.createTrack(&createTrackRequest{
			boundary: "------------------------6808b4f61ea0e5a2",
			request: func() *http.Request {
				r := httptest.NewRequest(http.MethodPost, "/", bytes.NewReader(body))
				r.Header.Set("X-Track-Asset-Uploads", "true")
				return r
			}(),
		})

		if len(want.body) == 0 {
			// The test didn't expect a body, the request should fail.
			if err == nil {
				t.Error("Expected failed upload error")
			}
			continue
		}

		if got, _ := ioutil.ReadAll(res.request.Body); !bytes.Equal(want.body, got) {
			t.Errorf("Expected body to be %s, got %s", want.body, got)
		}
	}
}
