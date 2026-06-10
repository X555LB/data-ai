package cn.boss.data.ai.controller.chat.vo.message;

import cn.boss.data.ai.framework.ai.core.websearch.AiWebSearchResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AiChatMessageSendRespVO {

    private Message send;

    private Message receive;

    @Data
    public static class Message {

        private Long id;

        private String type; // 参见 MessageType 枚举类

        private String content;

        private String reasoningContent;

        private List<Long> segmentIds;

        private List<AiChatMessageRespVO.KnowledgeSegment> segments;

        private List<AiWebSearchResponse.WebPage> webSearchPages;

        private LocalDateTime createTime;

    }

}
