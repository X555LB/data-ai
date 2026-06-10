package cn.boss.data.ai.controller.knowledge.vo.segment;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AiKnowledgeSegmentSaveReqVO {

    private Long id;

    private Long documentId;

    @NotEmpty(message = "切片内容不能为空")
    private String content;

}
