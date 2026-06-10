package cn.boss.data.ai.controller.chat.vo.conversation;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiChatConversationRespVO {

    private Long id;

    private Long userId;

    private String title;

    private Boolean pinned;

    private Long roleId;

    private Long modelId;

    private String model;

    private String modelName;

    private String systemMessage;

    private Double temperature;

    private Integer maxTokens;

    private Integer maxContexts;

    private LocalDateTime createTime;

    // ========== 关联 role 信息 ==========

    private String roleAvatar;

    private String roleName;

    // ========== 仅在【对话管理】时加载 ==========

    private Integer messageCount;

}
