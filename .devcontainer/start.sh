#!/usr/bin/env bash
# Runs every time the Codespace starts: PostgreSQL + stage backend (8080) + portal (3000).
# Secrets are generated once and kept only inside this Codespace (never in git).
set -uo pipefail

BACKEND_DIR="$(cd "$(dirname "$0")/.." && pwd)"
FRONTEND_DIR="${ORBIT_PORTAL_DIR:-$HOME/orbit-portal}"
ENV_FILE="$HOME/.orbit-stage.env"
LOG_DIR="$HOME/orbit-logs"
DB_NAME="postgresql_stage_v1_orbit_co_in"
DB_USER="orbit_stage_user"
mkdir -p "$LOG_DIR"

rand() { head -c 400 /dev/urandom | base64 -w0 | tr -dc 'A-Za-z0-9' | head -c "$1"; }
port_open() { (exec 3<>"/dev/tcp/127.0.0.1/$1") 2>/dev/null; }

# 1) Local secrets (generated once)
if [ ! -f "$ENV_FILE" ]; then
  {
    echo "STAGE_DB_PASSWORD=$(rand 24)"
    echo "JWT_SECRET=$(rand 64)"
    echo "ENCRYPTION_SECRET=$(rand 32)"
  } > "$ENV_FILE"
  chmod 600 "$ENV_FILE"
fi
set -a; . "$ENV_FILE"; set +a

# Razorpay: use Codespaces secrets if you added them, otherwise harmless placeholders
export RAZORPAY_KEY_ID="${RAZORPAY_KEY_ID:-rzp_test_placeholder}"
export RAZORPAY_KEY_SECRET="${RAZORPAY_KEY_SECRET:-placeholder_secret}"
export RAZORPAY_WEBHOOK_SECRET="${RAZORPAY_WEBHOOK_SECRET:-placeholder_webhook}"
export RAZORPAY_ACCOUNT_NUMBER="${RAZORPAY_ACCOUNT_NUMBER:-0000000000000000}"

# 2) PostgreSQL + stage database
sudo service postgresql start
for _ in $(seq 1 30); do sudo -u postgres pg_isready -q && break; sleep 1; done
sudo -u postgres psql -qtAc "SELECT 1 FROM pg_roles WHERE rolname='$DB_USER'" | grep -q 1 \
  || sudo -u postgres psql -v ON_ERROR_STOP=1 -qc "CREATE ROLE $DB_USER LOGIN PASSWORD '$STAGE_DB_PASSWORD' NOSUPERUSER NOCREATEDB NOCREATEROLE"
sudo -u postgres psql -qtAc "SELECT 1 FROM pg_database WHERE datname='$DB_NAME'" | grep -q 1 \
  || sudo -u postgres createdb -O "$DB_USER" "$DB_NAME"

# 3) Backend (stage profile on port 8080 so the portal's /api proxy works unchanged)
if ! port_open 8080; then
  JAR="$(ls -1 "$BACKEND_DIR"/build/libs/*.jar 2>/dev/null | grep -v -- '-plain' | head -1)"
  if [ -z "$JAR" ]; then
    (cd "$BACKEND_DIR" && ./gradlew bootJar -x test --no-daemon) && \
      JAR="$(ls -1 "$BACKEND_DIR"/build/libs/*.jar | grep -v -- '-plain' | head -1)"
  fi
  SPRING_PROFILES_ACTIVE=stage SERVER_PORT=8080 \
    nohup java -Xmx768m -jar "$JAR" > "$LOG_DIR/backend.log" 2>&1 &
fi

# 4) Portal (Vite dev server; *.app.github.dev is allowed in its vite.config.ts)
if ! port_open 3000; then
  (cd "$FRONTEND_DIR" && \
    nohup npm run dev -- --host 0.0.0.0 --port 3000 --strictPort > "$LOG_DIR/frontend.log" 2>&1 &)
fi

echo "ORBIT stage starting. Logs: $LOG_DIR/backend.log and $LOG_DIR/frontend.log"
echo "Open the Ports tab -> port 3000 (UI) / 8080 (API + /swagger-ui.html). Login: admin@orbit.com"
