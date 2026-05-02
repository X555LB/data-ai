package cn.boss.data.ai.dal.dataobject.model;

import cn.boss.data.ai.enums.model.AiModelTypeEnum;
import cn.boss.data.ai.enums.model.AiPlatformEnum;
import cn.boss.data.ai.framework.common.enums.CommonStatusEnum;
import cn.boss.data.ai.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI 模型 DO
 */
@TableName("ai_model")
@KeySequence("ai_model_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelDO extends BaseDO {

    @TableId
    private Long id;
    /**
     * API 秘钥编号
     *
     * 关联 {@link AiApiKeyDO#getId()}
     */
    private Long keyId;
    private String name;
    private String model;
    /**
     * 平台
     *
     * 枚举 {@link AiPlatformEnum}
     */
    private String platform;
    /**
     * 类型
     *
     * 枚举 {@link AiModelTypeEnum}
     */
    private Integer type;
    private Integer sort;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    // ========== 对话配置 ==========

    private Double temperature;
    private Integer maxTokens;
    private Integer maxContexts;

}
