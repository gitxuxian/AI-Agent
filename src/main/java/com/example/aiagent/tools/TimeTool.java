package com.example.aiagent.tools;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.zone.ZoneRulesException;


/**
 * 获取当前时间的工具
 */
@Component
public class TimeTool {

    @Tool(description = "获取当前时间，支持指定时区和格式化")
    public String getCurrentTime(
        @ToolParam(description = "时区ID，例如 'Asia/Shanghai', 'UTC', 'America/New_York'，默认为系统时区")
        String timezone,
        @ToolParam(description = "时间格式，例如 'yyyy-MM-dd HH:mm:ss', 'yyyy年MM月dd日 HH:mm:ss'，默认为 'yyyy-MM-dd HH:mm:ss'")
        String format) {

        try {
            // 处理时区参数
            ZoneId zoneId = (timezone != null && !timezone.trim().isEmpty())
                ? ZoneId.of(timezone.trim())
                : ZoneId.systemDefault();

            // 处理格式参数
            String timeFormat = (format != null && !format.trim().isEmpty())
                ? format.trim()
                : "yyyy-MM-dd HH:mm:ss";

            // 获取当前时间
            ZonedDateTime currentTime = ZonedDateTime.now(zoneId);

            // 格式化时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timeFormat);
            String formattedTime = currentTime.format(formatter);

            return String.format("当前时间: %s (时区: %s)", formattedTime, zoneId.toString());

        } catch (ZoneRulesException e) {
            return "错误：无效的时区 '" + timezone + "'。请使用有效的时区ID，例如 'Asia/Shanghai', 'UTC', 'America/New_York' 等。";
        } catch (Exception e) {
            return "获取时间时发生错误: " + e.getMessage();
        }
    }

    @Tool(description = "获取指定时区的当前时间戳（毫秒）")
    public String getCurrentTimestamp(
        @ToolParam(description = "时区ID，例如 'Asia/Shanghai', 'UTC'，默认为系统时区")
        String timezone) {

        try {
            ZoneId zoneId = (timezone != null && !timezone.trim().isEmpty())
                ? ZoneId.of(timezone.trim())
                : ZoneId.systemDefault();

            ZonedDateTime currentTime = ZonedDateTime.now(zoneId);
            long timestamp = currentTime.toInstant().toEpochMilli();

            return String.format("当前时间戳: %d (时区: %s, 对应时间: %s)",
                timestamp,
                zoneId.toString(),
                currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        } catch (ZoneRulesException e) {
            return "错误：无效的时区 '" + timezone + "'。";
        } catch (Exception e) {
            return "获取时间戳时发生错误: " + e.getMessage();
        }
    }

    @Tool(description = "比较两个时区的当前时间")
    public String compareTimeZones(
        @ToolParam(description = "第一个时区ID，例如 'Asia/Shanghai'")
        String timezone1,
        @ToolParam(description = "第二个时区ID，例如 'America/New_York'")
        String timezone2) {

        try {
            if (timezone1 == null || timezone1.trim().isEmpty() ||
                timezone2 == null || timezone2.trim().isEmpty()) {
                return "错误：请提供两个有效的时区ID。";
            }

            ZoneId zone1 = ZoneId.of(timezone1.trim());
            ZoneId zone2 = ZoneId.of(timezone2.trim());

            ZonedDateTime time1 = ZonedDateTime.now(zone1);
            ZonedDateTime time2 = ZonedDateTime.now(zone2);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            long timeDiffHours = (time1.toInstant().toEpochMilli() - time2.toInstant().toEpochMilli()) / (1000 * 60 * 60);

            return String.format(
                "%s: %s\n%s: %s\n时差: %s小时",
                timezone1, time1.format(formatter),
                timezone2, time2.format(formatter),
                timeDiffHours > 0 ? "+" + timeDiffHours : timeDiffHours
            );

        } catch (ZoneRulesException e) {
            return "错误：无效的时区。请检查时区ID是否正确。";
        } catch (Exception e) {
            return "比较时区时发生错误: " + e.getMessage();
        }
    }
}