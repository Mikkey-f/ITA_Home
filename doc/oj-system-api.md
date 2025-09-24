# OJ平台数据同步系统 API 文档

## 概述

这个系统允许用户绑定多个OJ（Online Judge）平台的账号，并同步获取解题数据。系统支持以下平台：

- **LeetCode中国站** (platformType: 1, code: leetcode_cn)
- **洛谷** (platformType: 2, code: luogu)  
- **Codeforces** (platformType: 3, code: codeforces)
- **牛客网** (platformType: 4, code: nowcoder)

## 数据来源

系统通过调用 [OJHunt API](https://ojhunt.com) 获取用户在各个平台的解题数据：
```
https://ojhunt.com/api/crawlers/{平台代码}/{用户名或用户ID}
```

## API 接口

### 1. 获取支持的OJ平台列表

**接口:** `GET /api/user-oj/platforms`

**描述:** 获取系统支持的所有OJ平台信息

**响应示例:**
```json
{
    "code": 1,
    "data": [
        {
            "platformId": 1,
            "platformCode": "leetcode_cn", 
            "platformName": "LeetCode中国站"
        },
        {
            "platformId": 2,
            "platformCode": "luogu",
            "platformName": "洛谷"
        },
        {
            "platformId": 3,
            "platformCode": "codeforces",
            "platformName": "Codeforces"
        },
        {
            "platformId": 4,
            "platformCode": "nowcoder",
            "platformName": "牛客网"
        }
    ]
}
```

### 2. 添加OJ账号

**接口:** `POST /api/user-oj/add`

**认证:** 需要登录

**请求体:**
```json
{
    "platformType": 1,
    "ojUsername": "your_username",
    "ojUserId": "your_user_id"
}
```

**参数说明:**
- `platformType`: 平台类型（1-4）
- `ojUsername`: OJ平台用户名（可选）
- `ojUserId`: OJ平台用户ID（可选）
- 注意：`ojUsername` 和 `ojUserId` 至少需要提供一个

**响应示例:**
```json
{
    "code": 1,
    "data": "添加OJ账号成功"
}
```

### 3. 获取用户的OJ账号列表

**接口:** `GET /api/user-oj/list`

**认证:** 需要登录

**响应示例:**
```json
{
    "code": 1,
    "data": [
        {
            "id": 1,
            "userId": 1,
            "platformType": 1,
            "ojUsername": "your_username",
            "ojUserId": null,
            "enabled": true,
            "lastSyncTime": "2025-09-24T10:30:00",
            "createTime": "2025-09-24T09:00:00",
            "updateTime": "2025-09-24T10:30:00"
        }
    ]
}
```

### 4. 更新OJ账号信息

**接口:** `PUT /api/user-oj/update`

**认证:** 需要登录

**请求体:**
```json
{
    "platformType": 1,
    "ojUsername": "new_username",
    "ojUserId": "new_user_id",
    "enabled": true
}
```

**响应示例:**
```json
{
    "code": 1,
    "data": "更新OJ账号成功"
}
```

### 5. 删除OJ账号

**接口:** `DELETE /api/user-oj/delete/{platformType}`

**认证:** 需要登录

**路径参数:**
- `platformType`: 要删除的平台类型

**响应示例:**
```json
{
    "code": 1,
    "data": "删除OJ账号成功"
}
```

### 6. 同步OJ数据

**接口:** `POST /api/user-oj/sync/{platformType}`

**认证:** 需要登录

**描述:** 从OJHunt API获取指定平台的最新解题数据

**路径参数:**
- `platformType`: 要同步的平台类型

**响应示例:**
```json
{
    "code": 1,
    "data": {
        "error": false,
        "data": {
            "solved": 97,
            "submissions": 363,
            "solvedList": [
                "2116A",
                "105487A",
                "11D",
                "148D",
                "607B",
                "55B"
            ]
        }
    }
}
```

### 7. 启用/禁用OJ账号

**接口:** `PUT /api/user-oj/toggle/{platformType}?enabled={true|false}`

**认证:** 需要登录

**路径参数:**
- `platformType`: 平台类型

**查询参数:**
- `enabled`: 是否启用（true/false）

**响应示例:**
```json
{
    "code": 1,
    "data": "启用OJ账号成功"
}
```

## 错误码说明

- `code: 1` - 成功
- `code: 0` - 失败，具体错误信息在 `msg` 字段中

## 使用场景

### 场景1：用户首次绑定OJ账号

1. 调用 `GET /api/user-oj/platforms` 获取支持的平台
2. 调用 `POST /api/user-oj/add` 添加OJ账号
3. 调用 `POST /api/user-oj/sync/{platformType}` 同步数据

### 场景2：查看已绑定的账号和数据

1. 调用 `GET /api/user-oj/list` 获取所有已绑定的OJ账号
2. 对每个账号调用 `POST /api/user-oj/sync/{platformType}` 获取最新数据

### 场景3：更新账号信息

1. 调用 `PUT /api/user-oj/update` 更新用户名或用户ID
2. 调用 `POST /api/user-oj/sync/{platformType}` 验证更新是否有效

## 注意事项

1. **认证要求**: 除获取平台列表外，所有接口都需要用户登录认证
2. **唯一性约束**: 每个用户在同一平台只能绑定一个账号
3. **用户标识**: 添加账号时，用户名和用户ID至少需要提供一个
4. **数据同步**: 系统会自动更新最后同步时间
5. **错误处理**: 如果OJHunt API返回错误或网络异常，同步会失败
6. **频率限制**: 为避免对OJHunt API造成压力，建议适当控制同步频率

## 数据库表结构

```sql
CREATE TABLE `user_oj` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `platform_type` INT NOT NULL,
    `oj_username` VARCHAR(100) NULL,
    `oj_user_id` VARCHAR(100) NULL,
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    `last_sync_time` DATETIME NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_platform` (`user_id`, `platform_type`)
);
```