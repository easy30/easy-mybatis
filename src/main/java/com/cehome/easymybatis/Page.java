package com.cehome.easymybatis;

import java.io.Serializable;
import java.util.List;

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
    private int pageOffset;

    private int pageOffsetEnd;


    private int totalRecord;


    private int totalPage;


    private List<E> data;



    public Page() {
    }

    public Page(int pageIndex,int pageSize) {
        setPageIndex(pageIndex);
        setPageSize(pageSize);
        setPageOffset((pageIndex-1)*pageSize);
        setPageOffsetEnd(pageIndex*pageIndex-1);//
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

    public int getTotalRecord() {
        return totalRecord;
    }

    public void setTotalRecord(int totalRecord) {
        this.totalRecord = totalRecord;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<E> getData() {
        return data;
    }

    public void setData(List<E> data) {
        this.data = data;
    }

    public int getPageOffset() {
        return pageOffset;
    }

    public void setPageOffset(int pageOffset) {
        this.pageOffset = pageOffset;
    }

    public int getPageOffsetEnd() {
        return pageOffsetEnd;
    }

    public void setPageOffsetEnd(int pageOffsetEnd) {
        this.pageOffsetEnd = pageOffsetEnd;
    }
}