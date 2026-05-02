package cn.boss.data.ai.framework.ai.core.model.hunyuan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

/**
 * 腾讯混元 {@link ChatModel} 实现类
 */
@Slf4j
@RequiredArgsConstructor
public class HunYuanChatModel implements ChatModel {

    public static final String BASE_URL = "https://api.hunyuan.cloud.tencent.com";
    public static final String COMPLETE_PATH = "/v1/chat/completions";

    public static final String MODEL_DEFAULT = "hunyuan-turbo";

    public static final String DEEP_SEEK_BASE_URL = "https://api.lkeap.cloud.tencent.com";

    public static final String DEEP_SEEK_MODEL_DEFAULT = "deepseek-v3";

    private final ChatModel openAiChatModel;

    @Override
    public ChatResponse call(Prompt prompt) {
        return openAiChatModel.call(prompt);
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        return openAiChatModel.stream(prompt);
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return openAiChatModel.getDefaultOptions();
    }

}
