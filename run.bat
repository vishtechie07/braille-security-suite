@echo off
echo Building Braille Script Printing App...
call mvn clean compile
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo Build successful! Starting application...
call mvn javafx:run
pause
