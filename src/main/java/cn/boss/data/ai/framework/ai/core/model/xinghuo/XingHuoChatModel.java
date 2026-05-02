package cn.boss.data.ai.framework.ai.core.model.xinghuo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

/**
 * 讯飞星火 {@link ChatModel} 实现类
 */
@Slf4j
@RequiredArgsConstructor
public class XingHuoChatModel implements ChatModel {

    public static final String BASE_URL_V1 = "https://spark-api-open.xf-yun.com";

    public static final String BASE_URL_V2 = "https://spark-api-open.xf-yun.com";
    public static final String BASE_COMPLETIONS_PATH_V2 = "/v2/chat/completions";

    public static final String MODEL_DEFAULT = "4.0Ultra";

    private final ChatModel openAiChatModelV1;

    @Override
    public ChatResponse call(Prompt prompt) {
        return openAiChatModelV1.call(prompt);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return openAiChatModelV1.stream(prompt);
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return openAiChatModelV1.getDefaultOptions();
    }

}
