package com.example.aiagent.config;

import com.example.aiagent.tools.*;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工具注册配置
 */
@Configuration
public class ToolRegistration {

    @Resource
    private TerminateTool terminateTool;

    @Resource
    private BrowserAutomationTool browserAutomationTool;

    @Resource
    private FileOperationTool fileOperationTool;

    @Resource
    private ExaWebSearchTool exaWebSearchTool;

    @Resource
    private WebScrapingTool webScrapingTool;

    @Resource
    private ResourceDownloadTool resourceDownloadTool;

    @Resource
    private PDFGenerationTool pdfGenerationTool;

    @Resource
    private TimeTool timeTool;

    @Bean
    public ToolCallback[] allTools() {
        return ToolCallbacks.from(
            fileOperationTool,
            exaWebSearchTool,
            webScrapingTool,
            timeTool,
            resourceDownloadTool,
            pdfGenerationTool,
            terminateTool,
            browserAutomationTool
        );
    }
}
