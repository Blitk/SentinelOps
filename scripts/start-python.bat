@echo off
title SentinelOps - Python Agent

cd /d "..\SentinelOps_Python"

echo ========================================
echo       SentinelOps - Python Agent
echo ========================================
echo.

if not exist "SentinelOps.py" (
    echo [ERRO] SentinelOps.py nao encontrado.
    pause
    exit /b 1
)

echo [INFO] Iniciando Python Agent...
echo.

python SentinelOps.py -lp 8080

pause
