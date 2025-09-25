# ITAHome Epoch2 - 二级缓存策略文档

## 概述

本文档详细描述了ITAHome后端系统Epoch2阶段OJ数据的二级缓存架构设计，包括缓存层次结构、失效策略、更新机制以及性能优化建议。

**设计目标:**
- 提高OJ数据查询响应速度
- 降低外部API调用频率
- 减少数据库访问压力
- 保证数据的相对新鲜度
- 适应2CPU2G的服务器环境

---

## 缓存架构总览

### 二级缓存层次结构

```
用户请求
    ↓
┌─────────────────┐
│   L1: Caffeine  │ ← 本地内存缓存 (6小时TTL)
│   本地内存缓存    │
└─────────────────┘
    ↓ (Cache Miss)
┌─────────────────┐
│   L2: Database  │ ← 数据库持久化缓存 (通过cacheTime控制)
│   数据库缓存      │
└─────────────────┘
    ↓ (Cache Miss or Expired)
┌─────────────────┐
│  External API   │ ← 外部OJ平台API调用
│  外部API获取     │
└─────────────────┘
    ↓
┌─────────────────┐
│ Async Update    │ ← 异步更新L1+L2缓存
│ 异步缓存更新      │
└─────────────────┘
```

### 核心设计原则

1. **读取优先级**: L1 → L2 → External API
2. **写入策略**: 异步更新，不阻塞用户请求
3. **故障降级**: 单层失效不影响其他层
4. **数据一致性**: 最终一致性，容忍短期数据延迟

---

## L1缓存: Caffeine本地内存缓存

### 基础配置

```java
@Bean
@Primary
public Cache<String, OjUserDataVo> ojDataCache(OjCacheProperties properties) {
    return Caffeine.newBuilder()
            .maximumSize(properties.getMaxSize())           // 最大1000条记录
            .expireAfterWrite(properties.getExpireHours(), TimeUnit.HOURS) // 6小时过期
            .recordStats()                                   // 开启统计
            .removalListener((key, value, cause) -> {
                log.debug("L1缓存被移除: key={}, cause={}", key, cause);
            })
            .build();
}
```

### 配置参数详解

| 参数 | 默认值 | 说明 | 调优建议 |
|------|--------|------|----------|
| maximumSize | 1000 | 最大缓存条目数 | 根据用户活跃度调整，1000可支撑中等规模应用 |
| expireAfterWrite | 6小时 | 写入后过期时间 | 平衡数据新鲜度与API调用频率 |
| recordStats | true | 开启性能统计 | 生产环境建议开启，便于监控 |

### 缓存键策略

```java
private String getCacheKey(Long userId) {
    return "oj_data:" + userId;
}
```

**键命名规范:**
- 格式: `oj_data:{userId}`
- 示例: `oj_data:123`
- 优势: 简单易理解，支持快速查找

### 内存占用估算

**单条记录大小计算:**
```
OjUserDataVo对象 ≈ 2KB (包含4个平台数据)
1000条记录 ≈ 2MB
加上Caffeine开销 ≈ 3-5MB总内存占用
```

**适用场景:**
- 2G内存服务器完全可以承受
- 对比Redis节省网络IO开销
- 应用重启后需要重建缓存

---

## L2缓存: 数据库持久化缓存

### 设计思路

L2缓存通过在UserOj表中添加汇总字段实现数据库层面的缓存:

```sql
-- 新增的缓存字段
ALTER TABLE user_oj ADD COLUMN total_ac_num INT NULL COMMENT '四个平台AC数之和';
ALTER TABLE user_oj ADD COLUMN total_commit_num INT NULL COMMENT '四个平台提交数之和';
ALTER TABLE user_oj ADD COLUMN cache_time DATETIME NULL COMMENT '数据缓存时间';
ALTER TABLE user_oj ADD COLUMN last_access_time DATETIME NULL COMMENT '最后访问时间';
```

### 缓存有效性判断

```java
private boolean isDatabaseCacheValid(UserOj userOj) {
    if (userOj.getCacheTime() == null || 
        userOj.getTotalAcNum() == null || 
        userOj.getTotalCommitNum() == null) {
        return false;
    }
    
    LocalDateTime expireTime = userOj.getCacheTime()
            .plusHours(cacheProperties.getExpireHours());
    return LocalDateTime.now().isBefore(expireTime);
}
```

