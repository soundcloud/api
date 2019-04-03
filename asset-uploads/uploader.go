package main

import (
	"io"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/s3/s3manager"
	"github.com/aws/aws-sdk-go/service/s3/s3manager/s3manageriface"
)

type uploaderAPI interface {
	uploadTrack(*uploadTrackRequest) (*uploadTrackResponse, error)
}

type uploader struct {
	moshimoshi moshimoshiClientAPI
	s3Bucket   string
	s3Uploader s3manageriface.UploaderAPI
}

type uploadTrackRequest struct {
	data io.Reader
}

type uploadTrackResponse struct {
	uid string
}

func (u uploader) uploadTrack(req *uploadTrackRequest) (*uploadTrackResponse, error) {
	uid, err := u.moshimoshi.createTrackUID()
	if err != nil {
		return nil, err
	}

	input := &s3manager.UploadInput{
		Bucket: aws.String(u.s3Bucket),
		Key:    aws.String(uid),
		Body:   req.data,
	}
	if _, err := u.s3Uploader.Upload(input); err != nil {
		return nil, err
	}

	if err := u.moshimoshi.createTranscoding(uid); err != nil {
		return nil, err
	}

	return &uploadTrackResponse{uid: uid}, nil
}

// Ensure that uploader implements uploaderAPI.
var _ uploaderAPI = (*uploader)(nil)
