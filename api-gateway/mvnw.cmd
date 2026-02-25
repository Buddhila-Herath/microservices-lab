@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

if not "%JAVA_HOME%"=="" goto javaHomeSet
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
goto error

:javaHomeSet
set JAVA_EXE="%JAVA_HOME%\bin\java.exe"
if exist %JAVA_EXE% goto javaFound
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
goto error

:javaFound
if exist %WRAPPER_JAR% goto runWrapper
echo Downloading Maven Wrapper...
powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri %WRAPPER_URL% -OutFile %WRAPPER_JAR% }"
if not exist %WRAPPER_JAR% (
    echo ERROR: Failed to download Maven Wrapper JAR.
    goto error
)

:runWrapper
%JAVA_EXE% -jar %WRAPPER_JAR% %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
endlocal
exit /B %ERROR_CODE%
