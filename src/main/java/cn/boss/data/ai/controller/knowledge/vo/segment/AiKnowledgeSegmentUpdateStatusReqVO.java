package cn.boss.data.ai.controller.knowledge.vo.segment;

import cn.boss.data.ai.framework.common.enums.CommonStatusEnum;
import cn.boss.data.ai.framework.common.validation.InEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiKnowledgeSegmentUpdateStatusReqVO {

    private Long id;

    @NotNull(message = "是否启用不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
