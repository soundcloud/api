package main

import (
	"crypto/md5"
	"fmt"
	"io"
	"log"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/s3/s3manager"
	"github.com/aws/aws-sdk-go/service/s3/s3manager/s3manageriface"
)

type uploaderAPI interface {
	uploadTrack(*uploadTrackRequest) (*uploadTrackResponse, error)
}

type uploader struct {
	trackCoordinator trackCoordinatorClientAPI
	s3Bucket         string
	s3Uploader       s3manageriface.UploaderAPI
}

type uploadTrackRequest struct {
	data     io.Reader
	session  *EnrichedSessionResponse
	filename string
	fileSize int64
}

type uploadTrackResponse struct {
	location string
	md5      string
	uid      string
}

func (u uploader) uploadTrack(req *uploadTrackRequest) (*uploadTrackResponse, error) {
	uid, err := u.trackCoordinator.createUserPolicy(req.filename, req.fileSize, req.session)
	if err != nil {
		log.Printf("Failed to generate user upload policy with error: %v", err)
		return nil, err
	}

	md5 := md5.New()
	tee := io.TeeReader(req.data, md5)
	out, err := u.s3Uploader.Upload(&s3manager.UploadInput{
		Bucket: aws.String(u.s3Bucket),
		Key:    aws.String(uid),
		Body:   tee,
	})
	if err != nil {
		return nil, err
	}

	if err := u.trackCoordinator.triggerTranscodings(uid, req.filename); err != nil {
		return nil, err
	}

	return &uploadTrackResponse{
		location: out.Location,
		md5:      fmt.Sprintf("%x", md5.Sum(nil)),
		uid:      uid,
	}, nil
}

// Ensure that uploader implements uploaderAPI.
var _ uploaderAPI = (*uploader)(nil)
