package com.translatr.dto;

import java.util.List;

public class PagedList<T> {
    public List<T> list;
    public int     total;
    public int     offset;
    public int     limit;
    /** Whether another page follows the current window — the UI's "Load more" hinges on this. */
    public boolean hasNext;
    /** Whether a page precedes the current window. */
    public boolean hasPrev;

    public PagedList(List<T> list, long total, int offset, int limit) {
        this.list    = list;
        this.total   = (int) total;
        this.offset  = offset;
        this.limit   = limit;
        this.hasNext = limit > 0 && offset + limit < total;
        this.hasPrev = offset > 0;
    }
}
