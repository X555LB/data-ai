package cn.boss.data.ai.controller.knowledge.vo.knowledge;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiKnowledgeRespVO {

    private Long id;

    private String name;

    private String description;

    private Long embeddingModelId;

    private String embeddingModel;

    private Integer topK;

    private Double similarityThreshold;

    private Integer status;

    private LocalDateTime createTime;

}
