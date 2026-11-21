package com.mall.common.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.util.NumberUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 继承自 hutool 的集合工具类
 */
public class CollUtils extends CollectionUtil {

    public static <T> List<T> emptyList() {
        return Collections.emptyList();
    }

    public static <T> Set<T> emptySet() {
        return Collections.emptySet();
    }

    public static <K,V> Map<K, V> emptyMap() {
        return Collections.emptyMap();
    }

    public static <T> Set<T> singletonSet(T t) {
        return Collections.singleton(t);
    }

    public static <T> List<T> singletonList(T t) {
        return Collections.singletonList(t);
    }

    /**
     * 将字符串列表转为整数列表。
     *
     * @param originList 源列表
     * @return 整数列表；源列表为空时返回 null（保持调用方原有的判空逻辑）
     * @throws NumberFormatException 任一元素不是合法整数时抛出
     */
    public static List<Integer> convertToInteger(List<String> originList) {
        return CollUtils.isNotEmpty(originList)
                ? originList.stream().map(NumberUtil::parseInt).collect(Collectors.toList())
                : null;
    }

    /**
     * 将字符串列表转为长整数列表。
     *
     * @param originList 源列表
     * @return 长整数列表；源列表为空时返回 null（保持调用方原有的判空逻辑）
     * @throws NumberFormatException 任一元素不是合法整数时抛出
     */
    public static List<Long> convertToLong(List<String> originList) {
        return CollUtils.isNotEmpty(originList)
                ? originList.stream().map(NumberUtil::parseLong).collect(Collectors.toList())
                : null;
    }

    /**
     * 以 conjunction 为分隔符将集合转换为字符串 如果集合元素为数组、Iterable或Iterator，则递归组合其为字符串
     * @param collection 集合
     * @param conjunction 分隔符
     * @param <T> 集合元素类型
     * @return 连接后的字符串
     * See Also: IterUtil.join(Iterator, CharSequence)
     */
    public static <T> String join(Collection<T> collection, CharSequence conjunction) {
        if (null == collection || collection.isEmpty()) {
            return null;
        }
        return IterUtil.join(collection.iterator(), conjunction);
    }

    public static <T> String joinIgnoreNull(Collection<T> collection, CharSequence conjunction) {
        if (null == collection || collection.isEmpty()) {
            return null;
        }
        var sb = new StringBuilder();
        for (T t : collection) {
            if(t == null) continue;
            sb.append(t).append(",");
        }
        if(sb.length() <= 0){
            return null;
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }
}