package cn.boss.data.ai.controller.knowledge.vo.segment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiKnowledgeSegmentSearchReqVO {

    @NotNull(message = "知识库编号不能为空")
    private Long knowledgeId;

    @NotEmpty(message = "内容不能为空")
    private String content;

    private Integer topK;

    private Double similarityThreshold;

}
