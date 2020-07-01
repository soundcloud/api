package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
)

const (
	jsonContentType  = "application/json"
	clientSystemName = "public-api-strangler-assets"
)

type moshimoshiClientAPI interface {
	createTrackUID() (string, error)
}

type moshimoshiClient struct {
	client *http.Client
	host   string
}

func (u *moshimoshiClient) createTrackUID() (string, error) {
	url := fmt.Sprintf("http://%s/track_uids?access_token=%s", u.host, clientSystemName)

	req, err := u.client.Post(url, jsonContentType, nil)
	if err != nil {
		return "", err
	}
	defer req.Body.Close()

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

type mediaServiceClientAPI interface {
	createTranscoding(string) error
}

type mediaServiceClient struct {
	client *http.Client
	host   string
}

type transcodingRequest struct {
	UID      string `json:"uid"`
	Key      string `json:"key"`
	Priority string `json:"priority"`
}

func transcodingRequestPayload(uid string) ([]byte, error) {
	payload := transcodingRequest{
		UID:      uid,
		Key:      uid,
		Priority: "manual",
	}
	return json.Marshal(payload)
}

func (u *mediaServiceClient) createTranscoding(uid string) error {
	url := fmt.Sprintf("http://%s/transcode", u.host)

	bs, err := transcodingRequestPayload(uid)
	if err != nil {
		return err
	}

	req, err := http.NewRequest("POST", url, bytes.NewBuffer(bs))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", jsonContentType)
	req.Header.Set("Sc-System", clientSystemName)

	resp, err := u.client.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusAccepted {
		return fmt.Errorf("Failed to trigger transcoding UID: %d", resp.StatusCode)
	}

	return nil
}
