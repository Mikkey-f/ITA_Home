# JWT 认证系统文档

## 概述

ITAHome 后端系统采用JWT (JSON Web Token) 作为用户身份认证机制，提供无状态的、安全的用户认证方案。

---

## JWT 基础知识

### 什么是JWT
JSON Web Token (JWT) 是一个开放标准 (RFC 7519)，它定义了一种紧凑的、自包含的方式，用于作为JSON对象在各方之间安全地传输信息。

### JWT 结构
JWT由三部分组成，通过点号(.)分隔：
```
eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYWRtaW4ifQ.signature
```

1. **Header（头部）**: 包含令牌类型和签名算法
2. **Payload（载荷）**: 包含声明信息
3. **Signature（签名）**: 验证令牌完整性

---

## 系统实现

### 1. JWT 工具类 (JwtUtil)

#### 核心配置
```java
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;              // JWT密钥
    
    @Value("${jwt.expire-hours}")  
    private int expireHours;            // 过期时间(小时)
    
    @Value("${jwt.token-prefix}")
    private String tokenPrefix;         // Token前缀 "Bearer "
    
    @Value("${jwt.header-name}")
    private String headerName;          // Header名称 "Authorization"
}
```

#### 密钥生成
```java
private SecretKey getSigningKey() {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
}
```

---

### 2. Token 生成

#### 生成方法
```java
public String generateToken(Long userId, String username) {
    // 设置载荷信息
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId);
    claims.put("username", username);
    claims.put("timestamp", System.currentTimeMillis());

    // 设置过期时间
    Date expirationDate = new Date(System.currentTimeMillis() + 
                                  (long) expireHours * 60 * 60 * 1000);

    // 生成JWT
    return Jwts.builder()
            .setClaims(claims)                    // 设置载荷
            .setSubject(username)                 // 设置主题
            .setIssuedAt(new Date())             // 设置签发时间
            .setExpiration(expirationDate)       // 设置过期时间
            .signWith(getSigningKey())           // 设置签名密钥
            .compact();
}
```

#### Token 内容
| 字段名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| userId | Long | 用户ID | 1 |
| username | String | 用户名 | "admin" |
| timestamp | Long | 生成时间戳 | 1695456789000 |
| sub | String | 主题(用户名) | "admin" |
| iat | Long | 签发时间 | 1695456789 |
| exp | Long | 过期时间 | 1695463989 |

---

### 3. Token 解析与验证

#### 解析Claims
```java
public Claims getClaimsFromToken(String token) {
    try {
        // 移除token前缀
        if (token.startsWith(tokenPrefix)) {
            token = token.substring(tokenPrefix.length());
        }

        // 解析token
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    } catch (ExpiredJwtException e) {
        throw new RuntimeException("令牌已过期，请重新登录");
    } catch (UnsupportedJwtException e) {
        throw new RuntimeException("不支持的令牌格式");
    } catch (MalformedJwtException e) {
        throw new RuntimeException("令牌格式错误");
    } catch (SecurityException e) {
        throw new RuntimeException("令牌签名验证失败");
    }
}
```

#### 获取用户信息
```java
// 获取用户ID
public Long getUserIdFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    Object userId = claims.get("userId");
    if (userId instanceof Integer) {
        return ((Integer) userId).longValue();
    } else if (userId instanceof Long) {
        return (Long) userId;
    }
    throw new RuntimeException("令牌中用户ID格式错误");
}

// 获取用户名
public String getUsernameFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.getSubject();
}
```

#### Token 验证
```java
public boolean validateToken(String token) {
    try {
        Claims claims = getClaimsFromToken(token);
        String username = claims.getSubject();
        Long userId = getUserIdFromToken(token);
        
        // 检查必要字段
        if (username == null || username.trim().isEmpty() || 
            userId == null || userId <= 0) {
            return false;
        }

        // 检查是否过期
        return !isTokenExpired(token);
    } catch (Exception e) {
        return false;
    }
}
```

---

### 4. 认证拦截器

