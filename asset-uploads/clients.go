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

type mediaServiceClientAPI interface {
	createTrackUID() (string, error)
	createTranscoding(string) error
}

type mediaServiceClient struct {
	client *http.Client
	host   string
}

func (u *mediaServiceClient) createTrackUID() (string, error) {
	url := fmt.Sprintf("http://%s/uid", u.host)

	req, err := http.NewRequest("POST", url, nil)
	if err != nil {
		return "", err
	}

	req.Header.Set("Content-Type", jsonContentType)
	req.Header.Set("Sc-System", clientSystemName)

	resp, err := u.client.Do(req)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusCreated {
		return "", fmt.Errorf("Failed to create track upload UID: %d", resp.StatusCode)
	}

	res := &struct {
		UID string `json:"uid"`
	}{}

	if err := json.NewDecoder(resp.Body).Decode(res); err != nil {
		return "", err
	}

	return res.UID, nil
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
