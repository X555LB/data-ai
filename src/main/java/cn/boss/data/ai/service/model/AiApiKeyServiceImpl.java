package cn.boss.data.ai.service.model;

import cn.boss.data.ai.controller.model.vo.apikey.AiApiKeyPageReqVO;
import cn.boss.data.ai.controller.model.vo.apikey.AiApiKeySaveReqVO;
import cn.boss.data.ai.dal.dataobject.model.AiApiKeyDO;
import cn.boss.data.ai.dal.mysql.model.AiApiKeyMapper;
import cn.boss.data.ai.framework.common.enums.CommonStatusEnum;
import cn.boss.data.ai.framework.common.pojo.PageResult;
import cn.boss.data.ai.framework.common.util.object.BeanUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.boss.data.ai.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.boss.data.ai.enums.ErrorCodeConstants.API_KEY_DISABLE;
import static cn.boss.data.ai.enums.ErrorCodeConstants.API_KEY_NOT_EXISTS;

/**
 * AI API 密钥 Service 实现类
 */
@Service
@Validated
public class AiApiKeyServiceImpl implements AiApiKeyService {

    @Resource
    private AiApiKeyMapper apiKeyMapper;

    @Override
    public Long createApiKey(AiApiKeySaveReqVO createReqVO) {
        AiApiKeyDO apiKey = BeanUtils.toBean(createReqVO, AiApiKeyDO.class);
        apiKeyMapper.insert(apiKey);
        return apiKey.getId();
    }

    @Override
    public void updateApiKey(AiApiKeySaveReqVO updateReqVO) {
        validateApiKeyExists(updateReqVO.getId());
        AiApiKeyDO updateObj = BeanUtils.toBean(updateReqVO, AiApiKeyDO.class);
        apiKeyMapper.updateById(updateObj);
    }

    @Override
    public void deleteApiKey(Long id) {
        validateApiKeyExists(id);
        apiKeyMapper.deleteById(id);
    }

    private AiApiKeyDO validateApiKeyExists(Long id) {
        AiApiKeyDO apiKey = apiKeyMapper.selectById(id);
        if (apiKey == null) {
            throw exception(API_KEY_NOT_EXISTS);
        }
        return apiKey;
    }

    @Override
    public AiApiKeyDO getApiKey(Long id) {
        return apiKeyMapper.selectById(id);
    }

    @Override
    public AiApiKeyDO validateApiKey(Long id) {
        AiApiKeyDO apiKey = validateApiKeyExists(id);
        if (CommonStatusEnum.isDisable(apiKey.getStatus())) {
            throw exception(API_KEY_DISABLE);
        }
        return apiKey;
    }

    @Override
    public PageResult<AiApiKeyDO> getApiKeyPage(AiApiKeyPageReqVO pageReqVO) {
        return apiKeyMapper.selectPage(pageReqVO);
    }

    @Override
    public List<AiApiKeyDO> getApiKeyList() {
        return apiKeyMapper.selectList();
    }

    @Override
    public AiApiKeyDO getRequiredDefaultApiKey(String platform, Integer status) {
        AiApiKeyDO apiKey = apiKeyMapper.selectFirstByPlatformAndStatus(platform, status);
        if (apiKey == null) {
            throw exception(API_KEY_NOT_EXISTS);
        }
        return apiKey;
    }

}
