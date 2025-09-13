@echo off
set DIR=%~dp0
set GRADLE_WRAPPER=%DIR%\gradle\wrapper\gradle-wrapper.jar
java -Xmx64m -cp "%GRADLE_WRAPPER%" org.gradle.wrapper.GradleWrapperMain %*
