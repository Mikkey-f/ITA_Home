# 🏠 ITA Home

基于 Spring Boot + MyBatis Plus 构建的用户管理系统，集成 JWT 身份认证和 Swagger API 文档。

## 📁 项目结构

```
ITAHome/backend/home/
├── src/main/java/com/ita/home/          # Java 源码目录
│   ├── annotation/                      # 自定义注解
│   ├── aop/                            # AOP 切面
│   ├── common/                         # 通用工具类
│   ├── config/                         # 配置类
│   │   ├── MyBatisPlusConfig.java      # MyBatis Plus 配置
│   │   └── SwaggerConfig.java          # Swagger API 文档配置
│   ├── constant/                       # 常量定义
│   ├── controller/                     # 控制器层
│   │   ├── IndexController.java        # 首页控制器
│   │   └── UserController.java         # 用户管理控制器
│   ├── exception/                      # 异常处理
│   │   └── BaseException.java          # 基础异常类
│   ├── filter/                         # 过滤器
│   ├── mapper/                         # 数据访问层
│   │   └── UserMapper.java             # 用户数据访问接口
│   ├── model/                          # 数据模型
│   │   ├── entity/                     # 实体类
│   │   │   └── User.java               # 用户实体
│   │   ├── req/                        # 请求对象
│   │   │   ├── LoginRequest.java       # 登录请求
│   │   │   └── RegisterRequest.java    # 注册请求
│   │   └── vo/                         # 视图对象
│   ├── result/                         # 统一返回格式
│   │   └── Result.java                 # 统一响应结果类
│   ├── service/                        # 业务逻辑层
│   │   ├── UserService.java            # 用户服务接口
│   │   └── impl/                       # 服务实现
│   │       └── UserServiceImpl.java    # 用户服务实现类
│   ├── utils/                          # 工具类
│   └── HomeApplication.java            # 主启动类
├── src/main/resources/                 # 资源文件目录
│   ├── mapper/                         # MyBatis XML 映射文件
│   ├── static/                         # 静态资源
│   │   └── db.sql                      # 数据库初始化脚本
│   ├── templates/                      # 模板文件
│   ├── application.yml                 # 主配置文件
│   ├── application-dev.yml             # 开发环境配置
│   ├── swagger-guide.md                # Swagger 使用指南
│   └── swagger-api-examples.md         # API 测试示例
├── src/test/                           # 测试代码目录
├── target/                             # 编译输出目录
├── pom.xml                             # Maven 配置文件
└── README.md                           # 项目说明文档
```

## 🛠️ 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.6 | 基础开发框架 |
| MyBatis Plus | 3.5.5 | ORM 框架，简化数据库操作 |
| MySQL | 8.0+ | 关系型数据库 |
| Druid | 1.2.16 | 数据库连接池 |
| Swagger 3 | 2.2.0 | API 文档生成工具 |
| JWT | 0.11.5 | JSON Web Token，用于身份认证 |
| Lombok | - | 简化 Java 代码 |
| BCrypt | - | 密码加密 |

## 🚀 快速开始

### 1. 环境要求

- **JDK**: 17+
- **Maven**: 3.6+  
- **MySQL**: 8.0+

### 2. 数据库准备

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS ita_home
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 执行初始化脚本
source src/main/resources/static/db.sql
```

### 3. 配置文件

修改 `src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ita_home?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true
    username: root          # 修改为你的数据库用户名
    password: 123456        # 修改为你的数据库密码
```

### 4. 启动项目

```bash
# 方式1：使用 Maven 启动
mvn spring-boot:run

# 方式2：使用 IDE 启动
# 直接运行 HomeApplication.java 主类

# 方式3：打包后启动
mvn clean package
java -jar target/home-0.0.1-SNAPSHOT.jar
```

## 📖 API 文档

### Swagger UI 访问地址

项目启动后，通过以下地址访问 API 文档：

| 地址 | 说明 |
|------|------|
| **http://localhost:8080/swagger-ui.html** | 🔗 **Swagger UI 界面**（推荐） |
| http://localhost:8080/v3/api-docs | JSON 格式的 API 文档 |
| http://localhost:8080/ | 项目首页（包含文档链接） |

### API 接口概览

| 接口路径 | 方法 | 功能描述 | 需要认证 |
|---------|------|---------|----------|
| `/api/user/register` | POST | 用户注册 | ❌ |
| `/api/user/login` | POST | 用户登录，返回JWT令牌 | ❌ |
| `/api/user/check/{name}` | GET | 检查用户名是否存在 | ❌ |
| `/api/user/{id}` | GET | 查询用户信息 | ✅ JWT |
| `/api/user/profile` | GET | 获取当前用户信息 | ✅ JWT |
| `/api/user/avatar` | PUT | 修改用户头像 | ✅ JWT |
| `/api/user/password` | PUT | 修改用户密码 | ✅ JWT |
| `/api/user/stats` | GET | 用户统计信息 | 🔄 可选 |

## 🧪 测试数据

系统预置了以下测试用户（密码均为 `123456`）：

| 用户名 | 密码 | 头像 | 说明 |
|--------|------|------|------|
| admin | 123456 | 1 | 管理员账号 |
| testuser | 123456 | 2 | 测试用户 |
| demo | 123456 | 3 | 演示账号 |

## 🎯 使用示例

### 1. 用户注册

```bash
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "newuser",
    "password": "123456",
    "confirmPassword": "123456",
    "avatar": 1
  }'
