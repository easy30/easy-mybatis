package com.github.easy30.easymybatis;

import com.github.easy30.easymybatis.annotation.LimitOne;
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
    int insert(@Param(Const.ENTITY)E entity, @Param(Const.OPTIONS) UpdateOption... options);

    /**
     * update entity by id
     * @param entity
     * @return update result, 0 or 1
     */
    @UpdateProvider(type = Provider.class, method = "update")
    int update(@Param(Const.ENTITY)E entity, @Param(Const.OPTIONS) UpdateOption... options);

    /**
     * insert or update entity by id
     * if id is null then insert, else update
     * @param entity
     * @return update result, 0 or 1
     */
    @UpdateProvider(type = Provider.class, method = "save")
    int save(@Param(Const.ENTITY)E entity, @Param(Const.OPTIONS) UpdateOption... options);


    /**
     *
     * @param entity  entity need to update
     * @param params  update conditions (equal condition).   entity , similar Object, Map
     * @param paramNames properties of params used for query. Using paramNames can avoid updating by mistake.
     *                   - at least one param name need.
     * @return
     */
    @UpdateProvider(type = Provider.class, method = "updateByParams")
    int updateByParams(@Param(Const.ENTITY) E entity,
                       @Param(Const.PARAMS) Object params,
                       @Param(Const.PARAM_NAEMS) String paramNames,
                       @Param(Const.OPTIONS) UpdateOption... options);


    /**
     *
     * @param entity    entity need to update
     * @param condition   "{id}=#{id} and {realName}=#{realName}"
     * @param params   condition params. can be entity , other Object, Map.  {id:2,realName:"mike" }
     * @return
     */
    @UpdateProvider(type = Provider.class, method = "updateByCondition")
    int updateByCondition(@Param(Const.ENTITY) E entity,
                          @Param(Const.CONDITION) String condition,
                          @Param(Const.PARAMS) Object params,
                          @Param(Const.OPTIONS) UpdateOption... options);

    /**
     * delete by id
     * @param id
     * @return
     */
    @DeleteProvider(type = Provider.class, method = "deleteById")
    int deleteById(@Param(Const.ID)Object id,@Param(Const.OPTIONS) DeleteOption... options);

    /**
     * delete by entity params (with properties equals)
     * @param params
     * @param paramNames, split by ,
     * @return
     */
    @DeleteProvider(type = Provider.class, method = "deleteByParams")
    int deleteByParams(@Param(Const.PARAMS) Object params,@Param(Const.PARAM_NAEMS) String paramNames,@Param(Const.OPTIONS) DeleteOption... options);

    /**
     *
     * @param condition   "{name}=#{name} and {realName}=#{realName}"
     * @param params  Entity(or similar object with the same props: name,realName... ) or Map
     * @return
     */
    @DeleteProvider(type = Provider.class, method = "deleteByCondition")
    int deleteByCondition(@Param(Const.CONDITION) String condition,
                          @Param(Const.PARAMS) Object params,@Param(Const.OPTIONS) DeleteOption... options);


    /**
     * get single entity
     * @param id  integer,long,string ...
     * @param selectColumns
     * @return
     */
    @SelectProvider(type = Provider.class, method = "get")
    R get(@Param(Const.ID) Object id,
              @Param(Const.COLUMNS) String selectColumns,
              @Param(Const.OPTIONS) SelectOption... options);


    /**
     * get single entity
     * @param params E(entity or same props Object), Map
     * @param selectColumns Can be prop or column. null means all columns.
     * @return
     */
    @LimitOne
    @SelectProvider(type = Provider.class, method = "getByParams")
    R getByParams(@Param(Const.PARAMS) Object params,
                  @Param(Const.ORDER) String orderBy,
                  @Param(Const.COLUMNS) String selectColumns,@Param(Const.OPTIONS) SelectOption... options);

    /**
     * get one column
     * @param params E(entity or same props Object), Map
     * @param column  prop or column name. null - return the first column
     * @param <T>
     * @return
     */
    @LimitOne
    @SelectProvider(type = Provider.class, method = "getValueByParams")
    <T> T getValueByParams(@Param(Const.PARAMS) Object params,
                           @Param(Const.ORDER) String orderBy,
                           @Param(Const.COLUMN)  String column,@Param(Const.OPTIONS) SelectOption... options);

    /**
     * get one column
     * @param condition  {realName}=#{realName} or real_name=#{realName} . You can use {prop} to instead of column name
     * @param params E(entity or same props Object), Map
     * @param column prop or column name. null - return the first column
     * @param <T>
     * @return
     */
    @LimitOne
    @SelectProvider(type = Provider.class, method = "getValueByCondition")
    <T> T getValueByCondition(@Param(Const.CONDITION) String condition,
                              @Param(Const.PARAMS) Object params,
                              @Param(Const.COLUMN) String column,@Param(Const.OPTIONS) SelectOption... options);

    /**
     * get one column
     * @param sql  select column1 from {TABLE}
     * @param params
     * @param <T>
     * @return
     */
    @LimitOne
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
                         @Param(Const.COLUMNS) String selectColumns,@Param(Const.OPTIONS) SelectOption... options);

    /**
     * select entity list by ids
     * @param ids id array,  integer,long,string ...
     * @param selectColumns
     * @return
     */
    @SelectProvider(type = Provider.class, method = "listByIds")
    List<R> listByIds(@Param(Const.IDS) Object[] ids,
                 @Param(Const.COLUMNS) String selectColumns,
                 @Param(Const.OPTIONS) SelectOption... options);


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
    @SelectProvider(type = Provider.class, method = "listBySQL")
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
                         @Param(Const.COLUMNS) String selectColumns,@Param(Const.OPTIONS) SelectOption... options);

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

    /*default TableContext useTable(String table){
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(this.getClass());
        entityAnnotation.setContextTable(table);
        return new TableContext(entityAnnotation);
    }
    default void removeTable(){
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(this.getClass());
        entityAnnotation.removeContextTable();
    }*/

    //@SelectProvider(type = Provider.class, method = "list")
    //List<Map> list(Map params);

}

