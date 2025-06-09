package com.example.aiagent.tools;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.example.aiagent.entity.TaskStatus;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PDF生成工具 - 阿里云OSS存储
 */

@Slf4j
@Component
public class PDFGenerationTool {

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    // 使用内存存储任务状态
    private final ConcurrentHashMap<String, TaskStatus> taskStatusMap = new ConcurrentHashMap<>();

    // 任务过期时间（24小时）
    private static final long TASK_EXPIRE_TIME = 24 * 60 * 60 * 1000L;

    @Tool(description = "Generate a PDF file with given content and upload to Aliyun OSS")
    public String generatePDF(
        @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
        @ToolParam(description = "Content to be included in the PDF") String content) {

        try {
            String taskId = IdUtil.simpleUUID();
            String pdfFileName = ensureFileExtension(fileName);

            // 初始化任务状态
            TaskStatus taskStatus = new TaskStatus(taskId, pdfFileName, "processing", "PDF生成任务已启动，正在处理...");
            taskStatusMap.put(taskId, taskStatus);

            // 异步生成并上传PDF
            asyncGenerateAndUpload(taskId, pdfFileName, content);

            // 立即返回任务信息
            JSONObject result = new JSONObject();
            result.put("taskId", taskId);
            result.put("fileName", pdfFileName);
            result.put("status", "processing");
            result.put("message", "PDF生成任务已启动，正在上传至阿里云OSS...");
            result.put("queryTaskId", taskId);

            return result.toString();

        } catch (Exception e) {
            log.error("PDF生成任务启动失败: {}", e.getMessage(), e);
            return createErrorResponse("PDF生成任务启动失败: " + e.getMessage());
        }
    }

