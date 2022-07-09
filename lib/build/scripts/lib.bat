@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  lib startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and LIB_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\lib.jar;%APP_HOME%\lib\commons-math3-3.6.1.jar;%APP_HOME%\lib\guava-29.0-jre.jar;%APP_HOME%\lib\akka-cluster-sharding_2.13-2.6.18.jar;%APP_HOME%\lib\akka-cluster-typed_2.13-2.6.18.jar;%APP_HOME%\lib\akka-distributed-data_2.13-2.6.18.jar;%APP_HOME%\lib\akka-cluster-tools_2.13-2.6.18.jar;%APP_HOME%\lib\akka-cluster_2.13-2.6.18.jar;%APP_HOME%\lib\akka-actor-typed_2.13-2.6.18.jar;%APP_HOME%\lib\akka-slf4j_2.13-2.6.18.jar;%APP_HOME%\lib\akka-remote_2.13-2.6.18.jar;%APP_HOME%\lib\akka-coordination_2.13-2.6.18.jar;%APP_HOME%\lib\akka-persistence_2.13-2.6.18.jar;%APP_HOME%\lib\akka-stream_2.13-2.6.18.jar;%APP_HOME%\lib\akka-pki_2.13-2.6.18.jar;%APP_HOME%\lib\akka-actor_2.13-2.6.18.jar;%APP_HOME%\lib\scala-java8-compat_2.13-1.0.0.jar;%APP_HOME%\lib\ssl-config-core_2.13-0.4.2.jar;%APP_HOME%\lib\scala-parser-combinators_2.13-1.1.2.jar;%APP_HOME%\lib\scala-library-2.13.7.jar;%APP_HOME%\lib\slf4j-simple-1.7.9.jar;%APP_HOME%\lib\neuroph-core-2.94.jar;%APP_HOME%\lib\la4j-0.6.0.jar;%APP_HOME%\lib\akka-protobuf-v3_2.13-2.6.18.jar;%APP_HOME%\lib\failureaccess-1.0.1.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\checker-qual-2.11.1.jar;%APP_HOME%\lib\error_prone_annotations-2.3.4.jar;%APP_HOME%\lib\j2objc-annotations-1.3.jar;%APP_HOME%\lib\logback-classic-1.1.2.jar;%APP_HOME%\lib\slf4j-api-1.7.32.jar;%APP_HOME%\lib\commons-lang3-3.3.2.jar;%APP_HOME%\lib\logback-core-1.1.2.jar;%APP_HOME%\lib\config-1.4.0.jar;%APP_HOME%\lib\agrona-1.14.0.jar;%APP_HOME%\lib\lmdbjava-0.7.0.jar;%APP_HOME%\lib\reactive-streams-1.0.3.jar;%APP_HOME%\lib\asn-one-0.5.0.jar;%APP_HOME%\lib\jnr-ffi-2.1.9.jar;%APP_HOME%\lib\jffi-1.2.18.jar;%APP_HOME%\lib\jffi-1.2.18-native.jar;%APP_HOME%\lib\jnr-constants-0.9.12.jar;%APP_HOME%\lib\asm-commons-5.0.3.jar;%APP_HOME%\lib\asm-analysis-5.0.3.jar;%APP_HOME%\lib\asm-util-5.0.3.jar;%APP_HOME%\lib\asm-tree-5.0.3.jar;%APP_HOME%\lib\asm-5.0.3.jar;%APP_HOME%\lib\jnr-a64asm-1.0.0.jar;%APP_HOME%\lib\jnr-x86asm-1.0.2.jar


@rem Execute lib
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %LIB_OPTS%  -classpath "%CLASSPATH%" main.Main %*

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable LIB_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%LIB_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
