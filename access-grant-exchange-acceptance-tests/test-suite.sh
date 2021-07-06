#!/usr/bin/env sh

base_dir=$(dirname "$0")
.  "${base_dir}/00-setup.sh"

echo "# OAuth access grant exchange test suite"

sh "${base_dir}/01-authorization-code.sh"
sh "${base_dir}/02-client-credentials.sh"
sh "${base_dir}/03-errors.sh"
sh "${base_dir}/04-password-mixed.sh"
