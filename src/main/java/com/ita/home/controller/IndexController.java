package com.ita.home.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 首页控制器
 * 提供系统说明和文档链接
 */
@Controller
@Hidden // 在Swagger文档中隐藏此控制器
public class IndexController {

    /**
     * 系统首页
     */
    @GetMapping({"/", "/home"})
    @ResponseBody
    public String home() {
        return """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>ITA Home - 用户管理系统</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; background: #f5f5f5; }
                    .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; margin-bottom: 30px; }
                    .api-link { display: inline-block; padding: 12px 24px; background: #007bff; color: white; text-decoration: none; border-radius: 5px; margin: 10px; font-weight: bold; }
                    .api-link:hover { background: #0056b3; }
                    .section { margin: 20px 0; padding: 15px; background: #f8f9fa; border-radius: 5px; }
                    .tech-list { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 10px; }
                    .tech-item { background: #e9ecef; padding: 8px; border-radius: 3px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🏠 ITA Home</h1>
                        <p>基于Spring Boot + MyBatis Plus的用户管理系统</p>
                    </div>
                    
                    <div class="section">
                        <h2>📚 API文档</h2>
                        <p>点击下方链接查看完整的API接口文档：</p>
                        <div style="text-align: center;">
                            <a href="/swagger-ui.html" target="_blank" class="api-link">
                                🔗 Swagger UI 接口文档
                            </a>
                        </div>
                    </div>
                    
                    <div class="section">
                        <h2>🔐 核心功能</h2>
                        <ul>
                            <li><strong>用户注册</strong> - 创建新用户账号，支持头像选择</li>
                            <li><strong>用户登录</strong> - 验证用户名密码，返回用户信息</li>
                            <li><strong>信息查询</strong> - 根据ID查询用户详细信息</li>
                            <li><strong>用户名验证</strong> - 检查用户名是否已被注册</li>
                        </ul>
                    </div>
                    
                    <div class="section">
                        <h2>🎨 头像系统</h2>
                        <p>支持9种不同风格的头像，编号1-9：</p>
                        <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; text-align: center;">
                            <div>1️⃣ 默认头像</div>
                            <div>2️⃣ 蓝色头像</div>
                            <div>3️⃣ 绿色头像</div>
                            <div>4️⃣ 红色头像</div>
                            <div>5️⃣ 紫色头像</div>
                            <div>6️⃣ 橙色头像</div>
                            <div>7️⃣ 粉色头像</div>
                            <div>8️⃣ 黄色头像</div>
                            <div>9️⃣ 灰色头像</div>
                        </div>
                    </div>
                    
                    <div class="section">
                        <h2>⚡ 技术栈</h2>
                        <div class="tech-list">
                            <div class="tech-item">Spring Boot 3.5</div>
                            <div class="tech-item">MyBatis Plus 3.5</div>
                            <div class="tech-item">MySQL 8.0</div>
                            <div class="tech-item">Swagger 3</div>
                            <div class="tech-item">Lombok</div>
                            <div class="tech-item">BCrypt</div>
                        </div>
                    </div>
                    
                    <div class="section">
                        <h2>👥 测试账号</h2>
                        <p>系统预置以下测试用户（密码都是 <code>123456</code>）：</p>
                        <ul>
                            <li><strong>admin</strong> - 管理员账号</li>
                            <li><strong>testuser</strong> - 测试用户</li>
                            <li><strong>demo</strong> - 演示账号</li>
                        </ul>
                    </div>
                    
                    <div style="text-align: center; margin-top: 30px; color: #6c757d;">
                        <p>© 2025 ITA Team. 使用 Spring Boot + Swagger 构建</p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}