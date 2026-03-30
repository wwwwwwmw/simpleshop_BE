@echo off
setlocal

set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=localization_shop
set DB_USER=postgres
set DB_PASSWORD=123456

set SCRIPT_PATH=%~dp0seed_test_data.sql
if not exist "%SCRIPT_PATH%" (
  echo Seed SQL file not found: %SCRIPT_PATH%
  exit /b 1
)

set "PSQL_EXE=psql"
set "HAS_PSQL_IN_PATH=1"
where psql >nul 2>nul
if errorlevel 1 (
  set "HAS_PSQL_IN_PATH=0"
  if exist "C:\Program Files\PostgreSQL\17\bin\psql.exe" set "PSQL_EXE=C:\Program Files\PostgreSQL\17\bin\psql.exe"
  if exist "C:\Program Files\PostgreSQL\16\bin\psql.exe" set "PSQL_EXE=C:\Program Files\PostgreSQL\16\bin\psql.exe"
  if exist "C:\Program Files\PostgreSQL\15\bin\psql.exe" set "PSQL_EXE=C:\Program Files\PostgreSQL\15\bin\psql.exe"
  if exist "C:\Program Files (x86)\PostgreSQL\17\bin\psql.exe" set "PSQL_EXE=C:\Program Files (x86)\PostgreSQL\17\bin\psql.exe"
  if exist "C:\Program Files (x86)\PostgreSQL\16\bin\psql.exe" set "PSQL_EXE=C:\Program Files (x86)\PostgreSQL\16\bin\psql.exe"
  if exist "C:\Program Files (x86)\PostgreSQL\15\bin\psql.exe" set "PSQL_EXE=C:\Program Files (x86)\PostgreSQL\15\bin\psql.exe"
)

if "%HAS_PSQL_IN_PATH%"=="0" (
  if not exist "%PSQL_EXE%" (
    echo psql command not found. Please install PostgreSQL client tools or add psql to PATH.
    exit /b 1
  )
)

set PGPASSWORD=%DB_PASSWORD%
set DB_EXISTS=
for /f %%i in ('"%PSQL_EXE%" -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname=''%DB_NAME%'';"') do set DB_EXISTS=%%i

if not "%DB_EXISTS%"=="1" (
  echo Database %DB_NAME% does not exist. Creating...
  "%PSQL_EXE%" -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE %DB_NAME% ENCODING 'UTF8';"
  if errorlevel 1 (
    echo Failed to create database %DB_NAME%.
    exit /b 1
  )
)

"%PSQL_EXE%" -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -v ON_ERROR_STOP=1 -f "%SCRIPT_PATH%"
if errorlevel 1 (
  echo Seed script failed.
  exit /b 1
)

echo Using psql: %PSQL_EXE%
echo Database bootstrap + seed completed successfully: %SCRIPT_PATH%
endlocal
