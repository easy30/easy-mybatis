package com.cehome.easymybatis;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @param <E>
 */
public class Page<E> implements Serializable {

    private static final long serialVersionUID = 7395507780937350288L;

    /**
     * page no 1,2,3...
     */
    private int pageIndex;


    private int pageSize;

    /**
     *  record start row
     */
    private int recordStart;

    private int recordEnd;


    private int recordCount;


    private int pageCount;


    private List<E> data;

    private boolean queryCount=true;

    public Page() {
    }

    public Page(int pageIndex,int pageSize) {
        setPageIndex(pageIndex);
        setPageSize(pageSize);
        setRecordStart((pageIndex-1)*pageSize);
        setRecordEnd(pageIndex*pageIndex-1);//
    }


    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public List<E> getData() {
        return data;
    }

    public void setData(List<E> data) {
        this.data = data;
    }

    public int getRecordStart() {
        return recordStart;
    }

    public void setRecordStart(int recordStart) {
        this.recordStart = recordStart;
    }

    public int getRecordEnd() {
        return recordEnd;
    }

    public void setRecordEnd(int recordEnd) {
        this.recordEnd = recordEnd;
    }

    public boolean isQueryCount() {
        return queryCount;
    }

    public void setQueryCount(boolean queryCount) {
        this.queryCount = queryCount;
    }
}