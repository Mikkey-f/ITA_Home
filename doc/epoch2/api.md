# ITAHome Epoch2 - OJ平台数据接口文档

## 概述

本文档描述了ITAHome后端系统Epoch2阶段的OJ平台数据相关API接口，包括用户OJ数据获取、排名查询、账号管理等功能。

**基础信息:**
- 服务名称: ITAHome Backend OJ Module
- API版本: v2.0
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

## 用户OJ数据接口

### 1. 获取用户OJ综合数据

**接口描述:** 获取当前用户在各个OJ平台的综合数据，包含AC数、提交数等统计信息

- **请求方式:** `GET`
- **请求路径:** `/api/user/oj-data`
- **是否需要认证:** 是

**请求参数:** 无

**响应示例:**
```json
{
  "code": 1,
  "msg": "获取成功",
  "data": {
    "ojUserDataDtoList": [
      {
        "platform": "leetcode_cn",
        "username": "user123",
        "error": false,
        "data": {
          "solved": 120,
          "submissions": 250,
          "acceptanceRate": 48.0,
          "ranking": 15670
        }
      },
      {
        "platform": "luogu",
        "username": "luogu_user",
        "error": false,
        "data": {
          "solved": 85,
          "submissions": 180,
          "acceptanceRate": 47.2,
          "ranking": null
        }
      }
    ],
    "totalAc": 205,
    "totalSubmit": 430
  }
}
```

**响应字段说明:**

| 字段名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| ojUserDataDtoList | Array | 各平台详细数据列表 | - |
| platform | String | 平台代码 | "leetcode_cn" |
| username | String | 平台用户名 | "user123" |
| error | Boolean | 是否有错误 | false |
| data.solved | Integer | AC题目数 | 120 |
| data.submissions | Integer | 总提交数 | 250 |
| data.acceptanceRate | Double | 通过率 | 48.0 |
| data.ranking | Integer | 平台排名 | 15670 |
| totalAc | Integer | 所有平台AC数之和 | 205 |
| totalSubmit | Integer | 所有平台提交数之和 | 430 |

---

## 用户排名接口

### 2. 获取用户刷题排名（分页）

**接口描述:** 根据AC数和提交数获取用户排名列表，支持分页查询

- **请求方式:** `POST`
- **请求路径:** `/api/user-oj/ranking`
- **是否需要认证:** 是

**请求参数:**

| 参数名 | 类型 | 必填 | 约束 | 说明 | 示例 |
|--------|------|------|------|------|------|
| pageNum | Integer | 是 | ≥1 | 页码 | 1 |
| pageSize | Integer | 是 | 1-100 | 每页大小 | 20 |
| onlyActiveUsers | Boolean | 否 | - | 是否只显示有数据的用户 | true |

**请求示例:**
```json
{
  "pageNum": 1,
  "pageSize": 20,
  "onlyActiveUsers": true
}
```

