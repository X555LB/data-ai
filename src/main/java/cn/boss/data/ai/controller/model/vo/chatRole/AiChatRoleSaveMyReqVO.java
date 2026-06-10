package cn.boss.data.ai.controller.model.vo.chatRole;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Data
public class AiChatRoleSaveMyReqVO {

    private Long id;

    @NotEmpty(message = "角色名称不能为空")
    private String name;

    @NotEmpty(message = "角色头像不能为空")
    @URL(message = "角色头像必须是 URL 格式")
    private String avatar;

    @NotEmpty(message = "角色描述不能为空")
    private String description;

    @NotEmpty(message = "角色设定不能为空")
    private String systemMessage;

    private List<Long> knowledgeIds;

    private List<Long> toolIds;

    private List<String> mcpClientNames;

}
