package com.example.imagesearchmcp.tool;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class ImagesSearchToolTest {

    @Resource
    private ImagesSearchTool imagesSearchTool;

    @Test
    void eachPhotos() {
        String results = imagesSearchTool.eachPhotos("computer");
        Assertions.assertNotNull(results);
    }
}