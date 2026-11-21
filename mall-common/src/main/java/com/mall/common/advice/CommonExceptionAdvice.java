package com.mall.common.advice;

import com.mall.common.domain.R;
import com.mall.common.exception.BadRequestException;
import com.mall.common.exception.CommonException;
import com.mall.common.exception.DbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.NestedServletException;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 * <p>
 * 统一把各类异常翻译为 {@link R} 结构并设置对应的 HTTP 状态码，避免异常堆栈直接暴露给前端。
 * 处理顺序由 Spring 按异常类型的具体程度匹配，越具体的处理器优先级越高。
 */
@RestControllerAdvice
@Slf4j
public class CommonExceptionAdvice {

    /**
     * 处理数据库操作异常。
     *
     * @param e 数据库异常
     * @return 统一错误响应
     */
    @ExceptionHandler(DbException.class)
    public Object handleDbException(DbException e) {
        log.error("mysql数据库操作异常 -> ", e);
        return processResponse(e);
    }

    /**
     * 处理所有自定义业务异常（参数、鉴权、业务规则等）。
     *
     * @param e 业务异常
     * @return 统一错误响应，HTTP 状态码取自异常的错误码
     */
    @ExceptionHandler(CommonException.class)
    public Object handleBadRequestException(CommonException e) {
        log.error("自定义异常 -> {} , 异常原因：{}", e.getClass().getName(), e.getMessage());
        log.debug("自定义异常堆栈", e);
        return processResponse(e);
    }

    /**
     * 处理 @Valid / @Validated 校验失败（RequestBody 参数）。
     *
     * @param e 参数校验异常
     * @return 400 响应，message 为各字段错误信息的拼接
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        var msg = e.getBindingResult().getAllErrors()
                .stream().map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("|"));
        log.error("请求参数校验异常 -> {}", msg);
        log.debug("请求参数校验异常堆栈", e);
        return processResponse(new BadRequestException(msg));
    }

    /**
     * 处理表单/查询参数绑定失败。
     * <p>
     * 注意：此处必须使用 Spring 的 {@code org.springframework.validation.BindException}，
     * 而非 {@code java.net.BindException}（后者与 Web 参数绑定无关，永远不会被该分支捕获）。
     *
     * @param e 参数绑定异常
     * @return 400 响应
     */
    @ExceptionHandler(BindException.class)
    public Object handleBindException(BindException e) {
        log.error("请求参数绑定异常 -> BindException，{}", e.getMessage());
        log.debug("请求参数绑定异常堆栈", e);
        return processResponse(new BadRequestException("请求参数格式错误"));
    }

    /**
     * 处理 Servlet 嵌套异常，通常由过滤器或拦截器中的运行时异常包装而来。
     *
     * @param e 嵌套异常
     * @return 400 响应
     */
    @ExceptionHandler(NestedServletException.class)
    public Object handleNestedServletException(NestedServletException e) {
        log.error("参数异常 -> NestedServletException，{}", e.getMessage());
        log.debug("参数异常堆栈", e);
        return processResponse(new BadRequestException("请求参数处理异常"));
    }

    /**
     * 兜底处理未被前面分支捕获的异常。
     * <p>
     * 请求上下文可能已在异常中被销毁（如异步请求），因此 URI 的获取做了空值保护，
     * 避免日志打印本身再抛空指针导致原始异常被覆盖。
     *
     * @param e 未预期异常
     * @param request 当前请求，由 Spring 注入，可避免手动获取时的空指针
     * @return 500 响应
     */
    @ExceptionHandler(Exception.class)
    public Object handleRuntimeException(Exception e, HttpServletRequest request) {
        var uri = request == null ? "unknown" : request.getRequestURI();
        log.error("其他异常 uri : {} -> ", uri, e);
        return processResponse(new CommonException("服务器内部异常", 500));
    }

    /**
     * 将业务异常转换为带 HTTP 状态码的响应体。
     *
     * @param e 业务异常
     * @return HTTP 状态码与响应体一致的 ResponseEntity
     */
    private ResponseEntity<R<Void>> processResponse(CommonException e) {
        return ResponseEntity.status(e.getCode()).body(R.error(e));
    }
}
