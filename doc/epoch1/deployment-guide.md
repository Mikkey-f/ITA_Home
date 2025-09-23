# ITAHome 部署配置指南

## 概述

本文档详细说明了ITAHome后端系统的部署配置，包括环境要求、配置项说明、部署步骤和运维注意事项。

---

## 环境要求

### 基础环境
- **Java**: JDK 17 或更高版本
- **Maven**: 3.6.0 或更高版本
- **数据库**: MySQL 8.0 或更高版本
- **缓存**: Redis 6.0 或更高版本 (可选，使用内存缓存也可)
- **邮件服务**: SMTP邮件服务器

### 硬件要求
- **CPU**: 2核心以上
- **内存**: 4GB以上
- **磁盘**: 20GB以上可用空间
- **网络**: 稳定的网络连接

---

## 配置文件详解

### 主配置文件 application.yml

```yaml
# 服务端口配置
server:
  port: 8080
  servlet:
    context-path: /
  # 生产环境建议启用压缩
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json
    min-response-size: 1024

# Spring框架配置
spring:
  # 应用名称
  application:
    name: ita-home-backend
  
  # 数据源配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:ita_home}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    # 连接池配置 (HikariCP)
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      auto-commit: true
      idle-timeout: 30000
      pool-name: ITAHomeHikariCP
      max-lifetime: 1800000
      connection-timeout: 30000

  # MyBatis Plus配置
  mybatis-plus:
    configuration:
      map-underscore-to-camel-case: true
      cache-enabled: false
      log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    global-config:
      db-config:
        logic-delete-field: deleted
        logic-delete-value: 1
        logic-not-delete-value: 0
        id-type: auto
    mapper-locations: classpath*:mapper/*.xml

  # 缓存配置
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=600s
    cache-names:
      - verifyCodeCache

  # 邮件配置
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME:your-email@gmail.com}
    password: ${MAIL_PASSWORD:your-app-password}
    default-encoding: UTF-8
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          timeout: 5000
          connection-timeout: 5000
          write-timeout: 5000

# JWT配置
jwt:
  secret: ${JWT_SECRET:ITAHome2025SecretKeyForJWTAuthenticationVeryLongAndSecure}
  expire-hours: ${JWT_EXPIRE_HOURS:2}
  token-prefix: "Bearer "
  header-name: "Authorization"

# 自定义邮件配置
ita:
  mail:
    username: ${MAIL_USERNAME:your-email@gmail.com}

# 日志配置
logging:
  level:
    com.ita.home: ${LOG_LEVEL:INFO}
    org.springframework.web: ${LOG_LEVEL:INFO}
    org.springframework.mail: ${LOG_LEVEL:INFO}
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/ita-home.log
    max-size: 100MB
    max-history: 30

# Swagger文档配置
springdoc:
  api-docs:
    enabled: true
    path: /api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  packages-to-scan: com.ita.home.controller
```

---

## 环境变量配置

### 必需环境变量

```bash
# 数据库配置
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=ita_home
export DB_USERNAME=root
export DB_PASSWORD=your_database_password

# JWT配置
export JWT_SECRET=your_very_strong_secret_key_here_at_least_256_bits
export JWT_EXPIRE_HOURS=2

# 邮件配置
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password

# 日志级别
export LOG_LEVEL=INFO
```

### 可选环境变量

```bash
# 服务端口 (默认8080)
export SERVER_PORT=8080

# 数据库连接池配置
export DB_POOL_MIN_IDLE=5
export DB_POOL_MAX_SIZE=20

# 缓存配置
export CACHE_MAX_SIZE=1000
export CACHE_EXPIRE_SECONDS=600
```

---

## 数据库配置

### MySQL 数据库创建

```sql
-- 创建数据库
CREATE DATABASE ita_home DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户(可选)
CREATE USER 'ita_user'@'%' IDENTIFIED BY 'strong_password_here';
GRANT ALL PRIVILEGES ON ita_home.* TO 'ita_user'@'%';
FLUSH PRIVILEGES;
```

### 用户表结构

