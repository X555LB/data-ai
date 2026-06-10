package cn.boss.data.ai.controller.knowledge.vo.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Data
public class AiKnowledgeDocumentCreateListReqVO {

    @NotNull(message = "知识库编号不能为空")
    private Long knowledgeId;

    @NotNull(message = "分段的最大 Token 数不能为空")
    private Integer segmentMaxTokens;

    @NotEmpty(message = "文档列表不能为空")
    private List<Document> list;

    @Data
    public static class Document {

        @NotBlank(message = "文档名称不能为空")
        private String name;

        @URL(message = "文档 URL 格式不正确")
        private String url;

    }

}
