package com.mall.common.domain;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.utils.BeanUtils;
import com.mall.common.utils.CollUtils;
import com.mall.common.utils.Convert;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页结果封装。
 * <p>
 * 与 {@link PageQuery} 配对使用：入参用 PageQuery，出参用 PageDTO，避免把 MyBatis-Plus 的
 * {@link Page} 对象直接暴露给前端（其中包含大量内部字段）。
 *
 * @param <T> 列表元素类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO<T> {

    /** 总记录数 */
    protected Long total;

    /** 总页数 */
    protected Long pages;

    /** 当前页数据，无数据时为空列表而非 null，前端可安全遍历 */
    protected List<T> list;

    /**
     * 构造空结果。
     *
     * @param total 总记录数
     * @param pages 总页数
     * @param <T>   列表元素类型
     * @return 空结果
     */
    public static <T> PageDTO<T> empty(Long total, Long pages) {
        return new PageDTO<>(total, pages, CollUtils.emptyList());
    }

    /**
     * 基于原分页对象构造空结果，保留总数与总页数。
     *
     * @param page 原分页对象
     * @param <T>  列表元素类型
     * @return 空结果
     */
    public static <T> PageDTO<T> empty(Page<?> page) {
        return new PageDTO<>(page.getTotal(), page.getPages(), CollUtils.emptyList());
    }

    /**
     * 直接转换分页对象，元素类型不变。
     *
     * @param page 分页对象，为 null 时返回空对象
     * @param <T>  列表元素类型
     * @return 分页结果
     */
    public static <T> PageDTO<T> of(Page<T> page) {
        if (page == null) {
            return new PageDTO<>();
        }
        if (CollUtils.isEmpty(page.getRecords())) {
            return empty(page);
        }
        return new PageDTO<>(page.getTotal(), page.getPages(), page.getRecords());
    }

    /**
     * 逐元素转换分页数据。
     *
     * @param page   分页对象，为 null 时返回空对象
     * @param mapper 单元素转换函数
     * @param <T>    目标元素类型
     * @param <R>    源元素类型
     * @return 分页结果
     */
    public static <T, R> PageDTO<T> of(Page<R> page, Function<R, T> mapper) {
        if (page == null) {
            return new PageDTO<>();
        }
        if (CollUtils.isEmpty(page.getRecords())) {
            return empty(page);
        }
        return new PageDTO<>(page.getTotal(), page.getPages(),
                page.getRecords().stream().map(mapper).collect(Collectors.toList()));
    }

    /**
     * 使用已转换好的列表组装分页结果，保留原分页的统计信息。
     *
     * @param page 分页对象，为 null 时返回空对象
     * @param list 已转换的数据列表
     * @param <T>  列表元素类型
     * @return 分页结果
     */
    public static <T> PageDTO<T> of(Page<?> page, List<T> list) {
        if (page == null) {
            return new PageDTO<>(0L, 0L, list == null ? CollUtils.emptyList() : list);
        }
        return new PageDTO<>(page.getTotal(), page.getPages(),
                list == null ? CollUtils.emptyList() : list);
    }

    /**
     * 按目标类型批量拷贝后组装分页结果。
     *
     * @param page  分页对象，为 null 时返回空对象
     * @param clazz 目标元素类型
     * @param <T>   目标元素类型
     * @param <R>   源元素类型
     * @return 分页结果
     */
    public static <T, R> PageDTO<T> of(Page<R> page, Class<T> clazz) {
        if (page == null) {
            return new PageDTO<>();
        }
        return new PageDTO<>(page.getTotal(), page.getPages(),
                BeanUtils.copyList(page.getRecords(), clazz));
    }

    /**
     * 按目标类型批量拷贝，并支持自定义字段转换器。
     *
     * @param page    分页对象，为 null 时返回空对象
     * @param clazz   目标元素类型
     * @param convert 自定义字段转换器
     * @param <T>     目标元素类型
     * @param <R>     源元素类型
     * @return 分页结果
     */
    public static <T, R> PageDTO<T> of(Page<R> page, Class<T> clazz, Convert<R, T> convert) {
        if (page == null) {
            return new PageDTO<>();
        }
        return new PageDTO<>(page.getTotal(), page.getPages(),
                BeanUtils.copyList(page.getRecords(), clazz, convert));
    }
}
