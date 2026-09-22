#!/bin/bash

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

echo "========================================"
echo "         SENTINELOPS STARTUP"
echo "========================================"
echo

echo "[1/3] Iniciando PostgreSQL..."
sudo systemctl start postgresql

echo "[OK] PostgreSQL iniciado."
echo

echo "[2/3] Iniciando Redis..."
sudo systemctl start redis

echo "[OK] Redis iniciado."
echo

echo "[3/3] Iniciando Spring Boot..."
"$PROJECT_ROOT/scripts/start-spring.sh" &
SPRING_PID=$!

sleep 10

echo
echo "[INFO] Iniciando Python Agent..."
"$PROJECT_ROOT/scripts/start-python.sh" &
PYTHON_PID=$!

echo
echo "========================================"
echo "       SENTINELOPS INICIADO"
echo "========================================"
echo
echo "Spring Boot PID: $SPRING_PID"
echo "Python Agent PID: $PYTHON_PID"
echo
echo "Spring Boot: http://localhost:8080"
echo

wait
