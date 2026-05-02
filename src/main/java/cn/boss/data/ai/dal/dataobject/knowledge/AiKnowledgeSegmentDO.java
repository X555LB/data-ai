package cn.boss.data.ai.dal.dataobject.knowledge;

import cn.boss.data.ai.framework.common.enums.CommonStatusEnum;
import cn.boss.data.ai.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * AI 知识库-文档分段 DO
 */
@TableName(value = "ai_knowledge_segment")
@KeySequence("ai_knowledge_segment_seq")
@Data
public class AiKnowledgeSegmentDO extends BaseDO {

    public static final String VECTOR_ID_EMPTY = "";

    @TableId
    private Long id;
    /**
     * 知识库编号
     *
     * 关联 {@link AiKnowledgeDO#getId()}
     */
    private Long knowledgeId;
    /**
     * 文档编号
     *
     * 关联 {@link AiKnowledgeDocumentDO#getId()}
     */
    private Long documentId;
    private String content;
    private Integer contentLength;
    private String vectorId;
    private Integer tokens;
    private Integer retrievalCount;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
