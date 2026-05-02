package cn.boss.data.ai.framework.ai.core.websearch;

import lombok.Data;

import java.util.List;

@Data
public class AiWebSearchResponse {

    /**
     * 总数（总共匹配的网页数）
     */
    private Long total;

    /**
     * 数据列表
     */
    private List<WebPage> lists;

    /**
     * 网页对象
     */
    @Data
    public static class WebPage {

        private String name;
        private String icon;
        private String title;
        private String url;
        private String snippet;
        private String summary;

    }

}
