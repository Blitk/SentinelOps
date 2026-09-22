@echo off
title SentinelOps

cd /d "%~dp0.."

echo ========================================
echo          SENTINELOPS STARTUP
echo ========================================
echo.

echo [1/3] Verificando PostgreSQL...
echo.

sc query postgresql-x64-17 >nul 2>&1

if %errorlevel%==0 (
    echo [OK] PostgreSQL encontrado.
    net start postgresql-x64-17 >nul 2>&1
) else (
    echo [AVISO] Servico PostgreSQL nao encontrado.
)

echo.
echo [2/3] Verificando Redis...
echo.

sc query Redis >nul 2>&1

if %errorlevel%==0 (
    echo [OK] Redis encontrado.
    net start Redis >nul 2>&1
) else (
    echo [AVISO] Servico Redis nao encontrado.
)

echo.
echo [3/3] Iniciando Spring Boot...
echo.

start "SentinelOps - Spring Boot" cmd /k "%~dp0start-spring.bat"

timeout /t 10 /nobreak >nul

echo.
echo [INFO] Iniciando Python Agent...
echo.

start "SentinelOps - Python Agent" cmd /k "%~dp0start-python.bat"

echo.
echo ========================================
echo       SENTINELOPS INICIADO
echo ========================================
echo.
echo Spring Boot: http://localhost:8080
echo Python Agent: ativo
echo PostgreSQL: ativo
echo Redis: ativo
echo.

pause
