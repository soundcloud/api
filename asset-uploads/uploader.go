package main

import (
	"crypto/md5"
	"fmt"
	"io"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/s3/s3manager"
	"github.com/aws/aws-sdk-go/service/s3/s3manager/s3manageriface"
	"github.com/google/uuid"
)

type uploaderAPI interface {
	uploadTrack(*uploadTrackRequest) (*uploadTrackResponse, error)
}

type uploader struct {
	mediaService            mediaServiceClientAPI
	s3Bucket                string
	s3Uploader              s3manageriface.UploaderAPI
	s3KeyGenerator			func() string
}

type uploadTrackRequest struct {
	data     io.Reader
	filename string
}

type uploadTrackResponse struct {
	location string
	md5      string
	uid      string
}

func (u uploader) uploadTrack(req *uploadTrackRequest) (*uploadTrackResponse, error) {
	key := u.s3KeyGenerator()

	md5 := md5.New()
	tee := io.TeeReader(req.data, md5)
	out, err := u.s3Uploader.Upload(&s3manager.UploadInput{
		Bucket:             aws.String(u.s3Bucket),
		Key:                aws.String(key),
		Body:               tee,
	})
	if err != nil {
		return nil, err
	}

	uid, err := u.mediaService.createTranscoding(key, req.filename)
	if err != nil {
		return nil, err
	}

	return &uploadTrackResponse{
		location: out.Location,
		md5:      fmt.Sprintf("%x", md5.Sum(nil)),
		uid:      uid,
	}, nil
}

func generateS3Key() string {
	return fmt.Sprintf("public-api/%s", uuid.New().String())
}

// Ensure that uploader implements uploaderAPI.
var _ uploaderAPI = (*uploader)(nil)