**判断逻辑:**
1. 检查必要字段是否为null
2. 检查cacheTime是否超过配置的过期时间（默认6小时）
3. 满足条件则认为缓存有效

### 缓存更新策略

**同步更新场景:**
- 用户首次访问且无任何缓存
- 实时数据获取后立即更新数据库

**异步更新场景:**
- 用户访问时更新lastAccessTime
- 定时任务批量更新活跃用户数据
- 缓存失效时的数据刷新

```java
@Async("ojApiExecutorService")
public CompletableFuture<Boolean> updateUserOjDataAsync(Long userId) {
    // 异步更新数据库缓存的具体实现
    // ...
}
```

### 索引优化

```sql
-- 缓存查询优化索引
CREATE INDEX idx_user_oj_cache_time ON user_oj(cache_time);
CREATE INDEX idx_user_oj_last_access ON user_oj(last_access_time);
CREATE INDEX idx_user_oj_total_ac ON user_oj(total_ac_num);

-- 复合索引用于排名查询
CREATE INDEX idx_user_oj_ranking ON user_oj(total_ac_num DESC, total_commit_num ASC);
```

---

## 外部API调用层

### API调用策略

**并行调用设计:**
```java
// 为每个平台创建异步任务
for (Map.Entry<String, String> entry : platformUserMap.entrySet()) {
    String platformCode = entry.getKey();
    String username = entry.getValue();
    
    CompletableFuture<OjUserDataDto> future = CompletableFuture.supplyAsync(() -> {
        return fetchSinglePlatformData(platformCode, username);
    }, executorService)
    .orTimeout(10, TimeUnit.SECONDS)  // 10秒超时
    .exceptionally(throwable -> {
        log.error("平台{}数据获取失败，用户名：{}", platformCode, username, throwable);
        return null; // 超时/异常时返回null
    });
    
    futures.add(future);
}
```

**关键特性:**
- **并行执行**: 4个平台同时调用，提高效率
- **超时保护**: 单个平台10秒超时，避免长时间等待
- **异常隔离**: 单个平台失败不影响其他平台
- **线程池**: 使用专用线程池`ojApiExecutorService`

### 外部API配置

```yaml
# application.yml
ita:
  oj:
    target: "https://ojhunt-api.example.com/api"  # 外部API基础URL
```

**API调用格式:**
- URL模板: `{baseUrl}/{platform}/{username}`
- 示例: `https://ojhunt-api.example.com/api/leetcode_cn/user123`

### 错误处理机制

```java
private OjUserDataDto fetchSinglePlatformData(String platformCode, String username) {
    try {
        String apiUrl = String.format("%s/%s/%s", OJ_HUNT_API_BASE_URL, platformCode, username);
        OjUserDataDto response = restTemplate.getForObject(apiUrl, OjUserDataDto.class);
        
        if (response != null && Boolean.FALSE.equals(response.getError())) {
            return response;
        } else {
            log.warn("{}平台API返回错误: user={}", platformCode, username);
            return null;
        }
    } catch (Exception e) {
        log.error("调用{}平台API失败: user={}", platformCode, username, e);
        return null;
    }
}
```

**容错策略:**
- API调用失败返回null，不影响其他平台
- 记录详细的错误日志便于排查
- 超时和异常统一处理

---

## 缓存更新机制

### 读取流程详解

```java
public OjUserDataVo getCacheOjUserDataVo(Long userId) {
    String cacheKey = "oj_data:" + userId;
    
    // 1. 检查L1缓存 (Caffeine)
    OjUserDataVo cachedData = ojDataCache.getIfPresent(cacheKey);
    if (cachedData != null) {
        log.info("L1缓存命中: userId={}", userId);
        asyncOjUpdateService.updateLastAccessTimeAsync(userId);
        return cachedData;
    }
    
    // 2. 检查L2缓存 (Database)
    UserOj userOj = userOjMapper.findByUserId(userId);
    if (userOj != null && isDatabaseCacheValid(userOj)) {
        log.info("L2缓存命中: userId={}", userId);
        OjUserDataVo dbCachedData = buildVoFromDatabase(userOj);
        ojDataCache.put(cacheKey, dbCachedData);  // 回填L1缓存
        asyncOjUpdateService.updateLastAccessTimeAsync(userId);
        return dbCachedData;
    }
    
    // 3. 缓存失效，获取实时数据
    log.info("缓存失效，获取实时数据: userId={}", userId);
    OjUserDataVo realTimeData = getRealTimeData(userOj);
    
    // 4. 更新缓存
    ojDataCache.put(cacheKey, realTimeData);  // 更新L1
    asyncOjUpdateService.updateUserOjDataAsync(userId);  // 异步更新L2
    
    return realTimeData;
}
```

