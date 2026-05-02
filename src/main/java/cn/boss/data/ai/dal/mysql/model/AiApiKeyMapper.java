package cn.boss.data.ai.dal.mysql.model;

import cn.boss.data.ai.framework.common.pojo.PageResult;
import cn.boss.data.ai.framework.mybatis.core.mapper.BaseMapperX;
import cn.boss.data.ai.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.boss.data.ai.controller.model.vo.apikey.AiApiKeyPageReqVO;
import cn.boss.data.ai.dal.dataobject.model.AiApiKeyDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI API 密钥 Mapper
 */
@Mapper
public interface AiApiKeyMapper extends BaseMapperX<AiApiKeyDO> {

    default PageResult<AiApiKeyDO> selectPage(AiApiKeyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiApiKeyDO>()
                .likeIfPresent(AiApiKeyDO::getName, reqVO.getName())
                .eqIfPresent(AiApiKeyDO::getPlatform, reqVO.getPlatform())
                .eqIfPresent(AiApiKeyDO::getStatus, reqVO.getStatus())
                .orderByDesc(AiApiKeyDO::getId));
    }

    default AiApiKeyDO selectFirstByPlatformAndStatus(String platform, Integer status) {
        return selectOne(new QueryWrapper<AiApiKeyDO>()
                .eq("platform", platform)
                .eq("status", status)
                .last("LIMIT 1")
                .orderByAsc("id"));
    }

}
