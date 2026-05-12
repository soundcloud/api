package main

import (
	"fmt"
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
	uid             string
	userPolicyErr   error
	transcodingsErr error
}

func (f fakeTrackCoordinatorClient) createUserPolicy(filename string, fileSize int64, session *EnrichedSessionResponse) (string, error) {
	return f.uid, f.userPolicyErr
}

func (f fakeTrackCoordinatorClient) triggerTranscodings(uid string) error {
	return f.transcodingsErr
}

func TestUploaderSuccess(t *testing.T) {
	tests := []struct {
		filename, uid, expectedContentDisposition string
	}{
		{"the_track.mp3", "uid1", `attachment; filename=the_track.mp3`},
		{"the_tråck.mp3", "uid2", `attachment; filename*=utf-8''the_tr%C3%A5ck.mp3`}, // Non-ASCII characters are encoded inline with RFC 2231
	}

	for _, test := range tests {
		t.Run(test.filename, func(t *testing.T) {
			var got *s3manager.UploadInput
			up := uploader{
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
			if want, got := test.expectedContentDisposition, *got.ContentDisposition; want != got {
				t.Errorf("wrong Content-Disposition: want `%v`, got `%v`", want, got)
			}
		})
	}
}

func TestUploaderFailure(t *testing.T) {
	tests := []struct {
		description     string
		userPolicyErr   error
		uploadErr       error
		transcodingsErr error
		expectedErr     string
	}{
		{description: "should propagate errors generating user policy", userPolicyErr: fmt.Errorf("not permitted to perform upload"), expectedErr: "not permitted to perform upload"},
		{description: "should propagate errors uploading to s3", uploadErr: fmt.Errorf("s3 connection issue"), expectedErr: "s3 connection issue"},
		{description: "should propagate errors triggering transcodings", transcodingsErr: fmt.Errorf("500 - could not reach service"), expectedErr: "500 - could not reach service"},
	}

	for _, test := range tests {
		t.Run(test.description, func(t *testing.T) {
			up := uploader{
				trackCoordinator: fakeTrackCoordinatorClient{uid: "someUid", userPolicyErr: test.userPolicyErr, transcodingsErr: test.transcodingsErr},
				s3Bucket:         "bucket",
				s3Uploader: fakeS3{
					uploadFn: func(in *s3manager.UploadInput) (*s3manager.UploadOutput, error) {
						return &s3manager.UploadOutput{}, test.uploadErr
					},
				},
			}
			_, err := up.uploadTrack(&uploadTrackRequest{
				data:     strings.NewReader("data"),
				filename: "test.mp3",
			})
			if err == nil {
				t.Errorf("expected error but none was present")
			}
			if want, got := test.expectedErr, err.Error(); want != got {
				t.Errorf("wrong error provided: want `%v`, got `%v`", want, got)
			}
		})
	}
}
