package main

import (
	"io"
	"io/ioutil"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/credentials"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3/s3manager"
)

type storage interface {
	store(io.Reader) (string, error)
}

type diskStorage struct {
	baseDir, filePrefix string
}

func (d *diskStorage) store(r io.Reader) (string, error) {
	f, err := ioutil.TempFile(d.baseDir, d.filePrefix)
	if err != nil {
		return "", nil
	}
	defer f.Close()

	_, err = io.Copy(f, r)
	if err != nil {
		return "", err
	}

	return f.Name(), nil
}

type s3Storage struct {
	moshi    *moshimoshiClient
	uploader *s3manager.Uploader

	bucket string
}

func newS3Storage(key, secret, region, bucket string, moshi *moshimoshiClient) (*s3Storage, error) {
	const (
		// The session token is optional.
		emptySessionToken = ""
	)

	creds := credentials.NewStaticCredentials(key, secret, emptySessionToken)

	conf := aws.NewConfig().WithCredentials(creds).WithRegion(region)
	sess := session.Must(session.NewSession(conf))

	storage := &s3Storage{
		bucket:   bucket,
		moshi:    moshi,
		uploader: s3manager.NewUploader(sess),
	}

	return storage, nil
}

func (s3 *s3Storage) store(r io.Reader) (string, error) {
	uid, err := s3.moshi.createTrackUID()
	if err != nil {
		return "", err
	}

	input := &s3manager.UploadInput{
		Bucket: aws.String(s3.bucket),
		Key:    aws.String(uid),
		Body:   r,
	}

	_, err = s3.uploader.Upload(input)
	if err != nil {
		return "", err
	}

	return uid, nil
}
