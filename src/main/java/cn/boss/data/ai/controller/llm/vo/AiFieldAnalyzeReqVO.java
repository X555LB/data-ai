package cn.boss.data.ai.controller.llm.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 字段重要性分析请求 VO
 */
@Data
public class AiFieldAnalyzeReqVO {

    @NotBlank(message = "字段列表不能为空")
    private String fields;

    @NotBlank(message = "产品名称不能为空")
    private String productName;

    private String usageScenario;

    private String productIntro;

}
