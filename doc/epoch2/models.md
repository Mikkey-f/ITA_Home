# ITAHome Epoch2 - OJ平台数据模型文档

## 概述

本文档描述了ITAHome后端系统Epoch2阶段中新增的OJ平台相关数据模型，包括请求类、响应类、DTO类等，为前端开发和数据交互提供详细的数据结构参考。

---

## 请求类 (Request)

### 1. RankingRequest (排名查询请求)

**描述:** 用于查询用户排名的请求参数

```java
public class RankingRequest {
    private Integer pageNum = 1;        // 页码，默认1
    private Integer pageSize = 20;      // 每页大小，默认20
    private Boolean onlyActiveUsers = true; // 是否只显示有数据的用户，默认true
}
```

**字段详细说明:**

| 字段名 | 类型 | 是否必填 | 约束 | 说明 | 示例值 |
|--------|------|----------|------|------|--------|
| pageNum | Integer | 否 | ≥1 | 页码，从1开始 | 1 |
| pageSize | Integer | 否 | 1-100 | 每页记录数 | 20 |
| onlyActiveUsers | Boolean | 否 | - | 是否只显示有AC数据的用户 | true |

**校验规则:**
- pageNum不能小于1
- pageSize不能小于1且不能大于100

### 2. UpdateUserOjRequest (更新OJ账号请求)

**描述:** 用于更新用户OJ平台账号信息的请求参数

```java
public class UpdateUserOjRequest {
    private String platformType;    // 平台类型
    private String value;           // 平台用户名或ID
}
```

**字段详细说明:**

| 字段名 | 类型 | 是否必填 | 约束 | 说明 | 示例值 |
|--------|------|----------|------|------|--------|
| platformType | String | 是 | 非空 | 平台类型标识 | "leetcode" |
| value | String | 是 | 非空 | 平台用户名或用户ID | "user123" |

**支持的平台类型:**
- `leetcode`: LeetCode中国站
- `luogu`: 洛谷
- `codeforces`: Codeforces  
- `nowcoder`: 牛客网

---

## 响应类 (Response/VO)

### 1. OjUserDataVo (OJ用户数据响应)

**描述:** 用户OJ平台综合数据的响应对象

```java
public class OjUserDataVo {
    private List<OjUserDataDto> ojUserDataDtoList; // 各平台详细数据列表
    private Integer totalAc;                       // 总AC数
    private Integer totalSubmit;                   // 总提交数
}
```

**字段详细说明:**

| 字段名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| ojUserDataDtoList | List<OjUserDataDto> | 各个OJ平台的详细数据 | - |
| totalAc | Integer | 所有平台AC数之和 | 205 |
| totalSubmit | Integer | 所有平台提交数之和 | 430 |

### 2. RankingPageVo (排名分页响应)

**描述:** 分页排名查询的响应对象

```java
public class RankingPageVo {
    private List<UserRankingVo> rankings;  // 排名列表
    private Long total;                    // 总记录数
    private Integer pageNum;               // 当前页码
    private Integer pageSize;              // 每页大小
    private Integer totalPages;            // 总页数
    private Boolean hasNext;               // 是否有下一页
    private Boolean hasPrevious;           // 是否有上一页
}
```

**字段详细说明:**

| 字段名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| rankings | List<UserRankingVo> | 当前页的排名数据 | - |
| total | Long | 符合条件的总记录数 | 1000 |
| pageNum | Integer | 当前页码 | 1 |
| pageSize | Integer | 每页大小 | 20 |
| totalPages | Integer | 总页数 | 50 |
| hasNext | Boolean | 是否有下一页 | true |
| hasPrevious | Boolean | 是否有上一页 | false |

### 3. UserRankingVo (用户排名信息)

**描述:** 单个用户排名信息的响应对象

```java
public class UserRankingVo {
    private Integer rank;                      // 排名
    private Long userId;                       // 用户ID
    private String username;                   // 用户名
    private Integer totalAc;                   // 总AC数
    private Integer totalSubmit;               // 总提交数
    private Double acRate;                     // AC率（百分比）
    private LocalDateTime lastUpdateTime;      // 最后更新时间
}
```

**字段详细说明:**

