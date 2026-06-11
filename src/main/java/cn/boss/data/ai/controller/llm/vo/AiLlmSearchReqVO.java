package cn.boss.data.ai.controller.llm.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 产品语义搜索请求 VO
 */
@Data
public class AiLlmSearchReqVO {

    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;
    
}
