package cn.boss.data.ai.dal.dataobject.model;

import cn.boss.data.ai.framework.common.enums.CommonStatusEnum;
import cn.boss.data.ai.framework.mybatis.core.dataobject.BaseDO;
import cn.boss.data.ai.framework.mybatis.core.type.LongListTypeHandler;
import cn.boss.data.ai.framework.mybatis.core.type.StringListTypeHandler;
import cn.boss.data.ai.dal.dataobject.knowledge.AiKnowledgeDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.util.List;

/**
 * AI 聊天角色 DO
 */
@TableName(value = "ai_chat_role", autoResultMap = true)
@KeySequence("ai_chat_role_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRoleDO extends BaseDO {

    @TableId
    private Long id;
    private String name;
    private String avatar;
    private String category;
    private String description;
    private String systemMessage;
    private Long userId;
    /**
     * 模型编号
     *
     * 关联 {@link AiModelDO#getId()}
     */
    private Long modelId;

    /**
     * 引用的知识库编号列表
     *
     * 关联 {@link AiKnowledgeDO#getId()}
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> knowledgeIds;
    /**
     * 引用的工具编号列表
     *
     * 关联 {@link AiToolDO#getId()}
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> toolIds;
    /**
     * 引用的 MCP Client 名字列表
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> mcpClientNames;

    private Boolean publicStatus;
    private Integer sort;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