#### @RequireAuth 注解
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuth {
}
```

#### JWT 拦截器实现
```java
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {
    
    private final JwtUtil jwtUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        
        // 检查是否需要认证
        if (!isRequireAuth(handler)) {
            return true;
        }
        
        // 获取token
        String authHeader = request.getHeader(jwtUtil.getHeaderName());
        String token = jwtUtil.extractTokenFromHeader(authHeader);
        
        if (token == null) {
            throw new UnauthorizedException("未授权，请先登录");
        }
        
        // 验证token
        if (!jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("令牌无效，请重新登录");
        }
        
        // 设置用户信息到请求属性
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        request.setAttribute("currentUserId", userId);
        request.setAttribute("currentUsername", username);
        
        return true;
    }
}
```

---

## 使用方式

### 1. 服务端使用

#### 登录接口
```java
@PostMapping("/login/name")
public Result<Map<String, Object>> login(@RequestBody LoginByNameRequest loginRequest) {
    // 验证用户名密码
    User user = userService.loginByName(loginRequest);
    if (user == null) {
        return Result.error("用户名或密码错误");
    }

    // 生成JWT令牌
    String token = jwtUtil.generateToken(user.getId(), user.getName());

    // 构建返回数据
    Map<String, Object> data = new HashMap<>();
    data.put("token", token);
    data.put("tokenType", "Bearer");
    data.put("expiresIn", jwtUtil.getExpireHours() * 3600);
    data.put("user", userInfo);

    return Result.success(data);
}
```

#### 需要认证的接口
```java
@RequireAuth
@GetMapping("/profile")
public Result<Map<String, Object>> getCurrentUserProfile(HttpServletRequest request) {
    // 从请求属性中获取当前用户信息（由JWT拦截器设置）
    Long currentUserId = (Long) request.getAttribute("currentUserId");
    String currentUsername = (String) request.getAttribute("currentUsername");
    
    // 业务逻辑处理
    User user = userService.findById(currentUserId);
    return Result.success(buildUserProfile(user));
}
```

---

### 2. 前端使用

#### 存储Token
```javascript
// 登录成功后存储token
const loginResponse = await fetch('/api/user/login/name', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name: 'admin', password: '123456' })
});

const result = await loginResponse.json();
if (result.code === 1) {
    // 存储到localStorage
    localStorage.setItem('token', result.data.token);
    localStorage.setItem('tokenType', result.data.tokenType);
}
```

#### 发送请求
```javascript
// 获取存储的token
const token = localStorage.getItem('token');
const tokenType = localStorage.getItem('tokenType');

// 在请求头中携带token
const response = await fetch('/api/user/profile', {
    method: 'GET',
    headers: {
        'Authorization': `${tokenType} ${token}`,
        'Content-Type': 'application/json'
    }
});
```

#### Axios 拦截器
```javascript
// 请求拦截器 - 自动添加token
axios.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    const tokenType = localStorage.getItem('tokenType');
    
    if (token) {
        config.headers.Authorization = `${tokenType} ${token}`;
    }
    return config;
});

// 响应拦截器 - 处理认证错误
axios.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            // token过期或无效，清除本地存储并跳转登录
            localStorage.removeItem('token');
            localStorage.removeItem('tokenType');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);
```

---

## 配置说明

### application.yml 配置
```yaml
jwt:
  # JWT密钥 - 生产环境必须使用强密钥
  secret: ${JWT_SECRET:mySecretKey123456789012345678901234567890}
  # Token过期时间(小时)
  expire-hours: ${JWT_EXPIRE_HOURS:2}
  # Token前缀
  token-prefix: "Bearer "
  # Header名称
  header-name: "Authorization"
