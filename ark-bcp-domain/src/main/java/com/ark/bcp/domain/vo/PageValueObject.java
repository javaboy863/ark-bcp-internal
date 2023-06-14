package com.ark.bcp.domain.vo;

import java.io.Serializable;

/**
 */
public class PageValueObject implements Serializable {

    private static final long serialVersionUID = -8321053260388873237L;

    /**
     * .
     */
    public static final int DEFAULT_PAGE_NO = 1;
    /**
     * .
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    private Integer pageNo = DEFAULT_PAGE_NO;
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        if (pageNo != null && pageNo > 0) {
            this.pageNo = pageNo;
        }

    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        if (pageSize != null && pageSize > 0) {
            this.pageSize = pageSize;
        }

    }

    public int getStartOfPage() {
        if (pageNo == 0) {
            pageNo = DEFAULT_PAGE_NO;
        }
        return (pageNo - 1) * pageSize;
    }

    @Override
    @SuppressWarnings({"OperatorWrap"})
    public String toString() {
        return "PageValueObject{" +
                "pageNo=" + pageNo +
                ", pageSize=" + pageSize +
                '}';
    }
}
