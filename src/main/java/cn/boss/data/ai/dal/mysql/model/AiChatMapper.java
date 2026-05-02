package cn.boss.data.ai.dal.mysql.model;

import cn.boss.data.ai.framework.common.pojo.PageResult;
import cn.boss.data.ai.framework.mybatis.core.mapper.BaseMapperX;
import cn.boss.data.ai.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.boss.data.ai.controller.model.vo.model.AiModelPageReqVO;
import cn.boss.data.ai.dal.dataobject.model.AiModelDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import javax.annotation.Nullable;
import java.util.List;

/**
 * API 模型 Mapper
 */
@Mapper
public interface AiChatMapper extends BaseMapperX<AiModelDO> {

    default AiModelDO selectFirstByStatus(Integer type, Integer status) {
        return selectOne(new QueryWrapper<AiModelDO>()
                .eq("type", type)
                .eq("status", status)
                .last("LIMIT 1")
                .orderByAsc("sort"));
    }

    default PageResult<AiModelDO> selectPage(AiModelPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiModelDO>()
                .likeIfPresent(AiModelDO::getName, reqVO.getName())
                .eqIfPresent(AiModelDO::getModel, reqVO.getModel())
                .eqIfPresent(AiModelDO::getPlatform, reqVO.getPlatform())
                .orderByAsc(AiModelDO::getSort));
    }

    default List<AiModelDO> selectListByStatusAndType(Integer status, Integer type,
                                                      @Nullable String platform) {
        return selectList(new LambdaQueryWrapperX<AiModelDO>()
                .eq(AiModelDO::getStatus, status)
                .eq(AiModelDO::getType, type)
                .eqIfPresent(AiModelDO::getPlatform, platform)
                .orderByAsc(AiModelDO::getSort));
    }

}
