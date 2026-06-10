package cn.boss.data.ai.controller.knowledge.vo.document;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiKnowledgeDocumentUpdateReqVO {

    @NotNull(message = "编号不能为空")
    private Long id;

    private String name;

    private Integer segmentMaxTokens;

}
