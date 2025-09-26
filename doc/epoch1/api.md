# ITAHome 后端 API 接口文档

## 概述

本文档描述了ITAHome后端系统的用户认证相关API接口，包括用户注册、登录、信息管理和邮箱验证码功能。

**基础信息:**
- 服务名称: ITAHome Backend
- API版本: v1.0
- 基础URL: `/api`
- 认证方式: JWT Bearer Token
- 响应格式: JSON

## 统一响应格式

所有API接口都采用统一的响应格式：

```json
{
  "code": 1,           // 状态码: 1=成功，0=失败
  "msg": "操作成功",    // 响应消息
  "data": {}          // 响应数据(可选)
}
```

**状态码说明:**
- `1`: 操作成功
- `0`: 操作失败

---

## 用户认证接口

### 1. 用户注册

**接口描述:** 创建新用户账号

- **请求方式:** `POST`
- **请求路径:** `/api/user/register`
- **是否需要认证:** 否

**请求参数:**

| 参数名 | 类型 | 必填 | 长度限制 | 说明 | 示例 |
|--------|------|------|----------|------|------|
| name | String | 是 | 3-20字符 | 用户名 | "newuser" |
| password | String | 是 | 6-20字符 | 密码 | "123456" |
| email | String | 是 | - | 邮箱地址 | "user@example.com" |
| code | String | 是 | 4位数字 | 邮箱验证码 | "1234" |

**请求示例:**
```json
{
  "name": "newuser",
  "password": "123456",
  "email": "user@example.com",
  "code": "1234"
}
```

**响应示例:**

成功响应:
```json
{
  "code": 1,
  "msg": "注册成功！",
  "data": null
}
```

失败响应:
```json
{
  "code": 0,
  "msg": "用户名已存在，请换一个试试",
  "data": null
}
```

**错误码说明:**
- 用户名长度必须在3-20字符之间
- 密码长度必须在6-20字符之间  
- 验证码长度不为4
- 用户名已存在，请换一个试试
- 邮箱已存在，请换一个试试
- 验证码已经过期
- 验证码错误

---

### 2. 用户登录

**接口描述:** 验证用户名和密码，返回JWT令牌

- **请求方式:** `POST`
- **请求路径:** `/api/user/login/name`  
- **是否需要认证:** 否

**请求参数:**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| name | String | 是 | 用户名 | "admin" |
| password | String | 是 | 密码 | "123456" |

**请求示例:**
```json
{
  "name": "admin",
  "password": "123456"
}
```

**响应示例:**

成功响应:
```json
{
  "code": 1,
  "msg": null,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "name": "admin",
      "avatar": 3,
      "createTime": "2025-09-22T15:30:00"
    }
  }
}
```

失败响应:
```json
{
  "code": 0,
  "msg": "用户名或密码错误",
  "data": null
}
```

**响应字段说明:**
- `token`: JWT访问令牌
- `tokenType`: 令牌类型，固定为"Bearer"
- `expiresIn`: 令牌过期时间(秒)
- `user`: 用户基本信息

---

### 3. 邮箱登录

**接口描述:** 验证邮箱和密码，返回JWT令牌

- **请求方式:** `POST`
- **请求路径:** `/api/user/login/email`  
- **是否需要认证:** 否

**请求参数:**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| email | String | 是 | 邮箱地址 | "user@example.com" |
| password | String | 是 | 密码 | "123456" |

**请求示例:**
```json
{
  "email": "user@example.com",
  "password": "123456"
}
```

**响应示例:**

成功响应:
```json
{
  "code": 1,
  "msg": null,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "name": "admin",
      "avatar": 3,
      "createTime": "2025-09-22T15:30:00"
    }
  }
}
```

失败响应:
```json
{
  "code": 0,
  "msg": "邮箱或密码错误",
  "data": null
}
```

**响应字段说明:**
- `token`: JWT访问令牌
- `tokenType`: 令牌类型，固定为"Bearer"
- `expiresIn`: 令牌过期时间(秒)
- `user`: 用户基本信息

**错误码说明:**
- 邮箱或密码错误
- 用户名和密码不能为空
- 系统异常，请联系管理员

---

### 4. 获取邮箱验证码

**接口描述:** 向指定邮箱发送验证码

- **请求方式:** `PUT`
- **请求路径:** `/api/user/activate`
- **是否需要认证:** 否

**请求参数:**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| email | String | 是 | 目标邮箱地址 | "user@example.com" |

**请求示例:**
```
PUT /api/user/activate?email=user@example.com
```

**响应示例:**

成功响应:
```json
{
  "code": 1,
  "msg": "获取验证码成功",
  "data": null
}
```

失败响应:
```json
{
  "code": 0,
  "msg": "获取验证码失败",
  "data": null
}
```

**功能说明:**
- 验证码为4位随机数字
- 验证码有效期为10分钟
- 验证码通过邮件发送，采用HTML格式
- 支持重试机制，最大重试3次

---

## 用户信息接口 (需要JWT认证)

以下接口都需要在请求头中携带JWT令牌：
```
Authorization: Bearer <JWT_TOKEN>
```

### 5. 获取用户信息

**接口描述:** 根据用户ID获取用户详细信息

- **请求方式:** `GET`
- **请求路径:** `/api/user/{id}`
- **是否需要认证:** 是

**路径参数:**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 用户ID | 1 |

