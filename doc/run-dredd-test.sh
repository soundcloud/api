#!/bin/bash

npm i
./node_modules/.bin/multi-file-swagger -o yaml api.yaml > compiled_api.yaml
npm test
rm compiled_api.yaml
