package com.ita.home.utils;

import com.ita.home.result.Result;
import com.ita.home.service.impl.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/28 20:55
 */
@Slf4j
public class FileUtil {

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }

    /**
     * 获取文件名（不含扩展名）
     */
    public static String getFileNameWithoutExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(0, lastDot) : filename;
    }

    /**
     * 检查文件名是否安全（防止路径遍历攻击）
     */
    public static boolean isSecureFileName(String filename) {
        if (!StringUtils.hasText(filename)) {
            return false;
        }
        // 检查是否包含危险字符
        return !filename.contains("..") &&
                !filename.contains("/") &&
                !filename.contains("\\") &&
                !filename.contains(":") &&
                !filename.contains("<") &&
                !filename.contains(">") &&
                !filename.contains("|") &&
                !filename.contains("?") &&
                !filename.contains("*") &&
                !filename.contains("\"") &&
                filename.length() <= 255 &&
                !filename.isEmpty();
    }

    /**
     * 确保目录存在
     */
    public static void ensureDirectoryExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new RuntimeException("无法创建目录: " + directoryPath);
            }
        }
    }

    /**
     * 生成最终文件名（支持覆盖策略）
     * @param originalName 原始文件名
     * @param customFileName 自定义文件名（可选）
     * @return 最终使用的文件名
     */
    public static String generateFinalFileName(String originalName, String customFileName) {
        if (StringUtils.hasText(customFileName)) {
            // 使用自定义文件名，但保留原始文件的扩展名
            String extension = getFileExtension(originalName);
            // 如果自定义文件名已经包含扩展名，就使用它；否则添加原扩展名
            if (customFileName.contains(".")) {
                return customFileName;
            } else {
                return customFileName + "." + extension;
            }
        } else {
            // 使用原始文件名
            return originalName;
        }
    }

    /**
     * 检查文件是否存在
     */
    public static boolean fileExists(String directoryPath, String fileName) {
        File file = new File(directoryPath, fileName);
        return file.exists() && file.isFile();
    }

    /**
     * 删除文件（如果存在）
     */
    public static boolean deleteFileIfExists(String directoryPath, String fileName) {
        File file = new File(directoryPath, fileName);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return true; // 文件不存在也算删除成功
    }

    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 验证文件名合法性并清理
     */
    public static String sanitizeFileName(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "untitled";
        }

        // 替换或移除非法字符
        String sanitized = filename
                .replaceAll("[<>:\"|?*\\\\]", "_")  // 替换Windows非法字符为下划线
                .replaceAll("/", "_")               // 替换Unix路径分隔符
                .replaceAll("\\s+", "_")           // 替换多个空格为单个下划线
                .replaceAll("_{2,}", "_");         // 多个连续下划线替换为单个

        // 移除开头和结尾的下划线和点号
        sanitized = sanitized.replaceAll("^[._]+|[._]+$", "");

        // 如果清理后为空，给个默认名称
        if (sanitized.isEmpty()) {
            sanitized = "file_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }

        // 限制文件名长度
        if (sanitized.length() > 200) {
            String extension = getFileExtension(filename);
            String baseName = sanitized.substring(0, 200 - extension.length() - 1);
            sanitized = baseName + "." + extension;
        }

        return sanitized;
    }

    /**
     * 处理普通下载
     */
    public static ResponseEntity<Resource> handleNormalDownload(Resource resource, String fileName,
                                                          String contentType, String encodedFileName, Long fileSize) {
        log.info("开始普通下载: 文件={}, 大小={}", fileName, formatFileSize(fileSize));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")  // 声明支持范围请求
                .header("X-Content-Type-Options", "nosniff")  // 安全头
                .body(resource);
    }

    /**
     * 处理断点续传下载
     */
    public static ResponseEntity<?> handleRangeDownload(Resource resource, String rangeHeader,
                                                  String fileName, String contentType, String encodedFileName, Long fileSize) {
        try {
            log.info("开始断点续传下载: 文件={}, Range={}", fileName, rangeHeader);

            // 解析Range头
            RangeInfo rangeInfo = parseRangeHeader(rangeHeader, fileSize);
            if (rangeInfo == null) {
                log.warn("无效的Range请求: {}", rangeHeader);
                return ResponseEntity.status(416)  // Range Not Satisfiable
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                        .build();
            }

            // 创建部分内容资源
            Resource partialResource = createPartialResource(resource, rangeInfo);

            long contentLength = rangeInfo.end - rangeInfo.start + 1;

            return ResponseEntity.status(206)  // Partial Content
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + rangeInfo.start + "-" + rangeInfo.end + "/" + fileSize)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .body(partialResource);

        } catch (Exception e) {
            log.error("处理断点续传失败: {}", fileName, e);
            return ResponseEntity.status(500)
                    .body(Result.error("断点续传处理失败: " + e.getMessage()));
        }
    }

    /**
     * 确定文件的Content-Type
     */
    public static String determineContentType(String fileName) {
        String extension = FileUtil.getFileExtension(fileName).toLowerCase();

        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "md" -> "text/markdown; charset=UTF-8";
            case "txt" -> "text/plain; charset=UTF-8";
            case "html", "htm" -> "text/html; charset=UTF-8";
            case "json" -> "application/json; charset=UTF-8";
            case "xml" -> "application/xml; charset=UTF-8";
            case "css" -> "text/css; charset=UTF-8";
            case "js" -> "application/javascript; charset=UTF-8";
            case "zip" -> "application/zip";
            case "rar" -> "application/x-rar-compressed";
            case "7z" -> "application/x-7z-compressed";
            case "tar" -> "application/x-tar";
            case "gz" -> "application/gzip";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "svg" -> "image/svg+xml";
            case "ico" -> "image/x-icon";
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "mp4" -> "video/mp4";
            case "avi" -> "video/x-msvideo";
            case "mov" -> "video/quicktime";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            default -> "application/octet-stream";  // 通用二进制流
        };
    }

    /**
     * 编码文件名，解决中文文件名下载问题
     */
    public static String encodeFileName(String fileName) {
        try {
            // 使用RFC 5987编码方式，支持中文文件名
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");  // 空格用%20而不是+

            log.debug("文件名编码: {} -> {}", fileName, encoded);
            return encoded;
        } catch (Exception e) {
            log.warn("文件名编码失败，使用原文件名: {}", fileName, e);
            return fileName;
        }
    }

    /**
     * 获取客户端真实IP
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // X-Forwarded-For可能包含多个IP，取第一个
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }

    /**
     * 验证IP是否有效
     */
    public static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }



    // todo 添加限流策略与IP检查策略
    /**
     * 检查下载频率限制
     */
    public static boolean isDownloadRateLimited(String clientIp) {
        // 简单的内存限流实现，生产环境建议使用Redis
        // 这里只是示例，实际应该实现更sophisticated的限流逻辑
        return false;
    }

    /**
     * 检查IP是否在黑名单中
     */
    public static boolean isIpBlacklisted(String clientIp) {
        // 实际实现中可以从数据库或配置文件读取黑名单
        return false;
    }

    /**
     * 记录下载尝试
     */
    public static void recordDownloadAttempt(String category, String fileName, String clientIp, String userAgent, boolean success) {
        try {
            log.info("下载记录: 分类={}, 文件={}, IP={}, userAgent={}, 状态={}", category, fileName, clientIp, userAgent, success ? "成功" : "失败");
            // todo 可以异步记录到数据库
            // downloadLogService.recordDownload(category, fileName, clientIp, userAgent, success);
        } catch (Exception e) {
            log.warn("记录下载日志失败", e);
        }
    }

    /**
     * 解析Range头信息
     */
    public static RangeInfo parseRangeHeader(String rangeHeader, long fileSize) {
        try {
            // Range: bytes=start-end
            String range = rangeHeader.substring(6); // 去掉"bytes="
            String[] parts = range.split("-");

            long start;
            long end;

            if (parts.length == 1) {
                if (range.startsWith("-")) {
                    // 后缀范围: bytes=-500 (最后500字节)
                    start = Math.max(0, fileSize - Long.parseLong(parts[0].substring(1)));
                    end = fileSize - 1;
                } else {
                    // 前缀范围: bytes=500- (从500字节到结尾)
                    start = Long.parseLong(parts[0]);
                    end = fileSize - 1;
                }
            } else {
                // 完整范围: bytes=200-1000
                start = Long.parseLong(parts[0]);
                end = parts[1].isEmpty() ? fileSize - 1 : Long.parseLong(parts[1]);
            }

            // 验证范围
            if (start < 0 || end < 0 || start > end || start >= fileSize) {
                return null;
            }

            // 确保end不超过文件大小
            end = Math.min(end, fileSize - 1);

            return new RangeInfo(start, end);

        } catch (Exception e) {
            log.warn("解析Range头失败: {}", rangeHeader, e);
            return null;
        }
    }



    /**
     * 创建部分内容资源
     */
    public static Resource createPartialResource(Resource resource, RangeInfo rangeInfo) throws Exception {
        return new InputStreamResource(new RangeInputStream(resource.getInputStream(), rangeInfo));
    }

    /**
     * Range信息内部类
     */
    private static class RangeInfo {
        final long start;
        final long end;

        RangeInfo(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }

    /**
     * 范围输入流，支持断点续传
     */
    private static class RangeInputStream extends InputStream {
        private final InputStream inputStream;
        private final long start;
        private final long end;
        private long currentPos;
        private boolean initialized = false;

        public RangeInputStream(InputStream inputStream, RangeInfo rangeInfo) {
            this.inputStream = inputStream;
            this.start = rangeInfo.start;
            this.end = rangeInfo.end;
            this.currentPos = 0;
        }

        @Override
        public int read() throws IOException {
            if (!initialized) {
                // 跳到起始位置
                inputStream.skip(start);
                currentPos = start;
                initialized = true;
            }

            if (currentPos > end) {
                return -1; // 超出范围
            }

            int data = inputStream.read();
            if (data != -1) {
                currentPos++;
            }
            return data;
        }

        @Override
        public int read(byte @NonNull [] b, int off, int len) throws IOException {
            if (!initialized) {
                inputStream.skip(start);
                currentPos = start;
                initialized = true;
            }

            if (currentPos > end) {
                return -1;
            }

            // 调整读取长度，不超过范围
            long remainingInRange = end - currentPos + 1;
            int bytesToRead = (int) Math.min(len, remainingInRange);

            int bytesRead = inputStream.read(b, off, bytesToRead);
            if (bytesRead > 0) {
                currentPos += bytesRead;
            }
            return bytesRead;
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
        }
    }
}
