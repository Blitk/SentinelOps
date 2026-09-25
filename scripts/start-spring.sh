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

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-sentinelops}"
DB_USER="${DB_USERNAME:-postgres}"

echo "[INFO] Verificando PostgreSQL..."

if ! command -v psql >/dev/null 2>&1; then
    echo "[ERRO] PostgreSQL não encontrado no PATH."
    exit 1
fi

echo "[OK] PostgreSQL encontrado."
echo

echo "[INFO] Verificando database '$DB_NAME'..."

DB_EXISTS=$(psql \
    -h "$DB_HOST" \
    -p "$DB_PORT" \
    -U "$DB_USER" \
    -d postgres \
    -tAc "SELECT 1 FROM pg_database WHERE datname='$DB_NAME'")

if [ "$DB_EXISTS" = "1" ]; then
    echo "[OK] Database '$DB_NAME' já existe."
else
    echo "[INFO] Database '$DB_NAME' não existe."
    echo "[INFO] Criando database..."

    createdb \
        -h "$DB_HOST" \
        -p "$DB_PORT" \
        -U "$DB_USER" \
        "$DB_NAME"

    echo "[OK] Database '$DB_NAME' criado com sucesso."
fi

echo
echo "========================================"
echo "      Iniciando Spring Boot"
echo "========================================"
echo

./mvnw spring-boot:run
