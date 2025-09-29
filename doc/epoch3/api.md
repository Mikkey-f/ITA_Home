# 文件管理系统 API 接口文档

## 概述

文件管理系统提供文件上传、下载、预览、列表查看和删除功能，支持Markdown和PDF文件格式。

**基础信息**：
- **Base URL**: `http://localhost:8080/api/files`
- **支持文件类型**: Markdown (.md)、PDF (.pdf)
- **文件分类**: 前端、java后端、cpp后端
- **文件大小限制**: 10MB
- **编码格式**: UTF-8

---

## 1. 文件上传

### 接口信息
- **URL**: `/upload`
- **Method**: `POST`
- **Content-Type**: `multipart/form-data`
- **认证**: 需要认证 (`@RequireAuth`)

### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| file | MultipartFile | 是 | 上传的文件 | test.pdf |
| category | String | 是 | 文件分类 | "前端" |
| customFileName | String | 否 | 自定义文件名 | "新文档.pdf" |

### 请求示例
```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);
formData.append('category', '前端');
formData.append('customFileName', '项目文档.pdf');

fetch('/api/files/upload', {
    method: 'POST',
    body: formData
});
```

### 响应格式
```json
{
    "code": 1,
    "msg": null,
    "data": {
        "filePath": "前端/项目文档.pdf",
        "originalName": "test.pdf",
        "savedName": "项目文档.pdf", 
        "fileSize": 1024000,
        "formattedSize": "1000.0 KB",
        "category": "前端",
        "overwritten": false
    }
}
```

### 错误响应
```json
{
    "code": 0,
    "msg": "不支持的文件类型，仅支持: md, pdf"
}
```

---

## 2. 获取文件列表

### 接口信息
- **URL**: `/list`
- **Method**: `GET`
- **认证**: 无需认证

### 请求参数
无

### 请求示例
```javascript
fetch('/api/files/list')
    .then(response => response.json())
    .then(data => console.log(data));
```

### 响应格式
```json
{
    "code": 1,
    "msg": null,
    "data": {
        "fileMap": {
            "前端": [
                "前端/Vue3实战指南.md",
                "前端/React开发手册.pdf"
            ],
            "java后端": [
                "java后端/Spring Boot教程.md",
                "java后端/微服务架构.pdf"
            ],
            "cpp后端": [
                "cpp后端/C++高级编程.md"
            ]
        },
        "categoryCount": {
            "前端": 2,
            "java后端": 2,
            "cpp后端": 1
        },
        "totalCount": 5
    }
}
```

---

## 3. 获取文件信息

### 接口信息
- **URL**: `/info`
- **Method**: `GET` 
- **认证**: 无需认证

### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| category | String | 是 | 文件分类 | "前端" |
| fileName | String | 是 | 文件名 | "Vue3实战指南.md" |

### 请求示例
```javascript
fetch('/api/files/info?category=前端&fileName=Vue3实战指南.md')
    .then(response => response.json())
    .then(data => console.log(data));
```

### 响应格式
```json
{
    "code": 1,
    "msg": null,
    "data": {
        "fileName": "Vue3实战指南.md",
        "category": "前端",
        "fileSize": 2048000,
        "formattedSize": "2.0 MB", 
        "lastModified": 1696003200000,
        "extension": "md",
        "mimeType": "text/markdown"
    }
}
```

---

## 4. 文件预览

### 接口信息
- **URL**: `/preview`
- **Method**: `GET`
- **认证**: 无需认证

### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| category | String | 是 | 文件分类 | "前端" |
| fileName | String | 是 | 文件名 | "Vue3实战指南.md" |

### 请求示例
```javascript
// Markdown文件预览
fetch('/api/files/preview?category=前端&fileName=Vue3实战指南.md')
    .then(response => response.json())
    .then(data => {
        // data.data 包含Markdown原始内容
        const htmlContent = marked.parse(data.data);
        document.getElementById('preview').innerHTML = htmlContent;
    });

// PDF文件预览  
const pdfUrl = '/api/files/preview?category=前端&fileName=开发手册.pdf';
document.getElementById('pdfViewer').src = pdfUrl;
```

### 响应格式

#### Markdown文件响应
```json
{
    "code": 1,
    "msg": null,
    "data": "# Vue3实战指南\n\n这是一个Vue3开发教程..."
}
```

#### PDF文件响应
- **Content-Type**: `application/pdf`
- **Content-Disposition**: `inline; filename="开发手册.pdf"`
- **响应体**: PDF二进制流

---

## 5. 文件下载

### 接口信息
- **URL**: `/download`
- **Method**: `GET`
- **认证**: 无需认证（可配置）

### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| category | String | 是 | 文件分类 | "前端" |
| fileName | String | 是 | 文件名 | "Vue3实战指南.md" |

### 请求示例
```javascript
// 方式1: 直接下载
function downloadFile(category, fileName) {
    const link = document.createElement('a');
    link.href = `/api/files/download?category=${encodeURIComponent(category)}&fileName=${encodeURIComponent(fileName)}`;
    link.download = fileName;
    link.click();
}

// 方式2: 新窗口打开
window.open('/api/files/download?category=前端&fileName=Vue3实战指南.md');
```

### 响应格式
- **Content-Type**: 根据文件类型自动判断
  - `.md`: `text/markdown; charset=UTF-8`
  - `.pdf`: `application/pdf`
  - 其他: `application/octet-stream`
- **Content-Disposition**: `attachment; filename="Vue3实战指南.md"`
- **Content-Length**: 文件大小（字节）
- **响应体**: 文件二进制流

### 断点续传支持
支持HTTP Range请求，用于大文件断点续传：

```javascript
fetch('/api/files/download?category=前端&fileName=大文件.pdf', {
    headers: {
        'Range': 'bytes=1024-2047'  // 下载1024-2047字节
    }
});
```

