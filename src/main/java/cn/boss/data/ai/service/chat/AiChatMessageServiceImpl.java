package cn.boss.data.ai.service.chat;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.boss.data.ai.controller.chat.vo.message.AiChatMessagePageReqVO;
import cn.boss.data.ai.controller.chat.vo.message.AiChatMessageRespVO;
import cn.boss.data.ai.controller.chat.vo.message.AiChatMessageSendReqVO;
import cn.boss.data.ai.controller.chat.vo.message.AiChatMessageSendRespVO;
import cn.boss.data.ai.dal.dataobject.chat.AiChatConversationDO;
import cn.boss.data.ai.dal.dataobject.chat.AiChatMessageDO;
import cn.boss.data.ai.dal.dataobject.knowledge.AiKnowledgeDocumentDO;
import cn.boss.data.ai.dal.dataobject.model.AiChatRoleDO;
import cn.boss.data.ai.dal.dataobject.model.AiModelDO;
import cn.boss.data.ai.dal.dataobject.model.AiToolDO;
import cn.boss.data.ai.dal.mysql.chat.AiChatMessageMapper;
import cn.boss.data.ai.enums.ErrorCodeConstants;
import cn.boss.data.ai.enums.model.AiPlatformEnum;
import cn.boss.data.ai.framework.ai.core.websearch.AiWebSearchClient;
import cn.boss.data.ai.framework.ai.core.websearch.AiWebSearchRequest;
import cn.boss.data.ai.framework.ai.core.websearch.AiWebSearchResponse;
import cn.boss.data.ai.framework.common.pojo.CommonResult;
import cn.boss.data.ai.framework.common.pojo.PageResult;
import cn.boss.data.ai.framework.common.util.object.BeanUtils;
import cn.boss.data.ai.service.knowledge.AiKnowledgeDocumentService;
import cn.boss.data.ai.service.knowledge.AiKnowledgeSegmentService;
import cn.boss.data.ai.service.knowledge.bo.AiKnowledgeSegmentSearchReqBO;
import cn.boss.data.ai.service.knowledge.bo.AiKnowledgeSegmentSearchRespBO;
import cn.boss.data.ai.service.model.AiChatRoleService;
import cn.boss.data.ai.service.model.AiModelService;
import cn.boss.data.ai.service.model.AiToolService;
import cn.boss.data.ai.util.AiUtils;
import cn.boss.data.ai.util.FileTypeUtils;
import com.google.common.collect.Maps;
import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.client.common.autoconfigure.properties.McpClientCommonProperties;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static cn.boss.data.ai.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.boss.data.ai.framework.common.pojo.CommonResult.error;
import static cn.boss.data.ai.framework.common.pojo.CommonResult.success;
import static cn.boss.data.ai.framework.common.util.collection.CollectionUtils.convertList;
import static cn.boss.data.ai.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.boss.data.ai.enums.ErrorCodeConstants.CHAT_CONVERSATION_NOT_EXISTS;
import static cn.boss.data.ai.enums.ErrorCodeConstants.CHAT_MESSAGE_NOT_EXIST;

/**
 * AI 聊天消息 Service 实现类
 */
@Service
@Slf4j
public class AiChatMessageServiceImpl implements AiChatMessageService {

    private static final Integer WEB_SEARCH_COUNT = 10;

    private static final String KNOWLEDGE_USER_MESSAGE_TEMPLATE = "使用 <Reference></Reference> 标记中的内容作为本次对话的参考:\n\n" +
            "%s\n\n" +
            "回答要求：\n- 避免提及你是从 <Reference></Reference> 获取的知识。";

    private static final String WEB_SEARCH_USER_MESSAGE_TEMPLATE = "使用 <WebSearch></WebSearch> 标记中的内容作为本次对话的参考:\n\n" +
            "%s\n\n" +
            "回答要求：\n- 避免提及你是从 <WebSearch></WebSearch> 获取的知识。";

    @SuppressWarnings("TextBlockMigration")
    private static final String Attachment_USER_MESSAGE_TEMPLATE = "使用 <Attachment></Attachment> 标记用户对话上传的附件内容:\n\n" +
            "%s\n\n" +
            "回答要求：\n- 避免提及 <Attachment></Attachment> 附件的编码格式。";

    @Resource
    private AiChatMessageMapper chatMessageMapper;

