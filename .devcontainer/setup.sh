#!/usr/bin/env bash
# One-time setup, runs when the Codespace is created.
set -euo pipefail

BACKEND_DIR="$(cd "$(dirname "$0")/.." && pwd)"
FRONTEND_REPO="https://github.com/Mohammedyusuf5577/sparrow-portal-v2-ORBIT-AI-Protection.git"
FRONTEND_DIR="${ORBIT_PORTAL_DIR:-$HOME/orbit-portal}"

echo "==> [1/4] Installing PostgreSQL"
sudo apt-get update -y || true
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y postgresql postgresql-contrib

echo "==> [2/4] Cloning the admin portal (frontend) into $FRONTEND_DIR"
if [ ! -d "$FRONTEND_DIR/.git" ]; then
  git clone "$FRONTEND_REPO" "$FRONTEND_DIR"
fi

echo "==> [3/4] Installing frontend dependencies"
(cd "$FRONTEND_DIR" && npm ci)

echo "==> [4/4] Building the backend"
cd "$BACKEND_DIR"
chmod +x gradlew
./gradlew bootJar -x test --no-daemon

echo "==> Setup complete"
