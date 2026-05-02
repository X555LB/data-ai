package cn.boss.data.ai.dal.dataobject.chat;

import cn.boss.data.ai.framework.mybatis.core.dataobject.BaseDO;
import cn.boss.data.ai.dal.dataobject.model.AiChatRoleDO;
import cn.boss.data.ai.dal.dataobject.model.AiModelDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI Chat 对话 DO
 */
@TableName("ai_chat_conversation")
@KeySequence("ai_chat_conversation_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatConversationDO extends BaseDO {

    public static final String TITLE_DEFAULT = "新对话";

    @TableId
    private Long id;
    private Long userId;
    private String title;
    private Boolean pinned;
    private LocalDateTime pinnedTime;
    /**
     * 角色编号
     *
     * 关联 {@link AiChatRoleDO#getId()}
     */
    private Long roleId;
    /**
     * 模型编号
     *
     * 关联 {@link AiModelDO#getId()}
     */
    private Long modelId;
    /**
     * 模型标志
     *
     * 冗余 {@link AiModelDO#getModel()}
     */
    private String model;

    // ========== 对话配置 ==========

    private String systemMessage;
    private Double temperature;
    private Integer maxTokens;
    private Integer maxContexts;

}