### 写入流程详解

**异步写入设计:**
```java
@Async("ojApiExecutorService")
public CompletableFuture<Boolean> updateUserOjDataAsync(Long userId) {
    String lockKey = localLockService.getUpdateLockKey(userId);
    
    // 获取分布式锁，防止并发更新
    if (!localLockService.tryLock(lockKey)) {
        log.info("用户{}正在更新中，跳过本次更新", userId);
        return CompletableFuture.completedFuture(false);
    }
    
    try {
        // 1. 获取实时数据
        OjUserDataVo realTimeData = fetchRealTimeData(userId);
        
        // 2. 更新数据库
        boolean dbUpdateSuccess = updateDatabase(userId, realTimeData);
        
        // 3. 更新L1缓存
        if (dbUpdateSuccess) {
            String cacheKey = getCacheKey(userId);
            ojDataCache.put(cacheKey, realTimeData);
            log.info("用户{}缓存更新成功", userId);
            return CompletableFuture.completedFuture(true);
        }
        
        return CompletableFuture.completedFuture(false);
    } finally {
        localLockService.releaseLock(lockKey);
    }
}
```

### 并发控制

**本地锁实现:**
```java
@Service
public class LocalLockService {
    private final Cache<String, String> lockCache;
    
    public boolean tryLock(String lockKey) {
        String existing = lockCache.getIfPresent(lockKey);
        if (existing == null) {
            lockCache.put(lockKey, Thread.currentThread().getName());
            return true;
        }
        return false;
    }
    
    public void releaseLock(String lockKey) {
        lockCache.invalidate(lockKey);
    }
}
```

**锁机制特点:**
- 基于Caffeine实现的轻量级本地锁
- 锁超时时间30分钟，防止锁泄露
- 每个用户一个锁，避免全局锁竞争

---

## 定时任务缓存刷新

### 任务调度配置

```java
@Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2点执行
public void refreshActiveUsersData() {
    log.info("开始刷新活跃用户OJ数据");
    
    LocalDateTime activeTime = LocalDateTime.now()
            .minusDays(cacheProperties.getActiveUserDays());
    
    List<Long> activeUserIds = userOjMapper.findActiveUserIds(activeTime);
    log.info("找到{}个活跃用户需要刷新", activeUserIds.size());
    
    // 分批处理，每批50个用户
    List<List<Long>> batches = partition(activeUserIds, cacheProperties.getBatchSize());
    
    for (int i = 0; i < batches.size(); i++) {
        List<Long> batch = batches.get(i);
        processBatch(batch);
        
        // 批次间暂停5秒，避免系统压力过大
        if (i < batches.size() - 1) {
            Thread.sleep(5000);
        }
    }
}
```

### 活跃用户定义

**活跃用户筛选逻辑:**
```sql
SELECT user_id FROM user_oj 
WHERE last_access_time >= #{activeTime}
ORDER BY last_access_time DESC
```

**活跃度分级:**
- **高活跃**: 最近24小时访问 → 缓存保持6小时
- **中活跃**: 最近7天访问 → 定时任务每日更新  
- **低活跃**: 7天以上未访问 → 不主动更新，按需刷新

### 批量处理策略

```java
private void processBatch(List<Long> userIds) {
    List<CompletableFuture<Boolean>> futures = userIds.stream()
            .map(asyncOjUpdateService::updateUserOjDataAsync)
            .collect(Collectors.toList());
    
    // 等待当前批次完成
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    
    // 统计成功率
    long successCount = futures.stream()
            .mapToLong(future -> {
                try {
                    return future.get() ? 1 : 0;
                } catch (Exception e) {
                    return 0;
                }
            })
            .sum();
    
    log.info("批次处理完成: 成功{}/{}", successCount, userIds.size());
}
```

