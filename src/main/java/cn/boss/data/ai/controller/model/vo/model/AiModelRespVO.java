package cn.boss.data.ai.controller.model.vo.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiModelRespVO {

    private Long id;

    private Long keyId;

    private String name;

    private String model;

    private String platform;

    private Integer type;

    private Integer sort;

    private Integer status;

    private Double temperature;

    private Integer maxTokens;

    private Integer maxContexts;

    private LocalDateTime createTime;

}
