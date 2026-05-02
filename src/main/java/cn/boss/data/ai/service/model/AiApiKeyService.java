package cn.boss.data.ai.service.model;

import cn.boss.data.ai.controller.model.vo.apikey.AiApiKeyPageReqVO;
import cn.boss.data.ai.controller.model.vo.apikey.AiApiKeySaveReqVO;
import cn.boss.data.ai.dal.dataobject.model.AiApiKeyDO;
import cn.boss.data.ai.framework.common.pojo.PageResult;
import jakarta.validation.Valid;

import java.util.List;

/**
 * AI API 密钥 Service 接口
 */
public interface AiApiKeyService {

    Long createApiKey(@Valid AiApiKeySaveReqVO createReqVO);

    void updateApiKey(@Valid AiApiKeySaveReqVO updateReqVO);

    void deleteApiKey(Long id);

    AiApiKeyDO getApiKey(Long id);

    AiApiKeyDO validateApiKey(Long id);

    PageResult<AiApiKeyDO> getApiKeyPage(AiApiKeyPageReqVO pageReqVO);

    List<AiApiKeyDO> getApiKeyList();

    AiApiKeyDO getRequiredDefaultApiKey(String platform, Integer status);

}
