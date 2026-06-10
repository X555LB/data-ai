package cn.boss.data.ai.controller.knowledge.vo.document;

import cn.boss.data.ai.framework.common.enums.CommonStatusEnum;
import cn.boss.data.ai.framework.common.validation.InEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiKnowledgeDocumentUpdateStatusReqVO {

    @NotNull(message = "编号不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
