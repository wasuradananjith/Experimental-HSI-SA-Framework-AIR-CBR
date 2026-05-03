@echo off
setlocal enabledelayedexpansion

:: Check if an argument is provided
if "%~1"=="" (
    echo Usage: %0 [package_name]
    exit /b 1
)

:: Define variables
set PACKAGE_NAME=%1
set OUTPUT_FILE=%PACKAGE_NAME%.csv

:: Write CSV header
echo cpu%%,mem%% > %OUTPUT_FILE%

:loop
:: Get current timestamp
set TIMESTAMP=%TIME%

:: Get PID of the app
set PID=
for /f "tokens=2" %%i in ('adb shell pidof %PACKAGE_NAME%') do (
    set PID=%%i
    goto :check_pid
)

:check_pid
:: Check if PID is retrieved successfully
if "%PID%"=="" (
    :: Fallback method to retrieve PID using ps, grep, and awk
    for /f "tokens=2" %%i in ('adb shell "ps | grep %PACKAGE_NAME% | awk \"{print $2}\""') do (
        set PID=%%i
        goto :check_pid
    )
)

:: If PID is still not retrieved, print error and exit
if "%PID%"=="" (
    echo Failed to retrieve PID for %PACKAGE_NAME%. Ensure the app is running and accessible.
    exit /b 1
)

:: Get CPU and memory usage
for /f "tokens=1,9" %%i in ('adb shell top -n 1 -d 1 -p %PID%') do (
    set CPU=%%i
    set MEM=%%j
    goto :append_data
)

:append_data
:: Append data to CSV file
echo %CPU%,%MEM% >> %OUTPUT_FILE%

:: Wait for 1 second
timeout /t 1 > nul

goto loop

:end
