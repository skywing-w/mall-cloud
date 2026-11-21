package com.mall.common.domain;

import com.mall.common.exception.CommonException;
import lombok.Data;

/**
 * 统一响应包装体。
 * <p>
 * 所有 Controller 对外返回的数据最终都会被包装成该结构，前端只需判断 {@code code} 是否为 200。
 * 约定：200 成功、400 参数错误、401 未登录、403 无权限、500 服务端异常。
 *
 * @param <T> 业务数据类型
 */
@Data
public class R<T> {

    /** 成功状态码，与 HTTP 200 保持一致，便于前端统一判断 */
    private static final int CODE_SUCCESS = 200;

    /** 默认失败状态码，业务未指定具体错误码时使用 */
    private static final int CODE_ERROR = 500;

    /** 业务状态码：200 成功，其余为各类错误 */
    private int code;

    /** 提示信息，失败时为可直接展示给用户的错误描述 */
    private String msg;

    /** 业务数据，失败时为 null */
    private T data;

    /**
     * 构造无数据的成功响应。
     *
     * @return 成功响应
     */
    public static R<Void> ok() {
        return ok(null);
    }

    /**
     * 构造带数据的成功响应。
     *
     * @param data 业务数据，允许为 null
     * @param <T>  业务数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok(T data) {
        return new R<>(CODE_SUCCESS, "OK", data);
    }

    /**
     * 构造默认错误码（500）的失败响应。
     *
     * @param msg 错误信息
     * @param <T> 业务数据类型
     * @return 失败响应
     */
    public static <T> R<T> error(String msg) {
        return new R<>(CODE_ERROR, msg, null);
    }

    /**
     * 构造指定错误码的失败响应。
     *
     * @param code 业务状态码
     * @param msg  错误信息
     * @param <T>  业务数据类型
     * @return 失败响应
     */
    public static <T> R<T> error(int code, String msg) {
        return new R<>(code, msg, null);
    }

    /**
     * 根据业务异常构造失败响应，错误码取自异常本身。
     *
     * @param e   业务异常，不可为 null
     * @param <T> 业务数据类型
     * @return 失败响应
     */
    public static <T> R<T> error(CommonException e) {
        return new R<>(e.getCode(), e.getMessage(), null);
    }

    public R() {
    }

    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 判断当前响应是否为成功响应。
     *
     * @return code 为 200 时返回 true
     */
    public boolean success() {
        return code == CODE_SUCCESS;
    }
}
