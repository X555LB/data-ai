package cn.boss.data.ai.service.chat;

import cn.boss.data.ai.controller.chat.vo.conversation.AiChatConversationCreateMyReqVO;
import cn.boss.data.ai.controller.chat.vo.conversation.AiChatConversationPageReqVO;
import cn.boss.data.ai.controller.chat.vo.conversation.AiChatConversationUpdateMyReqVO;
import cn.boss.data.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.boss.data.ai.framework.common.pojo.PageResult;

import java.util.List;

/**
 * AI 聊天对话 Service 接口
 */
public interface AiChatConversationService {

    Long createChatConversationMy(AiChatConversationCreateMyReqVO createReqVO, Long userId);

    void updateChatConversationMy(AiChatConversationUpdateMyReqVO updateReqVO, Long userId);

    List<AiChatConversationDO> getChatConversationListByUserId(Long userId);

    AiChatConversationDO getChatConversation(Long id);

    void deleteChatConversationMy(Long id, Long userId);

    void deleteChatConversationByAdmin(Long id);

    AiChatConversationDO validateChatConversationExists(Long id);

    void deleteChatConversationMyByUnpinned(Long userId);

    PageResult<AiChatConversationDO> getChatConversationPage(AiChatConversationPageReqVO pageReqVO);

}
