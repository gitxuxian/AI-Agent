package com.example.aiagent.tools;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class BrowserAutomationTool {

    private static final ConcurrentHashMap<String, WebDriver> drivers = new ConcurrentHashMap<>();
    private static final int DEFAULT_TIMEOUT = 10;
    private boolean isDriverAvailable = false;
    private String driverSetupError = null;

    @PostConstruct
    public void init() {
        initializeDriver();
    }

    /**
     * 延迟初始化驱动程序，避免静态初始化问题
     */
    private void initializeDriver() {
        try {
            // 尝试多种方式设置ChromeDriver
            setupChromeDriver();
            isDriverAvailable = true;
            System.out.println("✅ Chrome驱动初始化成功");
        } catch (Exception e) {
            isDriverAvailable = false;
            driverSetupError = e.getMessage();
            System.err.println("❌ Chrome驱动初始化失败: " + e.getMessage());
            System.err.println("浏览器自动化功能将不可用，但应用可以正常启动");
        }
    }

    /**
     * 设置Chrome驱动 - 支持多种方式
     */
    private void setupChromeDriver() throws Exception {
        // 方法1: 检查系统PATH中是否已有chromedriver
        if (isChromeDriverInPath()) {
            System.out.println("使用系统PATH中的chromedriver");
            return;
        }

        // 方法2: 检查项目目录下是否有chromedriver
        String projectDriverPath = "drivers/chromedriver.exe";
        if (Files.exists(Paths.get(projectDriverPath))) {
            System.setProperty("webdriver.chrome.driver", projectDriverPath);
            System.out.println("使用项目目录下的chromedriver: " + projectDriverPath);
            return;
        }

        // 方法3: 尝试自动下载（但不使用WebDriverManager，因为它有网络问题）
        downloadChromeDriverIfNeeded();
    }

    /**
     * 检查系统PATH中是否有chromedriver
     */
    private boolean isChromeDriverInPath() {
        try {
            ProcessBuilder pb = new ProcessBuilder("chromedriver", "--version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 下载Chrome驱动（简化版本，避免网络问题）
     */
    private void downloadChromeDriverIfNeeded() throws Exception {
        String driversDir = "drivers";
        String driverPath = driversDir + "/chromedriver.exe";

        // 创建drivers目录
        Files.createDirectories(Paths.get(driversDir));

        // 这里可以提供一个本地的chromedriver路径或下载链接
        // 为了避免网络问题，建议手动下载chromedriver到drivers目录
        if (!Files.exists(Paths.get(driverPath))) {
            throw new Exception("Chrome驱动未找到。请手动下载chromedriver到 " + driverPath +
                " 或确保chromedriver在系统PATH中");
        }

        System.setProperty("webdriver.chrome.driver", driverPath);
    }

    @Tool(description = "Open a web page in browser")
    public String openPage(
        @ToolParam(description = "URL to open") String url,
        @ToolParam(description = "Browser session ID (optional, default: 'default')") String sessionId) {

        if (!isDriverAvailable) {
            return "❌ 浏览器自动化不可用: " + driverSetupError +
                "\n请确保Chrome驱动已正确安装。";
        }

        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = "default";
        }

        try {
            WebDriver driver = getOrCreateDriver(sessionId);
            driver.get(url);

            String title = driver.getTitle();
            return String.format("✅ 成功打开页面: %s\n标题: %s", url, title);
        } catch (Exception e) {
            return "❌ 打开页面失败: " + e.getMessage();
        }
    }

    @Tool(description = "Get page title and current URL")
    public String getPageInfo(@ToolParam(description = "Browser session ID (optional)") String sessionId) {
        if (!isDriverAvailable) {
            return "❌ 浏览器自动化不可用: " + driverSetupError;
        }

        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = "default";
        }

        try {
            WebDriver driver = drivers.get(sessionId);
            if (driver == null) {
                return "❌ 未找到浏览器会话。请先打开一个页面。";
            }

            String title = driver.getTitle();
            String url = driver.getCurrentUrl();
            return String.format("📄 页面信息:\n标题: %s\nURL: %s", title, url);
        } catch (Exception e) {
            return "❌ 获取页面信息失败: " + e.getMessage();
        }
    }

    @Tool(description = "Click an element on the page")
    public String clickElement(
        @ToolParam(description = "CSS selector or XPath of the element to click") String selector,
        @ToolParam(description = "Browser session ID (optional)") String sessionId) {

        if (!isDriverAvailable) {
            return "❌ 浏览器自动化不可用: " + driverSetupError;
        }

        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = "default";
        }

        try {
            WebDriver driver = drivers.get(sessionId);
            if (driver == null) {
                return "❌ 未找到浏览器会话。请先打开一个页面。";
            }

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(getBy(selector)));
            element.click();

            return "✅ 成功点击元素: " + selector;
        } catch (Exception e) {
            return "❌ 点击元素失败: " + e.getMessage();
        }
    }

    @Tool(description = "Input text into an element")
    public String inputText(
        @ToolParam(description = "CSS selector or XPath of the input element") String selector,
        @ToolParam(description = "Text to input") String text,
        @ToolParam(description = "Browser session ID (optional)") String sessionId) {

        if (!isDriverAvailable) {
            return "❌ 浏览器自动化不可用: " + driverSetupError;
        }

        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = "default";
        }

        try {
            WebDriver driver = drivers.get(sessionId);
            if (driver == null) {
                return "❌ 未找到浏览器会话。请先打开一个页面。";
            }

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(getBy(selector)));
            element.clear();
            element.sendKeys(text);

            return String.format("✅ 成功输入文本 '%s' 到元素: %s", text, selector);
        } catch (Exception e) {
            return "❌ 输入文本失败: " + e.getMessage();
        }
    }

    @Tool(description = "Get text content from an element")
    public String getElementText(
        @ToolParam(description = "CSS selector or XPath of the element") String selector,
        @ToolParam(description = "Browser session ID (optional)") String sessionId) {

        if (!isDriverAvailable) {
            return "❌ 浏览器自动化不可用: " + driverSetupError;
        }

        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = "default";
        }

        try {
            WebDriver driver = drivers.get(sessionId);
            if (driver == null) {
                return "❌ 未找到浏览器会话。请先打开一个页面。";
            }

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(getBy(selector)));
            String text = element.getText();

            return String.format("📝 元素文本: %s", text);
        } catch (Exception e) {
            return "❌ 获取元素文本失败: " + e.getMessage();
        }
    }

    @Tool(description = "Get page content or specific content")
    public String getPageContent(
        @ToolParam(description = "CSS selector for specific content (optional, leave empty for full page)") String selector,
        @ToolParam(description = "Browser session ID (optional)") String sessionId) {

        if (!isDriverAvailable) {
            return "❌ 浏览器自动化不可用: " + driverSetupError;
        }

        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = "default";
        }

        try {
            WebDriver driver = drivers.get(sessionId);
            if (driver == null) {
                return "❌ 未找到浏览器会话。请先打开一个页面。";
            }

            if (selector == null || selector.trim().isEmpty()) {
                String title = driver.getTitle();
                String bodyText = driver.findElement(By.tagName("body")).getText();
                if (bodyText.length() > 2000) {
                    bodyText = bodyText.substring(0, 2000) + "... [内容已截断]";
                }
                return String.format("📄 页面标题: %s\n\n📝 页面内容:\n%s", title, bodyText);
            } else {
                WebElement element = driver.findElement(getBy(selector));
                return element.getText();
            }
        } catch (Exception e) {
            return "❌ 获取页面内容失败: " + e.getMessage();
        }
    }

    @Tool(description = "Wait for an element to be visible")
    public String waitForElement(
        @ToolParam(description = "CSS selector or XPath of the element to wait for") String selector,
        @ToolParam(description = "Timeout in seconds (default: 10)") Integer timeoutSeconds,
        @ToolParam(description = "Browser session ID (optional)") String sessionId) {

        if (!isDriverAvailable) {
            return "❌ 浏览器自动化不可用: " + driverSetupError;
        }

        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = "default";
        }
        if (timeoutSeconds == null || timeoutSeconds <= 0) {
            timeoutSeconds = DEFAULT_TIMEOUT;
        }

        try {
            WebDriver driver = drivers.get(sessionId);
            if (driver == null) {
                return "❌ 未找到浏览器会话。请先打开一个页面。";
            }

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            wait.until(ExpectedConditions.visibilityOfElementLocated(getBy(selector)));

            return String.format("✅ 元素已可见: %s", selector);
        } catch (Exception e) {
            return "❌ 等待元素失败: " + e.getMessage();
        }
    }

    @Tool(description = "Take a screenshot of the current page")
    public String takeScreenshot(
        @ToolParam(description = "File name for the screenshot (without extension)") String fileName,
        @ToolParam(description = "Browser session ID (optional)") String sessionId) {

        if (!isDriverAvailable) {
            return "❌ 浏览器自动化不可用: " + driverSetupError;
        }

        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = "default";
        }

        try {
            WebDriver driver = drivers.get(sessionId);
            if (driver == null) {
                return "❌ 未找到浏览器会话。请先打开一个页面。";
            }

            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            byte[] screenshot = takesScreenshot.getScreenshotAs(OutputType.BYTES);

            String screenshotsDir = "screenshots";
            String filePath = screenshotsDir + "/" + fileName + ".png";
            Files.createDirectories(Paths.get(screenshotsDir));
            Files.write(Paths.get(filePath), screenshot);

            return String.format("📸 截图已保存: %s", filePath);
        } catch (Exception e) {
            return "❌ 截图失败: " + e.getMessage();
        }
    }

    @Tool(description = "Close browser session")
    public String closeBrowser(@ToolParam(description = "Browser session ID (optional)") String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = "default";
        }

        try {
            WebDriver driver = drivers.remove(sessionId);
            if (driver != null) {
                driver.quit();
                return "✅ 浏览器会话已关闭: " + sessionId;
            } else {
                return "⚠️ 未找到浏览器会话: " + sessionId;
            }
        } catch (Exception e) {
            return "❌ 关闭浏览器失败: " + e.getMessage();
        }
    }

    @Tool(description = "List all active browser sessions")
    public String listSessions() {
        if (drivers.isEmpty()) {
            return "📋 当前没有活跃的浏览器会话。";
        }

        return "📋 活跃的浏览器会话: " + String.join(", ", drivers.keySet());
    }

    @Tool(description = "Check browser automation status")
    public String checkStatus() {
        if (isDriverAvailable) {
            return "✅ 浏览器自动化功能正常\n活跃会话数: " + drivers.size();
        } else {
            return "❌ 浏览器自动化不可用\n原因: " + driverSetupError +
                "\n\n💡 解决建议:\n" +
                "1. 下载chromedriver.exe到项目的drivers/目录\n" +
                "2. 或将chromedriver.exe添加到系统PATH\n" +
                "3. 确保Chrome浏览器已安装";
        }
    }

    // 获取或创建WebDriver实例
    private WebDriver getOrCreateDriver(String sessionId) {
        return drivers.computeIfAbsent(sessionId, k -> {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--remote-allow-origins=*");

            // 可选：无头模式
            // options.addArguments("--headless");

            return new ChromeDriver(options);
        });
    }

    // 根据选择器类型创建By对象
    private By getBy(String selector) {
        if (selector.startsWith("//") || selector.startsWith("(//")) {
            return By.xpath(selector);
        } else if (selector.startsWith("#")) {
            return By.cssSelector(selector);
        } else if (selector.startsWith(".")) {
            return By.cssSelector(selector);
        } else if (selector.contains("=")) {
            String[] parts = selector.split("=", 2);
            return By.name(parts[1]);
        } else {
            return By.cssSelector(selector);
        }
    }

    // 应用关闭时清理资源
    @PreDestroy
    public void cleanup() {
        drivers.values().forEach(driver -> {
            try {
                driver.quit();
            } catch (Exception e) {
                // 忽略关闭时的异常
            }
        });
        drivers.clear();
    }
}