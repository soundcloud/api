package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
)

const (
	jsonContentType  = "application/json"
	clientSystemName = "api-public-assets"
)

type mediaServiceClientAPI interface {
	createTranscoding(key string, filename string) (string, error)
}

type mediaServiceClient struct {
	client *http.Client
	host   string
}

type transcodingRequest struct {
	Key      	string `json:"key"`
	Priority 	string `json:"priority"`
	Filename	string `json:"filename"`
}

type transcodingResponse struct {
	UID      string `json:"uid"`
}

func transcodingRequestPayload(key, filename string) ([]byte, error) {
	payload := transcodingRequest{
		Key:      	key,
		Priority: 	"manual",
		Filename:	filename,
	}
	return json.Marshal(payload)
}

func (u *mediaServiceClient) createTranscoding(key, filename string) (string, error) {
	url := fmt.Sprintf("http://%s/transcode", u.host)

	bs, err := transcodingRequestPayload(key, filename)
	if err != nil {
		return "", err
	}

	req, err := http.NewRequest("POST", url, bytes.NewBuffer(bs))
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

	if resp.StatusCode != http.StatusAccepted {
		return "", fmt.Errorf("Failed to trigger transcoding key: %s, status %d", key, resp.StatusCode)
	}

	res := &transcodingResponse{}

	if err := json.NewDecoder(resp.Body).Decode(res); err != nil {
		return "", err
	}

	return res.UID, nil
}