    @Resource
    private AiChatConversationService chatConversationService;
    @Resource
    private AiChatRoleService chatRoleService;
    @Resource
    private AiModelService modalService;
    @Resource
    private AiKnowledgeSegmentService knowledgeSegmentService;
    @Resource
    private AiKnowledgeDocumentService knowledgeDocumentService;
    @Resource
    private AiToolService toolService;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired(required = false)
    private AiWebSearchClient webSearchClient;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired(required = false)
    private List<McpSyncClient> mcpClients;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired(required = false)
    private McpClientCommonProperties mcpClientCommonProperties;

    @Resource
    private ToolCallbackResolver toolCallbackResolver;

    @Transactional(rollbackFor = Exception.class)
    public AiChatMessageSendRespVO sendMessage(AiChatMessageSendReqVO sendReqVO, Long userId) {
        // 1.1 校验对话存在
        AiChatConversationDO conversation = chatConversationService
                .validateChatConversationExists(sendReqVO.getConversationId());
        if (ObjUtil.notEqual(conversation.getUserId(), userId)) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        List<AiChatMessageDO> historyMessages = chatMessageMapper.selectListByConversationId(conversation.getId());
        // 1.2 校验模型
        AiModelDO model = modalService.validateModel(conversation.getModelId());
        ChatModel chatModel = modalService.getChatModel(model.getId());

        // 2.1 知识库召回
        List<AiKnowledgeSegmentSearchRespBO> knowledgeSegments = recallKnowledgeSegment(
                sendReqVO.getContent(), conversation);

        // 2.2 联网搜索
        AiWebSearchResponse webSearchResponse = Boolean.TRUE.equals(sendReqVO.getUseSearch()) && webSearchClient != null ?
                webSearchClient.search(new AiWebSearchRequest().setQuery(sendReqVO.getContent())
                        .setSummary(true).setCount(WEB_SEARCH_COUNT)) : null;

        // 3. 插入 user 发送消息
        AiChatMessageDO userMessage = createChatMessage(conversation.getId(), null, model,
                userId, conversation.getRoleId(), MessageType.USER, sendReqVO.getContent(), sendReqVO.getUseContext(),
                null, sendReqVO.getAttachmentUrls(), null);

        // 4.1 插入 assistant 接收消息
        AiChatMessageDO assistantMessage = createChatMessage(conversation.getId(), userMessage.getId(), model,
                userId, conversation.getRoleId(), MessageType.ASSISTANT, "", sendReqVO.getUseContext(),
                knowledgeSegments, null, webSearchResponse);

        // 4.2 创建 chat 需要的 Prompt
        Prompt prompt = buildPrompt(conversation, historyMessages, knowledgeSegments, webSearchResponse, model, sendReqVO);
        ChatResponse chatResponse = chatModel.call(prompt);

        // 4.3 更新响应内容
        String newContent = AiUtils.getChatResponseContent(chatResponse);
        String newReasoningContent = AiUtils.getChatResponseReasoningContent(chatResponse);
        chatMessageMapper.updateById(new AiChatMessageDO().setId(assistantMessage.getId())
                .setContent(newContent).setReasoningContent(newReasoningContent));
        // 4.4 响应结果
        Map<Long, AiKnowledgeDocumentDO> documentMap = knowledgeDocumentService.getKnowledgeDocumentMap(
                convertSet(knowledgeSegments, AiKnowledgeSegmentSearchRespBO::getDocumentId));
        List<AiChatMessageRespVO.KnowledgeSegment> segments = BeanUtils.toBean(knowledgeSegments,
                AiChatMessageRespVO.KnowledgeSegment.class, segment -> {
                    AiKnowledgeDocumentDO document = documentMap.get(segment.getDocumentId());
                    segment.setDocumentName(document != null ? document.getName() : null);
                });
        return new AiChatMessageSendRespVO()
                .setSend(BeanUtils.toBean(userMessage, AiChatMessageSendRespVO.Message.class))
                .setReceive(BeanUtils.toBean(assistantMessage, AiChatMessageSendRespVO.Message.class)
                        .setContent(newContent).setSegments(segments)
                        .setWebSearchPages(webSearchResponse != null ? webSearchResponse.getLists() : null));
    }

