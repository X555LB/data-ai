package cn.boss.data.ai.controller.model.vo.apikey;

import lombok.Data;

@Data
public class AiApiKeyRespVO {

    private Long id;

    private String name;

    private String apiKey;

    private String platform;

    private String url;

    private Integer status;

}
