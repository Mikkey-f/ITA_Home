# ITAHome Epoch2 - 定时任务系统文档

## 概述

本文档详细描述了ITAHome后端系统Epoch2阶段的定时任务设计与实现，包括OJ数据刷新任务的调度配置、执行策略、异常处理机制以及日志管理。

**设计目标:**
- 自动化维护OJ数据的新鲜度
- 降低实时API调用的系统负载
- 提供可靠的数据同步机制
- 适应低配置服务器环境

---

## 任务架构总览

### 定时任务体系

```
定时任务调度器 (Spring Scheduler)
    ↓
┌─────────────────┐    ┌─────────────────┐
│  数据刷新任务    │    │  数据清理任务    │
│  每日02:00执行   │    │  每周日03:00执行  │
└─────────────────┘    └─────────────────┘
    ↓                      ↓
┌─────────────────┐    ┌─────────────────┐
│  活跃用户筛选    │    │  不活跃用户清理  │
│  批量数据更新    │    │  缓存空间释放    │
└─────────────────┘    └─────────────────┘
    ↓                      ↓
┌─────────────────┐    ┌─────────────────┐
│  异步API调用     │    │  数据库清理操作  │
│  缓存数据更新    │    │  统计信息记录    │
└─────────────────┘    └─────────────────┘
```

### 核心组件

1. **OjDataScheduleService**: 主要的定时任务服务
2. **AsyncOjUpdateService**: 异步数据更新服务
3. **LocalLockService**: 并发控制服务
4. **UserOjMapper**: 数据库操作接口

---

## 主要定时任务详解

### 1. 活跃用户数据刷新任务

#### 基础配置

```java
@Component
@Slf4j
@ConditionalOnProperty(name = "ita.cache.oj.schedule.enabled", havingValue = "true", matchIfMissing = true)
public class OjDataScheduleService {
    
    @Scheduled(cron = "0 0 2 * * ?")
    public void refreshActiveUsersData() {
        log.info("开始刷新活跃用户OJ数据");
        // 任务实现...
    }
}
```

**调度配置详解:**

| 配置项 | 值 | 说明 |
|--------|---|------|
| cron表达式 | `0 0 2 * * ?` | 每天凌晨2点执行 |
| 时区 | 系统默认时区 | 跟随服务器时区设置 |
| 启用控制 | `ita.cache.oj.schedule.enabled` | 可通过配置文件开关 |

**执行时间选择原因:**
- 凌晨2点系统负载较低
- 避开用户活跃时段
- 给数据同步留足时间
- 便于故障处理和恢复

#### 执行流程详解

```java
@Scheduled(cron = "0 0 2 * * ?")
public void refreshActiveUsersData() {
    long startTime = System.currentTimeMillis();
    log.info("开始刷新活跃用户OJ数据");
    
    try {
        // 1. 计算活跃用户时间阈值
        LocalDateTime activeTime = LocalDateTime.now()
                .minusDays(cacheProperties.getActiveUserDays());
        
        // 2. 查询活跃用户列表
        List<Long> activeUserIds = userOjMapper.findActiveUserIds(activeTime);
        log.info("找到{}个活跃用户需要刷新", activeUserIds.size());
        
        if (activeUserIds.isEmpty()) {
            log.info("没有活跃用户需要刷新");
            return;
        }
        
        // 3. 分批处理用户数据
        List<List<Long>> batches = partition(activeUserIds, cacheProperties.getBatchSize());
        log.info("分为{}批次处理，每批{}个用户", batches.size(), cacheProperties.getBatchSize());
        
        int totalSuccess = 0;
        int totalFailed = 0;
        
        // 4. 逐批处理
        for (int i = 0; i < batches.size(); i++) {
            List<Long> batch = batches.get(i);
            log.info("处理第{}/{}批，用户数: {}", i + 1, batches.size(), batch.size());
            
            try {
                BatchResult result = processBatch(batch);
                totalSuccess += result.getSuccessCount();
                totalFailed += result.getFailedCount();
                
                log.info("第{}批处理完成: 成功{}, 失败{}", i + 1, result.getSuccessCount(), result.getFailedCount());
                
                // 批次间暂停，避免系统压力
                if (i < batches.size() - 1) {
                    Thread.sleep(5000); // 5秒间隔
                }
                
            } catch (Exception e) {
                log.error("第{}批处理失败", i + 1, e);
                totalFailed += batch.size();
            }
        }
        
        // 5. 记录执行结果
        long duration = System.currentTimeMillis() - startTime;
        log.info("活跃用户OJ数据刷新完成: 总用户数={}, 成功={}, 失败={}, 耗时={}ms", 
                activeUserIds.size(), totalSuccess, totalFailed, duration);
        
        // 6. 记录统计信息到数据库（可选）
        recordTaskExecution("refresh_active_users", activeUserIds.size(), totalSuccess, totalFailed, duration);
        
    } catch (Exception e) {
        log.error("刷新活跃用户OJ数据失败", e);
        // 发送告警通知（可选）
        sendAlertNotification("定时任务执行失败", e.getMessage());
    }
}
```

