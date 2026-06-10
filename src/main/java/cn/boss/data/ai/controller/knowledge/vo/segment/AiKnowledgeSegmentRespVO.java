package cn.boss.data.ai.controller.knowledge.vo.segment;

import lombok.Data;

@Data
public class AiKnowledgeSegmentRespVO {

    private Long id;

    private Long documentId;

    private Long knowledgeId;

    private String vectorId;

    private String content;

    private Integer contentLength;

    private Integer tokens;

    private Integer retrievalCount;

    private Integer status;

    private Long createTime;

}
