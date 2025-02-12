package main

import (
	"bytes"
	"errors"
	"io"
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

func (f fakeUploader) uploadTrack(r *uploadTrackRequest) (*uploadTrackResponse, error) {
	return f.fn(r)
}

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
			return httptest.NewRequest(http.MethodPost, "/", bytes.NewReader(body))
		}(),
	}

	res, err := service.createTrack(req)
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	bs, _ := io.ReadAll(res.request.Body)
	if !bytes.Equal(body, bs) {
		t.Errorf("Expected request to be unmodified %s, %s", body, bs)
	}
}

func TestUploadWithFilename(t *testing.T) {
	body := []byte(
		"--------------------------6808b4f61ea0e5a2" +
			crlf + `Content-Disposition: form-data; name="track[asset_data]"; filename="my_track.wav"` +
			crlf + "Content-Type: application/octet-stream" +
			crlf + "" +
			crlf + "12345" +
			crlf + "" +
			crlf + "--------------------------6808b4f61ea0e5a2--" +
			crlf)

	req := &createTrackRequest{
		boundary: "------------------------6808b4f61ea0e5a2",
		request: func() *http.Request {
			r := httptest.NewRequest(http.MethodPost, "/", bytes.NewReader(body))
			return r
		}(),
	}

	var got *uploadTrackRequest
	service := &service{
		upload: &fakeUploader{fn: func(r *uploadTrackRequest) (*uploadTrackResponse, error) {
			got = r
			return &uploadTrackResponse{}, nil
		}},
	}
	if _, err := service.createTrack(req); err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}
	if want, got := "my_track.wav", got.filename; want != got {
		t.Fatalf("wrong filename: want %v, got %v", want, got)
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

	bs, _ := io.ReadAll(res.request.Body)
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
				return httptest.NewRequest(http.MethodPost, "/", bytes.NewReader(tt.body))
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

func TestUnsupportedMultipartTrackFields(t *testing.T) {
	tests := [...]struct {
		body []byte
	}{
		0: {
			body: []byte(
				"--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[uid]\"" +
					crlf + "" +
					crlf + "user-generated-upload-id" +
					crlf + "--------------------------becf7c3b48144d16--" +
					crlf),
		},
		1: {
			body: []byte(
				"--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Track" +
					crlf + "--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[original_filename]\"" +
					crlf + "" +
					crlf + "my_track.wav" +
					crlf + "--------------------------becf7c3b48144d16--" +
					crlf),
		},
		2: {
			body: []byte(
				"--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[replacing_uid]\"" +
					crlf + "" +
					crlf + "user-generated-upload-id" +
					crlf + "--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[asset_data]\"; filename=\"my_track.wav\"" +
					crlf + "Content-Type: application/octet-stream" +
					crlf + "" +
					crlf + "12345" +
					crlf + "" +
					crlf + "--------------------------becf7c3b48144d16--" +
					crlf),
		},
		3: {
			body: []byte(
				"--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Track" +
					crlf + "--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[replacing_original_filename]\"" +
					crlf + "" +
					crlf + "my_track.wav" +
					crlf + "--------------------------becf7c3b48144d16--" +
					crlf),
		},
		4: {
			body: []byte(
				"--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Track" +
					crlf + "--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[uid]\"" +
					crlf + "" +
					crlf + "user-generated-upload-id" +
					crlf + "--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[original_filename]\"" +
					crlf + "" +
					crlf + "my_track.wav" +
					crlf + "--------------------------becf7c3b48144d16--" +
					crlf),
		},
	}

	for _, tt := range tests {
		service := &service{}

		// TODO is there a generic way to do this rather than repeating the tests twice?
		_, err := service.createTrack(&createTrackRequest{
			boundary: "------------------------becf7c3b48144d16",
			request: func() *http.Request {
				return httptest.NewRequest(http.MethodPost, "/", bytes.NewReader(tt.body))
			}(),
		})

		if err == nil {
			t.Error("Expected to fail when invalid form names present")
		}

		if _, ok := err.(clientError); !ok {
			t.Errorf("Expected clientError, got: %s", err)
		}

		_, gErr := service.generic(&genericRequest{
			boundary: "------------------------becf7c3b48144d16",
			request: func() *http.Request {
				return httptest.NewRequest(http.MethodPost, "/", bytes.NewReader(tt.body))
			}(),
		})

		if gErr == nil {
			t.Error("Expected to fail when invalid form names present")
		}

		if _, ok := gErr.(clientError); !ok {
			t.Errorf("Expected clientError, got: %s", gErr)
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
				return httptest.NewRequest(http.MethodPost, "/", bytes.NewReader(tt.body))
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

		if got, _ := io.ReadAll(res.request.Body); !bytes.Equal(want.body, got) {
			t.Errorf("Expected body to be %s, got %s", want.body, got)
		}
	}
}

func TestStoreTrackAssetData(t *testing.T) {
	uploadFailedErr := errors.New("Upload failed")

	tests := [...]struct {
		req  []byte
		resp []byte
		fn   func(*uploadTrackRequest) (*uploadTrackResponse, error)
		err  error
	}{
		0: {
			req: []byte(
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
					crlf),
			resp: []byte(
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
				data, _ := io.ReadAll(r.data)

				return &uploadTrackResponse{
					uid: string(bytes.TrimSpace(data)),
				}, nil
			},
			err: nil,
		},
		1: {
			req: []byte(
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
					crlf),
			resp: []byte{},
			fn: func(r *uploadTrackRequest) (*uploadTrackResponse, error) {
				return nil, uploadFailedErr
			},
			err: uploadFailedErr,
		},
		2: {
			req: []byte(
				"--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[asset_data]\"; filename=\"my_track.wav\"" +
					crlf + "Content-Type: application/octet-stream" +
					crlf + "" +
					crlf + "12345" +
					crlf + "" +
					crlf + "--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[uid]\"" +
					crlf + "" +
					crlf + "user-specified-upload-id" +
					crlf + "" +
					crlf + "--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Track" +
					crlf + "--------------------------6808b4f61ea0e5a2--" +
					crlf),
			resp: []byte{},
			fn: func(r *uploadTrackRequest) (*uploadTrackResponse, error) {
				// Echo the track data (without whitespace) as the track uid
				data, _ := io.ReadAll(r.data)

				return &uploadTrackResponse{
					uid: string(bytes.TrimSpace(data)),
				}, nil
			},
			err: clientError{},
		},
	}

	for _, test := range tests {
		service := &service{
			upload: &fakeUploader{fn: test.fn},
		}

		res, err := service.createTrack(&createTrackRequest{
			boundary: "------------------------6808b4f61ea0e5a2",
			request: func() *http.Request {
				return httptest.NewRequest(http.MethodPost, "/", bytes.NewReader(test.req))
			}(),
		})

		if got := err; test.err != got {
			t.Errorf("Expected error to be %s, got %s", test.err, got)
		}

		if len(test.resp) != 0 {
			if got, _ := io.ReadAll(res.request.Body); !bytes.Equal(test.resp, got) {
				t.Errorf("Expected body to be %s, got %s", test.resp, got)
			}
		}
	}
}
