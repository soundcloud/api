package main

import (
	"strings"
	"testing"

	"github.com/aws/aws-sdk-go/service/s3/s3manager"
	"github.com/aws/aws-sdk-go/service/s3/s3manager/s3manageriface"
)

type fakeS3 struct {
	s3manageriface.UploaderAPI
	uploadFn func(*s3manager.UploadInput) (*s3manager.UploadOutput, error)
}

func (s3 fakeS3) Upload(in *s3manager.UploadInput, opts ...func(*s3manager.Uploader)) (*s3manager.UploadOutput, error) {
	return s3.uploadFn(in)
}

func TestUploaderContentDisposition(t *testing.T) {
	tests := []struct {
		filename, contentDisposition string
	}{
		{"the_track.mp3", `attachment;filename="the_track.mp3"; filename*=utf-8''the_track.mp3`},
		{"the_tråck.mp3", `attachment;filename="the_tr%C3%A5ck.mp3"; filename*=utf-8''the_tr%C3%A5ck.mp3`},
		{"https://example.com/the_track.mp3", `attachment;filename="the_track.mp3"; filename*=utf-8''the_track.mp3`},
	}

	for _, test := range tests {
		t.Run(test.filename, func(t *testing.T) {
			var got *s3manager.UploadInput
			up := uploader{
				moshimoshi: fakeMoshimoshiClient{uid: "uid"},
				s3Bucket:   "bucket",
				s3Uploader: fakeS3{
					uploadFn: func(in *s3manager.UploadInput) (*s3manager.UploadOutput, error) {
						got = in
						return &s3manager.UploadOutput{}, nil
					},
				},
			}
			_, err := up.uploadTrack(&uploadTrackRequest{
				data:     strings.NewReader("data"),
				filename: test.filename,
			})
			if err != nil {
				t.Errorf("unexpected error: %v", err)
			}
			if want, got := test.contentDisposition, *got.ContentDisposition; want != got {
				t.Errorf("wrong Content-Disposition: want `%v`, got `%v`", want, got)
			}
		})
	}
}