#### 活跃用户筛选策略

```java
/**
 * 查询活跃用户ID
 */
@Select("SELECT user_id FROM ita_home.user_oj " +
        "WHERE last_access_time >= #{activeTime} " +
        "ORDER BY last_access_time DESC")
List<Long> findActiveUserIds(@Param("activeTime") LocalDateTime activeTime);
```

**筛选逻辑:**
- **活跃定义**: 最近7天内有访问记录的用户
- **排序策略**: 按最后访问时间倒序，优先处理最活跃用户
- **数量控制**: 通过配置控制每批处理的用户数量

**活跃度分级:**

| 活跃度 | 最后访问时间 | 处理优先级 | 更新频率 |
|--------|--------------|------------|----------|
| 高活跃 | 24小时内 | 最高 | 每次任务执行 |
| 中活跃 | 3天内 | 中等 | 每次任务执行 |
| 低活跃 | 7天内 | 较低 | 每次任务执行 |
| 不活跃 | 7天以上 | 跳过 | 按需更新 |

#### 批量处理机制

```java
private BatchResult processBatch(List<Long> userIds) {
    log.debug("开始处理批次: 用户数={}", userIds.size());
    
    // 创建异步任务列表
    List<CompletableFuture<Boolean>> futures = userIds.stream()
            .map(userId -> {
                return asyncOjUpdateService.updateUserOjDataAsync(userId)
                        .orTimeout(30, TimeUnit.SECONDS)  // 30秒超时
                        .exceptionally(throwable -> {
                            log.error("用户{}数据更新失败", userId, throwable);
                            return false;
                        });
            })
            .collect(Collectors.toList());
    
    // 等待所有任务完成
    CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
    );
    
    try {
        allFutures.get(60, TimeUnit.SECONDS);  // 整批最多等待60秒
    } catch (TimeoutException e) {
        log.warn("批次处理超时，部分任务可能未完成");
    } catch (Exception e) {
        log.error("批次处理异常", e);
    }
    
    // 统计结果
    int successCount = 0;
    int failedCount = 0;
    
    for (CompletableFuture<Boolean> future : futures) {
        try {
            if (future.isDone() && future.get()) {
                successCount++;
            } else {
                failedCount++;
            }
        } catch (Exception e) {
            failedCount++;
        }
    }
    
    return new BatchResult(successCount, failedCount);
}
```

**批处理策略特点:**
- **并行执行**: 批次内的用户数据更新并行进行
- **超时保护**: 单个用户30秒超时，整批60秒超时
- **异常隔离**: 单个用户失败不影响同批次其他用户
- **批次间隔**: 批次间暂停5秒，避免系统压力过大

### 2. 不活跃用户缓存清理任务

#### 调度配置

```java
@Scheduled(cron = "0 0 3 ? * SUN")
public void cleanInactiveUsersCache() {
    log.info("开始清理不活跃用户缓存");
    // 清理实现...
}
```

**调度配置详解:**

| 配置项 | 值 | 说明 |
|--------|---|------|
| cron表达式 | `0 0 3 ? * SUN` | 每周日凌晨3点执行 |
| 执行频率 | 每周一次 | 避免频繁清理影响性能 |
| 执行时间 | 周日凌晨 | 系统负载最低时段 |

#### 清理逻辑实现

