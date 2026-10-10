#!/usr/bin/env bash
set -Eeuo pipefail

APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MYSQL_BIN="$(command -v mysqld)"
MYSQL_BASE="$(dirname "$(dirname "$(readlink -f "$MYSQL_BIN")")")"
MYSQL_DIR="${HOME}/.local/share/clearline/mysql"
MYSQL_PORT=3307
MYSQL_SOCKET="${MYSQL_DIR}/mysql.sock"
MYSQL_PID_FILE="${MYSQL_DIR}/mysql.pid"

mkdir -p "$MYSQL_DIR"

if [[ ! -d "${MYSQL_DIR}/mysql" ]]; then
  echo "Preparing the local development database..."
  "$MYSQL_BIN" --no-defaults --initialize-insecure \
    --user="$(id -un)" \
    --basedir="$MYSQL_BASE" \
    --datadir="$MYSQL_DIR" \
    --log-error="${MYSQL_DIR}/initialize.log"
fi

"$MYSQL_BIN" --no-defaults \
  --user="$(id -un)" \
  --basedir="$MYSQL_BASE" \
  --datadir="$MYSQL_DIR" \
  --socket="$MYSQL_SOCKET" \
  --pid-file="$MYSQL_PID_FILE" \
  --bind-address=127.0.0.1 \
  --port="$MYSQL_PORT" \
  --mysqlx=0 \
  --log-error="${MYSQL_DIR}/mysql.log" &
MYSQL_PID=$!

stop_mysql() {
  kill "$MYSQL_PID" 2>/dev/null || true
  wait "$MYSQL_PID" 2>/dev/null || true
}
trap stop_mysql EXIT INT TERM

ready=0
for _ in $(seq 1 60); do
  if mysqladmin --no-defaults --protocol=socket --socket="$MYSQL_SOCKET" -uroot ping >/dev/null 2>&1; then
    ready=1
    break
  fi
  sleep 1
done

if [[ "$ready" -ne 1 ]]; then
  echo "The local MySQL server did not start. See ${MYSQL_DIR}/mysql.log." >&2
  exit 1
fi

mysql --no-defaults --protocol=socket --socket="$MYSQL_SOCKET" -uroot <<'SQL'
CREATE DATABASE IF NOT EXISTS fraud_detection CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'fraud_app'@'127.0.0.1' IDENTIFIED BY '';
GRANT ALL PRIVILEGES ON fraud_detection.* TO 'fraud_app'@'127.0.0.1';
FLUSH PRIVILEGES;
SQL

export MYSQL_URL="jdbc:mysql://127.0.0.1:${MYSQL_PORT}/fraud_detection?serverTimezone=UTC"
export MYSQL_USER="fraud_app"
export MYSQL_PASSWORD=""

cd "$APP_DIR"
echo "Starting the website and its local MySQL database..."
mvn -B spring-boot:run
