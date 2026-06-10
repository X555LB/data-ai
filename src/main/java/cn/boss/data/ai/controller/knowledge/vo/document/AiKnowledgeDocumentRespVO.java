package cn.boss.data.ai.controller.knowledge.vo.document;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiKnowledgeDocumentRespVO {

    private Long id;

    private Long knowledgeId;

    private String name;

    private String url;

    private String content;

    private Integer contentLength;

    private Integer tokens;

    private Integer segmentMaxTokens;

    private Integer retrievalCount;

    private Integer status;

    private LocalDateTime createTime;

}
