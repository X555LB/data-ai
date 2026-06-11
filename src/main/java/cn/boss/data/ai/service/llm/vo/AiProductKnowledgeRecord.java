package cn.boss.data.ai.service.llm.vo;

import lombok.Data;

/**
 * 产品知识记录（估值后端 KnowledgeVO 对应结构）
 */
@Data
public class AiProductKnowledgeRecord {

    /**
     * 知识库 ID
     */
    private String id;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品介绍
     */
    private String descript;

    /**
     * 产品类型
     */
    private String prodTypeName;

    /**
     * 归属单位
     */
    private String enterpriseName;

    /**
     * 价格
     */
    private String prodPrice;

    /**
     * 价格单位
     */
    private String prodPriceUnit;

    /**
     * 使用场景
     */
    private String sectorsName;

    /**
     * 知识库类型：market 市场价格交易库, his 历史经验交易库
     */
    private String knowledgeType;

}
