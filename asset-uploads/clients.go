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

type trackCoordinatorClientAPI interface {
	createUserPolicy(filename string, fileSize int64, session *EnrichedSessionResponse) (string, error)
	triggerTranscodings(uid string) error
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

type transcodingsRequest struct {
	Uid string `json:"uid"`
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

	t.setUserPolicyHeaders(req, session)
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

func (t *trackCoordinatorClient) setUserPolicyHeaders(r *http.Request, session *EnrichedSessionResponse) {
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

func (t *trackCoordinatorClient) triggerTranscodings(uid string) error {
	url := fmt.Sprintf("%s/transcodings", t.host)
	bs, err := json.Marshal(transcodingsRequest{Uid: uid})
	if err != nil {
		return err
	}

	req, err := http.NewRequest("POST", url, bytes.NewBuffer(bs))
	if err != nil {
		return err
	}
	req.Header.Set("Content-Type", jsonContentType)
	req.Header.Set("Sc-System", clientSystemName)

	resp, err := t.client.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusCreated {
		var msg []byte
		msg, _ = io.ReadAll(resp.Body)
		return fmt.Errorf("failed to trigger transcoding for uid: %s; status: %d; resp: %s;", uid, resp.StatusCode, string(msg))
	}

	return nil
}