**响应示例:**
```json
{
  "code": 1,
  "msg": "获取成功",
  "data": {
    "rankings": [
      {
        "rank": 1,
        "userId": 123,
        "username": "编程高手",
        "totalAc": 500,
        "totalSubmit": 800,
        "acRate": 62.5,
        "lastUpdateTime": "2025-09-25T10:30:00"
      },
      {
        "rank": 2,
        "userId": 456,
        "username": "算法达人",
        "totalAc": 480,
        "totalSubmit": 750,
        "acRate": 64.0,
        "lastUpdateTime": "2025-09-25T09:15:00"
      }
    ],
    "total": 1000,
    "pageNum": 1,
    "pageSize": 20,
    "totalPages": 50,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

**排名规则:**
- 按AC数从高到低排序
- AC数相同时，按提交数从低到高排序
- 提交数越少说明效率越高

### 3. 获取指定用户排名

**接口描述:** 获取指定用户在排行榜中的位置和详细信息

- **请求方式:** `GET`
- **请求路径:** `/api/user-oj/ranking/{userId}`
- **是否需要认证:** 是

**路径参数:**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| userId | Long | 是 | 用户ID | 123 |

**响应示例:**
```json
{
  "code": 1,
  "msg": "获取成功",
  "data": {
    "rank": 15,
    "userId": 123,
    "username": "用户名",
    "totalAc": 280,
    "totalSubmit": 520,
    "acRate": 53.8,
    "lastUpdateTime": "2025-09-25T10:30:00"
  }
}
```

---

## 用户OJ账号管理接口

### 4. 更新用户OJ账号

**接口描述:** 更新当前用户在指定OJ平台的账号信息

- **请求方式:** `PUT`
- **请求路径:** `/api/user-oj/update`
- **是否需要认证:** 是

**请求参数:**

| 参数名 | 类型 | 必填 | 约束 | 说明 | 示例 |
|--------|------|------|------|------|------|
| platformType | String | 是 | - | 平台类型 | "leetcode" |
| value | String | 是 | 非空 | 平台用户名/ID | "user123" |

**支持的平台类型:**
- `leetcode`: LeetCode中国站
- `luogu`: 洛谷
- `codeforces`: Codeforces
- `nowcoder`: 牛客网

**请求示例:**
```json
{
  "platformType": "leetcode",
  "value": "user123"
}
```

**响应示例:**
```json
{
  "code": 1,
  "msg": "更新OJ账号成功",
  "data": null
}
```

---

## 缓存机制说明

### 数据缓存策略

**二级缓存架构:**
1. **L1缓存**: Caffeine本地内存缓存（6小时过期）
2. **L2缓存**: 数据库持久化缓存（通过cacheTime字段控制）

**缓存读取流程:**
1. 检查Caffeine缓存，命中则直接返回
2. 检查数据库缓存是否有效（6小时内）
3. 缓存无效时调用外部API获取实时数据
4. 异步更新数据库和Caffeine缓存

**缓存更新机制:**
- 定时任务每天凌晨2点更新活跃用户数据
- 用户访问时异步更新lastAccessTime
- 缓存失效时实时获取并异步更新

---

## 外部API依赖

### OJHunt API

**基础URL:** `${ita.oj.target}`（通过配置文件设置）

**接口格式:** `/{platform}/{username}`

**支持平台:**
- `leetcode_cn`: LeetCode中国站
- `luogu`: 洛谷  
- `codeforces`: Codeforces
- `nowcoder`: 牛客网

**响应格式:**
```json
{
  "error": false,
  "data": {
    "solved": 120,
    "submissions": 250,
    "acceptanceRate": 48.0,
    "ranking": 15670
  }
}
```

---

## 错误处理

### 常见错误码

| 错误信息 | 说明 | 解决方案 |
|----------|------|----------|
| "用户排名信息不存在" | 用户没有OJ数据 | 先绑定OJ账号并等待数据同步 |
| "无效的平台类型" | 不支持的平台类型 | 使用支持的平台类型 |
| "Oj表没有成功创建用户表项" | UserOj记录不存在 | 联系管理员检查数据一致性 |
| "获取用户排名失败" | 排名查询异常 | 稍后重试或联系管理员 |

### 外部API异常处理

- API调用超时（10秒）自动跳过该平台
- API返回错误不影响其他平台数据获取
- 异步更新失败不影响用户查询响应

---

## 性能说明

### 响应时间

| 接口 | 缓存命中 | 缓存失效 | 说明 |
|------|----------|----------|------|
| 获取OJ数据 | <100ms | 3-8s | 失效时需调用外部API |
| 获取排名 | <50ms | <200ms | 数据库查询 |
| 更新账号 | <100ms | <100ms | 数据库操作 |

### 并发处理

- 使用本地锁防止同一用户的并发更新
- 异步处理不阻塞用户请求
- 线程池`ojApiExecutorService`处理外部API调用

---

## 版本更新记录

| 版本 | 更新时间 | 更新内容 |
|------|----------|----------|
| v2.0 | 2025-09-25 | 初始版本，包含OJ数据获取和排名功能 |

---

## 联系方式

如有疑问，请联系开发团队。

**最后更新**: 2025-09-25