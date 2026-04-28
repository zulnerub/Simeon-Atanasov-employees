#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$ROOT_DIR/frontend"
npm install
npm run build

rm -rf "$ROOT_DIR/backend/src/main/resources/static"
mkdir -p "$ROOT_DIR/backend/src/main/resources/static"
cp -R "$ROOT_DIR/frontend/dist/"* "$ROOT_DIR/backend/src/main/resources/static/"

cd "$ROOT_DIR/backend"
mvn clean package

echo "Built backend/target/employees-0.0.1-SNAPSHOT.jar"
echo "Run with: java -jar backend/target/employees-0.0.1-SNAPSHOT.jar"
