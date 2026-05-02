package cn.boss.data.ai.service.model;

import cn.boss.data.ai.controller.model.vo.tool.AiToolPageReqVO;
import cn.boss.data.ai.controller.model.vo.tool.AiToolSaveReqVO;
import cn.boss.data.ai.dal.dataobject.model.AiToolDO;
import cn.boss.data.ai.dal.mysql.model.AiToolMapper;
import cn.boss.data.ai.framework.common.pojo.PageResult;
import cn.boss.data.ai.framework.common.util.object.BeanUtils;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.resolution.ToolCallbackResolver;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static cn.boss.data.ai.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.boss.data.ai.enums.ErrorCodeConstants.TOOL_NAME_NOT_EXISTS;
import static cn.boss.data.ai.enums.ErrorCodeConstants.TOOL_NOT_EXISTS;

/**
 * AI 工具 Service 实现类
 */
@Service
@Validated
public class AiToolServiceImpl implements AiToolService {

    @Resource
    private AiToolMapper toolMapper;

    @Resource
    private ToolCallbackResolver toolCallbackResolver;

    @Override
    public Long createTool(AiToolSaveReqVO createReqVO) {
        validateToolNameExists(createReqVO.getName());
        AiToolDO tool = BeanUtils.toBean(createReqVO, AiToolDO.class);
        toolMapper.insert(tool);
        return tool.getId();
    }

    @Override
    public void updateTool(AiToolSaveReqVO updateReqVO) {
        validateToolExists(updateReqVO.getId());
        validateToolNameExists(updateReqVO.getName());
        AiToolDO updateObj = BeanUtils.toBean(updateReqVO, AiToolDO.class);
        toolMapper.updateById(updateObj);
    }

    @Override
    public void deleteTool(Long id) {
        validateToolExists(id);
        toolMapper.deleteById(id);
    }

    @Override
    public void validateToolExists(Long id) {
        if (toolMapper.selectById(id) == null) {
            throw exception(TOOL_NOT_EXISTS);
        }
    }

    private void validateToolNameExists(String name) {
        ToolCallback toolCallback = toolCallbackResolver.resolve(name);
        if (toolCallback == null) {
            throw exception(TOOL_NAME_NOT_EXISTS, name);
        }
    }

    @Override
    public AiToolDO getTool(Long id) {
        return toolMapper.selectById(id);
    }

    @Override
    public List<AiToolDO> getToolList(Collection<Long> ids) {
        return toolMapper.selectByIds(ids);
    }

    @Override
    public PageResult<AiToolDO> getToolPage(AiToolPageReqVO pageReqVO) {
        return toolMapper.selectPage(pageReqVO);
    }

    @Override
    public List<AiToolDO> getToolListByStatus(Integer status) {
        return toolMapper.selectListByStatus(status);
    }

}
