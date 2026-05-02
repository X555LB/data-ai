package cn.boss.data.ai.service.chat;

import cn.boss.data.ai.controller.chat.vo.message.AiChatMessagePageReqVO;
import cn.boss.data.ai.controller.chat.vo.message.AiChatMessageSendReqVO;
import cn.boss.data.ai.controller.chat.vo.message.AiChatMessageSendRespVO;
import cn.boss.data.ai.dal.dataobject.chat.AiChatMessageDO;
import cn.boss.data.ai.framework.common.pojo.CommonResult;
import cn.boss.data.ai.framework.common.pojo.PageResult;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * AI 聊天消息 Service 接口
 */
public interface AiChatMessageService {

    AiChatMessageSendRespVO sendMessage(AiChatMessageSendReqVO sendReqVO, Long userId);

    Flux<CommonResult<AiChatMessageSendRespVO>> sendChatMessageStream(AiChatMessageSendReqVO sendReqVO, Long userId);

    List<AiChatMessageDO> getChatMessageListByConversationId(Long conversationId);

    void deleteChatMessage(Long id, Long userId);

    void deleteChatMessageByConversationId(Long conversationId, Long userId);

    void deleteChatMessageByAdmin(Long id);

    Map<Long, Integer> getChatMessageCountMap(Collection<Long> conversationIds);

    PageResult<AiChatMessageDO> getChatMessagePage(AiChatMessagePageReqVO pageReqVO);

}
