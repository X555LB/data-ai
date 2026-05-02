package cn.boss.data.ai.framework.common.pojo;

import cn.boss.data.ai.framework.common.exception.ErrorCode;
import cn.boss.data.ai.framework.common.exception.ServiceException;
import cn.boss.data.ai.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.boss.data.ai.framework.common.exception.util.ServiceExceptionUtil;
import cn.hutool.core.lang.Assert;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class CommonResult<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;

    public static <T> CommonResult<T> error(CommonResult<?> result) {
        return error(result.getCode(), result.getMsg());
    }

    public static <T> CommonResult<T> error(Integer code, String message) {
        Assert.notEquals(GlobalErrorCodeConstants.SUCCESS.getCode(), code, "code 必须是错误的！");
        CommonResult<T> result = new CommonResult<>();
        result.code = code;
        result.msg = message;
        return result;
    }

    public static <T> CommonResult<T> error(ErrorCode errorCode, Object... params) {
        Assert.notEquals(GlobalErrorCodeConstants.SUCCESS.getCode(), errorCode.getCode(), "code 必须是错误的！");
        CommonResult<T> result = new CommonResult<>();
        result.code = errorCode.getCode();
        result.msg = ServiceExceptionUtil.doFormat(errorCode.getCode(), errorCode.getMsg(), params);
        return result;
    }

    public static <T> CommonResult<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMsg());
    }

    public static <T> CommonResult<T> success(T data) {
        CommonResult<T> result = new CommonResult<>();
        result.code = GlobalErrorCodeConstants.SUCCESS.getCode();
        result.data = data;
        result.msg = "";
        return result;
    }

    public static boolean isSuccess(Integer code) {
        return Objects.equals(code, GlobalErrorCodeConstants.SUCCESS.getCode());
    }

    @JsonIgnore
    public boolean isSuccess() {
        return isSuccess(code);
    }

    @JsonIgnore
    public boolean isError() {
        return !isSuccess();
    }

    public void checkError() throws ServiceException {
        if (isSuccess()) {
            return;
        }
        throw new ServiceException(code, msg);
    }

    @JsonIgnore
    public T getCheckedData() {
        checkError();
        return data;
    }

    public static <T> CommonResult<T> error(ServiceException serviceException) {
        return error(serviceException.getCode(), serviceException.getMessage());
    }

}
