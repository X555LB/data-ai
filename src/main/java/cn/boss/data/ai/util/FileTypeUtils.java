package cn.boss.data.ai.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;

/**
 * 文件类型 Utils
 */
@Slf4j
public class FileTypeUtils {

    private static final Tika TIKA = new Tika();

    public static String getMineType(String name) {
        return TIKA.detect(name);
    }

    public static boolean isImage(String mineType) {
        return StrUtil.startWith(mineType, "image/");
    }

}