```sql
USE ita_home;

CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `name` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码(BCrypt加密)',
  `mail` varchar(100) NOT NULL COMMENT '邮箱',
  `group_id` int(11) DEFAULT NULL COMMENT '分组ID',
  `avatar` int(11) DEFAULT 1 COMMENT '头像编号(1-9)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  UNIQUE KEY `uk_mail` (`mail`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 插入测试数据(可选)
INSERT INTO `user` (`name`, `password`, `mail`, `avatar`) VALUES 
('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin@example.com', 1);
-- 密码是: password
```

---

## 邮件服务配置

### Gmail SMTP配置
1. 开启两步验证
2. 生成应用专用密码
3. 配置SMTP设置：
   ```yaml
   spring:
     mail:
       host: smtp.gmail.com
       port: 587
       username: your-email@gmail.com
       password: your-app-password  # 应用专用密码
   ```

### 腾讯企业邮箱配置
```yaml
spring:
  mail:
    host: smtp.exmail.qq.com
    port: 587
    username: your-email@company.com
    password: your-password
```

### 阿里云邮件推送
```yaml
spring:
  mail:
    host: smtpdm.aliyun.com
    port: 80
    username: your-username
    password: your-password
```

---

## 部署方式

### 1. JAR包部署

#### 构建JAR包
```bash
# 清理并构建
mvn clean package -DskipTests

# 生成的JAR包位置
ls target/home-*.jar
```

#### 运行JAR包
```bash
# 基本运行
java -jar target/home-1.0.0.jar

# 指定配置文件
java -jar target/home-1.0.0.jar --spring.config.location=classpath:/application.yml,file:./config/application-prod.yml

# 指定JVM参数
java -Xms1g -Xmx2g -XX:+UseG1GC -jar target/home-1.0.0.jar

# 后台运行
nohup java -jar target/home-1.0.0.jar > app.log 2>&1 &
```

### 2. Docker部署

#### Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制JAR文件
COPY target/home-*.jar app.jar

# 创建日志目录
RUN mkdir -p logs

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
```

#### 构建和运行
```bash
# 构建镜像
docker build -t ita-home-backend:latest .

# 运行容器
docker run -d \
  --name ita-home-backend \
  -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_PASSWORD=your_password \
  -e JWT_SECRET=your_secret \
  -e MAIL_USERNAME=your_email \
  -e MAIL_PASSWORD=your_app_password \
  -v /path/to/logs:/app/logs \
  ita-home-backend:latest
```

#### Docker Compose

```yaml
version: '3.8'

services:
   ita-home-backend:
      build: ..
      ports:
         - "8080:8080"
      environment:
         - DB_HOST=mysql
         - DB_PASSWORD=password
         - JWT_SECRET=your_secret_key
         - MAIL_USERNAME=your_email
         - MAIL_PASSWORD=your_app_password
      depends_on:
         - mysql
         - redis
      volumes:
         - ./logs:/app/logs
      restart: unless-stopped

   mysql:
      image: mysql:8.0
      environment:
         MYSQL_ROOT_PASSWORD: password
         MYSQL_DATABASE: ita_home
      ports:
         - "3306:3306"
      volumes:
         - mysql_data:/var/lib/mysql
      restart: unless-stopped

   redis:
      image: redis:6.2-alpine
      ports:
         - "6379:6379"
      restart: unless-stopped

volumes:
   mysql_data:
```

### 3. Systemd服务部署

#### 创建服务文件
```bash
sudo nano /etc/systemd/system/ita-home.service
```

```ini
[Unit]
Description=ITA Home Backend Service
After=network.target mysql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/ita-home
ExecStart=/usr/bin/java -jar -Xms1g -Xmx2g /opt/ita-home/home-1.0.0.jar
ExecStop=/bin/kill -15 $MAINPID
Restart=always
RestartSec=10
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=ita-home

# 环境变量
Environment=DB_HOST=localhost
Environment=DB_PASSWORD=your_password
Environment=JWT_SECRET=your_secret_key
Environment=MAIL_USERNAME=your_email
Environment=MAIL_PASSWORD=your_app_password

[Install]
WantedBy=multi-user.target
```

#### 管理服务
```bash
# 重新加载systemd配置
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start ita-home

# 开机自启
sudo systemctl enable ita-home

