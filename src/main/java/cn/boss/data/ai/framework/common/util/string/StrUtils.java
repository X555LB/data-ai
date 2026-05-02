package cn.boss.data.ai.framework.common.util.string;

import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StrUtils {

    public static String maxLength(CharSequence str, int maxLength) {
        return StrUtil.maxLength(str, maxLength - 3);
    }

    public static List<Long> splitToLong(String value, CharSequence separator) {
        long[] longs = StrUtil.splitToLong(value, separator);
        return Arrays.stream(longs).boxed().collect(Collectors.toList());
    }

    public static Set<Long> splitToLongSet(String value, CharSequence separator) {
        long[] longs = StrUtil.splitToLong(value, separator);
        return Arrays.stream(longs).boxed().collect(Collectors.toSet());
    }

    public static List<Integer> splitToInteger(String value, CharSequence separator) {
        int[] integers = StrUtil.splitToInt(value, separator);
        return Arrays.stream(integers).boxed().collect(Collectors.toList());
    }

}
