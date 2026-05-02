package cn.boss.data.ai.service.model;

import cn.boss.data.ai.controller.model.vo.model.AiModelPageReqVO;
import cn.boss.data.ai.controller.model.vo.model.AiModelSaveReqVO;
import cn.boss.data.ai.dal.dataobject.model.AiModelDO;
import cn.boss.data.ai.framework.common.pojo.PageResult;
import jakarta.validation.Valid;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * AI 模型 Service 接口
 */
public interface AiModelService {

    Long createModel(@Valid AiModelSaveReqVO createReqVO);

    void updateModel(@Valid AiModelSaveReqVO updateReqVO);

    void deleteModel(Long id);

    AiModelDO getModel(Long id);

    AiModelDO getRequiredDefaultModel(Integer type);

    PageResult<AiModelDO> getModelPage(AiModelPageReqVO pageReqVO);

    AiModelDO validateModel(Long id);

    List<AiModelDO> getModelListByStatusAndType(Integer status, Integer type,
                                                @Nullable String platform);

    // ========== 与 Spring AI 集成 ==========

    ChatModel getChatModel(Long id);

    VectorStore getOrCreateVectorStore(Long id, Map<String, Class<?>> metadataFields);

}
