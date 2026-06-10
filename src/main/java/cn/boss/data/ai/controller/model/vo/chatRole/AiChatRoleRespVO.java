package cn.boss.data.ai.controller.model.vo.chatRole;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AiChatRoleRespVO {

    private Long id;

    private Long userId;

    private Long modelId;
    private String modelName;
    private String model;

    private String name;

    private String avatar;

    private String category;

    private Integer sort;

    private String description;

    private String systemMessage;

    private List<Long> knowledgeIds;

    private List<Long> toolIds;

    private List<String> mcpClientNames;

    private Boolean publicStatus;

    private Integer status;

    private LocalDateTime createTime;

}
