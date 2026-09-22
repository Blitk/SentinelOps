@echo off
title SentinelOps - Spring Boot

cd /d "%~dp0..\sentinelOps"

echo ========================================
echo       SentinelOps - Spring Boot
echo ========================================
echo.

if not exist "pom.xml" (
    echo [ERRO] pom.xml nao encontrado.
    pause
    exit /b 1
)

echo [INFO] Iniciando Spring Boot...
echo.

call mvnw.cmd spring-boot:run

pause