    @Override
    public Flux<CommonResult<AiChatMessageSendRespVO>> sendChatMessageStream(AiChatMessageSendReqVO sendReqVO,
            Long userId) {
        // 1.1 校验对话存在
        AiChatConversationDO conversation = chatConversationService
                .validateChatConversationExists(sendReqVO.getConversationId());
        if (ObjUtil.notEqual(conversation.getUserId(), userId)) {
            throw exception(CHAT_CONVERSATION_NOT_EXISTS);
        }
        List<AiChatMessageDO> historyMessages = chatMessageMapper.selectListByConversationId(conversation.getId());
        // 1.2 校验模型
        AiModelDO model = modalService.validateModel(conversation.getModelId());
        StreamingChatModel chatModel = modalService.getChatModel(model.getId());

        // 2.1 知识库找回
        List<AiKnowledgeSegmentSearchRespBO> knowledgeSegments = recallKnowledgeSegment(
                sendReqVO.getContent(), conversation);

        // 2.2 联网搜索
        AiWebSearchResponse webSearchResponse = Boolean.TRUE.equals(sendReqVO.getUseSearch()) && webSearchClient != null ?
                webSearchClient.search(new AiWebSearchRequest().setQuery(sendReqVO.getContent())
                        .setSummary(true).setCount(WEB_SEARCH_COUNT)) : null;

        // 3. 插入 user 发送消息
        AiChatMessageDO userMessage = createChatMessage(conversation.getId(), null, model,
                userId, conversation.getRoleId(), MessageType.USER, sendReqVO.getContent(), sendReqVO.getUseContext(),
                null, sendReqVO.getAttachmentUrls(), null);

        // 4.1 插入 assistant 接收消息
        AiChatMessageDO assistantMessage = createChatMessage(conversation.getId(), userMessage.getId(), model,
                userId, conversation.getRoleId(), MessageType.ASSISTANT, "", sendReqVO.getUseContext(),
                knowledgeSegments, null, webSearchResponse);

        // 4.2 构建 Prompt，并进行调用
        Prompt prompt = buildPrompt(conversation, historyMessages, knowledgeSegments, webSearchResponse, model, sendReqVO);
        Flux<ChatResponse> streamResponse = chatModel.stream(prompt);

        // 4.3 流式返回
        StringBuffer contentBuffer = new StringBuffer();
        StringBuffer reasoningContentBuffer = new StringBuffer();

        AtomicBoolean firstExecuteFlag = new AtomicBoolean(true);
        AtomicReference<List<AiChatMessageRespVO.KnowledgeSegment>> cacheSegments = new AtomicReference<>();
        AtomicReference<List<AiWebSearchResponse.WebPage>> cacheWebSearchPages = new AtomicReference<>();
        return streamResponse.map(chunk -> {
            // 仅首次：返回知识库、联网搜索
            if (StrUtil.isEmpty(contentBuffer)) {
                if (firstExecuteFlag.compareAndSet(true, false)) {
                    Map<Long, AiKnowledgeDocumentDO> documentMap = knowledgeDocumentService.getKnowledgeDocumentMap(
                            convertSet(knowledgeSegments, AiKnowledgeSegmentSearchRespBO::getDocumentId));
                    cacheSegments.set(BeanUtils.toBean(knowledgeSegments, AiChatMessageRespVO.KnowledgeSegment.class, segment -> {
                        AiKnowledgeDocumentDO document = documentMap.get(segment.getDocumentId());
                        segment.setDocumentName(document != null ? document.getName() : null);
                    }));
                    if (webSearchResponse != null) {
                        cacheWebSearchPages.set(webSearchResponse.getLists());
                    }
                }
            }
            String newContent = AiUtils.getChatResponseContent(chunk);
            String newReasoningContent = AiUtils.getChatResponseReasoningContent(chunk);
            if (StrUtil.isNotEmpty(newContent)) {
                contentBuffer.append(newContent);
            }
            if (StrUtil.isNotEmpty(newReasoningContent)) {
                reasoningContentBuffer.append(newReasoningContent);
            }
            return success(new AiChatMessageSendRespVO()
                    .setSend(BeanUtils.toBean(userMessage, AiChatMessageSendRespVO.Message.class))
                    .setReceive(BeanUtils.toBean(assistantMessage, AiChatMessageSendRespVO.Message.class)
                            .setContent(StrUtil.nullToDefault(newContent, ""))
                            .setReasoningContent(StrUtil.nullToDefault(newReasoningContent, ""))
                            .setSegments(cacheSegments.get()).setWebSearchPages(cacheWebSearchPages.get())));
        }).doOnComplete(() -> {
            chatMessageMapper.updateById(
                    new AiChatMessageDO().setId(assistantMessage.getId()).setContent(contentBuffer.toString())
                            .setReasoningContent(reasoningContentBuffer.toString()));
        }).doOnError(throwable -> {
            log.error("[sendChatMessageStream][userId({}) sendReqVO({}) 发生异常]", userId, sendReqVO, throwable);
            if (StrUtil.isNotEmpty(contentBuffer)) {
                chatMessageMapper.updateById(new AiChatMessageDO().setId(assistantMessage.getId())
                        .setContent(contentBuffer.toString()).setReasoningContent(reasoningContentBuffer.toString()));
            } else {
                chatMessageMapper.deleteById(assistantMessage.getId());
            }
        }).doOnCancel(() -> {
            log.info("[sendChatMessageStream][userId({}) sendReqVO({}) 取消请求]", userId, sendReqVO);
            if (StrUtil.isNotEmpty(contentBuffer)) {
                chatMessageMapper.updateById(new AiChatMessageDO().setId(assistantMessage.getId())
                        .setContent(contentBuffer.toString()).setReasoningContent(reasoningContentBuffer.toString()));
            } else {
                chatMessageMapper.deleteById(assistantMessage.getId());
            }
        }).onErrorResume(error -> Flux.just(error(ErrorCodeConstants.CHAT_STREAM_ERROR)));
    }