---

## 缓存失效策略

### 被动失效

**时间失效:**
- L1缓存: 6小时TTL自动过期
- L2缓存: 通过cacheTime字段判断是否过期

**容量失效:**
- L1缓存: LRU策略，超过1000条目时淘汰最少使用的
- L2缓存: 无容量限制，通过定期清理不活跃用户数据

### 主动失效

**用户操作触发:**
```java
// 用户更新OJ账号时清理相关缓存
@PutMapping("/update")
public Result<String> updateUserOjAccount(UpdateUserOjRequest request, HttpServletRequest httpRequest) {
    Long currentUserId = (Long) httpRequest.getAttribute("currentUserId");
    
    // 更新账号信息
    boolean success = userOjService.updateUserOjAccount(userOj);
    
    if (success) {
        // 清理缓存，强制下次获取最新数据
        String cacheKey = "oj_data:" + currentUserId;
        ojDataCache.invalidate(cacheKey);
        log.info("用户{}更新OJ账号后清理缓存", currentUserId);
    }
    
    return success ? Result.success("更新成功") : Result.error("更新失败");
}
```

**系统维护清理:**
```java
@Scheduled(cron = "0 0 3 ? * SUN")  // 每周日凌晨3点
public void cleanInactiveUsersCache() {
    LocalDateTime inactiveTime = LocalDateTime.now().minusDays(30);
    int cleanedCount = userOjMapper.clearInactiveUsersCache(inactiveTime);
    log.info("清理了{}个不活跃用户的缓存数据", cleanedCount);
}
```

---

## 性能监控与优化

### 关键指标监控

**L1缓存统计:**
```java
@Component
public class CacheMonitorService {
    
    @Scheduled(fixedRate = 300000)  // 每5分钟输出一次统计
    public void logCacheStats() {
        CacheStats stats = ojDataCache.stats();
        
        log.info("L1缓存统计: " +
                "命中率={:.2f}%, " +
                "请求数={}, " +
                "命中数={}, " +
                "未命中数={}, " +
                "加载时间={:.2f}ms",
                stats.hitRate() * 100,
                stats.requestCount(),
                stats.hitCount(),
                stats.missCount(),
                stats.averageLoadTime() / 1_000_000.0
        );
    }
}
```

**监控指标:**
- **命中率**: L1缓存命中率应保持在70%以上
- **响应时间**: 缓存命中时应 < 50ms，失效时 < 5s
- **API调用频率**: 每小时API调用次数
- **错误率**: 外部API调用错误率应 < 5%

### 性能优化建议

#### 短期优化 (1-2周内实施)

1. **缓存预热策略**
```java
@EventListener(ApplicationReadyEvent.class)
public void preloadCache() {
    // 应用启动时预加载热点用户数据
    List<Long> hotUsers = userOjMapper.findRecentActiveUsers(100);
    hotUsers.forEach(userId -> {
        try {
            getCacheOjUserDataVo(userId);
        } catch (Exception e) {
            log.warn("预加载用户{}数据失败", userId, e);
        }
    });
}
```

2. **批量API调用优化**
```java
// 如果外部API支持批量查询，优化为单次调用
public List<OjUserDataDto> batchFetchUserData(List<String> usernames, String platform) {
    // 实现批量调用逻辑
}
```

3. **缓存分层细化**
```java
// 为不同类型数据设置不同过期时间
private static final Map<String, Duration> CACHE_TTL = Map.of(
    "summary", Duration.ofHours(6),     // 汇总数据6小时
    "ranking", Duration.ofHours(12),    // 排名数据12小时
    "profile", Duration.ofDays(1)       // 用户资料1天
);
```

#### 中期优化 (1-2月内实施)

1. **引入Redis分布式缓存**
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 1
    timeout: 3000ms
```

2. **实现缓存穿透保护**
```java
// 布隆过滤器防止缓存穿透
@Bean
public BloomFilter<String> userBloomFilter() {
    return BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 10000, 0.01);
}
```

3. **数据库读写分离**
```yaml
# 读写分离配置
spring:
  datasource:
    master:
      url: jdbc:mysql://master-db:3306/ita_home
    slave:
      url: jdbc:mysql://slave-db:3306/ita_home
