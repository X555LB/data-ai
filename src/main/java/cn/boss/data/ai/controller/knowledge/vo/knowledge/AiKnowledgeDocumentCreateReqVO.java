package cn.boss.data.ai.controller.knowledge.vo.knowledge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class AiKnowledgeDocumentCreateReqVO {

    @NotNull(message = "知识库编号不能为空")
    private Long knowledgeId;

    @NotBlank(message = "文档名称不能为空")
    private String name;

    @URL(message = "文档 URL 格式不正确")
    private String url;

    @NotNull(message = "分段的最大 Token 数不能为空")
    private Integer segmentMaxTokens;

}
