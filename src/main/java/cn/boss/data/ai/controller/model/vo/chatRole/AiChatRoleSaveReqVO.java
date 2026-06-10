package cn.boss.data.ai.controller.model.vo.chatRole;

import cn.boss.data.ai.framework.common.enums.CommonStatusEnum;
import cn.boss.data.ai.framework.common.validation.InEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Data
public class AiChatRoleSaveReqVO {

    private Long id;

    private Long modelId;

    @NotEmpty(message = "角色名称不能为空")
    private String name;

    @NotEmpty(message = "角色头像不能为空")
    @URL(message = "角色头像必须是 URL 格式")
    private String avatar;

    @NotEmpty(message = "角色类别不能为空")
    private String category;

    @NotNull(message = "角色排序不能为空")
    private Integer sort;

    @NotEmpty(message = "角色描述不能为空")
    private String description;

    @NotEmpty(message = "角色设定不能为空")
    private String systemMessage;

    private List<Long> knowledgeIds;

    private List<Long> toolIds;

    private List<String> mcpClientNames;

    @NotNull(message = "是否公开不能为空")
    private Boolean publicStatus;

    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
