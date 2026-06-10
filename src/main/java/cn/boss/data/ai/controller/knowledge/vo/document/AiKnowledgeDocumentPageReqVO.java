package cn.boss.data.ai.controller.knowledge.vo.document;

import cn.boss.data.ai.framework.common.pojo.PageParam;
import lombok.Data;

@Data
public class AiKnowledgeDocumentPageReqVO extends PageParam {

    private Long knowledgeId;

    private String name;

}
