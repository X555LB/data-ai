package cn.boss.data.ai.dal.dataobject.knowledge;

import cn.boss.data.ai.framework.common.enums.CommonStatusEnum;
import cn.boss.data.ai.framework.mybatis.core.dataobject.BaseDO;
import cn.boss.data.ai.dal.dataobject.model.AiModelDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * AI 知识库 DO
 */
@TableName(value = "ai_knowledge", autoResultMap = true)
@KeySequence("ai_knowledge_seq")
@Data
public class AiKnowledgeDO extends BaseDO {

    @TableId
    private Long id;
    private String name;
    private String description;
    /**
     * 向量模型编号
     *
     * 关联 {@link AiModelDO#getId()}
     */
    private Long embeddingModelId;
    /**
     * 模型标识
     *
     * 冗余 {@link AiModelDO#getModel()}
     */
    private String embeddingModel;
    private Integer topK;
    private Double similarityThreshold;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
