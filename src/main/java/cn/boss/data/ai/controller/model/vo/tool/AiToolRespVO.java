package cn.boss.data.ai.controller.model.vo.tool;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiToolRespVO {

    private Long id;

    private String name;

    private String description;

    private Integer status;

    private LocalDateTime createTime;

}
