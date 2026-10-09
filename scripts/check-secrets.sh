#!/usr/bin/env bash
set -euo pipefail
if rg -n --hidden --glob '!build/**' --glob '!coverage/**' --glob '!.git/**' --glob '!scripts/check-secrets.sh' \
  '(gh[pousr]_[A-Za-z0-9]{30,}|AKIA[0-9A-Z]{16}|-----BEGIN ([A-Z ]+ )?PRIVATE KEY-----|SONAR_TOKEN=|GITGUARDIAN_API_KEY=)' .; then
  echo "Potential credential found in repository content." >&2
  exit 1
fi