| 字段名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| rank | Integer | 用户排名（基于AC数和提交数） | 15 |
| userId | Long | 用户唯一标识 | 123 |
| username | String | 用户名 | "编程高手" |
| totalAc | Integer | 总AC题目数 | 280 |
| totalSubmit | Integer | 总提交次数 | 520 |
| acRate | Double | AC通过率，保留2位小数 | 53.85 |
| lastUpdateTime | LocalDateTime | 数据最后更新时间 | "2025-09-25T10:30:00" |

**排名计算规则:**
1. 主要按totalAc从高到低排序
2. totalAc相同时，按totalSubmit从低到高排序
3. 提交数越少表示解题效率越高

---

## 数据传输对象 (DTO)

### 1. OjUserDataDto (OJ平台数据传输对象)

**描述:** 单个OJ平台的用户数据

```java
public class OjUserDataDto {
    private String platform;       // 平台标识
    private String username;        // 平台用户名
    private Boolean error;          // 是否有错误
    private UserData data;          // 用户数据详情
    
    @Data
    @Builder
    public static class UserData {
        private Integer solved;         // AC题目数
        private Integer submissions;    // 总提交数
        private Double acceptanceRate;  // 通过率
        private Integer ranking;        // 平台排名
    }
}
```

**字段详细说明:**

| 字段名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| platform | String | OJ平台标识码 | "leetcode_cn" |
| username | String | 用户在该平台的用户名 | "user123" |
| error | Boolean | 数据获取是否出错 | false |
| data | UserData | 用户在该平台的详细数据 | - |
| data.solved | Integer | AC（通过）的题目数 | 120 |
| data.submissions | Integer | 总提交次数 | 250 |
| data.acceptanceRate | Double | 通过率百分比 | 48.0 |
| data.ranking | Integer | 在该平台的排名 | 15670 |

**平台标识码说明:**
- `leetcode_cn`: LeetCode中国站
- `luogu`: 洛谷
- `codeforces`: Codeforces
- `nowcoder`: 牛客网

### 2. UserRankingDto (用户排名数据传输对象)

**描述:** 用于内部数据传输的用户排名信息

```java
public class UserRankingDto {
    private Long userId;                    // 用户ID
    private String username;                // 用户名
    private Integer totalAc;                // 总AC数
    private Integer totalSubmit;            // 总提交数
    private LocalDateTime lastUpdateTime;   // 最后更新时间
}
```

**字段详细说明:**

| 字段名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| userId | Long | 用户唯一标识 | 123 |
| username | String | 用户名 | "编程高手" |
| totalAc | Integer | 总AC题目数 | 280 |
| totalSubmit | Integer | 总提交次数 | 520 |
| lastUpdateTime | LocalDateTime | 数据最后更新时间 | "2025-09-25T10:30:00" |

**注意事项:**
- 此DTO不包含rank字段，排名在Service层计算
- 用于Mapper层与Service层之间的数据传输

---

## 枚举类 (Enum)

### 1. OjPlatformEnum (OJ平台枚举)

**描述:** 支持的OJ平台枚举定义

```java
public enum OjPlatformEnum {
    LEETCODE_CN("leetcode", "LeetCode中国站"),
    LUOGU("luogu", "洛谷"),
    CODEFORCES("codeforces", "Codeforces"),
    NOWCODER("nowcoder", "牛客网");
    
    private final String platformId;    // 平台标识
    private final String platformName;  // 平台名称
}
```

**枚举值说明:**

| 枚举值 | platformId | platformName | 说明 |
|--------|------------|--------------|------|
| LEETCODE_CN | "leetcode" | "LeetCode中国站" | 力扣中国站 |
| LUOGU | "luogu" | "洛谷" | 洛谷OJ平台 |
| CODEFORCES | "codeforces" | "Codeforces" | CF竞赛平台 |
| NOWCODER | "nowcoder" | "牛客网" | 牛客刷题平台 |

**方法:**
- `getByPlatformId(String platformId)`: 根据平台ID获取枚举值
- `getPlatformId()`: 获取平台标识
- `getPlatformName()`: 获取平台名称

---

## 缓存相关配置类

### 1. OjCacheProperties (OJ缓存配置)

**描述:** OJ数据缓存相关的配置属性