    private List<AiKnowledgeSegmentSearchRespBO> recallKnowledgeSegment(String content,
            AiChatConversationDO conversation) {
        if (conversation == null || conversation.getRoleId() == null) {
            return Collections.emptyList();
        }
        AiChatRoleDO role = chatRoleService.getChatRole(conversation.getRoleId());
        if (role == null || CollUtil.isEmpty(role.getKnowledgeIds())) {
            return Collections.emptyList();
        }
        List<AiKnowledgeSegmentSearchRespBO> knowledgeSegments = new ArrayList<>();
        for (Long knowledgeId : role.getKnowledgeIds()) {
            knowledgeSegments.addAll(knowledgeSegmentService.searchKnowledgeSegment(new AiKnowledgeSegmentSearchReqBO()
                    .setKnowledgeId(knowledgeId).setContent(content)));
        }
        return knowledgeSegments;
    }

    private Prompt buildPrompt(AiChatConversationDO conversation, List<AiChatMessageDO> messages,
                               List<AiKnowledgeSegmentSearchRespBO> knowledgeSegments,
                               AiWebSearchResponse webSearchResponse,
                               AiModelDO model, AiChatMessageSendReqVO sendReqVO) {
        List<Message> chatMessages = new ArrayList<>();
        // 1.1 System Context 角色设定
        if (StrUtil.isNotBlank(conversation.getSystemMessage())) {
            chatMessages.add(new SystemMessage(conversation.getSystemMessage()));
        }
        // 1.2 历史 history message 历史消息
        List<AiChatMessageDO> contextMessages = filterContextMessages(messages, conversation, sendReqVO);
        contextMessages.forEach(message -> {
            chatMessages.add(AiUtils.buildMessage(message.getType(), message.getContent()));
            UserMessage attachmentUserMessage = buildAttachmentUserMessage(message.getAttachmentUrls());
            if (attachmentUserMessage != null) {
                chatMessages.add(attachmentUserMessage);
            }
        });
        // 1.3 当前 user message 新发送消息
        chatMessages.add(new UserMessage(sendReqVO.getContent()));
        // 1.4 知识库
        if (CollUtil.isNotEmpty(knowledgeSegments)) {
            String reference = knowledgeSegments.stream()
                    .map(segment -> "<Reference>" + segment.getContent() + "</Reference>")
                    .collect(Collectors.joining("\n\n"));
            chatMessages.add(new UserMessage(String.format(KNOWLEDGE_USER_MESSAGE_TEMPLATE, reference)));
        }
        // 1.5 联网搜索
        if (webSearchResponse != null && CollUtil.isNotEmpty(webSearchResponse.getLists())) {
            String webSearch = webSearchResponse.getLists().stream()
                    .map(page -> "<WebSearch title=\"" + page.getTitle() + "\" url=\"" + page.getUrl() + "\">"
                            + StrUtil.blankToDefault(page.getSummary(), page.getSnippet()) + "</WebSearch>")
                    .collect(Collectors.joining("\n\n"));
            chatMessages.add(new UserMessage(String.format(WEB_SEARCH_USER_MESSAGE_TEMPLATE, webSearch)));
        }
        // 1.6 附件
        if (CollUtil.isNotEmpty(sendReqVO.getAttachmentUrls())) {
            UserMessage attachmentUserMessage = buildAttachmentUserMessage(sendReqVO.getAttachmentUrls());
            if (attachmentUserMessage != null) {
                chatMessages.add(attachmentUserMessage);
            }
        }
        // 2.1 查询 tool 工具
        List<ToolCallback> toolCallbacks = getToolCallbackListByRoleId(conversation.getRoleId());
        Map<String, Object> toolContext = CollUtil.isNotEmpty(toolCallbacks) ? AiUtils.buildCommonToolContext()
                : Map.of();
        // 2.2 构建 ChatOptions 对象
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        ChatOptions chatOptions = AiUtils.buildChatOptions(platform, model.getModel(),
                conversation.getTemperature(), conversation.getMaxTokens(),
                toolCallbacks, toolContext);
        return new Prompt(chatMessages, chatOptions);
    }

