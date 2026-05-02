package cn.boss.data.ai.service.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.boss.data.ai.controller.model.vo.chatRole.AiChatRolePageReqVO;
import cn.boss.data.ai.controller.model.vo.chatRole.AiChatRoleSaveMyReqVO;
import cn.boss.data.ai.controller.model.vo.chatRole.AiChatRoleSaveReqVO;
import cn.boss.data.ai.dal.dataobject.model.AiChatRoleDO;
import cn.boss.data.ai.dal.mysql.model.AiChatRoleMapper;
import cn.boss.data.ai.framework.common.enums.CommonStatusEnum;
import cn.boss.data.ai.framework.common.pojo.PageResult;
import cn.boss.data.ai.framework.common.util.object.BeanUtils;
import cn.boss.data.ai.service.knowledge.AiKnowledgeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.boss.data.ai.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.boss.data.ai.framework.common.util.collection.CollectionUtils.convertList;
import static cn.boss.data.ai.enums.ErrorCodeConstants.CHAT_ROLE_DISABLE;
import static cn.boss.data.ai.enums.ErrorCodeConstants.CHAT_ROLE_NOT_EXISTS;

/**
 * AI 聊天角色 Service 实现类
 */
@Service
@Slf4j
public class AiChatRoleServiceImpl implements AiChatRoleService {

    @Resource
    private AiChatRoleMapper chatRoleMapper;

    @Resource
    private AiKnowledgeService knowledgeService;
    @Resource
    private AiToolService toolService;

    @Override
    public Long createChatRole(AiChatRoleSaveReqVO createReqVO) {
        validateDocuments(createReqVO.getKnowledgeIds());
        validateTools(createReqVO.getToolIds());
        AiChatRoleDO chatRole = BeanUtils.toBean(createReqVO, AiChatRoleDO.class);
        chatRoleMapper.insert(chatRole);
        return chatRole.getId();
    }

    @Override
    public Long createChatRoleMy(AiChatRoleSaveMyReqVO createReqVO, Long userId) {
        validateDocuments(createReqVO.getKnowledgeIds());
        validateTools(createReqVO.getToolIds());
        AiChatRoleDO chatRole = BeanUtils.toBean(createReqVO, AiChatRoleDO.class).setUserId(userId)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setPublicStatus(false);
        chatRoleMapper.insert(chatRole);
        return chatRole.getId();
    }

    @Override
    public void updateChatRole(AiChatRoleSaveReqVO updateReqVO) {
        validateChatRoleExists(updateReqVO.getId());
        validateDocuments(updateReqVO.getKnowledgeIds());
        validateTools(updateReqVO.getToolIds());
        AiChatRoleDO updateObj = BeanUtils.toBean(updateReqVO, AiChatRoleDO.class);
        chatRoleMapper.updateById(updateObj);
    }

    @Override
    public void updateChatRoleMy(AiChatRoleSaveMyReqVO updateReqVO, Long userId) {
        AiChatRoleDO chatRole = validateChatRoleExists(updateReqVO.getId());
        if (ObjectUtil.notEqual(chatRole.getUserId(), userId)) {
            throw exception(CHAT_ROLE_NOT_EXISTS);
        }
        validateDocuments(updateReqVO.getKnowledgeIds());
        validateTools(updateReqVO.getToolIds());
        AiChatRoleDO updateObj = BeanUtils.toBean(updateReqVO, AiChatRoleDO.class);
        chatRoleMapper.updateById(updateObj);
    }

    private void validateDocuments(List<Long> knowledgeIds) {
        if (CollUtil.isEmpty(knowledgeIds)) {
            return;
        }
        knowledgeIds.forEach(knowledgeService::validateKnowledgeExists);
    }

    private void validateTools(List<Long> toolIds) {
        if (CollUtil.isEmpty(toolIds)) {
            return;
        }
        toolIds.forEach(toolService::validateToolExists);
    }

    @Override
    public void deleteChatRole(Long id) {
        validateChatRoleExists(id);
        chatRoleMapper.deleteById(id);
    }

    @Override
    public void deleteChatRoleMy(Long id, Long userId) {
        AiChatRoleDO chatRole = validateChatRoleExists(id);
        if (ObjectUtil.notEqual(chatRole.getUserId(), userId)) {
            throw exception(CHAT_ROLE_NOT_EXISTS);
        }
        chatRoleMapper.deleteById(id);
    }

    private AiChatRoleDO validateChatRoleExists(Long id) {
        AiChatRoleDO chatRole = chatRoleMapper.selectById(id);
        if (chatRole == null) {
            throw exception(CHAT_ROLE_NOT_EXISTS);
        }
        return chatRole;
    }

    @Override
    public AiChatRoleDO getChatRole(Long id) {
        return chatRoleMapper.selectById(id);
    }

    @Override
    public List<AiChatRoleDO> getChatRoleList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return chatRoleMapper.selectByIds(ids);
    }

    @Override
    public AiChatRoleDO validateChatRole(Long id) {
        AiChatRoleDO chatRole = validateChatRoleExists(id);
        if (CommonStatusEnum.isDisable(chatRole.getStatus())) {
            throw exception(CHAT_ROLE_DISABLE, chatRole.getName());
        }
        return chatRole;
    }

    @Override
    public PageResult<AiChatRoleDO> getChatRolePage(AiChatRolePageReqVO pageReqVO) {
        return chatRoleMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<AiChatRoleDO> getChatRoleMyPage(AiChatRolePageReqVO pageReqVO, Long userId) {
        return chatRoleMapper.selectPageByMy(pageReqVO, userId);
    }

    @Override
    public List<String> getChatRoleCategoryList() {
        List<AiChatRoleDO> list = chatRoleMapper.selectListGroupByCategory(CommonStatusEnum.ENABLE.getStatus());
        return convertList(list, AiChatRoleDO::getCategory,
                role -> role != null && StrUtil.isNotBlank(role.getCategory()));
    }

    @Override
    public List<AiChatRoleDO> getChatRoleListByName(String name) {
        return chatRoleMapper.selectListByName(name);
    }

}
