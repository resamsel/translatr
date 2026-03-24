package com.translatr.dto;

import java.util.List;

public class PagedList<T> {
    public List<T> list;
    public int total;
    public int offset;
    public int limit;

    public PagedList(List<T> list, long total, int offset, int limit) {
        this.list   = list;
        this.total  = (int) total;
        this.offset = offset;
        this.limit  = limit;
    }
}
