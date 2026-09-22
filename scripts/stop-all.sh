#!/bin/bash

echo "========================================"
echo "        SENTINELOPS SHUTDOWN"
echo "========================================"
echo

echo "[INFO] Encerrando processos..."

pkill -f "SentinelOps.py" 2>/dev/null || true
pkill -f "spring-boot:run" 2>/dev/null || true

echo "[OK] SentinelOps encerrado."
echo
