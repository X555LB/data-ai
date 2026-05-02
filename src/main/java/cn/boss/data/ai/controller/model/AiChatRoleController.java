package cn.boss.data.ai.controller.model;

import cn.hutool.core.util.ObjUtil;
import cn.boss.data.ai.framework.common.pojo.CommonResult;
import cn.boss.data.ai.framework.common.pojo.PageResult;
import cn.boss.data.ai.framework.common.util.object.BeanUtils;
import cn.boss.data.ai.controller.model.vo.chatRole.AiChatRolePageReqVO;
import cn.boss.data.ai.controller.model.vo.chatRole.AiChatRoleRespVO;
import cn.boss.data.ai.controller.model.vo.chatRole.AiChatRoleSaveMyReqVO;
import cn.boss.data.ai.controller.model.vo.chatRole.AiChatRoleSaveReqVO;
import cn.boss.data.ai.dal.dataobject.model.AiChatRoleDO;
import cn.boss.data.ai.service.model.AiChatRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.boss.data.ai.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - AI 聊天角色")
@RestController
@RequestMapping("/ai/chat-role")
@Validated
public class AiChatRoleController {

    private static final Long DEFAULT_USER_ID = 1L;

    @Resource
    private AiChatRoleService chatRoleService;

    @GetMapping("/my-page")
    @Operation(summary = "获得【我的】聊天角色分页")
    public CommonResult<PageResult<AiChatRoleRespVO>> getChatRoleMyPage(@Valid AiChatRolePageReqVO pageReqVO) {
        PageResult<AiChatRoleDO> pageResult = chatRoleService.getChatRoleMyPage(pageReqVO, DEFAULT_USER_ID);
        return success(BeanUtils.toBean(pageResult, AiChatRoleRespVO.class));
    }

    @GetMapping("/get-my")
    @Operation(summary = "获得【我的】聊天角色")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AiChatRoleRespVO> getChatRoleMy(@RequestParam("id") Long id) {
        AiChatRoleDO chatRole = chatRoleService.getChatRole(id);
        if (ObjUtil.notEqual(chatRole.getUserId(), DEFAULT_USER_ID)) {
            return success(null);
        }
        return success(BeanUtils.toBean(chatRole, AiChatRoleRespVO.class));
    }

    @PostMapping("/create-my")
    @Operation(summary = "创建【我的】聊天角色")
    public CommonResult<Long> createChatRoleMy(@Valid @RequestBody AiChatRoleSaveMyReqVO createReqVO) {
        return success(chatRoleService.createChatRoleMy(createReqVO, DEFAULT_USER_ID));
    }

    @PutMapping("/update-my")
    @Operation(summary = "更新【我的】聊天角色")
    public CommonResult<Boolean> updateChatRoleMy(@Valid @RequestBody AiChatRoleSaveMyReqVO updateReqVO) {
        chatRoleService.updateChatRoleMy(updateReqVO, DEFAULT_USER_ID);
        return success(true);
    }

    @DeleteMapping("/delete-my")
    @Operation(summary = "删除【我的】聊天角色")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> deleteChatRoleMy(@RequestParam("id") Long id) {
        chatRoleService.deleteChatRoleMy(id, DEFAULT_USER_ID);
        return success(true);
    }

    @GetMapping("/category-list")
    @Operation(summary = "获得聊天角色的分类列表")
    public CommonResult<List<String>> getChatRoleCategoryList() {
        return success(chatRoleService.getChatRoleCategoryList());
    }

    // ========== 角色管理 ==========

    @PostMapping("/create")
    @Operation(summary = "创建聊天角色")
    public CommonResult<Long> createChatRole(@Valid @RequestBody AiChatRoleSaveReqVO createReqVO) {
        return success(chatRoleService.createChatRole(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新聊天角色")
    public CommonResult<Boolean> updateChatRole(@Valid @RequestBody AiChatRoleSaveReqVO updateReqVO) {
        chatRoleService.updateChatRole(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除聊天角色")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> deleteChatRole(@RequestParam("id") Long id) {
        chatRoleService.deleteChatRole(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得聊天角色")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AiChatRoleRespVO> getChatRole(@RequestParam("id") Long id) {
        AiChatRoleDO chatRole = chatRoleService.getChatRole(id);
        return success(BeanUtils.toBean(chatRole, AiChatRoleRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得聊天角色分页")
    public CommonResult<PageResult<AiChatRoleRespVO>> getChatRolePage(@Valid AiChatRolePageReqVO pageReqVO) {
        PageResult<AiChatRoleDO> pageResult = chatRoleService.getChatRolePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiChatRoleRespVO.class));
    }

}
