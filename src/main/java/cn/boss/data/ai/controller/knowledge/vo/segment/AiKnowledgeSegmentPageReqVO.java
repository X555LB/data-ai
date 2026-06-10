package cn.boss.data.ai.controller.knowledge.vo.segment;

import cn.boss.data.ai.framework.common.enums.CommonStatusEnum;
import cn.boss.data.ai.framework.common.pojo.PageParam;
import cn.boss.data.ai.framework.common.validation.InEnum;
import lombok.Data;

@Data
public class AiKnowledgeSegmentPageReqVO extends PageParam {

    private Long documentId;

    private String content;

    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
