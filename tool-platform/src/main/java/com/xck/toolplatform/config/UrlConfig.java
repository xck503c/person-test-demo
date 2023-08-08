package com.xck.toolplatform.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class UrlConfig {

    public final static String TOOL_STATISTICS = "tool/statistics";
    public final static String TOOL_CODING= "tool/coding";

    public final static String TOOL_LINUX_SERVER= "tool/linux/server";

    public final static String GAME_GREEDY_SNAKE = "game/greedySnake";

    public final static String TEST_SMPP = "test/smpp";

    private final static Map<String, Object> defaultUrls = new LinkedHashMap<String, Object>(){{
        Map<String, Object> toolUrls = new LinkedHashMap<String, Object>(){{
            put("文本长度获取", TOOL_STATISTICS);
            put("编码转换处理", TOOL_CODING);
            put("linux服务器", TOOL_LINUX_SERVER);
        }};
        put("工具类", toolUrls);

        Map<String, Object> gameUrls = new LinkedHashMap<String, Object>(){{
            put("贪吃蛇", GAME_GREEDY_SNAKE);
        }};
        put("游戏类", gameUrls);

        Map<String, Object> testUrls = new LinkedHashMap<String, Object>(){{
            put("smpp测试", TEST_SMPP);
        }};
        put("测试类", testUrls);
    }};

    public static Map<String, Object> getDefaultUrls() {
        return defaultUrls;
    }
}
