package cn.boss.data.ai.controller.chat.vo.message;

import cn.boss.data.ai.framework.ai.core.websearch.AiWebSearchResponse;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AiChatMessageRespVO {

    private Long id;

    private Long conversationId;

    private Long replyId;

    private String type; // 参见 MessageType 枚举类

    private Long userId;

    private Long roleId;

    private String model;

    private Long modelId;

    private String content;

    private String reasoningContent;

    private Boolean useContext;

    private List<Long> segmentIds;

    private List<KnowledgeSegment> segments;

    private List<AiWebSearchResponse.WebPage> webSearchPages;

    private List<String> attachmentUrls;

    private LocalDateTime createTime;

    // ========== 仅在【对话管理】时加载 ==========

    private String roleName;

    @Data
    public static class KnowledgeSegment {

        private Long id;

        private String content;

        private Long documentId;

        private String documentName;

    }

}
