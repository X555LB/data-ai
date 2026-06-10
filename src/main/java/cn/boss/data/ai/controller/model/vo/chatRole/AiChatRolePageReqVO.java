package cn.boss.data.ai.controller.model.vo.chatRole;

import cn.boss.data.ai.framework.common.pojo.PageParam;
import lombok.Data;

@Data
public class AiChatRolePageReqVO extends PageParam {

    private String name;

    private String category;

    private Boolean publicStatus;

}
