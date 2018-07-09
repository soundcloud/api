package main

import (
	"encoding/json"
	"fmt"
	"net/http"
)

type moshimoshiClient struct {
	host       string
	httpClient *http.Client
}

func (u *moshimoshiClient) createTrackUID() (string, error) {
	const (
		accessToken       = "public-api-strangler-assets"
		jsonContentType   = "application/json"
		trackUIDsEndpoint = "http://%s/track_uids?access_token=%s"
	)

	url := fmt.Sprintf(trackUIDsEndpoint, u.host, accessToken)

	req, err := u.httpClient.Post(url, jsonContentType, nil)
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
