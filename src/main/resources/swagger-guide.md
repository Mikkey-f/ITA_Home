# Swagger API 文档使用指南

## 🚀 快速开始

项目已集成 Swagger 3 (OpenAPI 3) 自动生成API文档，提供可视化的接口测试功能。

## 📖 访问方式

启动项目后，通过以下地址访问：

### Swagger UI (推荐)
```
http://localhost:8080/swagger-ui.html
```

### API文档JSON格式
```
http://localhost:8080/v3/api-docs
```

## 🔧 主要功能

### 1. 接口文档自动生成
- 自动根据Controller注解生成接口文档
- 显示请求参数、响应格式、状态码等详细信息
- 支持参数示例和字段说明

### 2. 在线接口测试
- 可直接在页面上测试API接口
- 支持请求参数填写和响应结果查看
- 无需额外的测试工具

### 3. 数据模型展示
- 自动展示请求和响应的数据结构
- 包含字段类型、必填项、取值范围等信息

## 📋 接口分组

### 用户管理 (UserController)
- **POST** `/api/user/register` - 用户注册
- **POST** `/api/user/login` - 用户登录
- **GET** `/api/user/{id}` - 查询用户信息
- **GET** `/api/user/check/{name}` - 检查用户名是否存在

## 🎯 测试示例

### 用户注册测试
1. 点击 "用户管理" 分组
2. 找到 "POST /api/user/register" 接口
3. 点击 "Try it out" 按钮
4. 填写请求参数：
   ```json
   {
     "name": "testuser",
     "password": "123456",
     "confirmPassword": "123456",
     "avatar": 1
   }
   ```
5. 点击 "Execute" 执行测试

### 用户登录测试
1. 找到 "POST /api/user/login" 接口
2. 填写登录信息：
   ```json
   {
     "name": "admin",
     "password": "123456"
   }
   ```
3. 执行测试查看返回结果

## ⚙️ 配置说明

Swagger配置位于 `SwaggerConfig.java`：
- 设置API标题和描述
- 配置联系人信息
- 设置许可证信息

页面配置在 `application.yml`：
- API文档路径：`/v3/api-docs`
- Swagger UI路径：`/swagger-ui.html`
- 接口排序：按字母顺序

## 🔍 注解说明

### Controller级别
- `@Tag` - 控制器分组和描述
- `@Hidden` - 隐藏不需要展示的控制器

### 方法级别
- `@Operation` - 接口描述和摘要
- `@ApiResponses` - 响应状态码说明
- `@Parameter` - 参数描述

### 模型级别
- `@Schema` - 数据模型和字段描述
- `@JsonIgnore` - 忽略敏感字段(如密码)

## 💡 使用技巧

1. **接口测试**：建议先测试不需要认证的接口
2. **参数填写**：参考示例数据快速填写测试参数
3. **响应查看**：注意查看响应的状态码和数据格式
4. **错误排查**：失败时查看响应消息了解错误原因

## 🔒 注意事项

- 密码字段在文档中已隐藏，不会显示实际值
- 生产环境建议禁用Swagger或设置访问权限
- 测试时请使用测试数据，避免影响真实数据