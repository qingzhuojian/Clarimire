@echo off
chcp 65001 >nul
echo 正在启动后端服务...
echo.

cd /d "%~dp0"

REM 检查是否存在已编译的jar文件
if exist "target\clarimire-backend-1.0.0.jar" (
    echo 找到已编译的jar文件，正在启动...
    java -jar target\clarimire-backend-1.0.0.jar
    goto :end
)

REM 如果没有jar文件，尝试使用Maven启动
if exist "mvnw.cmd" (
    echo 使用Maven Wrapper启动...
    call mvnw.cmd spring-boot:run
    goto :end
)

if exist "pom.xml" (
    echo 使用Maven启动（需要安装Maven）...
    mvn spring-boot:run
    goto :end
)

echo 错误：未找到可用的启动方式
echo 请确保：
echo 1. 已编译项目（运行 mvn clean package）
echo 2. 或已安装Maven并配置到PATH
pause

:end

