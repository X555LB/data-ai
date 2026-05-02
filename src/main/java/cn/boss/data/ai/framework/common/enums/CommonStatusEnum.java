package cn.boss.data.ai.framework.common.enums;

import cn.hutool.core.util.ObjUtil;
import cn.boss.data.ai.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CommonStatusEnum implements ArrayValuable<Integer> {

    ENABLE(0, "开启"),
    DISABLE(1, "关闭");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(CommonStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isEnable(Integer status) {
        return ObjUtil.equal(ENABLE.status, status);
    }

    public static boolean isDisable(Integer status) {
        return ObjUtil.equal(DISABLE.status, status);
    }

}
