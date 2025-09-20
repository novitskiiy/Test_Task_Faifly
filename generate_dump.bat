@echo off
REM Script to generate database dump for the visit tracking system
REM This script creates a complete dump of the database with test data

setlocal

REM Database configuration - modify these values if needed
set DB_NAME=visit_tracking
set DB_USER=root
set DB_PASSWORD=root
set DUMP_FILE=visit_tracking_dump.sql

echo ========================================
echo Healthcare Visit Tracking System
echo Database Dump Generator
echo ========================================
echo.

REM Check if mysqldump is available
where mysqldump >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: mysqldump not found in PATH
    echo Please make sure MySQL is installed and mysqldump is accessible
    echo.
    echo Common locations:
    echo - C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe
    echo - C:\Program Files\MySQL\MySQL Server 5.7\bin\mysqldump.exe
    echo.
    pause
    exit /b 1
)

echo Generating database dump for %DB_NAME%...
echo Database: %DB_NAME%
echo User: %DB_USER%
echo Output file: %DUMP_FILE%
echo.

REM Create dump with structure and data
echo Creating dump...
mysqldump -u %DB_USER% -p%DB_PASSWORD% --single-transaction --routines --triggers --events --add-drop-database --databases %DB_NAME% > %DUMP_FILE%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS: Database dump created!
    echo ========================================
    echo File: %DUMP_FILE%
    echo.
    echo To restore the database on another system:
    echo   mysql -u root -p ^< %DUMP_FILE%
    echo.
    echo To restore with different credentials:
    echo   mysql -u [username] -p[password] ^< %DUMP_FILE%
    echo.
) else (
    echo.
    echo ========================================
    echo ERROR: Failed to create database dump
    echo ========================================
    echo.
    echo Possible causes:
    echo 1. Database '%DB_NAME%' does not exist
    echo 2. Wrong username or password
    echo 3. MySQL server is not running
    echo 4. Insufficient permissions
    echo.
    echo Please check your database configuration and try again.
    echo.
    exit /b 1
)

echo Press any key to exit...
pause >nul
