package cn.boss.data.ai.dal.dataobject.model;

import cn.boss.data.ai.enums.model.AiPlatformEnum;
import cn.boss.data.ai.framework.common.enums.CommonStatusEnum;
import cn.boss.data.ai.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI API 秘钥 DO
 */
@TableName("ai_api_key")
@KeySequence("ai_api_key_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiApiKeyDO extends BaseDO {

    @TableId
    private Long id;
    private String name;
    private String apiKey;
    /**
     * 平台
     *
     * 枚举 {@link AiPlatformEnum}
     */
    private String platform;
    private String url;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
