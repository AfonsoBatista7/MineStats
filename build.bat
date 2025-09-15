@echo off
echo ====================================
echo Building MineStats Plugin
echo ====================================

REM Check if Maven is installed
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven and try again
    pause
    exit /b 1
)

echo Cleaning previous builds...
call mvn clean

if errorlevel 1 (
    echo ERROR: Maven clean failed
    pause
    exit /b 1
)

echo Compiling and packaging...
call mvn package assembly:single

if errorlevel 1 (
    echo ERROR: Maven build failed
    pause
    exit /b 1
)

echo Renaming and moving JAR file...
if exist "target\PluginStats-0.0.1-SNAPSHOT-jar-with-dependencies.jar" (
    ren "target\PluginStats-0.0.1-SNAPSHOT-jar-with-dependencies.jar" "MineStats.jar"
    copy "target\MineStats.jar" ".\MineStats.jar"
    echo SUCCESS: MineStats.jar created successfully!
) else (
    echo ERROR: JAR file not found in target directory
    pause
    exit /b 1
)

echo ====================================
echo Build completed successfully!
echo ====================================
pause