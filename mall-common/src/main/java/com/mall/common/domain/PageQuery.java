package com.mall.common.domain;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;

/**
 * 分页查询条件基类。
 * <p>
 * 所有带分页的查询接口都直接使用本类接收前端参数，并提供转换为 MyBatis-Plus {@link Page} 的方法。
 * 页码与每页条数均做了最小值校验，避免非法分页参数穿透到 SQL 层。
 */
@Data
@ApiModel(description = "分页查询条件")
@Accessors(chain = true)
public class PageQuery {

    /** 默认每页条数：20 条，与前端分页组件默认值保持一致 */
    public static final Integer DEFAULT_PAGE_SIZE = 20;

    /** 默认页码：第 1 页 */
    public static final Integer DEFAULT_PAGE_NUM = 1;

    @ApiModelProperty("页码")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNo = DEFAULT_PAGE_NUM;

    @ApiModelProperty("每页查询数量")
    @Min(value = 1, message = "每页查询数量不能小于1")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    @ApiModelProperty("是否升序")
    private Boolean isAsc = true;

    @ApiModelProperty("排序方式")
    private String sortBy;

    /**
     * 计算分页偏移量，供需要手写分页 SQL 的 Mapper 方法使用。
     *
     * @return 偏移量（从 0 开始）
     */
    public int from() {
        return (pageNo - 1) * pageSize;
    }

    /**
     * 转换为 MyBatis-Plus 分页对象。
     * <p>
     * 排序优先级：显式传入的 {@code orderItems} &gt; 前端传入的 {@code sortBy} &gt; 无排序。
     *
     * @param orderItems 显式指定的排序项，可为空
     * @param <T>        分页记录类型
     * @return 分页对象
     */
    public <T> Page<T> toMpPage(OrderItem... orderItems) {
        Page<T> page = new Page<>(pageNo, pageSize);
        // 是否手动指定排序方式
        if (orderItems != null && orderItems.length > 0) {
            for (OrderItem orderItem : orderItems) {
                page.addOrder(orderItem);
            }
            return page;
        }
        // 前端是否传了排序字段，传了则按其排序
        if (StrUtil.isNotEmpty(sortBy)) {
            OrderItem orderItem = new OrderItem();
            orderItem.setAsc(isAsc);
            orderItem.setColumn(sortBy);
            page.addOrder(orderItem);
        }
        return page;
    }

    /**
     * 转换为 MyBatis-Plus 分页对象并指定默认排序。
     * <p>
     * 前端未传 {@code sortBy} 时使用默认排序字段，避免无序分页导致的数据重复或遗漏。
     *
     * @param defaultSortBy 默认排序字段（数据库列名）
     * @param isAsc         默认是否升序
     * @param <T>           分页记录类型
     * @return 分页对象
     */
    public <T> Page<T> toMpPage(String defaultSortBy, boolean isAsc) {
        if (StringUtils.isBlank(sortBy)) {
            sortBy = defaultSortBy;
            this.isAsc = isAsc;
        }
        Page<T> page = new Page<>(pageNo, pageSize);
        OrderItem orderItem = new OrderItem();
        orderItem.setAsc(this.isAsc);
        orderItem.setColumn(sortBy);
        page.addOrder(orderItem);
        return page;
    }

    /**
     * 按创建时间倒序分页，列表类查询的通用默认排序。
     *
     * @param <T> 分页记录类型
     * @return 分页对象
     */
    public <T> Page<T> toMpPageDefaultSortByCreateTimeDesc() {
        return toMpPage("create_time", false);
    }
}
