/**
 * 文件预览组件
 * 思路：根据文件类型选择不同的预览方式
 * 适配Result<T>响应格式
 */
class FilePreview {
    constructor(containerSelector) {
        this.container = document.querySelector(containerSelector);
    }

    /**
     * 预览文件 - 入口方法
     */
    async previewFile(category, fileName) {
        try {
            // 1. 显示加载状态
            this.showLoading();

            // 2. 根据文件扩展名选择预览方式
            const extension = this.getFileExtension(fileName);

            switch (extension) {
                case 'md':
                    await this.previewMarkdown(category, fileName);
                    break;
                case 'pdf':
                    await this.previewPdf(category, fileName);
                    break;
                default:
                    this.showError('不支持预览的文件类型');
            }
        } catch (error) {
            console.error('预览失败:', error);
            this.showError('预览失败: ' + error.message);
        }
    }

    /**
     * Markdown预览实现
     * 思路：调用后端获取文本 → 前端渲染为HTML
     * 适配Result<String>格式
     */
    async previewMarkdown(category, fileName) {
        try {
            // 1. 获取Markdown原始内容 - 使用8080端口
            const response = await axios.get(`${API_BASE_URL}/api/files/preview`, {
                params: { category, fileName }
            });

            console.log('Markdown预览响应:', response.data);

            // 🔧 适配Result格式：检查响应状态和获取数据
            if (response.data.code === 1) {
                const markdownContent = response.data.data; // Result的data字段

                if (!markdownContent) {
                    throw new Error('文件内容为空');
                }

                // 2. 使用marked.js将Markdown转换为HTML
                let htmlContent;
                if (typeof marked !== 'undefined') {
                    htmlContent = marked.parse(markdownContent, {
                        highlight: function(code, lang) {
                            // 使用highlight.js进行代码高亮
                            if (typeof hljs !== 'undefined' && lang && hljs.getLanguage(lang)) {
                                return hljs.highlight(code, { language: lang }).value;
                            }
                            if (typeof hljs !== 'undefined') {
                                return hljs.highlightAuto(code).value;
                            }
                            return code;
                        },
                        breaks: true,  // 支持GFM换行
                        gfm: true      // 启用GitHub风格Markdown
                    });
                } else {
                    // 如果marked.js未加载，显示原始文本
                    htmlContent = `<pre style="white-space: pre-wrap;">${this.escapeHtml(markdownContent)}</pre>`;
                }

                // 3. 渲染到页面
                this.container.innerHTML = `
                    <div class="markdown-preview">
                        <div class="file-header">
                            <h3>📄 ${fileName}</h3>
                            <button onclick="downloadFile('${category}', '${fileName}')" 
                                    class="download-btn">💾 下载文件</button>
                        </div>
                        <div class="markdown-content">
                            ${htmlContent}
                        </div>
                    </div>
                `;

                // 4. 应用样式
                this.applyMarkdownStyles();

            } else {
                // Result返回失败
                throw new Error(response.data.msg || '预览失败');
            }

        } catch (error) {
            console.error('Markdown预览失败:', error);
            throw new Error('Markdown预览失败: ' + this.getErrorMessage(error));
        }
    }

    /**
     * PDF预览实现
     * 思路：直接用iframe加载PDF流
     */
    async previewPdf(category, fileName) {
        try {
            // 构建PDF预览URL - 使用8080端口
            const pdfUrl = `${API_BASE_URL}/api/files/preview?category=${encodeURIComponent(category)}&fileName=${encodeURIComponent(fileName)}`;

            console.log('PDF预览URL:', pdfUrl);

            // 创建iframe显示PDF
            this.container.innerHTML = `
                <div class="pdf-preview">
                    <div class="file-header">
                        <h3>📄 ${fileName}</h3>
                        <div class="pdf-controls">
                            <button onclick="downloadFile('${category}', '${fileName}')" 
                                    class="download-btn">💾 下载文件</button>
                            <button onclick="openInNewTab('${pdfUrl}')" 
                                    class="new-tab-btn">🔗 新标签页打开</button>
                        </div>
                    </div>
                    <iframe src="${pdfUrl}" 
                            class="pdf-iframe"
                            frameborder="0">
                        您的浏览器不支持PDF预览，请<a href="${pdfUrl}" target="_blank">点击这里下载</a>
                    </iframe>
                </div>
            `;
        } catch (error) {
            console.error('PDF预览失败:', error);
            throw new Error('PDF预览失败: ' + this.getErrorMessage(error));
        }
    }

