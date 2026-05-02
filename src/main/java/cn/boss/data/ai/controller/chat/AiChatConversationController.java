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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static cn.boss.data.ai.framework.common.pojo.CommonResult.success;
import static cn.boss.data.ai.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - AI 聊天对话")
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
    @Operation(summary = "创建【我的】聊天对话")
    public CommonResult<Long> createChatConversationMy(@RequestBody @Valid AiChatConversationCreateMyReqVO createReqVO) {
        return success(chatConversationService.createChatConversationMy(createReqVO, DEFAULT_USER_ID));
    }

    @PutMapping("/update-my")
    @Operation(summary = "更新【我的】聊天对话")
    public CommonResult<Boolean> updateChatConversationMy(@RequestBody @Valid AiChatConversationUpdateMyReqVO updateReqVO) {
        chatConversationService.updateChatConversationMy(updateReqVO, DEFAULT_USER_ID);
        return success(true);
    }

    @GetMapping("/my-list")
    @Operation(summary = "获得【我的】聊天对话列表")
    public CommonResult<List<AiChatConversationRespVO>> getChatConversationMyList() {
        List<AiChatConversationDO> list = chatConversationService.getChatConversationListByUserId(DEFAULT_USER_ID);
        return success(BeanUtils.toBean(list, AiChatConversationRespVO.class));
    }

    @GetMapping("/get-my")
    @Operation(summary = "获得【我的】聊天对话")
    @Parameter(name = "id", required = true, description = "对话编号", example = "1024")
    public CommonResult<AiChatConversationRespVO> getChatConversationMy(@RequestParam("id") Long id) {
        AiChatConversationDO conversation = chatConversationService.getChatConversation(id);
        if (conversation != null && ObjUtil.notEqual(conversation.getUserId(), DEFAULT_USER_ID)) {
            conversation = null;
        }
        return success(BeanUtils.toBean(conversation, AiChatConversationRespVO.class));
    }

    @DeleteMapping("/delete-my")
    @Operation(summary = "删除聊天对话")
    @Parameter(name = "id", required = true, description = "对话编号", example = "1024")
    public CommonResult<Boolean> deleteChatConversationMy(@RequestParam("id") Long id) {
        chatConversationService.deleteChatConversationMy(id, DEFAULT_USER_ID);
        return success(true);
    }

    @DeleteMapping("/delete-by-unpinned")
    @Operation(summary = "删除未置顶的聊天对话")
    public CommonResult<Boolean> deleteChatConversationMyByUnpinned() {
        chatConversationService.deleteChatConversationMyByUnpinned(DEFAULT_USER_ID);
        return success(true);
    }

    // ========== 对话管理 ==========

    @GetMapping("/page")
    @Operation(summary = "获得对话分页", description = "用于【对话管理】菜单")
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

    @Operation(summary = "管理员删除对话")
    @DeleteMapping("/delete-by-admin")
    @Parameter(name = "id", required = true, description = "对话编号", example = "1024")
    public CommonResult<Boolean> deleteChatConversationByAdmin(@RequestParam("id") Long id) {
        chatConversationService.deleteChatConversationByAdmin(id);
        return success(true);
    }

}