    private List<ToolCallback> getToolCallbackListByRoleId(Long roleId) {
        if (roleId == null) {
            return null;
        }
        AiChatRoleDO chatRole = chatRoleService.getChatRole(roleId);
        if (chatRole == null) {
            return null;
        }
        List<ToolCallback> toolCallbacks = new ArrayList<>();
        // 1. 通过 toolIds
        if (CollUtil.isNotEmpty(chatRole.getToolIds())) {
            Set<String> toolNames = convertSet(toolService.getToolList(chatRole.getToolIds()), AiToolDO::getName);
            toolNames.forEach(toolName -> {
                ToolCallback toolCallback = toolCallbackResolver.resolve(toolName);
                if (toolCallback != null) {
                    toolCallbacks.add(toolCallback);
                }
            });
        }
        // 2. 通过 mcpClients
        if (CollUtil.isNotEmpty(mcpClients) && CollUtil.isNotEmpty(chatRole.getMcpClientNames())) {
            chatRole.getMcpClientNames().forEach(mcpClientName -> {
                String finalMcpClientName = mcpClientCommonProperties.getName() + " - " + mcpClientName;
                mcpClients.forEach(mcpClient -> {
                    if (ObjUtil.notEqual(mcpClient.getClientInfo().name(), finalMcpClientName)) {
                        return;
                    }
                    ToolCallback[] mcpToolCallBacks = new SyncMcpToolCallbackProvider(mcpClient).getToolCallbacks();
                    CollUtil.addAll(toolCallbacks, mcpToolCallBacks);
                });
            });
        }
        return toolCallbacks;
    }

    private List<AiChatMessageDO> filterContextMessages(List<AiChatMessageDO> messages,
            AiChatConversationDO conversation,
            AiChatMessageSendReqVO sendReqVO) {
        if (conversation.getMaxContexts() == null || ObjUtil.notEqual(sendReqVO.getUseContext(), Boolean.TRUE)) {
            return Collections.emptyList();
        }
        List<AiChatMessageDO> contextMessages = new ArrayList<>(conversation.getMaxContexts() * 2);
        for (int i = messages.size() - 1; i >= 0; i--) {
            AiChatMessageDO assistantMessage = CollUtil.get(messages, i);
            if (assistantMessage == null || assistantMessage.getReplyId() == null) {
                continue;
            }
            AiChatMessageDO userMessage = CollUtil.get(messages, i - 1);
            if (userMessage == null
                    || ObjUtil.notEqual(assistantMessage.getReplyId(), userMessage.getId())
                    || StrUtil.isEmpty(assistantMessage.getContent())) {
                continue;
            }
            contextMessages.add(assistantMessage);
            contextMessages.add(userMessage);
            if (contextMessages.size() >= conversation.getMaxContexts() * 2) {
                break;
            }
        }
        Collections.reverse(contextMessages);
        return contextMessages;
    }

