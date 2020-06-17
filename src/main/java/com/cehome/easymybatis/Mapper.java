package com.cehome.easymybatis;

import com.cehome.easymybatis.provider.*;
import com.cehome.easymybatis.utils.Const;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * coolma 2019/10/24
 * E means Entity, R means return DTO. R can be same as E or  R extends E ...
 **/
public interface Mapper<E,R> {

    /**
     *
     * @param entity
     * @return sql result ( usual is 1)
     */
    @InsertProvider(type = Provider.class, method = "insert")
    int insert(E entity);


    /**
     * update entity
     * @param entity
     * @return update result, 0 or 1
     */
    @UpdateProvider(type = Provider.class, method = "update")
    int update(E entity);


    /**
     *
     * @param entity  entity need to update
     * @param params  update conditions (equal condition).   entity , similar Object, Map
     * @return
     */
    @UpdateProvider(type = Provider.class, method = "updateByParams")
    int updateByParams(@Param(Const.ENTITY) E entity,
                       @Param(Const.PARAMS) Object params);


    /**
     *
     * @param entity    entity need to update
     * @param where   "{id}=#{id} and {realName}=#{realName}"
     * @param params   where params. can be entity , other Object, Map.  {id:2,realName:"mike" }
     * @return
     */
    @UpdateProvider(type = Provider.class, method = "updateByWhere")
    int updateByWhere(@Param(Const.ENTITY) E entity,
                      @Param(Const.WHERE) String where,
                      @Param(Const.PARAMS) Object params);



    /**
     * delete by id
     * @param id
     * @return
     */
    @DeleteProvider(type = Provider.class, method = "deleteById")
    int deleteById(Object id);

    /**
     * delete by entity params (with properties equals)
     * @param params
     * @return
     */
    @DeleteProvider(type = Provider.class, method = "deleteByParams")
    int deleteByParams(Object params);

    /**
     *
     * @param where   "{name}=#{name} and {realName}=#{realName}"
     * @param params  Entity(or similar object with the same props: name,realName... ) or Map
     * @return
     */
    @DeleteProvider(type = Provider.class, method = "deleteByWhere")
    int deleteByWhere(@Param(Const.WHERE) String where,
                      @Param(Const.PARAMS) Object params);


    /**
     * get single entity
     * @param id  integer,long,string ...
     * @param selectColumns
     * @return
     */
    @SelectProvider(type = Provider.class, method = "getById")
    R getById(@Param(Const.ID) Object id,
              @Param(Const.COLUMNS) String selectColumns);

    /**
     * get single entity
     * @param params E(entity or same props Object), Map
     * @param selectColumns Can be prop or column. null means all columns.
     * @return
     */
    @SelectProvider(type = Provider.class, method = "getByParams")
    R getByParams(@Param(Const.PARAMS) Object params,
                  @Param(Const.COLUMNS) String selectColumns);

    /**
     * get one column
     * @param params E(entity or same props Object), Map
     * @param column  prop or column name. null - return the first column
     * @param <T>
     * @return
     */
    @SelectProvider(type = Provider.class, method = "getValueByParams")
    <T> T getValueByParams(@Param(Const.PARAMS) Object params,
                           @Param(Const.COLUMN)  String column);

    /**
     * get one column
     * @param where  {realName}=#{realName} or real_name=#{realName} . You can use {prop} to instead of column name
     * @param params E(entity or same props Object), Map
     * @param column prop or column name. null - return the first column
     * @param <T>
     * @return
     */
    @SelectProvider(type = Provider.class, method = "getValueByWhere")
    <T> T getValueByWhere(@Param(Const.WHERE) String where,
                          @Param(Const.PARAMS) Object params,
                          @Param(Const.COLUMN) String column);

    /**
     * get one column
     * @param sql  select column1 from {TABLE}
     * @param params
     * @param <T>
     * @return
     */
    @SelectProvider(type = Provider.class, method = "getValueBySQL")
    <T> T getValueBySQL(@Param(Const.SQL) String sql,
                        @Param(Const.PARAMS) Object params);

    /**
     *
     * @param params E(entity or same props Object), Map
     * @param orderBy   column1 desc,column2
     * @param selectColumns prop or column names. null means *(all columns).
     * @return
     */
    @SelectProvider(type = Provider.class, method = "listByParams")
    List<R> listByParams(@Param(Const.PARAMS) Object params,
                         @Param(Const.ORDER) String orderBy,
                         @Param(Const.COLUMNS) String selectColumns);

    /**
     *
     * @param sql
     *               select * from table1 where real_name=#{realName}
     *               select * from {TABLE} where real_name=#{realName} // {TABLE} point to entity table
     *               real_name=#{realName}  //  ignore select...where
     *              {realName}=#{realName}  // replace real_name with prop {realName}

     * @param params E(entity or same props Object), Map
     * @return
     */
    @SelectProvider(type = BySQLProvider.class, method = "listBySQL")
    List<R> listBySQL(@Param(Const.SQL) String sql,
                      @Param(Const.PARAMS) Object params);

    /**
     *
     * @param params E(entity or same props Object), Map
     * @param page  After invoke, Page object also fill with record count and the return list data (use page.getDate())
     * @param orderBy - column1 desc,column2
     * @param selectColumns - prop or column names. null means *(all columns).
     * @return
     */
    @SelectProvider(type = Provider.class, method = "pageByParams")
    List<R> pageByParams(@Param(Const.PARAMS) Object params,
                         @Param(Const.PAGE) Page page,
                         @Param(Const.ORDER)String orderBy,
                         @Param(Const.COLUMNS) String selectColumns);

    /**
     *
     * @param sql {@link Mapper#listBySQL(java.lang.String, java.lang.Object)}
     * @param params E(entity or same props Object), Map
     * @param page After invoke, Page object also fill with record count and the return list data (use page.getDate())
     * @return
     */
    @SelectProvider(type = Provider.class, method = "pageBySQL")
    List<R> pageBySQL(@Param(Const.SQL) String sql,
                      @Param(Const.PARAMS) Object params,
                      @Param(Const.PAGE) Page page);

}

