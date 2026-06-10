package cn.boss.data.ai.controller.model.vo.apikey;

import cn.boss.data.ai.framework.common.pojo.PageParam;
import lombok.Data;

@Data
public class AiApiKeyPageReqVO extends PageParam {

    private String name;

    private String platform;

    private Integer status;

}
