package main

import (
	"fmt"
	"io"
	"os"
)

type storageWriter interface {
	Store(string, io.Reader) error
}

type fileSystemWriter struct {
	baseDir string
}

func (fs *fileSystemWriter) Store(key string, val io.Reader) error {
	path := fmt.Sprintf("/%s/%s", fs.baseDir, key)

	f, err := os.Create(path)
	if err != nil {
		return err
	}

	_, err = io.Copy(f, val)
	if err != nil {
		return err
	}

	return nil
}

type s3Writer struct{}
