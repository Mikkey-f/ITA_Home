# ITAHome 数据模型文档

## 概述

本文档描述了ITAHome后端系统中使用的数据模型，包括实体类、请求类、响应类等，为前端开发提供详细的数据结构参考。

---

## 实体类 (Entity)

### 1. User (用户实体)

**表名:** `user`  
**描述:** 用户基本信息实体，对应数据库用户表

```java
public class User {
    private Long id;                    // 用户ID - 主键自增
    private String name;                // 用户名 - 唯一
    private String password;            // 密码 - 加密存储  
    private String mail;                // 邮箱 - 唯一
    private Integer groupId;            // 分组ID (1-3)
    private Integer avatar;             // 头像编号 (1-9)
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 修改时间
}
```

**字段详细说明:**

| 字段名 | 类型 | 是否必填 | 约束 | 说明 | 示例值 |
|--------|------|----------|------|------|--------|
| id | Long | 否 | 主键自增 | 用户唯一标识 | 1 |
| name | String | 是 | 3-20字符，唯一 | 用户名 | "admin" |
| password | String | 是 | BCrypt加密 | 用户密码 | "$2a$10$..." |
| mail | String | 是 | 邮箱格式，唯一 | 用户邮箱 | "user@example.com" |
| groupId | Integer | 否 | 1-3 | 用户分组ID | 1 |
| avatar | Integer | 否 | 1-9，默认1 | 头像编号 | 3 |
| createTime | LocalDateTime | 否 | 自动生成 | 注册时间 | "2025-09-22T15:30:00" |
| updateTime | LocalDateTime | 否 | 自动更新 | 最后修改时间 | "2025-09-23T10:15:00" |

**安全说明:**
- 密码使用BCrypt算法加密存储
- 在JSON序列化时，password字段会被忽略
- 查询时需要手动清除密码信息

---

## 请求类 (Request)

### 1. RegisterRequest (注册请求)

**描述:** 用户注册时传递的参数

```java
public class RegisterRequest {
    private String name;        // 用户名
    private String password;    // 密码
    private String email;       // 邮箱
    private String code;        // 验证码
}
```

**字段说明:**

| 字段名 | 类型 | 是否必填 | 验证规则 | 说明 | 示例值 |
|--------|------|----------|----------|------|--------|
| name | String | 是 | 3-20字符 | 用户名，不能重复 | "newuser" |
| password | String | 是 | 6-20字符 | 密码 | "123456" |
| email | String | 是 | 邮箱格式 | 邮箱地址，不能重复 | "user@example.com" |
| code | String | 是 | 4位数字 | 邮箱验证码 | "1234" |

**JSON示例:**
```json
{
  "name": "newuser",
  "password": "123456",
  "email": "user@example.com",
  "code": "1234"
}
```

---

### 2. LoginByNameRequest (用户名登录请求)

**描述:** 用户使用用户名登录时传递的参数

```java
public class LoginByNameRequest {
    private String name;        // 用户名
    private String password;    // 密码
}
```

**字段说明:**

| 字段名 | 类型 | 是否必填 | 验证规则 | 说明 | 示例值 |
|--------|------|----------|----------|------|--------|
| name | String | 是 | 非空 | 用户名 | "admin" |
| password | String | 是 | 非空 | 密码 | "123456" |

**JSON示例:**
```json
{
  "name": "admin",
  "password": "123456"
}
```

---

### 3. LoginByEmailRequest (邮箱登录请求)

**描述:** 用户使用邮箱登录时传递的参数（预留接口）

```java
public class LoginByEmailRequest {
    private String email;       // 邮箱
    private String password;    // 密码
}
```

**字段说明:**

| 字段名 | 类型 | 是否必填 | 验证规则 | 说明 | 示例值 |
|--------|------|----------|----------|------|--------|
| email | String | 是 | 邮箱格式 | 邮箱地址 | "user@example.com" |
| password | String | 是 | 非空 | 密码 | "123456" |

**JSON示例:**
```json
{
  "email": "user@example.com",
  "password": "123456"
}
```

---

### 4. UpdatePasswordRequest (修改密码请求)

**描述:** 用户修改密码时传递的参数

```java
public class UpdatePasswordRequest {
    private String oldPassword;     // 原密码
    private String newPassword;     // 新密码
    private String confirmPassword; // 确认新密码
}
```

**字段说明:**

| 字段名 | 类型 | 是否必填 | 验证规则 | 说明 | 示例值 |
|--------|------|----------|----------|------|--------|
| oldPassword | String | 是 | 非空 | 原密码 | "123456" |
| newPassword | String | 是 | 6-20字符 | 新密码 | "654321" |
| confirmPassword | String | 是 | 必须与newPassword一致 | 确认新密码 | "654321" |

