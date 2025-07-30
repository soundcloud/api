package main

import "fmt"

type clientError struct {
	cause error
}

func (e clientError) Error() string {
	return fmt.Sprintf("client error: %s", e.cause)
}

type fileNameValidationError struct{}

func (e fileNameValidationError) Error() string {
	return "file name validation error"
}
