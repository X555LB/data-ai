package cn.boss.data.ai.dal.dataobject.chat;

import cn.boss.data.ai.framework.mybatis.core.dataobject.BaseDO;
import cn.boss.data.ai.framework.mybatis.core.type.LongListTypeHandler;
import cn.boss.data.ai.framework.mybatis.core.type.StringListTypeHandler;
import cn.boss.data.ai.dal.dataobject.knowledge.AiKnowledgeSegmentDO;
import cn.boss.data.ai.dal.dataobject.model.AiChatRoleDO;
import cn.boss.data.ai.dal.dataobject.model.AiModelDO;
import cn.boss.data.ai.framework.ai.core.websearch.AiWebSearchResponse;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.MessageType;

import java.util.List;

/**
 * AI Chat 消息 DO
 */
@TableName(value = "ai_chat_message", autoResultMap = true)
@KeySequence("ai_chat_message_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatMessageDO extends BaseDO {

    @TableId
    private Long id;
    /**
     * 对话编号
     *
     * 关联 {@link AiChatConversationDO#getId()}
     */
    private Long conversationId;
    /**
     * 回复消息编号
     */
    private Long replyId;
    /**
     * 消息类型
     *
     * 枚举 {@link MessageType}
     */
    private String type;
    private Long userId;
    /**
     * 角色编号
     *
     * 关联 {@link AiChatRoleDO#getId()}
     */
    private Long roleId;
    /**
     * 模型标志
     *
     * 冗余 {@link AiModelDO#getModel()}
     */
    private String model;
    /**
     * 模型编号
     *
     * 关联 {@link AiModelDO#getId()}
     */
    private Long modelId;

    private String content;
    private String reasoningContent;
    private Boolean useContext;

    /**
     * 知识库段落编号数组
     *
     * 关联 {@link AiKnowledgeSegmentDO#getId()}
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> segmentIds;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<AiWebSearchResponse.WebPage> webSearchPages;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> attachmentUrls;

}