```java
@Scheduled(cron = "0 0 3 ? * SUN")
public void cleanInactiveUsersCache() {
    long startTime = System.currentTimeMillis();
    log.info("开始清理不活跃用户缓存");
    
    try {
        // 1. 计算不活跃时间阈值（30天）
        LocalDateTime inactiveTime = LocalDateTime.now().minusDays(30);
        
        // 2. 查询即将清理的数据数量
        Long countToClean = userOjMapper.countInactiveUsersCache(inactiveTime);
        log.info("发现{}条不活跃用户缓存数据需要清理", countToClean);
        
        if (countToClean == 0) {
            log.info("没有需要清理的不活跃用户缓存");
            return;
        }
        
        // 3. 执行清理操作
        int cleanedCount = userOjMapper.clearInactiveUsersCache(inactiveTime);
        
        // 4. 记录清理结果
        long duration = System.currentTimeMillis() - startTime;
        log.info("不活跃用户缓存清理完成: 预期清理={}, 实际清理={}, 耗时={}ms", 
                countToClean, cleanedCount, duration);
        
        // 5. 记录统计信息
        recordTaskExecution("clean_inactive_cache", countToClean.intValue(), cleanedCount, 0, duration);
        
        // 6. 清理Caffeine缓存中的相关数据（可选）
        cleanCaffeineCache(inactiveTime);
        
    } catch (Exception e) {
        log.error("清理不活跃用户缓存失败", e);
        sendAlertNotification("缓存清理任务失败", e.getMessage());
    }
}
```

#### 数据库清理操作

```java
/**
 * 统计不活跃用户缓存数量
 */
@Select("SELECT COUNT(*) FROM ita_home.user_oj " +
        "WHERE last_access_time < #{inactiveTime} " +
        "AND (total_ac_num IS NOT NULL OR total_commit_num IS NOT NULL)")
Long countInactiveUsersCache(@Param("inactiveTime") LocalDateTime inactiveTime);

/**
 * 清理不活跃用户的缓存数据
 */
@Update("UPDATE ita_home.user_oj SET " +
        "total_ac_num = NULL, " +
        "total_commit_num = NULL, " +
        "cache_time = NULL " +
        "WHERE last_access_time < #{inactiveTime}")
int clearInactiveUsersCache(@Param("inactiveTime") LocalDateTime inactiveTime);
```

**清理策略说明:**
- **时间阈值**: 30天未访问视为不活跃
- **清理字段**: 只清理缓存字段，保留基础账号信息
- **保留数据**: last_access_time等元数据保留，便于统计分析

---

## 异常处理机制

### 异常分类与处理策略

#### 1. 网络异常处理

```java
@Component
public class NetworkExceptionHandler {
    
    @Retryable(value = {ConnectTimeoutException.class, SocketTimeoutException.class}, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 2000, multiplier = 2))
    public OjUserDataDto callExternalApi(String url) {
        try {
            return restTemplate.getForObject(url, OjUserDataDto.class);
        } catch (Exception e) {
            log.warn("外部API调用失败: url={}, 将进行重试", url, e);
            throw e;
        }
    }
    
    @Recover
    public OjUserDataDto recover(Exception ex, String url) {
        log.error("外部API调用最终失败: url={}", url, ex);
        return null; // 返回null，调用方会跳过此平台
    }
}
```

**重试策略:**
- **重试次数**: 最多3次
- **重试间隔**: 2秒、4秒、8秒（指数退避）
- **重试条件**: 网络超时、连接异常
- **最终失败**: 记录错误日志，返回null

#### 2. 数据库异常处理

```java
@Service
public class DatabaseExceptionHandler {
    
    @Retryable(value = SQLException.class, maxAttempts = 2, backoff = @Backoff(delay = 1000))
    public int updateUserOjData(Long userId, Integer totalAc, Integer totalCommit) {
        try {
            return userOjMapper.updateCacheData(userId, totalAc, totalCommit, 
                    LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());
        } catch (SQLException e) {
            log.warn("数据库更新失败，用户ID: {}, 将进行重试", userId, e);
            throw e;
        }
    }
    
    @Recover
    public int recover(SQLException ex, Long userId, Integer totalAc, Integer totalCommit) {
        log.error("数据库更新最终失败，用户ID: {}", userId, ex);
        // 记录到失败队列，稍后重试
        recordFailedUpdate(userId, totalAc, totalCommit);
        return 0;
    }
}
```

#### 3. 业务逻辑异常处理

