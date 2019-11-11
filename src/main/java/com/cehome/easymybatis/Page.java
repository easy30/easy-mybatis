package com.cehome.easymybatis;

import java.io.Serializable;
import java.util.List;

public class Page<E> implements Serializable {

    private static final long serialVersionUID = 7395507780937350288L;

    /**
     * 第几页,从1开始
     */
    private int pageIndex;

    /**
     * 每页显示多少条
     */
    private int pageSize;

    /**
     * 分页的开始值
     */
    private int pageOffset;
    private int pageOffsetEnd;

    /**
     * 总共多少条记录
     */
    private int totalRecord;

    /**
     * 总共多少页
     */
    private int totalPage;

    /**
     * 放置具体数据的列表
     */
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