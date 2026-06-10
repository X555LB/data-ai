package cn.boss.data.ai.controller.model.vo.apikey;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiApiKeySaveReqVO {

    private Long id;

    @NotEmpty(message = "名称不能为空")
    private String name;

    @NotEmpty(message = "密钥不能为空")
    private String apiKey;

    @NotEmpty(message = "平台不能为空")
    private String platform;

    private String url;

    @NotNull(message = "状态不能为空")
    private Integer status;

}