    private UserMessage buildAttachmentUserMessage(List<String> attachmentUrls) {
        if (CollUtil.isEmpty(attachmentUrls)) {
            return null;
        }
        Map<String, String> attachmentContents = Maps.newLinkedHashMapWithExpectedSize(attachmentUrls.size());
        for (String attachmentUrl : attachmentUrls) {
            try {
                String name = FileNameUtil.getName(attachmentUrl);
                String mineType = FileTypeUtils.getMineType(name);
                String content;
                if (FileTypeUtils.isImage(mineType)) {
                    byte[] bytes = HttpUtil.downloadBytes(attachmentUrl);
                    content = Base64.encode(bytes);
                } else {
                    content = knowledgeDocumentService.readUrl(attachmentUrl);
                }
                if (StrUtil.isNotEmpty(content)) {
                    attachmentContents.put(name, content);
                }
            } catch (Exception e) {
                log.error("[buildAttachmentUserMessage][读取附件({}) 发生异常]", attachmentUrl, e);
            }
        }
        if (CollUtil.isEmpty(attachmentContents)) {
            return null;
        }
        String attachment = attachmentContents.entrySet().stream()
                .map(entry -> "<Attachment name=\"" + entry.getKey() + "\">" + entry.getValue() + "</Attachment>")
                .collect(Collectors.joining("\n\n"));
        return new UserMessage(String.format(Attachment_USER_MESSAGE_TEMPLATE, attachment));
    }

    private AiChatMessageDO createChatMessage(Long conversationId, Long replyId,
                                              AiModelDO model, Long userId, Long roleId,
                                              MessageType messageType, String content, Boolean useContext,
                                              List<AiKnowledgeSegmentSearchRespBO> knowledgeSegments,
                                              List<String> attachmentUrls,
                                              AiWebSearchResponse webSearchResponse) {
        AiChatMessageDO message = new AiChatMessageDO().setConversationId(conversationId).setReplyId(replyId)
                .setModel(model.getModel()).setModelId(model.getId()).setUserId(userId).setRoleId(roleId)
                .setType(messageType.getValue()).setContent(content).setUseContext(useContext)
                .setSegmentIds(convertList(knowledgeSegments, AiKnowledgeSegmentSearchRespBO::getId))
                .setAttachmentUrls(attachmentUrls);
        if (webSearchResponse != null) {
            message.setWebSearchPages(webSearchResponse.getLists());
        }
        message.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(message);
        return message;
    }

    @Override
    public List<AiChatMessageDO> getChatMessageListByConversationId(Long conversationId) {
        return chatMessageMapper.selectListByConversationId(conversationId);
    }

    @Override
    public void deleteChatMessage(Long id, Long userId) {
        AiChatMessageDO message = chatMessageMapper.selectById(id);
        if (message == null || ObjUtil.notEqual(message.getUserId(), userId)) {
            throw exception(CHAT_MESSAGE_NOT_EXIST);
        }
        chatMessageMapper.deleteById(id);
    }

    @Override
    public void deleteChatMessageByConversationId(Long conversationId, Long userId) {
        List<AiChatMessageDO> messages = chatMessageMapper.selectListByConversationId(conversationId);
        if (CollUtil.isEmpty(messages) || ObjUtil.notEqual(messages.get(0).getUserId(), userId)) {
            throw exception(CHAT_MESSAGE_NOT_EXIST);
        }
        chatMessageMapper.deleteByIds(convertList(messages, AiChatMessageDO::getId));
    }

    @Override
    public void deleteChatMessageByAdmin(Long id) {
        AiChatMessageDO message = chatMessageMapper.selectById(id);
        if (message == null) {
            throw exception(CHAT_MESSAGE_NOT_EXIST);
        }
        chatMessageMapper.deleteById(id);
    }

    @Override
    public Map<Long, Integer> getChatMessageCountMap(Collection<Long> conversationIds) {
        return chatMessageMapper.selectCountMapByConversationId(conversationIds);
    }

    @Override
    public PageResult<AiChatMessageDO> getChatMessagePage(AiChatMessagePageReqVO pageReqVO) {
        return chatMessageMapper.selectPage(pageReqVO);
    }

}
