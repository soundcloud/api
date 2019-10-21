package main

import (
	"testing"
)

func contains(ss []string, s string) bool {
	for _, x := range ss {
		if x == s {
			return true
		}
	}
	return false
}

func TestTrackRoutePrefixes(t *testing.T) {
	expect := []string{"/tracks", "/tracks/", "/v1/tracks", "/tracks.json", "/v1/tracks/"}
	routes := trackRoutes()
	for _, want := range expect {
		if !contains(routes, want) {
			t.Errorf("expected routes %v to include %s but it does not", routes, want)
		}
	}
}
