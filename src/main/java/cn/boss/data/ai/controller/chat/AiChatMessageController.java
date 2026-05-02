package cn.boss.data.ai.controller.chat;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.boss.data.ai.framework.common.pojo.CommonResult;
import cn.boss.data.ai.framework.common.pojo.PageResult;
import cn.boss.data.ai.framework.common.util.collection.MapUtils;
import cn.boss.data.ai.framework.common.util.object.BeanUtils;
import cn.boss.data.ai.controller.chat.vo.message.AiChatMessagePageReqVO;
import cn.boss.data.ai.controller.chat.vo.message.AiChatMessageRespVO;
import cn.boss.data.ai.controller.chat.vo.message.AiChatMessageSendReqVO;
import cn.boss.data.ai.controller.chat.vo.message.AiChatMessageSendRespVO;
import cn.boss.data.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.boss.data.ai.dal.dataobject.chat.AiChatMessageDO;
import cn.boss.data.ai.dal.dataobject.knowledge.AiKnowledgeDocumentDO;
import cn.boss.data.ai.dal.dataobject.knowledge.AiKnowledgeSegmentDO;
import cn.boss.data.ai.dal.dataobject.model.AiChatRoleDO;
import cn.boss.data.ai.service.chat.AiChatConversationService;
import cn.boss.data.ai.service.chat.AiChatMessageService;
import cn.boss.data.ai.service.knowledge.AiKnowledgeDocumentService;
import cn.boss.data.ai.service.knowledge.AiKnowledgeSegmentService;
import cn.boss.data.ai.service.model.AiChatRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.boss.data.ai.framework.common.pojo.CommonResult.success;
import static cn.boss.data.ai.framework.common.util.collection.CollectionUtils.*;

@Tag(name = "管理后台 - 聊天消息")
@RestController
@RequestMapping("/ai/chat/message")
@Slf4j
public class AiChatMessageController {

    private static final Long DEFAULT_USER_ID = 1L;

    @Resource
    private AiChatMessageService chatMessageService;
    @Resource
    private AiChatConversationService chatConversationService;
    @Resource
    private AiChatRoleService chatRoleService;
    @Resource
    private AiKnowledgeSegmentService knowledgeSegmentService;
    @Resource
    private AiKnowledgeDocumentService knowledgeDocumentService;

    @Operation(summary = "发送消息（段式）", description = "一次性返回，响应较慢")
    @PostMapping("/send")
    public CommonResult<AiChatMessageSendRespVO> sendMessage(@Valid @RequestBody AiChatMessageSendReqVO sendReqVO) {
        return success(chatMessageService.sendMessage(sendReqVO, DEFAULT_USER_ID));
    }

    @Operation(summary = "发送消息（流式）", description = "流式返回，响应较快")
    @PostMapping(value = "/send-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CommonResult<AiChatMessageSendRespVO>> sendChatMessageStream(@Valid @RequestBody AiChatMessageSendReqVO sendReqVO) {
        return chatMessageService.sendChatMessageStream(sendReqVO, DEFAULT_USER_ID);
    }

    @Operation(summary = "获得指定对话的消息列表")
    @GetMapping("/list-by-conversation-id")
    @Parameter(name = "conversationId", required = true, description = "对话编号", example = "1024")
    public CommonResult<List<AiChatMessageRespVO>> getChatMessageListByConversationId(
            @RequestParam("conversationId") Long conversationId) {
        AiChatConversationDO conversation = chatConversationService.getChatConversation(conversationId);
        if (conversation == null || ObjUtil.notEqual(conversation.getUserId(), DEFAULT_USER_ID)) {
            return success(Collections.emptyList());
        }
        // 1. 获取消息列表
        List<AiChatMessageDO> messageList = chatMessageService.getChatMessageListByConversationId(conversationId);
        if (CollUtil.isEmpty(messageList)) {
            return success(Collections.emptyList());
        }

        // 2. 拼接数据，主要是知识库段落信息
        Map<Long, AiKnowledgeSegmentDO> segmentMap = knowledgeSegmentService.getKnowledgeSegmentMap(convertListByFlatMap(messageList,
                message -> CollUtil.isEmpty(message.getSegmentIds()) ? null : message.getSegmentIds().stream()));
        Map<Long, AiKnowledgeDocumentDO> documentMap = knowledgeDocumentService.getKnowledgeDocumentMap(
                convertList(segmentMap.values(), AiKnowledgeSegmentDO::getDocumentId));
        List<AiChatMessageRespVO> messageVOList = BeanUtils.toBean(messageList, AiChatMessageRespVO.class);
        for (int i = 0; i < messageList.size(); i++) {
            AiChatMessageDO message = messageList.get(i);
            if (CollUtil.isEmpty(message.getSegmentIds())) {
                continue;
            }
            // 设置知识库段落信息
            messageVOList.get(i).setSegments(convertList(message.getSegmentIds(), segmentId -> {
                AiKnowledgeSegmentDO segment = segmentMap.get(segmentId);
                if (segment == null) {
                    return null;
                }
                AiKnowledgeDocumentDO document = documentMap.get(segment.getDocumentId());
                if (document == null) {
                    return null;
                }
                return new AiChatMessageRespVO.KnowledgeSegment().setId(segment.getId()).setContent(segment.getContent())
                        .setDocumentId(segment.getDocumentId()).setDocumentName(document.getName());
            }));
        }
        return success(messageVOList);
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/delete")
    @Parameter(name = "id", required = true, description = "消息编号", example = "1024")
    public CommonResult<Boolean> deleteChatMessage(@RequestParam("id") Long id) {
        chatMessageService.deleteChatMessage(id, DEFAULT_USER_ID);
        return success(true);
    }

    @Operation(summary = "删除指定对话的消息")
    @DeleteMapping("/delete-by-conversation-id")
    @Parameter(name = "conversationId", required = true, description = "对话编号", example = "1024")
    public CommonResult<Boolean> deleteChatMessageByConversationId(@RequestParam("conversationId") Long conversationId) {
        chatMessageService.deleteChatMessageByConversationId(conversationId, DEFAULT_USER_ID);
        return success(true);
    }

    // ========== 对话管理 ==========

    @GetMapping("/page")
    @Operation(summary = "获得消息分页", description = "用于【对话管理】菜单")
    public CommonResult<PageResult<AiChatMessageRespVO>> getChatMessagePage(AiChatMessagePageReqVO pageReqVO) {
        PageResult<AiChatMessageDO> pageResult = chatMessageService.getChatMessagePage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }
        // 拼接数据
        Map<Long, AiChatRoleDO> roleMap = chatRoleService.getChatRoleMap(
                convertSet(pageResult.getList(), AiChatMessageDO::getRoleId));
        return success(BeanUtils.toBean(pageResult, AiChatMessageRespVO.class,
                respVO -> MapUtils.findAndThen(roleMap, respVO.getRoleId(),
                        role -> respVO.setRoleName(role.getName()))));
    }

    @Operation(summary = "管理员删除消息")
    @DeleteMapping("/delete-by-admin")
    @Parameter(name = "id", required = true, description = "消息编号", example = "1024")
    public CommonResult<Boolean> deleteChatMessageByAdmin(@RequestParam("id") Long id) {
        chatMessageService.deleteChatMessageByAdmin(id);
        return success(true);
    }

}
