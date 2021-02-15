#!/bin/bash

cd app && ./node_modules/.bin/multi-file-swagger -o yaml api.yaml > compiled_api.yaml && node app.js
