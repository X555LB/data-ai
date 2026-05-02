package cn.boss.data.ai.service.model;

import cn.boss.data.ai.controller.model.vo.tool.AiToolPageReqVO;
import cn.boss.data.ai.controller.model.vo.tool.AiToolSaveReqVO;
import cn.boss.data.ai.dal.dataobject.model.AiToolDO;
import cn.boss.data.ai.framework.common.pojo.PageResult;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * AI 工具 Service 接口
 */
public interface AiToolService {

    Long createTool(@Valid AiToolSaveReqVO createReqVO);

    void updateTool(@Valid AiToolSaveReqVO updateReqVO);

    void deleteTool(Long id);

    void validateToolExists(Long id);

    AiToolDO getTool(Long id);

    List<AiToolDO> getToolList(Collection<Long> ids);

    PageResult<AiToolDO> getToolPage(AiToolPageReqVO pageReqVO);

    List<AiToolDO> getToolListByStatus(Integer status);

}
