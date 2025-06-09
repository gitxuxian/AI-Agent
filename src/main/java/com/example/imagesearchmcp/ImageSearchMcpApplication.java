package com.example.imagesearchmcp;

import com.example.imagesearchmcp.tool.ImagesSearchTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ImageSearchMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageSearchMcpApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider imagesSearchTools(ImagesSearchTool imagesSearchTool) {
        return MethodToolCallbackProvider
            .builder().toolObjects(imagesSearchTool).build();
    }
}
