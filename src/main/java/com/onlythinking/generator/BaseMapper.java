package com.onlythinking.generator;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * <p> BaseMapper </p>
 *
 * @author Li Xingping
 */
public interface BaseMapper<T> {
    T getByPK(Serializable id);

    T getOne(Map<String, Object> params);

    List<T> getList(Map<String, Object> params);

    void insert(T object);

    void insertBatch(List<T> list);

    void updateByPK(T object);

    void updateBySelective(Map<String, Object> params);

    void deleteByPK(Serializable id);

    void deleteInBatch(List<String> ids);

    Long count(Map<String, Object> params);

    void disabledByPk(Serializable id);

    void disabledBySelective(Map<String, Object> params);
}