    @Async
    public CompletableFuture<String> asyncGenerateAndUpload(String taskId, String fileName, String content) {
        OSS ossClient = null;
        try {
            log.info("开始生成PDF任务: {}", taskId);

            // 更新任务状态为生成中
            updateTaskStatus(taskId, "generating", "正在生成PDF文件...");

            // 1. 生成PDF到内存
            byte[] pdfBytes = generatePDFToBytes(content);
            log.info("PDF文件生成完成，大小: {} bytes", pdfBytes.length);

            // 更新任务状态为上传中
            updateTaskStatus(taskId, "uploading", "正在上传到阿里云OSS...");

            // 2. 上传到阿里云OSS
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            String objectKey = "pdf/" + new Date().getTime() + "/" + fileName;

            // 设置对象元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(pdfBytes.length);
            metadata.setContentType("application/pdf");
            metadata.setContentDisposition("inline; filename=\"" + fileName + "\"");

            // 上传文件
            ossClient.putObject(bucketName, objectKey, new ByteArrayInputStream(pdfBytes), metadata);

            // 生成24小时有效的访问URL
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000 * 24);
            URL url = ossClient.generatePresignedUrl(bucketName, objectKey, expiration);
            String fileUrl = url.toString();

            log.info("PDF生成并上传完成: taskId={}, url={}", taskId, fileUrl);

            // 更新任务状态为完成
            TaskStatus taskStatus = taskStatusMap.get(taskId);
            if (taskStatus != null) {
                taskStatus.setStatus("completed");
                taskStatus.setMessage("PDF生成并上传完成");
                taskStatus.setFileUrl(fileUrl);
                taskStatus.setFileSize(Long.valueOf(pdfBytes.length));
                taskStatus.setUpdateTime(new Date());
            }

            return CompletableFuture.completedFuture(fileUrl);

        } catch (Exception e) {
            log.error("PDF生成任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            updateTaskStatus(taskId, "failed", "PDF生成失败: " + e.getMessage());
            return CompletableFuture.failedFuture(e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 生成PDF到字节数组
     */
    private byte[] generatePDFToBytes(String content) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (PdfWriter writer = new PdfWriter(baos);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {

                // 设置中文字体
                PdfFont font;
                try {
                    // 尝试使用中文字体
                    font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
                } catch (Exception e) {
                    // 如果中文字体不可用，使用默认字体
                    log.warn("中文字体加载失败，使用默认字体: {}", e.getMessage());
                    font = PdfFontFactory.createFont();
                }

                // 设置文档字体
                document.setFont(font);

                // 添加内容 - 支持换行
                String[] lines = content.split("\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        Paragraph paragraph = new Paragraph(line);
                        document.add(paragraph);
                    } else {
                        // 添加空行
                        document.add(new Paragraph(" "));
                    }
                }

                // 添加生成时间戳
                document.add(new Paragraph("\n\n生成时间: " + new Date()));
            }

            return baos.toByteArray();
        }
    }

    /**
     * 查询任务状态
     */
    @Tool(description = "Query PDF generation task status")
    public String queryTaskStatus(@ToolParam(description = "Task ID") String taskId) {
        try {
            TaskStatus taskStatus = taskStatusMap.get(taskId);
            if (taskStatus == null) {
                return createErrorResponse("任务不存在或已过期: " + taskId);
            }

            JSONObject result = new JSONObject();
            result.put("taskId", taskStatus.getTaskId());
            result.put("fileName", taskStatus.getFileName());
            result.put("status", taskStatus.getStatus());
            result.put("message", taskStatus.getMessage());
            result.put("createTime", taskStatus.getCreateTime());
            result.put("updateTime", taskStatus.getUpdateTime());

            if ("completed".equals(taskStatus.getStatus()) && taskStatus.getFileUrl() != null) {
                result.put("fileUrl", taskStatus.getFileUrl());
                result.put("fileSize", taskStatus.getFileSize());
                result.put("downloadUrl", taskStatus.getFileUrl());
                result.put("urlExpireTime", "24小时后过期");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("查询任务状态失败: taskId={}, error={}", taskId, e.getMessage(), e);
            return createErrorResponse("查询任务状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有任务状态（用于调试）
     */
    @Tool(description = "Get all PDF generation tasks status")
    public String getAllTasksStatus() {
        try {
            JSONObject result = new JSONObject();
            result.put("totalTasks", taskStatusMap.size());
            result.put("tasks", taskStatusMap.values());
            return result.toString();
        } catch (Exception e) {
            log.error("获取所有任务状态失败: {}", e.getMessage(), e);
            return createErrorResponse("获取所有任务状态失败: " + e.getMessage());
        }
    }

    /**
     * 更新任务状态
     */
    private void updateTaskStatus(String taskId, String status, String message) {
        try {
            TaskStatus taskStatus = taskStatusMap.get(taskId);
            if (taskStatus != null) {
                taskStatus.setStatus(status);
                taskStatus.setMessage(message);
                taskStatus.setUpdateTime(new Date());
            }
        } catch (Exception e) {
            log.error("更新任务状态失败: taskId={}, error={}", taskId, e.getMessage());
        }
    }

    /**
     * 定时清理过期任务（每小时执行一次）
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void cleanExpiredTasks() {
        long currentTime = System.currentTimeMillis();
        final int[] removedCount = {0};

        taskStatusMap.entrySet().removeIf(entry -> {
            TaskStatus taskStatus = entry.getValue();
            boolean expired = (currentTime - taskStatus.getCreateTime().getTime()) > TASK_EXPIRE_TIME;
            if (expired) {
                removedCount[0]++;
            }
            return expired;
        });

        if (removedCount[0] > 0) {
            log.info("清理过期任务: {} 个", removedCount[0]);
        }
    }

    /**
     * 确保文件名有PDF扩展名
     */
    private String ensureFileExtension(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "document_" + System.currentTimeMillis();
        }

        if (!fileName.toLowerCase().endsWith(".pdf")) {
            return fileName + ".pdf";
        }
        return fileName;
    }

    /**
     * 创建错误响应
     */
    private String createErrorResponse(String errorMessage) {
        JSONObject result = new JSONObject();
        result.put("status", "error");
        result.put("message", errorMessage);
        result.put("timestamp", new Date());
        return result.toString();
    }
}