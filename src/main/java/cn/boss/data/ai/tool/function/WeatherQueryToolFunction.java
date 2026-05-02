package cn.boss.data.ai.tool.function;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.Function;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;

/**
 * 工具：查询指定城市的天气信息
 */
@Component("weather_query")
public class WeatherQueryToolFunction
        implements Function<WeatherQueryToolFunction.Request, WeatherQueryToolFunction.Response> {

    private static final String[] WEATHER_CONDITIONS = { "晴朗", "多云", "阴天", "小雨", "大雨", "雷雨", "小雪", "大雪" };

    @Data
    @JsonClassDescription("查询指定城市的天气信息")
    public static class Request {

        @JsonProperty(required = true, value = "city")
        @JsonPropertyDescription("城市名称，例如：北京、上海、广州")
        private String city;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {

        private String city;
        private WeatherInfo weatherInfo;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class WeatherInfo {

            private Integer temperature;
            private String condition;
            private Integer humidity;
            private Integer windSpeed;
            private String queryTime;

        }

    }

    @Override
    public Response apply(Request request) {
        if (StrUtil.isBlank(request.getCity())) {
            return new Response("未知城市", null);
        }
        String city = request.getCity();
        Response.WeatherInfo weatherInfo = generateMockWeatherInfo();
        return new Response(city, weatherInfo);
    }

    private Response.WeatherInfo generateMockWeatherInfo() {
        int temperature = RandomUtil.randomInt(-5, 30);
        int humidity = RandomUtil.randomInt(1, 100);
        int windSpeed = RandomUtil.randomInt(1, 30);
        String condition = RandomUtil.randomEle(WEATHER_CONDITIONS);
        return new Response.WeatherInfo(temperature, condition, humidity, windSpeed,
                LocalDateTimeUtil.format(LocalDateTime.now(), NORM_DATETIME_PATTERN));
    }

}
