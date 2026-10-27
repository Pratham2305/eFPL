#!/bin/bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
mkdir -p "$DIR/logs"

echo "⚽ Starting FPL Assistant services..."

# Load .env if present
if [ -f "$DIR/.env" ]; then
  export $(grep -v '^#' "$DIR/.env" | xargs)
fi

TEAM_ID="${FPL_TEAM_ID:-3500002}"

# Override Docker service names to localhost for local machine execution
if [ "$ML_SERVICE_URL" = "http://ml-service:8000" ] || [ -z "$ML_SERVICE_URL" ]; then
  export ML_SERVICE_URL="http://localhost:8000"
fi
if [ "$OLLAMA_URL" = "http://ollama:11434" ] || [ -z "$OLLAMA_URL" ]; then
  export OLLAMA_URL="http://localhost:11434"
fi

# 1. Start Python ML Service (port 8000)
echo "🐍 Starting Python ML service on :8000..."
cd "$DIR/ml-service"
nohup ./venv/bin/uvicorn app.main:app --host 127.0.0.1 --port 8000 > "$DIR/logs/ml-service.log" 2>&1 &
echo $! > "$DIR/logs/ml-service.pid"

# 2. Start Java Spring Boot Backend (port 8080)
echo "☕ Starting Spring Boot API on :8080..."
cd "$DIR/backend-java"
nohup env FPL_TEAM_ID="$TEAM_ID" ML_SERVICE_URL="$ML_SERVICE_URL" OLLAMA_URL="$OLLAMA_URL" ./mvnw spring-boot:run > "$DIR/logs/backend-java.log" 2>&1 &
echo $! > "$DIR/logs/backend-java.pid"

# 3. Start Vite React Frontend (port 3000)
echo "⚛️  Starting Frontend on :3000..."
cd "$DIR/frontend"
nohup npm run dev > "$DIR/logs/frontend.log" 2>&1 &
echo $! > "$DIR/logs/frontend.pid"

echo "✅ All services launched in background!"
echo "   - Frontend:  http://localhost:3000"
echo "   - Backend:   http://localhost:8080"
echo "   - ML Service: http://localhost:8000"
echo "   - Logs:      $DIR/logs/"
