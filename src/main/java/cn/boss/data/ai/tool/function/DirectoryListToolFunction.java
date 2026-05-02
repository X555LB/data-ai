package cn.boss.data.ai.tool.function;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;
import static cn.boss.data.ai.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 工具：列出指定目录的文件列表
 */
@Component("directory_list")
public class DirectoryListToolFunction implements Function<DirectoryListToolFunction.Request, DirectoryListToolFunction.Response> {

    @Data
    @JsonClassDescription("列出指定目录的文件列表")
    public static class Request {

        @JsonProperty(required = true, value = "path")
        @JsonPropertyDescription("目录路径，例如说：/Users/yunai")
        private String path;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {

        private List<File> files;

        @Data
        public static class File {

            private Boolean directory;
            private String name;
            private String size;
            private String lastModified;

        }

    }

    @Override
    public Response apply(Request request) {
        String path = StrUtil.blankToDefault(request.getPath(), "/");
        if (!FileUtil.exist(path) || !FileUtil.isDirectory(path)) {
            return new Response(Collections.emptyList());
        }
        File[] files = FileUtil.ls(path);
        if (ArrayUtil.isEmpty(files)) {
            return new Response(Collections.emptyList());
        }
        return new Response(convertList(Arrays.asList(files), file ->
                new Response.File().setDirectory(file.isDirectory()).setName(file.getName())
                        .setLastModified(LocalDateTimeUtil.format(LocalDateTimeUtil.of(file.lastModified()), NORM_DATETIME_PATTERN))));
    }

}