    /**
     * 应用Markdown样式
     * 思路：让预览效果更美观
     */
    applyMarkdownStyles() {
        // 检查是否已经应用过样式
        if (document.getElementById('markdown-styles')) {
            return;
        }

        const style = document.createElement('style');
        style.id = 'markdown-styles';
        style.textContent = `
            .markdown-preview {
                max-width: 800px;
                margin: 0 auto;
                padding: 20px;
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            }
            
            .file-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                border-bottom: 2px solid #e1e5e9;
                padding-bottom: 10px;
                margin-bottom: 20px;
            }
            
            .markdown-content {
                line-height: 1.8;
                color: #374151;
            }
            
            .markdown-content h1, 
            .markdown-content h2,
            .markdown-content h3,
            .markdown-content h4,
            .markdown-content h5,
            .markdown-content h6 {
                color: #1e293b;
                margin-top: 24px;
                margin-bottom: 16px;
                font-weight: 600;
            }
            
            .markdown-content h1, .markdown-content h2 {
                border-bottom: 1px solid #eaecef;
                padding-bottom: 10px;
            }
            
            .markdown-content p {
                margin-bottom: 16px;
            }
            
            .markdown-content ul, .markdown-content ol {
                margin-bottom: 16px;
                padding-left: 24px;
            }
            
            .markdown-content li {
                margin-bottom: 4px;
            }
            
            .markdown-content pre {
                background: #f6f8fa;
                padding: 16px;
                border-radius: 6px;
                overflow-x: auto;
                margin-bottom: 16px;
                border: 1px solid #e1e5e9;
            }
            
            .markdown-content code {
                background: #f6f8fa;
                padding: 2px 4px;
                border-radius: 4px;
                font-family: 'Courier New', monospace;
                font-size: 14px;
            }
            
            .markdown-content pre code {
                background: none;
                padding: 0;
            }
            
            .markdown-content blockquote {
                border-left: 4px solid #dfe2e5;
                padding-left: 16px;
                color: #6a737d;
                margin-bottom: 16px;
                font-style: italic;
            }
            
            .markdown-content table {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 16px;
            }
            
            .markdown-content th, .markdown-content td {
                border: 1px solid #e1e5e9;
                padding: 8px 12px;
                text-align: left;
            }
            
            .markdown-content th {
                background: #f6f8fa;
                font-weight: 600;
            }
            
            .pdf-iframe {
                width: 100%;
                height: 600px;
                border: 1px solid #ddd;
                border-radius: 8px;
            }
            
            .download-btn, .new-tab-btn {
                background: #0366d6;
                color: white;
                border: none;
                padding: 8px 16px;
                border-radius: 6px;
                cursor: pointer;
                margin-left: 10px;
                transition: background-color 0.2s;
            }
            
            .download-btn:hover, .new-tab-btn:hover {
                background: #0256cc;
            }
            
            .pdf-controls {
                display: flex;
                gap: 10px;
            }
        `;
        document.head.appendChild(style);
    }

    // 工具方法
    getFileExtension(fileName) {
        return fileName.split('.').pop().toLowerCase();
    }

    showLoading() {
        this.container.innerHTML = '<div class="loading">📄 正在加载预览...</div>';
    }

    showError(message) {
        this.container.innerHTML = `<div class="error">❌ ${message}</div>`;
    }

    /**
     * 转义HTML特殊字符
     */
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * 获取详细错误信息
     */
    getErrorMessage(error) {
        if (error.response) {
            // 服务器返回了错误响应
            if (error.response.data && error.response.data.msg) {
                return `服务器错误: ${error.response.data.msg}`;
            }
            return `服务器错误 (${error.response.status})`;
        } else if (error.request) {
            // 请求发出但没有收到响应
            return '网络连接失败，请检查后端服务';
        } else {
            // 其他错误
            return error.message;
        }
    }
}

// ================================
// 全局函数 - 与index.html中的函数保持一致
// ================================

/**
 * 下载文件 - 适配8080端口
 */
function downloadFile(category, fileName) {
    console.log(`下载文件: ${category}/${fileName}`);
    const downloadUrl = `${API_BASE_URL}/api/files/download?category=${encodeURIComponent(category)}&fileName=${encodeURIComponent(fileName)}`;

    // 创建隐藏的下载链接
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = fileName;
    link.style.display = 'none';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

/**
 * 在新标签页打开
 */
function openInNewTab(url) {
    window.open(url, '_blank');
}

// ================================
// 确保API_BASE_URL存在
// ================================
if (typeof API_BASE_URL === 'undefined') {
    const API_BASE_URL = 'http://localhost:8080';
    console.warn('API_BASE_URL未定义，使用默认值:', API_BASE_URL);
}