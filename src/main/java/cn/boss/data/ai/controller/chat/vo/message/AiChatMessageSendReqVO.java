package cn.boss.data.ai.controller.chat.vo.message;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AiChatMessageSendReqVO {

    @NotNull(message = "聊天对话编号不能为空")
    private Long conversationId;

    @NotEmpty(message = "聊天内容不能为空")
    private String content;

    private Boolean useContext;

    private Boolean useSearch;

    private List<String> attachmentUrls;

}
