package com.ark.bcp.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoadDataTemplateVO {
    private String field;
    /**
     * 1 mysql
     */
    private Integer connStrategy;
    private String host;
    private String port;
    private String database;
    private String usr;
    private String pwd;
    private String sql;
}
