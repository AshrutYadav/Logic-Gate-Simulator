@echo off
setlocal
echo =======================================================
echo Running Logic Gate Simulator (JavaFX Desktop Application)
echo =======================================================

set "ROOT_DIR=%~dp0"
if "%ROOT_DIR:~-1%"=="\" set "ROOT_DIR=%ROOT_DIR:~0,-1%"

where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    if exist "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.4\plugins\maven\lib\maven3\bin\mvn.cmd" (
        set "JAVA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.4\jbr"
        set "PATH=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.4\jbr\bin;C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.4\plugins\maven\lib\maven3\bin;%PATH%"
    )
)

call mvn javafx:run -f "%ROOT_DIR%\pom.xml"
endlocal
