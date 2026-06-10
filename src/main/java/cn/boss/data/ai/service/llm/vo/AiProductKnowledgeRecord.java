package cn.boss.data.ai.service.llm.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 产品知识记录（估值后端返回的数据结构）
 */
@Data
public class AiProductKnowledgeRecord {

    /**
     * 记录 ID
     */
    private Long id;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 相关信息
     */
    private String info;

    /**
     * 价格
     */
    private BigDecimal prodPrice;

    /**
     * 价格单位
     */
    private String prodPriceUnit;

    /**
     * 使用场景
     */
    private String sectorsName;

    /**
     * 产品类型
     */
    private String prodTypeName;

    /**
     * 服务商
     */
    private String enterpriseName;

    /**
     * 是否屏蔽：T-已屏蔽，F-未屏蔽
     */
    private String blockFlag;

}
