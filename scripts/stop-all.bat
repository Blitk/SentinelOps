@echo off
title SentinelOps - Stop

echo ========================================
echo        SENTINELOPS SHUTDOWN
echo ========================================
echo.

echo [1/3] Encerrando Python Agent...
taskkill /FI "WINDOWTITLE eq SentinelOps - Python Agent*" /T /F >nul 2>&1

echo [OK] Python Agent encerrado.

echo.
echo [2/3] Encerrando Spring Boot...
taskkill /FI "WINDOWTITLE eq SentinelOps - Spring Boot*" /T /F >nul 2>&1

echo [OK] Spring Boot encerrado.

echo.
echo [3/3] Finalizando...
echo.

echo ========================================
echo       SENTINELOPS ENCERRADO
echo ========================================
echo.

pause