**JSON示例:**
```json
{
  "oldPassword": "123456",
  "newPassword": "654321",
  "confirmPassword": "654321"
}
```

---

## 响应类 (Response)

### 1. Result<T> (统一响应结果)

**描述:** 所有API接口的统一响应格式

```java
public class Result<T> {
    private Integer code;   // 状态码
    private String msg;     // 响应消息
    private T data;         // 响应数据
}
```

**字段说明:**

| 字段名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| code | Integer | 状态码：1=成功，0=失败 | 1 |
| msg | String | 响应消息，成功时可为null | "操作成功" |
| data | T | 响应数据，泛型类型 | {...} |

**成功响应示例:**
```json
{
  "code": 1,
  "msg": null,
  "data": {
    "id": 1,
    "name": "admin"
  }
}
```

**失败响应示例:**
```json
{
  "code": 0,
  "msg": "用户名或密码错误",
  "data": null
}
```

---

### 2. 登录响应数据结构

**描述:** 用户登录成功时返回的数据结构

```javascript
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",     // JWT访问令牌
  "tokenType": "Bearer",                   // 令牌类型
  "expiresIn": 7200,                       // 过期时间(秒)
  "user": {                                // 用户信息
    "id": 1,                               // 用户ID
    "name": "admin",                       // 用户名
    "avatar": 3,                           // 头像编号
    "createTime": "2025-09-22T15:30:00"    // 注册时间
  }
}
```

---

### 3. 用户信息响应数据结构

**描述:** 查询用户信息时返回的数据结构

```javascript
{
  "id": 1,                                   // 用户ID
  "name": "admin",                           // 用户名
  "password": "xxxx",                        // 密码(已隐藏)
  "mail": "admin@example.com",               // 邮箱
  "groupId": 1,                              // 分组ID
  "avatar": 3,                               // 头像编号
  "createTime": "2025-09-22T15:30:00",       // 注册时间
  "updateTime": "2025-09-23T10:15:00"        // 最后修改时间
}
```

---

### 4. 个人信息响应数据结构

**描述:** 获取个人信息时返回的数据结构（精简版）

```javascript
{
  "id": 1,                                   // 用户ID
  "name": "admin",                           // 用户名
  "avatar": 3,                               // 头像编号
  "createTime": "2025-09-22T15:30:00",       // 注册时间
  "updateTime": "2025-09-23T10:15:00"        // 最后修改时间
}
```

---

## 事件类 (Event)

### 1. EmailEvent (邮箱事件)

**描述:** 邮箱发送事件，用于异步发送邮件

```java
public class EmailEvent {
    private String toEmail;     // 目的地邮箱
    private String subject;     // 邮件主题  
    private String content;     // 邮件内容
    private int retryCount;     // 重试次数
}
```

**字段说明:**

| 字段名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| toEmail | String | 收件人邮箱地址 | "user@example.com" |
| subject | String | 邮件主题 | "【ITAHome】账号验证码通知" |
| content | String | 邮件内容(HTML格式) | "&lt;h3&gt;尊敬的用户：&lt;/h3&gt;..." |
| retryCount | int | 发送失败重试次数 | 0 |

---

## 数据验证规则

### 字段长度限制

| 字段类型 | 最小长度 | 最大长度 | 说明 |
|----------|----------|----------|------|
| 用户名 | 3 | 20 | 字符数限制 |
| 密码 | 6 | 20 | 字符数限制 |
| 验证码 | 4 | 4 | 固定4位数字 |
| 头像编号 | 1 | 9 | 数值范围限制 |

### 格式验证

| 字段类型 | 验证规则 | 说明 |
|----------|----------|------|
| 邮箱 | 标准邮箱格式 | 包含@符号，有效域名 |
| 密码 | 任意字符 | 建议包含字母数字 |
| 用户名 | 字母数字下划线 | 不允许特殊字符 |

### 唯一性约束

| 字段 | 约束类型 | 说明 |
|------|----------|------|
| 用户名 | 全局唯一 | 不能重复注册 |
| 邮箱 | 全局唯一 | 一个邮箱只能注册一个账号 |
| 用户ID | 主键 | 数据库自动生成 |

---

## 安全考虑

### 密码安全
- 使用BCrypt算法进行密码加密
- 密码强度建议：6-20字符，包含字母和数字
- 密码传输过程中需要HTTPS保护

### 数据脱敏
- 响应中密码字段自动隐藏或显示为"xxxx"
- 日志记录时不输出敏感信息
- JSON序列化时忽略敏感字段

### 验证码安全
- 验证码有效期：10分钟
- 验证码长度：4位数字
- 验证码一次性使用，验证后失效

---

