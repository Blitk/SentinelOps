#!/bin/bash

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

echo "========================================"
echo "      SentinelOps - Spring Boot"
echo "========================================"
echo

cd "$PROJECT_ROOT"

if [ ! -f "pom.xml" ]; then
    echo "[ERRO] pom.xml não encontrado."
    exit 1
fi

echo "[INFO] Iniciando Spring Boot..."
echo

./mvnw spring-boot:run