```java
@ConfigurationProperties(prefix = "ita.cache.oj")
public class OjCacheProperties {
    private Long maxSize = 1000L;           // 缓存最大条目数
    private Integer expireHours = 6;        // 缓存过期时间（小时）
    private Integer activeUserDays = 7;     // 活跃用户定义（天数）
    private Integer asyncUpdateTimeoutSeconds = 30; // 异步更新超时时间（秒）
    private Integer batchSize = 50;         // 定时任务批次大小
}
```

**字段详细说明:**

| 字段名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| maxSize | Long | 1000 | Caffeine缓存最大条目数 |
| expireHours | Integer | 6 | 缓存过期时间（小时） |
| activeUserDays | Integer | 7 | 活跃用户定义（最近N天有访问） |
| asyncUpdateTimeoutSeconds | Integer | 30 | 异步更新操作超时时间 |
| batchSize | Integer | 50 | 定时任务每批处理的用户数 |

---

## 前端TypeScript接口定义

### 基础类型定义

```typescript
// OJ用户数据接口
interface OjUserDataVo {
  ojUserDataDtoList: OjUserDataDto[];
  totalAc: number;
  totalSubmit: number;
}

// OJ平台数据接口
interface OjUserDataDto {
  platform: string;
  username: string;
  error: boolean;
  data: {
    solved: number;
    submissions: number;
    acceptanceRate: number;
    ranking: number | null;
  };
}

// 排名查询请求接口
interface RankingRequest {
  pageNum: number;
  pageSize: number;
  onlyActiveUsers?: boolean;
}

// 排名分页响应接口
interface RankingPageVo {
  rankings: UserRankingVo[];
  total: number;
  pageNum: number;
  pageSize: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

// 用户排名信息接口
interface UserRankingVo {
  rank: number;
  userId: number;
  username: string;
  totalAc: number;
  totalSubmit: number;
  acRate: number;
  lastUpdateTime: string;
}

// 更新OJ账号请求接口
interface UpdateUserOjRequest {
  platformType: 'leetcode' | 'luogu' | 'codeforces' | 'nowcoder';
  value: string;
}
```

### 使用示例

```typescript
// 获取用户OJ数据
const getUserOjData = async (): Promise<ApiResponse<OjUserDataVo>> => {
  const response = await fetch('/api/user/oj-data', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  return response.json();
};

// 获取排名列表
const getUserRanking = async (request: RankingRequest): Promise<ApiResponse<RankingPageVo>> => {
  const response = await fetch('/api/user-oj/ranking', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(request)
  });
  return response.json();
};

// 更新OJ账号
const updateOjAccount = async (request: UpdateUserOjRequest): Promise<ApiResponse> => {
  const response = await fetch('/api/user-oj/update', {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(request)
  });
  return response.json();
};
```

---

## 数据库表结构变更

### UserOj表新增字段

**新增的缓存相关字段:**

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| total_ac_num | INT | NULL | 四个平台AC数之和 |
| total_commit_num | INT | NULL | 四个平台提交数之和 |
| last_access_time | DATETIME | NULL | 最后访问时间 |
| cache_time | DATETIME | NULL | 数据缓存时间 |

**索引:**
```sql
CREATE INDEX idx_last_access_time ON user_oj(last_access_time);
CREATE INDEX idx_cache_time ON user_oj(cache_time);
CREATE INDEX idx_total_ac_num ON user_oj(total_ac_num);
```

---

## 版本更新记录

| 版本 | 更新时间 | 更新内容 |
|------|----------|----------|
| v2.0 | 2025-09-25 | 初始版本，包含OJ平台数据相关的完整数据模型 |

---

## 注意事项

1. **时间格式**: 所有时间字段均使用ISO 8601格式 (yyyy-MM-ddTHH:mm:ss)
2. **编码格式**: 统一使用UTF-8编码
3. **空值处理**: null值在JSON中正常序列化为null
4. **大小写**: JSON字段名采用驼峰命名法
5. **数据类型**: 严格按照定义的数据类型进行传输和处理
6. **缓存字段**: total_ac_num和total_commit_num为缓存字段，可能为null
7. **排名计算**: 排名在Service层动态计算，不存储在数据库中
8. **异步更新**: 缓存更新为异步操作，不影响用户查询响应

---

**最后更新**: 2025-09-25