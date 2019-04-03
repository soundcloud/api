package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
)

const (
	jsonContentType       = "application/json"
	moshimoshiAccessToken = "public-api-strangler-assets"
)

type moshimoshiClientAPI interface {
	createTrackUID() (string, error)
	createTranscoding(string) error
}

type moshimoshiClient struct {
	host   string
	client *http.Client
}

func (u *moshimoshiClient) createTrackUID() (string, error) {
	url := fmt.Sprintf("http://%s/track_uids?access_token=%s", u.host, moshimoshiAccessToken)

	req, err := u.client.Post(url, jsonContentType, nil)
	if err != nil {
		return "", err
	}

	if req.StatusCode != http.StatusCreated {
		return "", fmt.Errorf("Failed to create track UID: %d", req.StatusCode)
	}

	res := &struct {
		UID string `json:"uid"`
	}{}

	if err := json.NewDecoder(req.Body).Decode(res); err != nil {
		return "", err
	}

	return res.UID, nil
}

func (u *moshimoshiClient) createTranscoding(uid string) error {
	url := fmt.Sprintf("http://%s/transcodings?access_token=%s", u.host, moshimoshiAccessToken)

	bs, err := json.Marshal(struct {
		Transcoding struct {
			UID string `json:"uid"`
		} `json:"transcoding"`
	}{
		struct {
			UID string `json:"uid"`
		}{uid},
	})
	if err != nil {
		return err
	}

	req, err := u.client.Post(url, jsonContentType, bytes.NewBuffer(bs))
	if err != nil {
		return err
	}

	if req.StatusCode != http.StatusOK {
		return fmt.Errorf("Failed to trigger transcoding UID: %d", req.StatusCode)
	}

	return nil
}

// Ensure that moshimoshiClient implements moshimoshiClientAPI.
var _ moshimoshiClientAPI = (*moshimoshiClient)(nil)
