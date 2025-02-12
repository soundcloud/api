#!/usr/bin/env bash

set -euo pipefail

# See https://github.com/soundcloud/beep-utils/blob/master/github-actions/gocd-to-workflow/gocd-to-github-actions-migration-advice.md#pipeline-trigger
EVENT="deploy-to-production"
REPOS=$(cat <<-END
  soundcloud/auth-e2e-tests
END
)

gha-repo-dispatch.sh $EVENT $REPOS