@echo off
setlocal
echo =======================================================
echo Building Logic Gate Simulator (Java 21, JavaFX, Maven)
echo =======================================================

set "ROOT_DIR=%~dp0"
if "%ROOT_DIR:~-1%"=="\" set "ROOT_DIR=%ROOT_DIR:~0,-1%"

:: If mvn or java is not in PATH, fallback to JetBrains JBR and bundled Maven
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    if exist "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.4\plugins\maven\lib\maven3\bin\mvn.cmd" (
        set "JAVA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.4\jbr"
        set "PATH=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.4\jbr\bin;C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.4\plugins\maven\lib\maven3\bin;%PATH%"
    )
)

echo Using Maven from PATH...
call mvn clean compile -f "%ROOT_DIR%\pom.xml"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Maven compilation failed.
    exit /b %ERRORLEVEL%
)

echo.
echo [SUCCESS] Compilation successful!
endlocal
