package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
)

const (
	jsonContentType  = "application/json"
	clientSystemName = "api-public-assets"
)

type mediaServiceClientAPI interface {
	createTranscoding(uid string, filename string) error
}

type mediaServiceClient struct {
	client *http.Client
	host   string
}

type trackCoordinatorClientAPI interface {
	createUserPolicy(filename string, fileSize int64, session *EnrichedSessionResponse) (string, error)
}

type trackCoordinatorClient struct {
	client *http.Client
	host   string
}

type policyRequest struct {
	Filename string `json:"filename"`
	FileSize int64  `json:"filesize"`
}

type policy struct {
	Uid string `json:"uid"`
}

type transcodingRequest struct {
	Uid      string `json:"uid"`
	Key      string `json:"key"`
	Priority string `json:"priority"`
	Filename string `json:"filename"`
}

type transcodingResponse struct {
	UID string `json:"uid"`
}

func transcodingRequestPayload(uid, filename string) ([]byte, error) {
	payload := transcodingRequest{
		Key:      uid,
		Uid:      uid,
		Priority: "manual",
		Filename: filename,
	}
	return json.Marshal(payload)
}

func (t *trackCoordinatorClient) createUserPolicy(filename string, fileSize int64, session *EnrichedSessionResponse) (string, error) {
	url := fmt.Sprintf("%s/user/upload-policy", t.host)
	bs, err := json.Marshal(policyRequest{Filename: filename, FileSize: fileSize})
	if err != nil {
		return "", err
	}

	req, err := http.NewRequest("POST", url, bytes.NewBuffer(bs))
	if err != nil {
		return "", err
	}

	t.setHeaders(req, session)
	resp, err := t.client.Do(req)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	switch resp.StatusCode {
	case http.StatusCreated:
		return t.parseUid(resp.Body, session.Urn)
	case http.StatusBadRequest:
		return "", clientError{fmt.Errorf("failed to generate upload id for user %s", session.Urn)}
	case http.StatusForbidden:
		return "", authorizationError{fmt.Errorf("not permitted to perform upload for user %s", session.Urn)}
	default:
		return "", fmt.Errorf("failed to generate upload id for user %s", session.Urn)
	}
}

func (t *trackCoordinatorClient) setHeaders(r *http.Request, session *EnrichedSessionResponse) {
	if session == nil {
		session = &EnrichedSessionResponse{}
	}
	for k, v := range CreateSessionHeaders(session) {
		r.Header.Set(k, v)
	}

	r.Header.Set("Content-Type", jsonContentType)
	r.Header.Set("Sc-System", clientSystemName)
}

func (t *trackCoordinatorClient) parseUid(body io.ReadCloser, userUrn string) (string, error) {
	policy := &policy{}
	if err := json.NewDecoder(body).Decode(policy); err != nil {
		return "", err
	}
	if policy.Uid == "" {
		return "", fmt.Errorf("failed to generate upload id for user %s", userUrn)
	}

	return policy.Uid, nil
}

func (u *mediaServiceClient) createTranscoding(uid, filename string) error {
	url := fmt.Sprintf("http://%s/transcode", u.host)

	bs, err := transcodingRequestPayload(uid, filename)
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
		return fmt.Errorf("failed to trigger transcoding for uid: %s, status %d", uid, resp.StatusCode)
	}

	res := &transcodingResponse{}

	if err := json.NewDecoder(resp.Body).Decode(res); err != nil {
		return err
	}

	return nil
}
