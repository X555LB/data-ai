package cn.boss.data.ai.controller.model.vo.tool;

import cn.boss.data.ai.framework.common.enums.CommonStatusEnum;
import cn.boss.data.ai.framework.common.validation.InEnum;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AiToolSaveReqVO {

    private Long id;

    @NotEmpty(message = "工具名称不能为空")
    private String name;

    private String description;

    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