```

#### 长期优化 (3-6月内实施)

1. **消息队列异步处理**
```java
// 使用RabbitMQ处理数据更新任务
@RabbitListener(queues = "oj.data.update")
public void handleDataUpdate(OjUpdateMessage message) {
    // 异步处理数据更新
}
```

2. **CDN缓存静态数据**
```java
// 用户头像、排名图片等静态资源使用CDN
@GetMapping("/ranking/image/{userId}")
public ResponseEntity<byte[]> getRankingImage(@PathVariable Long userId) {
    // 生成排名图片并缓存到CDN
}
```

3. **智能缓存策略**
```java
// 基于用户行为模式的智能缓存
public class SmartCacheStrategy {
    public Duration calculateTTL(Long userId) {
        UserBehavior behavior = analyzeBehavior(userId);
        return behavior.isActive() ? Duration.ofHours(2) : Duration.ofDays(1);
    }
}
```

---

## 故障处理与降级

### 故障场景与应对

**场景1: Caffeine缓存故障**
```java
public OjUserDataVo getCacheOjUserDataVoWithFallback(Long userId) {
    try {
        return getCacheOjUserDataVo(userId);
    } catch (Exception e) {
        log.error("Caffeine缓存异常，降级到数据库查询", e);
        return getDataFromDatabase(userId);
    }
}
```

**场景2: 数据库连接异常**
```java
@Retryable(value = SQLException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
public UserOj findByUserId(Long userId) {
    return userOjMapper.findByUserId(userId);
}

@Recover
public UserOj recover(SQLException ex, Long userId) {
    log.error("数据库查询最终失败，返回默认数据", ex);
    return createDefaultUserOj(userId);
}
```

**场景3: 外部API大规模故障**
```java
@CircuitBreaker(name = "ojApi", fallbackMethod = "getDataFromCache")
public OjUserDataVo getRealTimeData(UserOj userOj) {
    // 外部API调用逻辑
}

public OjUserDataVo getDataFromCache(UserOj userOj, Exception ex) {
    log.warn("外部API故障，使用旧缓存数据", ex);
    return buildVoFromDatabase(userOj);  // 返回数据库中的缓存数据
}
```

### 监控告警配置

```yaml
# 告警阈值配置
monitoring:
  cache:
    hit-rate-threshold: 0.7      # 命中率低于70%告警
    response-time-threshold: 5000 # 响应时间超过5秒告警
  api:
    error-rate-threshold: 0.05   # 错误率超过5%告警
    timeout-threshold: 10000     # 超时时间超过10秒告警
```

---

## 最佳实践总结

### 开发最佳实践

1. **缓存键设计**
   - 使用有意义的前缀: `oj_data:`
   - 包含版本信息: `oj_data:v1:`
   - 避免特殊字符和过长键名

2. **异常处理**
   - 缓存操作异常不应影响业务逻辑
   - 记录详细的错误日志
   - 实现优雅的降级策略

3. **性能考虑**
   - 避免缓存雪崩：设置随机过期时间
   - 防止缓存穿透：空值缓存或布隆过滤器
   - 控制缓存大小：设置合理的最大条目数

### 运维最佳实践

1. **监控告警**
   - 设置缓存命中率、响应时间告警
   - 监控外部API调用成功率
   - 关注系统内存使用情况

2. **容量规划**
   - 定期评估缓存使用情况
   - 根据用户增长调整缓存大小
   - 预留足够的内存空间

3. **故障处理**
   - 制定详细的故障应急预案
   - 定期进行故障演练
   - 建立有效的故障通知机制

---

## 版本更新记录

| 版本 | 更新时间 | 更新内容 |
|------|----------|----------|
| v2.0 | 2025-09-25 | 初始版本，二级缓存架构设计和实现方案 |

---

## 参考资料

- [Caffeine官方文档](https://github.com/ben-manes/caffeine)
- [Spring Boot缓存抽象](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.caching)
- [MySQL索引优化最佳实践](https://dev.mysql.com/doc/refman/8.0/en/optimization-indexes.html)

---

**最后更新**: 2025-09-25