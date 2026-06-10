package cn.boss.data.ai.controller.knowledge.vo.segment;

import lombok.Data;

@Data
public class AiKnowledgeSegmentSearchRespVO extends AiKnowledgeSegmentRespVO {

    private String documentName;

    private Double score;

}
