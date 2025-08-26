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

type fakeTrackCoordinatorClient struct {
	uid string
}

func (f fakeTrackCoordinatorClient) createUserPolicy(filename string, fileSize int64, session *EnrichedSessionResponse) (string, error) {
	return f.uid, nil
}

func TestUploader(t *testing.T) {
	tests := []struct {
		filename, uid string
	}{
		{"the_track.mp3", "uid1"},
		{"the_tråck.mp3", "uid2"},
	}

	for _, test := range tests {
		t.Run(test.filename, func(t *testing.T) {
			var got *s3manager.UploadInput
			up := uploader{
				mediaService:     fakeMediaServiceClient{},
				trackCoordinator: fakeTrackCoordinatorClient{uid: test.uid},
				s3Bucket:         "bucket",
				s3Uploader: fakeS3{
					uploadFn: func(in *s3manager.UploadInput) (*s3manager.UploadOutput, error) {
						got = in
						return &s3manager.UploadOutput{}, nil
					},
				},
			}
			response, err := up.uploadTrack(&uploadTrackRequest{
				data:     strings.NewReader("data"),
				filename: test.filename,
			})
			if err != nil {
				t.Errorf("unexpected error: %v", err)
			}
			if want, got := test.uid, *got.Key; want != got {
				t.Errorf("wrong S3 key: want `%v`, got `%v`", want, got)
			}
			if want, got := test.uid, response.uid; want != got {
				t.Errorf("wrong uid: want `%v`, got `%v`", want, got)
			}
		})
	}
}
