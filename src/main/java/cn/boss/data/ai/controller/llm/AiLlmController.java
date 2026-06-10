package cn.boss.data.ai.controller.llm;

import cn.boss.data.ai.controller.llm.vo.AiFieldAnalyzeReqVO;
import cn.boss.data.ai.controller.llm.vo.AiLlmSearchReqVO;
import cn.boss.data.ai.service.llm.AiLlmService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 估值系统 - LLM 服务控制器
 */
@RestController
@RequestMapping("/")
@Slf4j
public class AiLlmController {

    @Resource
    private AiLlmService llmService;

    @PostMapping(value = "/llm-search", produces = MediaType.TEXT_PLAIN_VALUE)
    public String llmSearch(@Valid @RequestBody AiLlmSearchReqVO reqVO) {
        try {
            return llmService.llmSearch(reqVO);
        } catch (Exception e) {
            log.error("[llmSearch] 产品语义搜索失败", e);
            return "空";
        }
    }

    @PostMapping(value = "/field/analyze-by-llm", produces = MediaType.APPLICATION_JSON_VALUE)
    public String fieldAnalyzeByLlm(@Valid @RequestBody AiFieldAnalyzeReqVO reqVO) {
        try {
            return llmService.fieldAnalyzeByLlm(reqVO);
        } catch (Exception e) {
            log.error("[fieldAnalyzeByLlm] 字段重要性分析失败", e);
            return "[]";
        }
    }

}