**请求示例:**
```
GET /api/user/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例:**

成功响应:
```json
{
  "code": 1,
  "msg": null,
  "data": {
    "id": 1,
    "name": "admin",
    "password": "xxxx",
    "mail": "admin@example.com",
    "groupId": 1,
    "avatar": 3,
    "createTime": "2025-09-22T15:30:00",
    "updateTime": "2025-09-23T10:15:00"
  }
}
```

---

### 6. 获取个人信息

**接口描述:** 获取当前登录用户的详细信息

- **请求方式:** `GET`
- **请求路径:** `/api/user/profile`
- **是否需要认证:** 是

**请求示例:**
```
GET /api/user/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例:**
```json
{
  "code": 1,
  "msg": null,
  "data": {
    "id": 4,
    "name": "mikkeyf",
    "avatar": 9,
    "createTime": "2025-09-25T00:15:01",
    "updateTime": "2025-09-25T00:15:01",
    "ojUserDataVo": {
      "ojUserDataDtoList": [],
      "totalAc": 422,
      "totalSubmit": 1805
    }
  }
}
```

**响应字段说明:**

| 字段名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| id | Long | 用户ID | 4 |
| name | String | 用户名 | "mikkeyf" |
| avatar | Integer | 头像编号(1-9) | 9 |
| createTime | String | 创建时间(ISO 8601格式) | "2025-09-25T00:15:01" |
| updateTime | String | 更新时间(ISO 8601格式) | "2025-09-25T00:15:01" |
| ojUserDataVo | Object | OJ平台数据汇总 | - |
| ojUserDataVo.ojUserDataDtoList | Array | 各平台详细数据列表 | [] |
| ojUserDataVo.totalAc | Integer | 总AC题目数 | 422 |
| ojUserDataVo.totalSubmit | Integer | 总提交数 | 1805 |

**注意事项:**
- `ojUserDataVo` 字段包含用户在各OJ平台的数据汇总
- `ojUserDataDtoList` 为空数组表示未配置具体平台账号或数据获取失败
- `totalAc` 和 `totalSubmit` 为所有平台的汇总数据

---

### 7. 修改用户头像

**接口描述:** 修改当前登录用户的头像

- **请求方式:** `PUT`
- **请求路径:** `/api/user/avatar`
- **是否需要认证:** 是

**请求参数:**

| 参数名 | 类型 | 必填 | 取值范围 | 说明 | 示例 |
|--------|------|------|----------|------|------|
| avatar | Integer | 是 | 1-9 | 头像编号 | 5 |

**请求示例:**
```
PUT /api/user/avatar?avatar=5
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例:**
```json
{
  "code": 1,
  "msg": "头像修改成功",
  "data": null
}
```

---

### 8. 修改密码

**接口描述:** 修改当前登录用户的密码

- **请求方式:** `PUT`
- **请求路径:** `/api/user/password`
- **是否需要认证:** 是

**请求参数:**

| 参数名 | 类型 | 必填 | 长度限制 | 说明 | 示例 |
|--------|------|------|----------|------|------|
| oldPassword | String | 是 | - | 原密码 | "123456" |
| newPassword | String | 是 | 6-20字符 | 新密码 | "654321" |
| confirmPassword | String | 是 | 6-20字符 | 确认新密码 | "654321" |

**请求示例:**
```json
{
  "oldPassword": "123456",
  "newPassword": "654321", 
  "confirmPassword": "654321"
}
```

**响应示例:**
```json
{
  "code": 1,
  "msg": "密码修改成功",
  "data": null
}
```

**错误码说明:**
- 原密码不能为空
- 新密码长度必须在6-20字符之间
- 两次输入的新密码不一致
- 原密码错误

---

## 工具接口

### 9. 检查用户名是否存在

**接口描述:** 检查指定用户名是否已被注册

- **请求方式:** `GET`
- **请求路径:** `/api/user/check/{name}`
- **是否需要认证:** 否

**路径参数:**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| name | String | 是 | 要检查的用户名 | "testuser" |

**请求示例:**
```
GET /api/user/check/testuser
```

**响应示例:**
```json
{
  "code": 1,
  "msg": null,
  "data": true    // true=已存在，false=可用
}
```

---

## JWT认证说明

### Token格式
```
Authorization: Bearer <JWT_TOKEN>
```

### Token包含信息
- `userId`: 用户ID
- `username`: 用户名  
- `timestamp`: 生成时间戳
- `exp`: 过期时间

### Token有效期
- 默认有效期：2小时
- 过期后需要重新登录获取新token

### 认证失败响应
```json
{
  "code": 0,
  "msg": "未授权，请先登录",
  "data": null
}
```

---

## 错误处理

### 通用错误响应
```json
{
  "code": 0,
  "msg": "具体错误信息",
  "data": null  
}
```

### 常见错误码
- **参数错误**: 请求参数不能为空、格式不正确等
- **认证错误**: 未授权、令牌过期、令牌无效等  
- **业务错误**: 用户名已存在、密码错误、验证码错误等
- **系统错误**: 系统异常，请联系管理员

---

## 开发环境测试

### 基础URL
```
http://localhost:8080/api
```

### 测试工具推荐
- Postman
- Apifox  
- curl命令行

### 测试流程
1. 先调用获取验证码接口
2. 使用验证码进行用户注册
3. 使用注册的账号进行登录
4. 使用返回的JWT token调用需要认证的接口

---

## 版本更新记录

| 版本 | 更新时间 | 更新内容 |
|------|----------|----------|
| v1.0 | 2025-09-23 | 初始版本，包含用户注册、登录、信息管理功能 |
| v1.1 | 2025-09-25 | 获取个人信息接口新增OJ数据汇总字段(ojUserDataVo) |
| v1.2 | 2025-09-25 | 新增邮箱登录接口(/api/user/login/email) |

---

## 联系方式

如有疑问，请联系开发团队。