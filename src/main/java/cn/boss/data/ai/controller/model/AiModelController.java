package cn.boss.data.ai.controller.model;

import cn.boss.data.ai.framework.common.enums.CommonStatusEnum;
import cn.boss.data.ai.framework.common.pojo.CommonResult;
import cn.boss.data.ai.framework.common.pojo.PageResult;
import cn.boss.data.ai.framework.common.util.object.BeanUtils;
import cn.boss.data.ai.controller.model.vo.model.AiModelPageReqVO;
import cn.boss.data.ai.controller.model.vo.model.AiModelRespVO;
import cn.boss.data.ai.controller.model.vo.model.AiModelSaveReqVO;
import cn.boss.data.ai.dal.dataobject.model.AiModelDO;
import cn.boss.data.ai.service.model.AiModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.boss.data.ai.framework.common.pojo.CommonResult.success;
import static cn.boss.data.ai.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - AI 模型")
@RestController
@RequestMapping("/ai/model")
@Validated
public class AiModelController {

    @Resource
    private AiModelService modelService;

    @PostMapping("/create")
    @Operation(summary = "创建模型")
    public CommonResult<Long> createModel(@Valid @RequestBody AiModelSaveReqVO createReqVO) {
        return success(modelService.createModel(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新模型")
    public CommonResult<Boolean> updateModel(@Valid @RequestBody AiModelSaveReqVO updateReqVO) {
        modelService.updateModel(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除模型")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> deleteModel(@RequestParam("id") Long id) {
        modelService.deleteModel(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得模型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AiModelRespVO> getModel(@RequestParam("id") Long id) {
        AiModelDO model = modelService.getModel(id);
        return success(BeanUtils.toBean(model, AiModelRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得模型分页")
    public CommonResult<PageResult<AiModelRespVO>> getModelPage(@Valid AiModelPageReqVO pageReqVO) {
        PageResult<AiModelDO> pageResult = modelService.getModelPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiModelRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得模型列表")
    @Parameter(name = "type", description = "类型", required = true, example = "1")
    @Parameter(name = "platform", description = "平台", example = "midjourney")
    public CommonResult<List<AiModelRespVO>> getModelSimpleList(
            @RequestParam("type") Integer type,
            @RequestParam(value = "platform", required = false) String platform) {
        List<AiModelDO> list = modelService.getModelListByStatusAndType(
                CommonStatusEnum.ENABLE.getStatus(), type, platform);
        return success(convertList(list, model -> new AiModelRespVO().setId(model.getId())
                .setName(model.getName()).setModel(model.getModel()).setPlatform(model.getPlatform())));
    }

}
