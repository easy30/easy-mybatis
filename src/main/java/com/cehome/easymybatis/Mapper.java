package com.cehome.easymybatis;

import com.cehome.easymybatis.provider.*;
import com.cehome.easymybatis.utils.Const;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * coolma 2019/10/24
 **/
public interface Mapper<E> {


    @InsertProvider(type = Provider.class, method = "insert")
    int insert(E entity);


    /**
     * update entity
     * @param entity
     * @return
     */
    @UpdateProvider(type = Provider.class, method = "update")
    int update(E entity);


    @UpdateProvider(type = Provider.class, method = "updateByEntity")
    int updateByEntity(@Param(Const.ENTITY) E entity,
                       @Param(Const.PARAMS) E params);


    @UpdateProvider(type = Provider.class, method = "updateByWhere")
    int updateByWhere(@Param(Const.ENTITY) E entity,
                      @Param(Const.WHERE) String where,
                      @Param(Const.PARAMS) Object params);

    /**
     * delete by id in entity
     * @param entity
     * @return
     */
    @DeleteProvider(type = Provider.class, method = "delete")
    int delete(E entity);

    /**
     * delete by id value
     * @param id
     * @return
     */
    @DeleteProvider(type = Provider.class, method = "deleteById")
    int deleteById(Object id);

    /**
     * delete by entity params
     * @param params
     * @return
     */
    @DeleteProvider(type = Provider.class, method = "deleteByEntity")
    int deleteByEntity(E params);

    @DeleteProvider(type = Provider.class, method = "deleteByWhere")
    int deleteByWhere(@Param(Const.WHERE) String where,
                      @Param(Const.PARAMS) Object params);


    @SelectProvider(type = Provider.class, method = "getById")
    E getById(@Param(Const.ID) Object id,
              @Param(Const.COLUMNS) String selectColumns);

    @SelectProvider(type = Provider.class, method = "getByEntity")
    E getByEntity(@Param(Const.PARAMS) E params,
                  @Param(Const.COLUMNS) String selectColumns);

    @SelectProvider(type = Provider.class, method = "getValueByEntity")
    <T> T getValueByEntity(@Param(Const.PARAMS) E params,
                           @Param(Const.COLUMN)  String column);

    @SelectProvider(type = Provider.class, method = "getValueByWhere")
    <T> T getValueByWhere(@Param(Const.WHERE) String where,
                          @Param(Const.PARAMS) Object params,
                          @Param(Const.COLUMN) String column);


    @SelectProvider(type = Provider.class, method = "listByEntity")
    List<E> listByEntity(@Param(Const.PARAMS) E params,
                         @Param(Const.ORDER) String orderBy,
                         @Param(Const.COLUMNS) String selectColumns);

    @SelectProvider(type = BySQLProvider.class, method = "listBySQL")
    List<E> listBySQL(@Param(Const.SQL) String sql,
                      @Param(Const.PARAMS) Object params);

    @SelectProvider(type = Provider.class, method = "pageByEntity")
    List<E> pageByEntity(@Param(Const.PARAMS) E params,
                         @Param(Const.PAGE) Page page,
                         @Param(Const.ORDER)String orderBy,
                         @Param(Const.COLUMNS) String selectColumns);

    @SelectProvider(type = Provider.class, method = "pageBySQL")
    List<E> pageBySQL(@Param(Const.SQL) String sql,
                      @Param(Const.PARAMS) Object params,
                      @Param(Const.PAGE) Page page);

}

