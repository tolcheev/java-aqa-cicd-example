#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$project_root"

if git ls-files --error-unmatch .env >/dev/null 2>&1; then
  echo "Нельзя коммитить .env" >&2
  exit 1
fi

pattern="BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY|Bearer[[:space:]]+eyJ|https://(prod[.-])?cinescope\\.t-qa\\.ru|((api[_-]?key|secret|access[_-]?token)[[:space:]]*[:=][[:space:]]*['\"][A-Za-z0-9_./+=-]{20,})"

if git ls-files --cached --others --exclude-standard -z \
  | xargs -0 grep -nE -I "$pattern"; then
  echo "Найдены данные, похожие на секрет или адрес Prod" >&2
  exit 1
fi

echo "Секреты и адрес Prod не найдены"
