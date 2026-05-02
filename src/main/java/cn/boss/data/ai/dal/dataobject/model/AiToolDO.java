package cn.boss.data.ai.dal.dataobject.model;

import cn.boss.data.ai.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * AI 工具 DO
 */
@TableName("ai_tool")
@KeySequence("ai_tool_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiToolDO extends BaseDO {

    @TableId
    private Long id;
    private String name;
    private String description;
    /**
     * 状态
     *
     * 枚举 {@link cn.boss.data.ai.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

}
