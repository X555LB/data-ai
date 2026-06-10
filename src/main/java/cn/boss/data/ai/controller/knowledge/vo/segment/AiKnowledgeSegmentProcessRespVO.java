package cn.boss.data.ai.controller.knowledge.vo.segment;

import lombok.Data;

@Data
public class AiKnowledgeSegmentProcessRespVO {

    private Long documentId;

    private Long count;

    private Long embeddingCount;

}