```

### 2. 用户登录（获取JWT令牌）

```bash
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "name": "admin",
    "password": "123456"
  }'
```

**登录成功响应示例：**
```json
{
  "code": 1,
  "msg": null,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYWRtaW4iLCJ0aW1lc3RhbXAiOjE2OTU0NzIyMDAwMDAsInN1YiI6ImFkbWluIiwiaWF0IjoxNjk1NDcyMjAwLCJleHAiOjE2OTU1NTg2MDB9.xxx",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "name": "admin",
      "avatar": 1,
      "createTime": "2025-09-22T15:30:00"
    }
  }
}
```

### 3. 使用JWT令牌访问受保护接口

```bash
# 获取个人信息
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"

# 修改头像
curl -X PUT "http://localhost:8080/api/user/avatar?avatar=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"

# 修改密码
curl -X PUT http://localhost:8080/api/user/password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "oldPassword": "123456",
    "newPassword": "newpassword",
    "confirmPassword": "newpassword"
  }'
```

## 🎨 头像系统

项目支持 9 种不同风格的头像，编号 1-9：

| 编号 | 说明 | 编号 | 说明 | 编号 | 说明 |
|------|------|------|------|------|------|
| 1 | 默认头像 | 4 | 红色头像 | 7 | 粉色头像 |
| 2 | 蓝色头像 | 5 | 紫色头像 | 8 | 黄色头像 |
| 3 | 绿色头像 | 6 | 橙色头像 | 9 | 灰色头像 |

## 📊 数据模型

### User 用户表

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 用户ID，主键自增 |
| name | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(255) | 密码，BCrypt加密 |
| avatar | TINYINT | 头像编号，1-9 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 修改时间 |

### Result 统一返回格式

```json
{
  "code": 1,          // 1=成功，0=失败
  "msg": "操作成功",   // 响应消息
  "data": {...}       // 响应数据
}
```

## 🔧 开发说明

### 代码结构说明

- **Controller 层**：处理 HTTP 请求，参数验证，调用 Service
- **Service 层**：业务逻辑处理，事务控制
- **Mapper 层**：数据访问，使用 MyBatis Plus 简化 CRUD
- **Model 层**：数据模型，包含实体类、请求对象、响应对象

### 日志配置

```yaml
logging:
  level:
    com.ita.home:
      mapper: debug      # SQL 日志
      service: info      # 业务日志  
      controller: info   # 控制器日志
```

## 🚨 常见问题

### 1. 数据库连接失败

**错误**：`Failed to configure a DataSource`

**解决**：
- 检查 MySQL 服务是否启动
- 确认数据库 `ita_home` 是否存在
- 验证用户名密码是否正确

### 2. 端口被占用

**错误**：`Port 8080 was already in use`

**解决**：修改 `application.yml` 中的端口号
```yaml
server:
  port: 8081  # 改为其他端口
```

### 3. Swagger 无法访问

**解决**：确保项目启动成功，访问 http://localhost:8080/swagger-ui.html

## 🔐 JWT 身份认证

### JWT 配置说明

系统使用JWT（JSON Web Token）进行用户身份认证：

- **令牌有效期**：24小时
- **认证方式**：请求头携带 `Authorization: Bearer <token>`
- **自动过期**：令牌过期后需要重新登录

### 认证流程

1. **用户登录** → 获得JWT令牌
2. **携带令牌** → 在请求头中添加 `Authorization: Bearer <token>`
3. **访问受保护接口** → 系统自动验证令牌有效性
4. **令牌过期** → 返回401错误，需要重新登录

### 认证注解

- `@RequireAuth` - 必须登录才能访问
- `@RequireAuth(required = false)` - 可选登录，登录后获得更多信息

### Swagger中测试JWT

1. 登录获得token
2. 点击Swagger右上角的🔒图标
3. 输入：`Bearer <your_token>`
4. 点击"Authorize"按钮
5. 现在可以测试需要认证的接口

## 📝 更新日志

### v2.0.0 (2025-09-22)
- ✅ **JWT身份认证系统**
- ✅ 用户个人信息管理
- ✅ 头像和密码修改功能
- ✅ 可选登录接口支持
- ✅ 完善的异常处理和日志

### v1.0.0 (2025-09-22)
- ✅ 基础用户注册登录功能
- ✅ MyBatis Plus 集成
- ✅ Swagger API 文档
- ✅ 统一异常处理
- ✅ 密码 BCrypt 加密
- ✅ 头像系统支持

## 📄 许可证

本项目基于 Apache 2.0 许可证开源。

## 👨‍💻 作者

**ITA Team**
- 📧 Email: support@ita.com
- 🔗 Website: https://ita.com

---

**🔗 快速链接**
- [Swagger UI](http://localhost:8080/swagger-ui.html) - API 接口文档
- [项目首页](http://localhost:8080/) - 系统说明页面
