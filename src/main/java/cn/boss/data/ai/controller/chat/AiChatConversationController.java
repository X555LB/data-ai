package cn.boss.data.ai.controller.chat;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.boss.data.ai.framework.common.pojo.CommonResult;
import cn.boss.data.ai.framework.common.pojo.PageResult;
import cn.boss.data.ai.framework.common.util.object.BeanUtils;
import cn.boss.data.ai.controller.chat.vo.conversation.AiChatConversationCreateMyReqVO;
import cn.boss.data.ai.controller.chat.vo.conversation.AiChatConversationPageReqVO;
import cn.boss.data.ai.controller.chat.vo.conversation.AiChatConversationRespVO;
import cn.boss.data.ai.controller.chat.vo.conversation.AiChatConversationUpdateMyReqVO;
import cn.boss.data.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.boss.data.ai.service.chat.AiChatConversationService;
import cn.boss.data.ai.service.chat.AiChatMessageService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static cn.boss.data.ai.framework.common.pojo.CommonResult.success;
import static cn.boss.data.ai.framework.common.util.collection.CollectionUtils.convertList;

@RestController
@RequestMapping("/ai/chat/conversation")
@Validated
public class AiChatConversationController {

    private static final Long DEFAULT_USER_ID = 1L;

    @Resource
    private AiChatConversationService chatConversationService;
    @Resource
    private AiChatMessageService chatMessageService;

    @PostMapping("/create-my")
    public CommonResult<Long> createChatConversationMy(@RequestBody @Valid AiChatConversationCreateMyReqVO createReqVO) {
        return success(chatConversationService.createChatConversationMy(createReqVO, DEFAULT_USER_ID));
    }

    @PutMapping("/update-my")
    public CommonResult<Boolean> updateChatConversationMy(@RequestBody @Valid AiChatConversationUpdateMyReqVO updateReqVO) {
        chatConversationService.updateChatConversationMy(updateReqVO, DEFAULT_USER_ID);
        return success(true);
    }

    @GetMapping("/my-list")
    public CommonResult<List<AiChatConversationRespVO>> getChatConversationMyList() {
        List<AiChatConversationDO> list = chatConversationService.getChatConversationListByUserId(DEFAULT_USER_ID);
        return success(BeanUtils.toBean(list, AiChatConversationRespVO.class));
    }

    @GetMapping("/get-my")
    public CommonResult<AiChatConversationRespVO> getChatConversationMy(@RequestParam("id") Long id) {
        AiChatConversationDO conversation = chatConversationService.getChatConversation(id);
        if (conversation != null && ObjUtil.notEqual(conversation.getUserId(), DEFAULT_USER_ID)) {
            conversation = null;
        }
        return success(BeanUtils.toBean(conversation, AiChatConversationRespVO.class));
    }

    @DeleteMapping("/delete-my")
    public CommonResult<Boolean> deleteChatConversationMy(@RequestParam("id") Long id) {
        chatConversationService.deleteChatConversationMy(id, DEFAULT_USER_ID);
        return success(true);
    }

    @DeleteMapping("/delete-by-unpinned")
    public CommonResult<Boolean> deleteChatConversationMyByUnpinned() {
        chatConversationService.deleteChatConversationMyByUnpinned(DEFAULT_USER_ID);
        return success(true);
    }

    // ========== 对话管理 ==========

    @GetMapping("/page")
    public CommonResult<PageResult<AiChatConversationRespVO>> getChatConversationPage(AiChatConversationPageReqVO pageReqVO) {
        PageResult<AiChatConversationDO> pageResult = chatConversationService.getChatConversationPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }
        // 拼接关联数据
        Map<Long, Integer> messageCountMap = chatMessageService.getChatMessageCountMap(
                convertList(pageResult.getList(), AiChatConversationDO::getId));
        return success(BeanUtils.toBean(pageResult, AiChatConversationRespVO.class,
                conversation -> conversation.setMessageCount(messageCountMap.getOrDefault(conversation.getId(), 0))));
    }

    @DeleteMapping("/delete-by-admin")
    public CommonResult<Boolean> deleteChatConversationByAdmin(@RequestParam("id") Long id) {
        chatConversationService.deleteChatConversationByAdmin(id);
        return success(true);
    }

}
