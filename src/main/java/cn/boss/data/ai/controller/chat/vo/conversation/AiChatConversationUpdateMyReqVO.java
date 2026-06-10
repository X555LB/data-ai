package cn.boss.data.ai.controller.chat.vo.conversation;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiChatConversationUpdateMyReqVO {

    @NotNull(message = "对话编号不能为空")
    private Long id;

    private String title;

    private Boolean pinned;

    private Long modelId;

    private Long knowledgeId;

    private String systemMessage;

    private Double temperature;

    private Integer maxTokens;

    private Integer maxContexts;

}