## 数据库设计

### User表结构

```sql
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `name` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码(加密)',
  `mail` varchar(100) NOT NULL COMMENT '邮箱',
  `group_id` int(11) DEFAULT NULL COMMENT '分组ID',
  `avatar` int(11) DEFAULT 1 COMMENT '头像编号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  UNIQUE KEY `uk_mail` (`mail`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

---

## 前端集成指南

### TypeScript接口定义

```typescript
// 用户实体
interface User {
  id: number;
  name: string;
  mail: string;
  groupId?: number;
  avatar: number;
  createTime: string;
  updateTime: string;
}

// 注册请求
interface RegisterRequest {
  name: string;
  password: string;
  email: string;
  code: string;
}

// 登录请求
interface LoginRequest {
  name: string;
  password: string;
}

// 统一响应
interface ApiResponse<T = any> {
  code: number;
  msg?: string;
  data?: T;
}

// 登录响应
interface LoginResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}
```

### 使用示例

```typescript
// 用户注册
const registerUser = async (data: RegisterRequest): Promise<ApiResponse> => {
  const response = await fetch('/api/user/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  return response.json();
};

// 用户登录
const loginUser = async (data: LoginRequest): Promise<ApiResponse<LoginResponse>> => {
  const response = await fetch('/api/user/login/name', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  return response.json();
};
```

### 2. UserOj (用户OJ平台账号实体)

**表名:** `user_oj`  
**描述:** 用户OJ平台账号信息实体，对应数据库user_oj表，用于存储用户在各个OJ平台的账号信息

```java
public class UserOj {
    private Long id;                    // 主键ID - 自增
    private Long userId;                // 用户ID - 关联user表
    private String luoguUsername;       // 洛谷平台用户名
    private String leetcodeCnUsername;  // LeetCode中国站用户名
    private String nowcoderUserId;      // 牛客网用户ID
    private String codeforceUsername;   // Codeforces用户名
    private Integer totalAcNum;         // 四个平台AC数之和 (缓存字段)
    private Integer totalCommitNum;     // 四个平台提交数之和 (缓存字段)
    private LocalDateTime lastAccessTime; // 最后访问时间 (缓存字段)
    private LocalDateTime cacheTime;    // 数据缓存时间 (缓存字段)
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 修改时间
}
```

**字段详细说明:**

| 字段名 | 类型 | 是否必填 | 约束 | 说明 | 示例值 |
|--------|------|----------|------|------|--------|
| id | Long | 否 | 主键自增 | OJ账号唯一标识 | 1 |
| userId | Long | 是 | 外键关联user表 | 关联的用户ID | 123 |
| luoguUsername | String | 否 | - | 洛谷平台用户名 | "luogu_user123" |
| leetcodeCnUsername | String | 否 | - | LeetCode中国站用户名 | "leetcode_user123" |
| nowcoderUserId | String | 否 | - | 牛客网用户ID | "nowcoder123" |
| codeforceUsername | String | 否 | - | Codeforces用户名 | "cf_user123" |
| totalAcNum | Integer | 否 | 缓存字段 | 四个平台AC数之和 | 150 |
| totalCommitNum | Integer | 否 | 缓存字段 | 四个平台提交数之和 | 300 |
| lastAccessTime | LocalDateTime | 否 | 缓存相关 | 最后访问时间 | "2025-09-25T10:30:00" |
| cacheTime | LocalDateTime | 否 | 缓存相关 | 数据缓存时间 | "2025-09-25T10:30:00" |
| createTime | LocalDateTime | 否 | 自动生成 | 创建时间 | "2025-09-22T15:30:00" |
| updateTime | LocalDateTime | 否 | 自动更新 | 最后修改时间 | "2025-09-23T10:15:00" |

**设计说明:**
- 用户注册时自动创建对应的UserOj记录
- 平台用户名字段允许为空，用户可以选择性绑定
- totalAcNum和totalCommitNum为缓存字段，通过定时任务和实时接口更新
- lastAccessTime和cacheTime用于缓存策略控制

---

## 版本更新记录

| 版本 | 更新时间 | 更新内容 |
|------|----------|----------|
| v1.0 | 2025-09-23 | 初始版本，包含用户相关数据模型 |
| v1.1 | 2025-09-25 | 新增UserOj实体模型，支持OJ平台账号管理 |

---

## 注意事项

1. **时间格式**: 所有时间字段均使用ISO 8601格式 (yyyy-MM-ddTHH:mm:ss)
2. **编码格式**: 统一使用UTF-8编码
3. **空值处理**: null值在JSON中正常序列化为null
4. **大小写**: JSON字段名采用驼峰命名法
5. **数据类型**: 严格按照定义的数据类型进行传输和处理