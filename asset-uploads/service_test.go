package main

import (
	"bytes"
	"errors"
	"fmt"
	"io"
	"net/http"
	"net/http/httptest"
	"reflect"
	"sort"
	"strings"
	"testing"
)

const (
	crlf = "\r\n"
)

type fakeUploader struct {
	fn func(*uploadTrackRequest) (*uploadTrackResponse, error)
}

func (f fakeUploader) uploadTrack(r *uploadTrackRequest) (*uploadTrackResponse, error) {
	return f.fn(r)
}

func TestValidMultipartWithoutFileUpload(t *testing.T) {
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
	tests := [...]struct {
		filename string
		expected string
	}{
		0: {"my_track.wav", "my_track.wav"},
		1: {`"my "track.wav""`, ""},
	}

	for _, tt := range tests {
		body := []byte(
			"--------------------------6808b4f61ea0e5a2" +
				crlf + fmt.Sprintf(`Content-Disposition: form-data; name="track[asset_data]"; filename="%s"`, tt.filename) +
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
		var uploaded bool
		service := &service{
			upload: &fakeUploader{fn: func(r *uploadTrackRequest) (*uploadTrackResponse, error) {
				uploaded = true
				got = r
				return &uploadTrackResponse{}, nil
			}},
		}

		if _, err := service.createTrack(req); err != nil {
			t.Fatalf("Expected no error, got %v", err)
		}

		if !uploaded {
			t.Errorf("Expected an upload, got none")
		}

		if want, got := tt.expected, got.filename; want != got {
			t.Fatalf("wrong filename: want %v, got %v", want, got)
		}
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
	var largeToken = strings.Repeat("0", 600) + strings.Repeat("1", 425)
	tests := [...]struct {
		incomingBody       []byte
		incomingHeaders    map[string]string
		expectedAuthHeader string
	}{
		0: {
			incomingBody: []byte(
				"--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Track" +
					crlf + "--------------------------becf7c3b48144d16--" +
					crlf),
			incomingHeaders:    map[string]string{},
			expectedAuthHeader: "",
		},
		1: {
			incomingBody: []byte(
				"--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"oauth_token\"" +
					crlf + "" +
					crlf + "some-token" +
					crlf + "--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Track" +
					crlf + "--------------------------becf7c3b48144d16--" +
					crlf),
			incomingHeaders:    map[string]string{"client_id": "oDuGHdoKZgaZtpROWLLAJpikJUoEV2dr"},
			expectedAuthHeader: "OAuth some-token",
		},
		2: {
			incomingBody: []byte(
				"--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Track" +
					crlf + "--------------------------becf7c3b48144d16--" +
					crlf),
			incomingHeaders:    map[string]string{"Authorization": "OAuth some-token"},
			expectedAuthHeader: "OAuth some-token",
		},
		3: {
			incomingBody: []byte(
				"--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"oauth_token\"" +
					crlf + "" +
					crlf + largeToken +
					crlf + "--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Track" +
					crlf + "--------------------------becf7c3b48144d16--" +
					crlf),
			incomingHeaders:    map[string]string{"some_header": "some_header_value"},
			expectedAuthHeader: "OAuth " + largeToken[:1024],
		},
		4: {
			incomingBody: []byte(
				"--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"oauth_token\"" +
					crlf + "" +
					crlf + "2-305162-1405850550-8ZhPonqpmBHFG" +
					crlf + "--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Track" +
					crlf + "--------------------------becf7c3b48144d16--" +
					crlf),
			incomingHeaders:    map[string]string{"Authorization": "OAuth overridden-token"},
			expectedAuthHeader: "OAuth " + "2-305162-1405850550-8ZhPonqpmBHFG",
		},
	}

	for _, tt := range tests {
		service := &service{}

		res, err := service.createTrack(&createTrackRequest{
			boundary: "------------------------becf7c3b48144d16",
			request: func() *http.Request {
				req := httptest.NewRequest(http.MethodPost, "/", bytes.NewReader(tt.incomingBody))
				for k, v := range tt.incomingHeaders {
					req.Header.Set(k, v)
				}
				return req
			}(),
		})
		if err != nil {
			t.Fatalf("Expected no error, got: %v", err)
		}

		want := struct {
			auth string
			body []byte
		}{
			auth: tt.expectedAuthHeader,
			body: []byte(
				"--------------------------becf7c3b48144d16" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Track" +
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

	var largeToken = "eyJraWQiOiIwZTY0ODM1NC1hYjQ1LTQ3ZDYtYjQ2YS05ZGZkZmEyNzc2ZDkiLCJ0eXAiOiJhdCtKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJzb3VuZGNsb3VkOnVzZXJzOjk4NzY4ODcwIiwiYXVkIjoiaHR0cHM6Ly9zb3VuZGNsb3VkLmNvbSIsInNjb3BlIjoiKiIsImlzcyI6Imh0dHBzOi8vc2VjdXJlLnNvdW5kY2xvdWQuY29tIiwiY2FpIjoiMTI0IiwiZXhwIjoxNzQ0ODgxMTIzLCJpYXQiOjE3NDQyNzYzMjMsImp0aSI6IjdmYjNiYzUwLTg1YjctNDM3MS04OWY5LTU0Nzg2NTMwMGYyYiIsImNsaWVudF9pZCI6Im9EdUdIZG9LWmdhWnRwUk9XTExBSnBpa0pVb0VWMmRyIiwic2lkIjoiMDFKUkZGR1pLUUtSMkZSNk1KUjg0N0NNWFYifQ.OCCoaIO2dsUuAhxkpLf7jnuelCbKnLOZ9U0ar0tVcCp12jjpXandOhmJvLRRdzwDsjRuIJ3mu_BvIeAikGfZUgmSDRDlLJwc2Tzauhb3UMjfg83H5MroHowfaHXncWuw864ltSqXK0sCmnTLkj0vDRbJ50yk5Jd3SvYTEtsEB3HMbp-yzm2wmmU3oUkbovNMsjQ919CW9Pnknofz8TpnE60pahCIfRbUHJJQCn8NdAAY_qJjZ8txLRhksS3HvxXHvOla2yBGlMidSoAuN7NeDTHlHbPJDlhUVSVnzOVxI1D0K3t3XRrhG9PVXCufO1v1JF2z-cjgNbYPN1jvCxGnRQ"

	var pic = "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b24+CiAgICAgICAgIDxleGlmOlBpeGVsWERpbWVuc2lvbj43ODQ8L2V4aWY6UGl4ZWxYRGltZW5zaW9uPgogICAgICAgICA8ZXhpZjpVc2VyQ29tbWVudD5TY3JlZW5zaG90PC9leGlmOlVzZXJDb21tZW50PgogICAgICA8L3JkZjpEZXNjcmlwdGlvbj4KICAgPC9yZGY6UkRGPgo8L3g6eG1wbWV0YT4KxlYMAwAAQABJREFUeAHtnQd8HMX1x596l2zJXe64UAw2HRyKaSH03iG0EHpCTQgkxBBIQgghBkILNYHQOxgbU4yNwRhjTMfGvcjqvZ+k+7/fnOe0d9rTreQTkvn/xh9r92ZnZ2e+Mzvz3syb2Ti/OqEjARIgARIgARIgARIgARIgAQ8E4j2EYRASIAESIAESIAESIAESIAESMASoQLAikAAJkAAJkAAJkAAJkAAJeCZABcIzKgYkARIgARIgARIgARIgARKgAsE6QAIkQAIkQAIkQAIkQAIk4JkAFQjPqBiQBEiABEiABEiABEiABEiACgTrAAmQAAmQAAmQAAmQAAmQgGcCVCA8o2JAEiABEiABEiABEiABEiABKhCsAyRAAiRAAiRAAiRAAiRAAp4JUIHwjIoBSYAESIAESIAESIAESIAEqECwDpAACZAACZAACZAACZAACXgmQAXCMyoGJAESIAESIAESIAESIAESoALBOkACJEACJEACJEACJEACJOCZABUIz6gYkARIgARIgARIgARIgARIgAoE6wAJkAAJkAAJkAAJkAAJkIBnAlQgPKNiQBIgARIgARIgARIgARIgASoQrAMkQAIkQAIkQAIkQAIkQAKeCVCB8IyKAUmABEiABEiABEiABEiABKhAsA6QAAmQAAmQAAmQAAmQAAl4JkAFwjMqBiQBEiABEiABEiABEiABEvjRKBB+v1+WL18uJSUlLNUfMYGVK1dKQUHBjziHsc3aunXr5J133jHvRmxj7rnY1q5dK2vWrOm5B/yIYsa7sGLFih9RjmKflaamJlP/KysrYx/5Vh5jc3OzYVNRUbGV54TJ78sE/G0t0lj8tVQvf11aaov7clKZti4QSJiurgvhPQX9z3/+Ixs2bJAJEyaEhP/+++/l4IMPllNOOUXS0tJCrj300ENSXl4uY8eODfH3+gMN4b777iupqamy1157eb2t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"

	var longFilename = strings.Repeat("a", 255) + ".mp3"

	tests := [...]struct {
		incomingHeaders map[string]string
		req             []byte
		fn              func(*uploadTrackRequest) (*uploadTrackResponse, error)
		outgoingHeaders map[string]string
		resp            []byte
		err             error
	}{
		//successful upload where asset data provided before other form parts
		0: {
			incomingHeaders: map[string]string{"Authorization": "OAuth some-token"},
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
			fn: func(r *uploadTrackRequest) (*uploadTrackResponse, error) {
				// Echo the track data (without whitespace) as the track uid
				data, _ := io.ReadAll(r.data)

				return &uploadTrackResponse{
					uid:      string(bytes.TrimSpace(data)),
					location: "someLocation",
					md5:      "someMd5Hash",
				}, nil
			},
			outgoingHeaders: map[string]string{
				"Authorization":          "OAuth some-token",
				"X-Track-Asset-Location": "someLocation",
				"X-Track-Asset-Md5":      "someMd5Hash",
			},
			resp: []byte(
				"--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[artwork_data]\"; filename=\"my_track.jpg\"" +
					crlf + "Content-Type: application/octet-stream" +
					crlf + "" +
					crlf + "<JPEG data; won't be modified>" +
					crlf + "" +
					crlf + "--------------------------6808b4f61ea0e5a2" +
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
					crlf + "12345" +
					crlf + "--------------------------6808b4f61ea0e5a2--" +
					crlf),
			err: nil,
		},
		//successful upload where asset data provided after other form parts
		1: {
			incomingHeaders: map[string]string{"Authorization": "OAuth some-token"},
			req: []byte(
				"--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[artwork_data]\"; filename=\"my_track.jpg\"" +
					crlf + "Content-Type: application/octet-stream" +
					crlf + "" +
					crlf + "<JPEG data; won't be modified>" +
					crlf + "" +
					crlf + "--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Track" +
					crlf + "--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[asset_data]\"; filename=\"my_track.wav\"" +
					crlf + "Content-Type: application/octet-stream" +
					crlf + "" +
					crlf + "12345" +
					crlf + "" +
					crlf + "--------------------------6808b4f61ea0e5a2--" +
					crlf),
			fn: func(r *uploadTrackRequest) (*uploadTrackResponse, error) {
				// Echo the track data (without whitespace) as the track uid
				data, _ := io.ReadAll(r.data)

				return &uploadTrackResponse{
					uid:      string(bytes.TrimSpace(data)),
					location: "someLocation",
					md5:      "someMd5Hash",
				}, nil
			},
			outgoingHeaders: map[string]string{
				"Authorization":          "OAuth some-token",
				"X-Track-Asset-Location": "someLocation",
				"X-Track-Asset-Md5":      "someMd5Hash",
			},
			resp: []byte(
				"--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[artwork_data]\"; filename=\"my_track.jpg\"" +
					crlf + "Content-Type: application/octet-stream" +
					crlf + "" +
					crlf + "<JPEG data; won't be modified>" +
					crlf + "" +
					crlf + "--------------------------6808b4f61ea0e5a2" +
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
					crlf + "12345" +
					crlf + "--------------------------6808b4f61ea0e5a2--" +
					crlf),
			err: nil,
		},
		//successful upload where oauth token provided as form part
		2: {
			incomingHeaders: map[string]string{"Authorization": "OAuth overridden-token"},
			req: []byte(
				"--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[asset_data]\"; filename=\"my_track.wav\"" +
					crlf + "Content-Type: application/octet-stream" +
					crlf + "" +
					crlf + "12345" +
					crlf + "" +
					crlf + "--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"oauth_token\"" +
					crlf + "" +
					crlf + largeToken +
					crlf + "--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[artwork_data]\"; filename=\"my_track.jpg\"" +
					crlf + "Content-Type: application/octet-stream" +
					crlf + "" +
					crlf + pic +
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
			outgoingHeaders: map[string]string{
				"Authorization":          fmt.Sprintf("OAuth %s", largeToken),
				"X-Track-Asset-Location": "",
				"X-Track-Asset-Md5":      "",
			},
			resp: []byte(
				"--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[artwork_data]\"; filename=\"my_track.jpg\"" +
					crlf + "Content-Type: application/octet-stream" +
					crlf + "" +
					crlf + pic +
					crlf + "" +
					crlf + "--------------------------6808b4f61ea0e5a2" +
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
					crlf + "12345" +
					crlf + "--------------------------6808b4f61ea0e5a2--" +
					crlf),
			err: nil,
		},
		//successful upload where asset data provided without form name because of escaping error
		3: {
			incomingHeaders: map[string]string{"Authorization": "OAuth some-token"},
			req: []byte(
				"--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; filename=\"my_track\"  .wav;name=\"track[asset_data]\"" +
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
			fn: func(r *uploadTrackRequest) (*uploadTrackResponse, error) {
				// Echo the track data (without whitespace) as the track uid
				data, _ := io.ReadAll(r.data)

				return &uploadTrackResponse{
					uid:      string(bytes.TrimSpace(data)),
					location: "someLocation",
					md5:      "someMd5Hash",
				}, nil
			},
			outgoingHeaders: map[string]string{
				"Authorization":          "OAuth some-token",
				"X-Track-Asset-Location": "someLocation",
				"X-Track-Asset-Md5":      "someMd5Hash",
			},
			resp: []byte(
				"--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[artwork_data]\"; filename=\"my_track.jpg\"" +
					crlf + "Content-Type: application/octet-stream" +
					crlf + "" +
					crlf + "<JPEG data; won't be modified>" +
					crlf + "" +
					crlf + "--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[title]\"" +
					crlf + "" +
					crlf + "My Track" +
					crlf + "--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[original_filename]\"" +
					crlf + "" +
					crlf + "" +
					crlf + "--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[uid]\"" +
					crlf + "" +
					crlf + "12345" +
					crlf + "--------------------------6808b4f61ea0e5a2--" +
					crlf),
			err: nil,
		},
		//failed upload - filename too long
		4: {
			incomingHeaders: map[string]string{"Authorization": "OAuth some-token"},
			req: []byte(
				"--------------------------6808b4f61ea0e5a2" +
					crlf + "Content-Disposition: form-data; name=\"track[asset_data]\"; filename=\"" + longFilename + "\"" +
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
			fn: func(r *uploadTrackRequest) (*uploadTrackResponse, error) {
				return nil, nil
			},
			resp: []byte{},
			err:  fileNameValidationError{},
		},
		//failed upload - failure when uploading audio
		5: {
			incomingHeaders: map[string]string{"Authorization": "OAuth some-token"},
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
		//failed upload - forbidden track fields present as form parts
		6: {
			incomingHeaders: map[string]string{"Authorization": "OAuth some-token"},
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
				req := httptest.NewRequest(http.MethodPost, "/", bytes.NewReader(test.req))
				for k, v := range test.incomingHeaders {
					req.Header.Set(k, v)
				}
				return req
			}(),
		})

		if got := err; test.err != got {
			t.Errorf("Expected error to be %s, got %s", test.err, got)
		}

		if len(test.resp) != 0 {
			if got, _ := io.ReadAll(res.request.Body); !bytes.Equal(test.resp, got) {
				t.Errorf("Expected body to be %s, got %s", test.resp, got)
			}

			// Check if headers are equal
			gotHeaders := make(map[string]string, len(res.request.Header))
			headerKeys := make([]string, 0, len(res.request.Header))
			for key := range res.request.Header {
				headerKeys = append(headerKeys, key)
			}

			sort.Strings(headerKeys)
			for _, k := range headerKeys {
				gotHeaders[k] = res.request.Header.Get(k)
			}

			if !reflect.DeepEqual(test.outgoingHeaders, gotHeaders) {
				t.Errorf("Unexpected output headers, wanted: %v\n, got: %v\n", test.outgoingHeaders, gotHeaders)
			}
		}

	}
}
