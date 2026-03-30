param(
    [string]$DbHost = "localhost",
    [int]$DbPort = 5432,
    [string]$DbName = "localization_shop",
    [string]$DbUser = "postgres",
    [string]$DbPassword = "123456"
)

$scriptPath = Join-Path $PSScriptRoot "seed_test_data.sql"
if (-not (Test-Path $scriptPath)) {
    Write-Error "Seed SQL file not found: $scriptPath"
    exit 1
}

$psqlCommand = Get-Command psql -ErrorAction SilentlyContinue
$psqlExe = if ($psqlCommand) { $psqlCommand.Source } else { $null }

if (-not $psqlExe) {
    $searchRoots = @(
        "C:\Program Files\PostgreSQL",
        "C:\Program Files (x86)\PostgreSQL"
    )

    foreach ($root in $searchRoots) {
        if (-not (Test-Path $root)) {
            continue
        }

        $match = Get-ChildItem -Path $root -Directory -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        ForEach-Object {
            $candidate = Join-Path $_.FullName "bin\psql.exe"
            if (Test-Path $candidate) { $candidate }
        } |
        Select-Object -First 1

        if ($match) {
            $psqlExe = $match
            break
        }
    }
}

if (-not $psqlExe) {
    Write-Error "psql command not found. Please install PostgreSQL client tools or add psql to PATH."
    exit 1
}

$env:PGPASSWORD = $DbPassword
$dbExists = & $psqlExe -h $DbHost -p $DbPort -U $DbUser -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = '$DbName';"
if ($LASTEXITCODE -ne 0) {
    Write-Error "Cannot check database existence for: $DbName"
    exit $LASTEXITCODE
}

if ($dbExists.Trim() -ne "1") {
    Write-Host "Database $DbName does not exist. Creating..."
    & $psqlExe -h $DbHost -p $DbPort -U $DbUser -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE \"$DbName\" ENCODING 'UTF8';"
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to create database: $DbName"
        exit $LASTEXITCODE
    }
}

& $psqlExe -h $DbHost -p $DbPort -U $DbUser -d $DbName -v ON_ERROR_STOP=1 -f $scriptPath
if ($LASTEXITCODE -ne 0) {
    Write-Error "Seed script failed."
    exit $LASTEXITCODE
}

Write-Host "Using psql: $psqlExe"
Write-Host "Database bootstrap + seed completed successfully: $scriptPath"