**断点续传响应**：
- **状态码**: `206 Partial Content`
- **Content-Range**: `bytes 1024-2047/10240`
- **Accept-Ranges**: `bytes`

---

## 6. 删除文件

### 接口信息
- **URL**: `/delete`
- **Method**: `DELETE`
- **认证**: 需要认证 (`@RequireAuth`)

### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| category | String | 是 | 文件分类 | "前端" |
| fileName | String | 是 | 文件名 | "Vue3实战指南.md" |

### 请求示例
```javascript
fetch('/api/files/delete?category=前端&fileName=Vue3实战指南.md', {
    method: 'DELETE',
    headers: {
        'Authorization': 'Bearer ' + token  // 需要认证
    }
})
.then(response => response.json())
.then(data => console.log(data));
```

### 响应格式
```json
{
    "code": 1,
    "msg": null,
    "data": "文件删除成功"
}
```

---

## 错误码说明

| 错误码 | 说明 | 常见原因 |
|--------|------|---------|
| 0 | 操作失败 | 业务逻辑错误 |
| 1 | 操作成功 | - |

### 常见错误响应

#### 参数错误
```json
{
    "code": 0,
    "msg": "文件分类和文件名不能为空"
}
```

#### 文件不存在
```json
{
    "code": 0,
    "msg": "文件不存在"
}
```

#### 文件类型不支持
```json
{
    "code": 0,
    "msg": "不支持的文件类型，仅支持: md, pdf"
}
```

#### 文件过大
```json
{
    "code": 0,
    "msg": "文件大小不能超过 10.0 MB"
}
```

#### 认证失败
```json
{
    "code": 0,
    "msg": "认证失败，请先登录"
}
```

#### 限流
```json
{
    "code": 0,
    "msg": "下载频率过高，已用 10/10 次，请 60 秒后重试"
}
```

---

## 限流说明

### 下载频率限制
- **普通用户**: 10次/分钟
- **VIP用户**: 20次/分钟  
- **受限IP**: 5次/分钟

### 限流响应头
```
HTTP/1.1 429 Too Many Requests
Retry-After: 60
```

---

## 安全说明

### 文件上传安全
1. **文件类型白名单**: 仅支持 `.md` 和 `.pdf` 文件
2. **文件大小限制**: 最大 10MB
3. **文件名安全检查**: 防止路径遍历攻击
4. **权限验证**: 上传和删除需要认证

### 文件下载安全  
1. **路径验证**: 确保文件在允许的目录内
2. **存在性检查**: 验证文件是否存在且可读
3. **频率限制**: 防止恶意下载
4. **IP黑名单**: 支持IP封禁功能

---

## 前端集成示例

### 完整的文件管理组件示例

```javascript
class FileManager {
    constructor(baseUrl = '/api/files') {
        this.baseUrl = baseUrl;
    }
    
    // 上传文件
    async uploadFile(file, category, customFileName = null) {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('category', category);
        if (customFileName) {
            formData.append('customFileName', customFileName);
        }
        
        const response = await fetch(`${this.baseUrl}/upload`, {
            method: 'POST',
            body: formData,
            headers: {
                'Authorization': 'Bearer ' + this.getToken()
            }
        });
        
        return await response.json();
    }
    
    // 获取文件列表
    async getFileList() {
        const response = await fetch(`${this.baseUrl}/list`);
        return await response.json();
    }
    
    // 预览文件
    async previewFile(category, fileName) {
        const response = await fetch(`${this.baseUrl}/preview?category=${encodeURIComponent(category)}&fileName=${encodeURIComponent(fileName)}`);
        
        if (fileName.endsWith('.md')) {
            return await response.json();
        } else {
            return response; // PDF返回流
        }
    }
    
    // 下载文件
    downloadFile(category, fileName) {
        const link = document.createElement('a');
        link.href = `${this.baseUrl}/download?category=${encodeURIComponent(category)}&fileName=${encodeURIComponent(fileName)}`;
        link.download = fileName;
        link.click();
    }
    
    // 删除文件
    async deleteFile(category, fileName) {
        const response = await fetch(`${this.baseUrl}/delete?category=${encodeURIComponent(category)}&fileName=${encodeURIComponent(fileName)}`, {
            method: 'DELETE',
            headers: {
                'Authorization': 'Bearer ' + this.getToken()
            }
        });
        
        return await response.json();
    }
    
    // 获取token（需要根据实际认证方式实现）
    getToken() {
        return localStorage.getItem('authToken') || '';
    }
}

// 使用示例
const fileManager = new FileManager();

// 上传
fileManager.uploadFile(file, '前端', '新文档.pdf')
    .then(result => console.log('上传成功:', result));

// 获取列表
fileManager.getFileList()
    .then(result => console.log('文件列表:', result));

// 下载
fileManager.downloadFile('前端', 'Vue3实战指南.md');
```

---

## 注意事项

### 开发注意事项
1. **CORS配置**: 确保前端域名在CORS白名单中
2. **文件编码**: 所有文本文件使用UTF-8编码
3. **错误处理**: 前端需要处理各种错误状态
4. **进度显示**: 大文件上传建议显示进度条

### 生产环境建议
1. **文件存储**: 建议使用对象存储服务（如OSS、S3）
2. **CDN加速**: 为下载接口配置CDN
3. **监控告警**: 监控上传下载成功率和响应时间
4. **备份策略**: 定期备份重要文件

---

## 更新日志

| 版本 | 日期 | 更新内容 |
|------|------|---------|
| 1.0.0 | 2025-09-29 | 初始版本，支持基础上传下载预览功能 |

---

## 联系方式

如有问题或建议，请联系开发团队。