# 查看状态
sudo systemctl status ita-home

# 查看日志
sudo journalctl -u ita-home -f
```

---

## 反向代理配置

### Nginx配置
```nginx
upstream ita_home_backend {
    server 127.0.0.1:8080;
    # server 127.0.0.1:8081;  # 负载均衡时添加更多服务器
}

server {
    listen 80;
    server_name api.itahome.com;
    
    # 重定向到HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.itahome.com;
    
    # SSL证书配置
    ssl_certificate /path/to/certificate.crt;
    ssl_certificate_key /path/to/private.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    
    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline'" always;
    
    # 日志配置
    access_log /var/log/nginx/ita-home-access.log;
    error_log /var/log/nginx/ita-home-error.log;
    
    # 反向代理配置
    location / {
        proxy_pass http://ita_home_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时配置
        proxy_connect_timeout 30s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;
        
        # 缓冲配置
        proxy_buffering on;
        proxy_buffer_size 8k;
        proxy_buffers 8 8k;
        proxy_busy_buffers_size 16k;
    }
    
    # 静态文件缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        proxy_pass http://ita_home_backend;
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
    
    # API路径
    location /api/ {
        proxy_pass http://ita_home_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # CORS配置
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS";
        add_header Access-Control-Allow-Headers "DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization";
        
        if ($request_method = 'OPTIONS') {
            return 204;
        }
    }
}
```

---

## 监控和日志

### 1. 应用监控

#### 添加Actuator依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

#### 监控配置
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,env
  endpoint:
    health:
      show-details: when-authorized
  info:
    env:
      enabled: true
```

#### 健康检查端点
```bash
# 基础健康检查
curl http://localhost:8080/actuator/health

# 详细健康信息
curl http://localhost:8080/actuator/health/db
curl http://localhost:8080/actuator/health/mail
```

### 2. 日志管理

#### Logback配置 (logback-spring.xml)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="30 seconds">
    
    <!-- 控制台输出 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- 文件输出 -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/ita-home.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/ita-home.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- 错误日志单独文件 -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/ita-home-error.log</file>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/ita-home-error.%d{yyyy-MM-dd}.log.gz</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- 应用日志级别 -->
    <logger name="com.ita.home" level="INFO"/>
    <logger name="org.springframework.mail" level="INFO"/>
    <logger name="org.springframework.web" level="INFO"/>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
        <appender-ref ref="ERROR_FILE"/>
    </root>
    
</configuration>
```

---

## 性能优化

### 1. JVM参数优化
```bash
# 基础参数
-Xms2g -Xmx4g

# GC参数
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+UseStringDeduplication

# 监控参数
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-Xloggc:gc.log

# 完整启动命令
java -Xms2g -Xmx4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+UseStringDeduplication \
     -XX:+PrintGCDetails \
     -XX:+PrintGCTimeStamps \
     -Xloggc:logs/gc.log \
     -jar home-1.0.0.jar
```

### 2. 数据库连接池优化
```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 10
      maximum-pool-size: 50
      auto-commit: true
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-timeout: 30000
      validation-timeout: 5000
      leak-detection-threshold: 60000
```

### 3. 缓存优化
```yaml
spring:
  cache:
    caffeine:
      spec: maximumSize=10000,expireAfterWrite=600s,recordStats
```

---

## 安全配置

### 1. 防火墙配置
```bash
# 只开放必要端口
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 22/tcp
sudo ufw deny 8080/tcp  # 应用端口不直接暴露
sudo ufw enable
```

### 2. 应用安全
```yaml
server:
  # 隐藏服务器信息
  server-header: ""
  
spring:
  # 安全配置
  security:
    headers:
      frame-options: DENY
      content-type-options: nosniff
      xss-protection: "1; mode=block"
```

### 3. 数据库安全
```sql
-- 删除匿名用户
DELETE FROM mysql.user WHERE User='';

-- 删除test数据库
DROP DATABASE IF EXISTS test;

-- 设置强密码策略
SET GLOBAL validate_password.policy=STRONG;
```

---

## 故障排查

### 常见问题和解决方案

#### 1. 应用启动失败
```bash
# 检查端口占用
netstat -tulpn | grep 8080