```

### 环境变量配置
```bash
# 生产环境建议通过环境变量配置
export JWT_SECRET="your-very-strong-secret-key-here"
export JWT_EXPIRE_HOURS=2
```

---

## 安全考虑

### 1. 密钥安全
- **密钥长度**: 至少256位 (32字节)
- **密钥复杂度**: 包含字母、数字、特殊字符
- **密钥保护**: 不要硬编码，使用环境变量
- **密钥轮换**: 定期更换密钥

### 2. Token 安全
- **HTTPS**: 生产环境必须使用HTTPS传输
- **存储安全**: 前端避免存储在不安全的地方
- **过期时间**: 设置合理的过期时间
- **刷新机制**: 考虑实现token刷新机制

### 3. 防御措施
- **输入验证**: 严格验证token格式
- **异常处理**: 不泄露敏感错误信息
- **日志记录**: 记录认证失败事件
- **频率限制**: 防止暴力破解

---

## 错误处理

### 常见错误类型

#### 1. Token格式错误
```json
{
  "code": 0,
  "msg": "令牌格式错误",
  "data": null
}
```

#### 2. Token过期
```json
{
  "code": 0,
  "msg": "令牌已过期，请重新登录", 
  "data": null
}
```

#### 3. Token无效
```json
{
  "code": 0,
  "msg": "令牌签名验证失败",
  "data": null
}
```

#### 4. 未提供Token
```json
{
  "code": 0,
  "msg": "未授权，请先登录",
  "data": null
}
```

### 错误处理最佳实践
1. **统一错误格式**: 保持错误响应格式一致
2. **详细日志**: 服务端记录详细错误信息
3. **用户友好**: 前端显示用户友好的错误信息
4. **自动处理**: 自动跳转登录页面或刷新token

---

## 性能优化

### 1. Token 解析优化
- **缓存Claims**: 在请求生命周期内缓存解析结果
- **减少解析**: 避免重复解析同一个token
- **异常缓存**: 缓存验证失败的token避免重复验证

### 2. 密钥操作优化
- **密钥缓存**: 缓存密钥对象避免重复生成
- **算法选择**: 使用高效的签名算法
- **批量操作**: 批量验证多个token

---

## 监控与日志

### 关键监控指标
1. **Token生成频率**: 登录频率统计
2. **Token验证频率**: API调用频率
3. **验证失败率**: 认证失败比例
4. **Token过期率**: 过期token比例

### 日志记录
```java
// 成功日志
log.info("为用户 {} 生成JWT令牌成功", username);
log.info("用户 {} JWT令牌验证成功", username);

// 警告日志  
log.warn("JWT令牌已过期: {}", e.getMessage());
log.warn("用户 {} JWT令牌验证失败", username);

// 错误日志
log.error("JWT令牌格式错误: {}", e.getMessage());
log.error("JWT令牌签名验证失败: {}", e.getMessage());
```

---

## 测试指南

### 1. 单元测试
```java
@Test
public void testGenerateToken() {
    String token = jwtUtil.generateToken(1L, "testuser");
    assertNotNull(token);
    
    Long userId = jwtUtil.getUserIdFromToken(token);
    assertEquals(1L, userId);
    
    String username = jwtUtil.getUsernameFromToken(token);
    assertEquals("testuser", username);
}

@Test
public void testTokenExpiration() {
    // 设置很短的过期时间测试
    String token = generateShortLivedToken();
    Thread.sleep(2000);
    assertFalse(jwtUtil.validateToken(token));
}
```

### 2. 集成测试
```java
@Test
public void testAuthenticatedEndpoint() throws Exception {
    // 获取token
    String token = getValidToken();
    
    // 测试需要认证的接口
    mockMvc.perform(get("/api/user/profile")
            .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(1));
}

@Test  
public void testUnauthorizedAccess() throws Exception {
    // 测试未认证访问
    mockMvc.perform(get("/api/user/profile"))
            .andExpect(status().isUnauthorized());
}
```

---

## 最佳实践

### 1. 开发阶段
- 使用较短的过期时间便于测试
- 在开发环境提供token刷新机制
- 详细的错误日志和调试信息

### 2. 生产环境
- 使用强密钥和合适的过期时间
- 启用HTTPS和安全头
- 监控和告警机制

### 3. 前端集成
- 实现自动token刷新
- 优雅处理认证失败
- 安全存储和传输token

---

## 扩展功能

### 1. 刷新Token
```java
public String refreshToken(String oldToken) {
    if (validateToken(oldToken)) {
        Long userId = getUserIdFromToken(oldToken);
        String username = getUsernameFromToken(oldToken);
        return generateToken(userId, username);
    }
    throw new RuntimeException("无法刷新无效的token");
}
```

### 2. Token黑名单
```java
@Component
public class TokenBlacklist {
    private final Set<String> blacklistedTokens = new ConcurrentHashMap<>();
    
    public void addToBlacklist(String token) {
        blacklistedTokens.add(token);
    }
    
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
```

### 3. 多设备登录
```java
// 在token中添加设备信息
claims.put("deviceId", deviceId);
claims.put("deviceType", deviceType);
```

---

## 版本历史

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| v1.0 | 2025-09-23 | 初始版本，基础JWT认证功能 |

---

## 相关文档

- [API接口文档](api.md)
- [数据模型文档](models.md)
- [部署配置文档](deployment-guide.md)
- [邮箱验证码系统](email-verification-system.md)