```java
public class BusinessExceptionHandler {
    
    public void handleUserDataUpdate(Long userId) {
        try {
            // 业务逻辑处理
            processUserData(userId);
        } catch (IllegalArgumentException e) {
            log.warn("用户数据格式错误: userId={}, error={}", userId, e.getMessage());
            // 跳过此用户，继续处理下一个
        } catch (BusinessException e) {
            log.error("业务逻辑异常: userId={}", userId, e);
            // 记录到错误队列
            recordBusinessError(userId, e);
        } catch (Exception e) {
            log.error("未知异常: userId={}", userId, e);
            // 发送告警通知
            sendAlertNotification("未知异常", e.getMessage());
        }
    }
}
```

### 异常恢复机制

#### 失败重试队列

```java
@Component
public class FailedTaskRecovery {
    
    private final Queue<FailedTask> failedTaskQueue = new ConcurrentLinkedQueue<>();
    
    /**
     * 记录失败任务
     */
    public void recordFailedTask(Long userId, String taskType, String errorMessage) {
        FailedTask task = FailedTask.builder()
                .userId(userId)
                .taskType(taskType)
                .errorMessage(errorMessage)
                .retryCount(0)
                .createTime(LocalDateTime.now())
                .build();
        
        failedTaskQueue.offer(task);
        log.info("记录失败任务: userId={}, taskType={}", userId, taskType);
    }
    
    /**
     * 定期重试失败任务
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void retryFailedTasks() {
        if (failedTaskQueue.isEmpty()) {
            return;
        }
        
        log.info("开始重试失败任务，队列大小: {}", failedTaskQueue.size());
        
        int processedCount = 0;
        int maxRetryCount = 3;
        
        while (!failedTaskQueue.isEmpty() && processedCount < 50) { // 每次最多处理50个
            FailedTask task = failedTaskQueue.poll();
            
            if (task.getRetryCount() >= maxRetryCount) {
                log.warn("任务重试次数超限，放弃重试: userId={}, taskType={}", 
                        task.getUserId(), task.getTaskType());
                continue;
            }
            
            try {
                // 重新执行任务
                boolean success = retryTask(task);
                if (success) {
                    log.info("失败任务重试成功: userId={}, taskType={}", 
                            task.getUserId(), task.getTaskType());
                } else {
                    // 增加重试次数，重新加入队列
                    task.setRetryCount(task.getRetryCount() + 1);
                    failedTaskQueue.offer(task);
                }
            } catch (Exception e) {
                log.error("重试任务异常: userId={}, taskType={}", 
                        task.getUserId(), task.getTaskType(), e);
                task.setRetryCount(task.getRetryCount() + 1);
                failedTaskQueue.offer(task);
            }
            
            processedCount++;
        }
    }
}
```

---

## 日志管理系统

### 日志分级策略

#### 1. 日志级别定义

```properties
# logback-spring.xml 配置
logging.level.com.ita.home.schedule=INFO
logging.level.com.ita.home.service.impl.async.AsyncOjUpdateService=DEBUG
logging.level.com.ita.home.mapper=DEBUG
```

**级别说明:**
- **ERROR**: 系统错误、异常中断、需要人工干预
- **WARN**: 预期内的异常、重试操作、降级处理
- **INFO**: 任务开始/结束、关键节点、统计信息
- **DEBUG**: 详细的执行过程、调试信息

#### 2. 结构化日志格式

```java
@Component
public class StructuredLogger {
    
    public void logTaskStart(String taskName, Map<String, Object> context) {
        log.info("任务开始 | task={} | context={}", taskName, toJson(context));
    }
    
    public void logTaskEnd(String taskName, long duration, Map<String, Object> result) {
        log.info("任务完成 | task={} | duration={}ms | result={}", 
                taskName, duration, toJson(result));
    }
    
    public void logBatchProgress(String taskName, int currentBatch, int totalBatches, 
                               int successCount, int failedCount) {
        log.info("批次进度 | task={} | batch={}/{} | success={} | failed={}", 
                taskName, currentBatch, totalBatches, successCount, failedCount);
    }
    
    public void logApiCall(String platform, String username, long duration, boolean success) {
        log.debug("API调用 | platform={} | username={} | duration={}ms | success={}", 
                platform, username, duration, success);
    }
}
```

#### 3. 关键事件日志示例

