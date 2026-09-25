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

echo [INFO] Verificando PostgreSQL...
echo.

where psql >nul 2>&1

if errorlevel 1 (
    echo [ERRO] PostgreSQL nao encontrado no PATH.
    echo.
    echo Instale o PostgreSQL e adicione a pasta bin ao PATH.
    echo.
    pause
    exit /b 1
)

echo [OK] PostgreSQL encontrado.
echo.

set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=sentinelops
set DB_USER=postgres

echo [INFO] Verificando database "%DB_NAME%"...
echo.

psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='%DB_NAME%'" > temp_db_check.txt 2>nul

set /p DB_EXISTS=<temp_db_check.txt

del temp_db_check.txt

if "%DB_EXISTS%"=="1" (
    echo [OK] Database "%DB_NAME%" ja existe.
) else (
    echo [INFO] Database "%DB_NAME%" nao existe.
    echo [INFO] Criando database...

    createdb -h %DB_HOST% -p %DB_PORT% -U %DB_USER% %DB_NAME%

    if errorlevel 1 (
        echo.
        echo [ERRO] Nao foi possivel criar o database.
        echo Verifique usuario e senha do PostgreSQL.
        echo.
        pause
        exit /b 1
    )

    echo [OK] Database "%DB_NAME%" criado com sucesso.
)

echo.
echo ========================================
echo       Iniciando Spring Boot
echo ========================================
echo.

call mvnw.cmd spring-boot:run

pause
