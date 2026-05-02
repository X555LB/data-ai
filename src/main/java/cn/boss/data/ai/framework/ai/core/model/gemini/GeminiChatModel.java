package cn.boss.data.ai.framework.ai.core.model.gemini;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import reactor.core.publisher.Flux;

/**
 * 谷歌 Gemini {@link ChatModel} 实现类
 */
@Slf4j
@RequiredArgsConstructor
public class GeminiChatModel implements ChatModel {

    public static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/openai/";
    public static final String COMPLETE_PATH = "/chat/completions";

    public static final String MODEL_DEFAULT = "gemini-2.5-flash";

    private final OpenAiChatModel openAiChatModel;

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