# 检查Java进程
ps aux | grep java

# 查看启动日志
tail -f logs/ita-home.log
```

#### 2. 数据库连接失败
```bash
# 测试数据库连接
mysql -h localhost -u root -p

# 检查数据库服务状态
systemctl status mysql

# 查看数据库错误日志
tail -f /var/log/mysql/error.log
```

#### 3. 邮件发送失败
```bash
# 测试SMTP连接
telnet smtp.gmail.com 587

# 检查邮件配置
curl -v smtp://smtp.gmail.com:587
```

#### 4. 内存不足
```bash
# 检查内存使用
free -h
top -p $(pgrep java)

# 分析堆转储
jmap -dump:format=b,file=heap.hprof <pid>
```

### 日志分析
```bash
# 查看错误日志
grep -i error logs/ita-home.log

# 统计API调用频率
grep "POST\|GET\|PUT\|DELETE" logs/ita-home.log | awk '{print $7}' | sort | uniq -c

# 查看最近的异常
tail -100 logs/ita-home-error.log
```

---

## 备份和恢复

### 1. 数据库备份
```bash
# 每日备份脚本
#!/bin/bash
DATE=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="/backup/mysql"
mkdir -p $BACKUP_DIR

mysqldump -u root -p$DB_PASSWORD ita_home > $BACKUP_DIR/ita_home_$DATE.sql
gzip $BACKUP_DIR/ita_home_$DATE.sql

# 保留30天的备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete
```

### 2. 应用备份
```bash
# 备份应用和配置
tar -czf backup_$(date +%Y%m%d).tar.gz \
  home-*.jar \
  application*.yml \
  logs/ \
  config/
```

### 3. 自动化备份
```bash
# 添加到crontab
crontab -e

# 每天凌晨2点备份
0 2 * * * /opt/ita-home/backup.sh
```

---

## 更新和维护

### 1. 应用更新流程
```bash
# 1. 停止服务
sudo systemctl stop ita-home

# 2. 备份当前版本
cp home-current.jar home-backup.jar

# 3. 部署新版本
cp home-new-version.jar home-current.jar

# 4. 启动服务
sudo systemctl start ita-home

# 5. 验证服务
curl http://localhost:8080/actuator/health
```

### 2. 数据库迁移
```sql
-- 创建迁移脚本版本管理表
CREATE TABLE migration_history (
  id INT AUTO_INCREMENT PRIMARY KEY,
  version VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 执行迁移脚本
SOURCE migration_v1.1.0.sql;
INSERT INTO migration_history (version, description) VALUES ('1.1.0', 'Add new columns');
```

### 3. 零停机更新
```bash
# 使用蓝绿部署
# 1. 启动新版本在不同端口
java -jar -Dserver.port=8081 home-new.jar

# 2. 健康检查
curl http://localhost:8081/actuator/health

# 3. 切换Nginx配置
# 4. 停止老版本
```

---

## 监控告警

### 1. 基础监控脚本
```bash
#!/bin/bash
# 服务健康检查脚本

APP_URL="http://localhost:8080/actuator/health"
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" $APP_URL)

if [ $RESPONSE -ne 200 ]; then
    echo "Application health check failed: HTTP $RESPONSE"
    # 发送告警通知
    exit 1
fi

# 检查磁盘空间
DISK_USAGE=$(df / | awk 'NR==2 {print $5}' | sed 's/%//')
if [ $DISK_USAGE -gt 80 ]; then
    echo "Disk usage is above 80%: $DISK_USAGE%"
fi

# 检查内存使用
MEM_USAGE=$(free | awk 'NR==2{printf "%.1f", $3*100/$2}')
if (( $(echo "$MEM_USAGE > 80" | bc -l) )); then
    echo "Memory usage is above 80%: $MEM_USAGE%"
fi
```

### 2. 日志监控
```bash
# 监控错误日志
tail -f logs/ita-home-error.log | while read line; do
    echo "ERROR: $line"
    # 发送告警
done
```

---

## 版本记录

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| v1.0 | 2025-09-23 | 初始部署指南 |

---

## 联系支持

如果在部署过程中遇到问题，请联系开发团队获取支持。