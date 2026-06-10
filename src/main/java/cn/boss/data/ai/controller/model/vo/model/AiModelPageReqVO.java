package cn.boss.data.ai.controller.model.vo.model;

import cn.boss.data.ai.framework.common.pojo.PageParam;
import lombok.Data;

@Data
public class AiModelPageReqVO extends PageParam {

    private String name;

    private String model;

    private String platform;

}
