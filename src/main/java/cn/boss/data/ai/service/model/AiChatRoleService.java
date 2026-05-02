package cn.boss.data.ai.service.model;

import cn.boss.data.ai.controller.model.vo.chatRole.AiChatRolePageReqVO;
import cn.boss.data.ai.controller.model.vo.chatRole.AiChatRoleSaveMyReqVO;
import cn.boss.data.ai.controller.model.vo.chatRole.AiChatRoleSaveReqVO;
import cn.boss.data.ai.dal.dataobject.model.AiChatRoleDO;
import cn.boss.data.ai.framework.common.pojo.PageResult;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.boss.data.ai.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * AI 聊天角色 Service 接口
 */
public interface AiChatRoleService {

    Long createChatRole(@Valid AiChatRoleSaveReqVO createReqVO);

    Long createChatRoleMy(AiChatRoleSaveMyReqVO createReqVO, Long userId);

    void updateChatRole(@Valid AiChatRoleSaveReqVO updateReqVO);

    void updateChatRoleMy(AiChatRoleSaveMyReqVO updateReqVO, Long userId);

    void deleteChatRole(Long id);

    void deleteChatRoleMy(Long id, Long userId);

    AiChatRoleDO getChatRole(Long id);

    List<AiChatRoleDO> getChatRoleList(Collection<Long> ids);

    default Map<Long, AiChatRoleDO> getChatRoleMap(Collection<Long> ids) {
        return convertMap(getChatRoleList(ids), AiChatRoleDO::getId);
    }

    AiChatRoleDO validateChatRole(Long id);

    PageResult<AiChatRoleDO> getChatRolePage(AiChatRolePageReqVO pageReqVO);

    PageResult<AiChatRoleDO> getChatRoleMyPage(AiChatRolePageReqVO pageReqVO, Long userId);

    List<String> getChatRoleCategoryList();

    List<AiChatRoleDO> getChatRoleListByName(String name);

}
