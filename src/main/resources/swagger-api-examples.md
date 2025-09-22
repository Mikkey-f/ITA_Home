# Swagger API 接口测试示例

## 📋 测试用例

### 1. 用户注册 (POST /api/user/register)

**请求示例：**
```json
{
  "name": "newuser2025",
  "password": "123456",
  "confirmPassword": "123456",
  "avatar": 5
}
```

**成功响应：**
```json
{
  "code": 1,
  "msg": null,
  "data": "注册成功！"
}
```

**失败响应示例：**
```json
{
  "code": 0,
  "msg": "用户名已存在，请换一个试试",
  "data": null
}
```

### 2. 用户登录 (POST /api/user/login)

**请求示例：**
```json
{
  "name": "admin",
  "password": "123456"
}
```

**成功响应：**
```json
{
  "code": 1,
  "msg": null,
  "data": {
    "id": 1,
    "name": "admin",
    "avatar": 1,
    "createTime": "2025-09-22T15:30:00"
  }
}
```

### 3. 查询用户信息 (GET /api/user/{id})

**请求：** `GET /api/user/1`

**成功响应：**
```json
{
  "code": 1,
  "msg": null,
  "data": {
    "id": 1,
    "name": "admin",
    "avatar": 1,
    "createTime": "2025-09-22T15:30:00",
    "updateTime": "2025-09-22T15:30:00"
  }
}
```

### 4. 检查用户名 (GET /api/user/check/{name})

**请求：** `GET /api/user/check/admin`

**响应：**
```json
{
  "code": 1,
  "msg": null,
  "data": true
}
```

## 🎯 在Swagger UI中测试步骤

### 步骤1：访问Swagger UI
1. 启动项目：`mvn spring-boot:run`
2. 浏览器打开：`http://localhost:8080/swagger-ui.html`

### 步骤2：测试用户注册
1. 找到 "用户管理" 分组下的 "POST /api/user/register"
2. 点击接口展开详情
3. 点击 "Try it out" 按钮
4. 在请求体中填写用户信息
5. 点击 "Execute" 执行测试
6. 查看响应结果

### 步骤3：测试用户登录
1. 使用刚注册的用户或测试账号进行登录
2. 查看返回的用户信息

### 步骤4：测试其他接口
- 使用返回的用户ID测试查询接口
- 测试用户名检查功能

## 📊 状态码说明

| 状态码 | 说明 | 示例场景 |
|--------|------|----------|
| 1 | 成功 | 操作执行成功 |
| 0 | 失败 | 参数错误、业务逻辑错误等 |

## 🔍 常见问题

### Q: 为什么密码字段不显示？
A: 出于安全考虑，密码字段使用了 `@JsonIgnore` 和 `@Schema(hidden = true)` 注解。

### Q: 如何测试空值验证？
A: 尝试传递空的用户名或密码，系统会返回相应的错误信息。

### Q: 头像编号范围是什么？
A: 头像编号必须在1-9之间，超出范围会返回验证错误。

## 💡 测试建议

1. **按顺序测试**：先注册 → 再登录 → 然后查询
2. **边界值测试**：测试用户名长度限制、头像编号范围等
3. **错误场景**：测试重复注册、错误密码等场景
4. **数据清理**：测试完成后可以手动清理测试数据