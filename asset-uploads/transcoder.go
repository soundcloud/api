package main

import (
	"log"
)

type transcoder interface {
	transcode(string) (bool, error)
}

type moshimoshiTranscoder struct {
	moshimoshiClient *moshimoshiClient
}

func newMoshimoshiTranscoder(moshimoshi *moshimoshiClient) *moshimoshiTranscoder {
	return &moshimoshiTranscoder{
		moshimoshiClient: moshimoshi,
	}
}

func (m *moshimoshiTranscoder) transcode(uid string) (bool, error) {
	log.Printf("*** Transcode uid: %s", uid)

	return m.moshimoshiClient.createTranscoding(uid)
}
