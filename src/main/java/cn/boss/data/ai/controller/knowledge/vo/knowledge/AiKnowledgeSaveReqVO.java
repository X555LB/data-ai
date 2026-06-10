package cn.boss.data.ai.controller.knowledge.vo.knowledge;

import cn.boss.data.ai.framework.common.enums.CommonStatusEnum;
import cn.boss.data.ai.framework.common.validation.InEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiKnowledgeSaveReqVO {

    private Long id;

    @NotBlank(message = "知识库名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "向量模型不能为空")
    private Long embeddingModelId;

    @NotNull(message = "topK 不能为空")
    private Integer topK;

    @NotNull(message = "相似性阈值不能为空")
    private Double similarityThreshold;

    @NotNull(message = "是否启用不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