```text
# 任务开始
2025-09-25 02:00:00.123 INFO  [scheduler-1] c.i.h.s.OjDataScheduleService - 任务开始 | task=refreshActiveUsers | context={"activeUserDays":7,"batchSize":50}

# 用户筛选
2025-09-25 02:00:01.456 INFO  [scheduler-1] c.i.h.s.OjDataScheduleService - 找到123个活跃用户需要刷新

# 批次处理
2025-09-25 02:00:02.789 INFO  [scheduler-1] c.i.h.s.OjDataScheduleService - 批次进度 | task=refreshActiveUsers | batch=1/3 | success=45 | failed=5

# API调用详情
2025-09-25 02:00:03.012 DEBUG [async-pool-1] c.i.h.s.i.AsyncOjUpdateService - API调用 | platform=leetcode_cn | username=user123 | duration=2300ms | success=true

# 任务完成
2025-09-25 02:05:30.567 INFO  [scheduler-1] c.i.h.s.OjDataScheduleService - 任务完成 | task=refreshActiveUsers | duration=330444ms | result={"totalUsers":123,"successCount":118,"failedCount":5}
```

### 日志文件管理

#### 文件分割策略

```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="SCHEDULE_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/schedule.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>logs/schedule.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{50} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="com.ita.home.schedule" level="INFO" additivity="false">
        <appender-ref ref="SCHEDULE_FILE"/>
    </logger>
</configuration>
```

**文件管理策略:**
- **文件大小**: 单个文件最大100MB
- **保留时间**: 保留30天的日志文件
- **总大小限制**: 所有日志文件总计不超过3GB
- **压缩存储**: 历史日志文件自动压缩

#### 日志监控告警

```java
@Component
public class LogMonitorService {
    
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final AtomicInteger warnCount = new AtomicInteger(0);
    
    @EventListener
    public void handleLogEvent(ILoggingEvent event) {
        if (event.getLevel().equals(Level.ERROR)) {
            int count = errorCount.incrementAndGet();
            if (count > 10) { // 5分钟内超过10个错误
                sendAlertNotification("错误日志过多", "5分钟内出现" + count + "个错误");
                errorCount.set(0);
            }
        }
    }
    
    @Scheduled(fixedRate = 300000) // 每5分钟重置计数
    public void resetCounters() {
        errorCount.set(0);
        warnCount.set(0);
    }
}
```

### 性能监控日志

#### 关键指标记录

```java
@Component
public class PerformanceLogger {
    
    @Scheduled(fixedRate = 300000) // 每5分钟记录一次性能指标
    public void logPerformanceMetrics() {
        // 系统资源指标
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed() / 1024 / 1024; // MB
        
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double cpuUsage = osBean.getProcessCpuLoad() * 100;
        
        // 缓存指标
        CacheStats cacheStats = ojDataCache.stats();
        double hitRate = cacheStats.hitRate() * 100;
        
        // 数据库连接池指标
        HikariDataSource dataSource = (HikariDataSource) this.dataSource;
        int activeConnections = dataSource.getHikariPoolMXBean().getActiveConnections();
        int totalConnections = dataSource.getHikariPoolMXBean().getTotalConnections();
        
        log.info("性能指标 | memory={}MB | cpu={:.2f}% | cacheHitRate={:.2f}% | dbConn={}/{}", 
                usedMemory, cpuUsage, hitRate, activeConnections, totalConnections);
    }
    
    public void logTaskPerformance(String taskName, long duration, int processedCount) {
        double avgTimePerItem = processedCount > 0 ? (double) duration / processedCount : 0;
        log.info("任务性能 | task={} | totalTime={}ms | processedCount={} | avgTimePerItem={:.2f}ms", 
                taskName, duration, processedCount, avgTimePerItem);
    }
}
```

---

## 配置管理

### 应用配置文件

```yaml
# application.yml
ita:
  cache:
    oj:
      max-size: 1000          # Caffeine缓存最大条目数
      expire-hours: 6         # 缓存过期时间（小时）
      active-user-days: 7     # 活跃用户定义（天）
      async-update-timeout-seconds: 30  # 异步更新超时时间
      batch-size: 50          # 定时任务批次大小
      schedule:
        enabled: true         # 是否启用定时任务
        refresh-cron: "0 0 2 * * ?"     # 数据刷新任务cron表达式
        cleanup-cron: "0 0 3 ? * SUN"   # 数据清理任务cron表达式
  
  oj:
    target: "https://ojhunt-api.example.com/api"  # 外部API地址
    
# 日志配置
logging:
  level:
    com.ita.home.schedule: INFO
    com.ita.home.service.impl.async.AsyncOjUpdateService: DEBUG
  file:
    name: logs/application.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"

# 线程池配置
spring:
  task:
    execution:
      pool:
        core-size: 4
        max-size: 8
        queue-capacity: 100
    scheduling:
      pool:
        size: 2
```

