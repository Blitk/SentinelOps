#!/bin/bash

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PYTHON_DIR="$PROJECT_ROOT/SentinelOps_Python"

echo "========================================"
echo "      SentinelOps - Python Agent"
echo "========================================"
echo

cd "$PYTHON_DIR"

if [ ! -f "SentinelOps.py" ]; then
    echo "[ERRO] SentinelOps.py não encontrado."
    exit 1
fi

echo "[INFO] Iniciando Python Agent..."
echo

python3 SentinelOps.py -lp 8080
