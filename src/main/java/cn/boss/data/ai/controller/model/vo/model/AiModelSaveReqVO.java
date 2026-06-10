package cn.boss.data.ai.controller.model.vo.model;

import cn.boss.data.ai.enums.model.AiModelTypeEnum;
import cn.boss.data.ai.enums.model.AiPlatformEnum;
import cn.boss.data.ai.framework.common.enums.CommonStatusEnum;
import cn.boss.data.ai.framework.common.validation.InEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiModelSaveReqVO {

    private Long id;

    @NotNull(message = "API 秘钥编号不能为空")
    private Long keyId;

    @NotEmpty(message = "模型名字不能为空")
    private String name;

    @NotEmpty(message = "模型标识不能为空")
    private String model;

    @NotEmpty(message = "模型平台不能为空")
    @InEnum(AiPlatformEnum.class)
    private String platform;

    @NotNull(message = "模型类型不能为空")
    @InEnum(AiModelTypeEnum.class)
    private Integer type;

    @NotNull(message = "排序不能为空")
    private Integer sort;

    @InEnum(CommonStatusEnum.class)
    @NotNull(message = "状态不能为空")
    private Integer status;

    private Double temperature;

    private Integer maxTokens;

    private Integer maxContexts;

}
