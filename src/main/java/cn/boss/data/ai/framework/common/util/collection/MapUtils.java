package cn.boss.data.ai.framework.common.util.collection;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;

import java.util.Map;
import java.util.function.Consumer;

public class MapUtils {

    public static <K, V> void findAndThen(Map<K, V> map, K key, Consumer<V> consumer) {
        if (ObjUtil.isNull(key) || CollUtil.isEmpty(map)) {
            return;
        }
        V value = map.get(key);
        if (value == null) {
            return;
        }
        consumer.accept(value);
    }

}
