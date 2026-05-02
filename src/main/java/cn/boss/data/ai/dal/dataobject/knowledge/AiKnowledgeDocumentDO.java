package cn.boss.data.ai.dal.dataobject.knowledge;

import cn.boss.data.ai.framework.common.enums.CommonStatusEnum;
import cn.boss.data.ai.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * AI 知识库-文档 DO
 */
@TableName(value = "ai_knowledge_document")
@KeySequence("ai_knowledge_document_seq")
@Data
public class AiKnowledgeDocumentDO extends BaseDO {

    @TableId
    private Long id;
    /**
     * 知识库编号
     *
     * 关联 {@link AiKnowledgeDO#getId()}
     */
    private Long knowledgeId;
    private String name;
    private String url;
    private String content;
    private Integer contentLength;
    private Integer tokens;
    private Integer segmentMaxTokens;
    private Integer retrievalCount;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