### 环境特定配置

#### 开发环境 (application-dev.yml)

```yaml
ita:
  cache:
    oj:
      batch-size: 10          # 开发环境减小批次
      schedule:
        enabled: false        # 开发环境关闭定时任务
  oj:
    target: "http://localhost:8081/mock-api"  # 使用Mock API

logging:
  level:
    com.ita.home: DEBUG       # 开发环境详细日志
```

#### 生产环境 (application-prod.yml)

```yaml
ita:
  cache:
    oj:
      batch-size: 100         # 生产环境增大批次
      schedule:
        enabled: true
  oj:
    target: "https://ojhunt-api.prod.com/api"

logging:
  level:
    com.ita.home.schedule: INFO
    org.springframework: WARN
```

### 动态配置更新

```java
@Component
@RefreshScope
public class DynamicScheduleConfig {
    
    @Value("${ita.cache.oj.batch-size:50}")
    private Integer batchSize;
    
    @Value("${ita.cache.oj.active-user-days:7}")
    private Integer activeUserDays;
    
    // 支持运行时配置更新
    @EventListener(RefreshEvent.class)
    public void onConfigRefresh() {
        log.info("定时任务配置已刷新: batchSize={}, activeUserDays={}", 
                batchSize, activeUserDays);
    }
}
```

---

## 监控与告警

### 健康检查

```java
@Component
public class ScheduleHealthIndicator implements HealthIndicator {
    
    private volatile LocalDateTime lastSuccessTime = LocalDateTime.now();
    private volatile String lastTaskStatus = "SUCCESS";
    
    @Override
    public Health health() {
        Duration timeSinceLastSuccess = Duration.between(lastSuccessTime, LocalDateTime.now());
        
        if (timeSinceLastSuccess.toHours() > 25) { // 超过25小时没有成功执行
            return Health.down()
                    .withDetail("lastSuccessTime", lastSuccessTime)
                    .withDetail("timeSinceLastSuccess", timeSinceLastSuccess.toHours() + "小时")
                    .withDetail("lastTaskStatus", lastTaskStatus)
                    .build();
        }
        
        return Health.up()
                .withDetail("lastSuccessTime", lastSuccessTime)
                .withDetail("lastTaskStatus", lastTaskStatus)
                .build();
    }
    
    public void recordTaskSuccess() {
        this.lastSuccessTime = LocalDateTime.now();
        this.lastTaskStatus = "SUCCESS";
    }
    
    public void recordTaskFailure(String errorMessage) {
        this.lastTaskStatus = "FAILED: " + errorMessage;
    }
}
```

### 指标收集

```java
@Component
public class ScheduleMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Timer taskDurationTimer;
    private final Counter taskSuccessCounter;
    private final Counter taskFailureCounter;
    
    public ScheduleMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.taskDurationTimer = Timer.builder("schedule.task.duration")
                .description("定时任务执行时长")
                .register(meterRegistry);
        this.taskSuccessCounter = Counter.builder("schedule.task.success")
                .description("定时任务成功次数")
                .register(meterRegistry);
        this.taskFailureCounter = Counter.builder("schedule.task.failure")
                .description("定时任务失败次数")
                .register(meterRegistry);
    }
    
    public void recordTaskExecution(String taskName, long duration, boolean success) {
        taskDurationTimer.record(duration, TimeUnit.MILLISECONDS);
        
        if (success) {
            taskSuccessCounter.increment(Tags.of("task", taskName));
        } else {
            taskFailureCounter.increment(Tags.of("task", taskName));
        }
    }
}
```

### 告警通知

```java
@Service
public class AlertNotificationService {
    
    public void sendAlertNotification(String title, String message) {
        try {
            // 邮件告警
            sendEmailAlert(title, message);
            
            // 钉钉群通知（如果配置了）
            sendDingTalkAlert(title, message);
            
            log.info("告警通知已发送: title={}", title);
        } catch (Exception e) {
            log.error("发送告警通知失败", e);
        }
    }
    
    private void sendEmailAlert(String title, String message) {
        // 邮件发送逻辑
        EmailEvent emailEvent = EmailEvent.builder()
                .to("admin@example.com")
                .subject("[ITAHome告警] " + title)
                .content(buildAlertEmailContent(title, message))
                .build();
        
        emailProducer.sendEmail(emailEvent);
    }
    
    private String buildAlertEmailContent(String title, String message) {
        return String.format("""
            告警标题: %s
            告警时间: %s
            告警内容: %s
            
            服务器信息:
            - 主机名: %s
            - IP地址: %s
            - 应用版本: %s
            
            请及时处理！
            """, 
            title,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            message,
            getHostname(),
            getLocalIP(),
            getApplicationVersion()
        );
    }
}
```

---

## 最佳实践建议

### 开发阶段

1. **任务幂等性设计**
   - 确保重复执行不会产生副作用
   - 使用分布式锁防止并发执行
   - 记录任务执行状态和结果

2. **资源控制**
   - 合理设置线程池大小
   - 控制批处理的数量
   - 设置合适的超时时间

3. **错误处理**
   - 区分可重试和不可重试的异常
   - 实现合理的重试策略
   - 记录详细的错误信息

### 运维阶段

1. **监控告警**
   - 设置任务执行状态监控
   - 配置性能指标告警阈值
   - 建立故障通知机制

2. **日志管理**
   - 定期清理历史日志文件
   - 监控日志文件大小和磁盘空间
   - 分析日志中的异常模式

3. **容量规划**
   - 根据用户增长预估任务负载
   - 适时调整批处理大小和执行频率
   - 监控系统资源使用情况

### 性能优化

1. **短期优化**
   - 优化数据库查询语句
   - 调整线程池配置参数
   - 实现更细粒度的并发控制

2. **中期优化**
   - 引入消息队列处理异步任务
   - 实现分布式定时任务
   - 增加缓存预热机制

3. **长期优化**
   - 考虑微服务架构拆分
   - 实现智能的任务调度算法
   - 基于机器学习的性能优化

---

## 故障排查指南

### 常见问题诊断

#### 1. 任务执行超时

**现象:** 任务执行时间异常长，可能超过预期时间

**排查步骤:**
```bash
# 1. 检查线程池状态
jstack <pid> | grep -A 10 "ojApiExecutorService"

# 2. 检查数据库连接
SHOW PROCESSLIST;

# 3. 检查外部API响应时间
curl -w "@curl-format.txt" https://ojhunt-api.example.com/api/leetcode_cn/testuser
```

**解决方案:**
- 增加超时时间配置
- 调整线程池大小
- 优化数据库查询
- 联系外部API提供方

#### 2. 内存泄漏

**现象:** 服务器内存使用持续增长，可能导致OOM

**排查步骤:**
```bash
# 1. 检查JVM内存使用
jstat -gc <pid> 5s

# 2. 生成内存快照
jmap -dump:format=b,file=heap.hprof <pid>

# 3. 分析内存使用情况
jhat heap.hprof
```

**解决方案:**
- 检查缓存配置是否合理
- 优化对象生命周期管理
- 调整JVM堆内存大小

#### 3. 数据库死锁

**现象:** 任务执行失败，日志显示数据库死锁

**排查步骤:**
```sql
-- 查看当前锁信息
SHOW ENGINE INNODB STATUS;

-- 查看正在执行的事务
SELECT * FROM INFORMATION_SCHEMA.INNODB_TRX;

-- 查看锁等待情况
SELECT * FROM INFORMATION_SCHEMA.INNODB_LOCK_WAITS;
```

**解决方案:**
- 优化SQL语句执行顺序
- 减少事务持有时间
- 调整数据库隔离级别

---

## 版本更新记录

| 版本 | 更新时间 | 更新内容 |
|------|----------|----------|
| v2.0 | 2025-09-25 | 初始版本，包含完整的定时任务系统设计和实现 |

---

## 参考资料

- [Spring Task Scheduling](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#scheduling)
- [Cron表达式在线生成器](https://crontab.guru/)
- [Logback官方文档](http://logback.qos.ch/documentation.html)
- [Java并发编程实战](https://www.oreilly.com/library/view/java-concurrency-in/9780321349606/)

---

**最后更新**: 2025-09-25