

package com.ark.bcp.app.utils;

import com.missfresh.risk.bcp.domain.vo.LoadDataTemplateVO;
import com.missfresh.risk.bcp.dto.LoadDataTemplateDto;

import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class LoadDataTemplates {
    public static List<LoadDataTemplateDto> toLoadDataTemplateDtos(final List<LoadDataTemplateVO> loadDataTemplates) {
        if (null == loadDataTemplates) {
            return null;
        }
        List<LoadDataTemplateDto> loadDataTemplateDtos = loadDataTemplates.stream().map(LoadDataTemplates::transToLoadDataTemplateDto).collect(Collectors.toList());
        while (loadDataTemplateDtos.remove(null)) {
        }
        return loadDataTemplateDtos;
    }

    public static LoadDataTemplateDto transToLoadDataTemplateDto(final LoadDataTemplateVO loadDataTemplateVO) {
        if (null == loadDataTemplateVO) {
            return null;
        }
        return LoadDataTemplateDto.builder()
                .fieldName(loadDataTemplateVO.getField())
                .connStrategy(loadDataTemplateVO.getConnStrategy())
                .connMysqlAddr(loadDataTemplateVO.getHost())
                .connMysqlPort(loadDataTemplateVO.getPort())
                .connMysqlUsername(loadDataTemplateVO.getUsr())
                .connMysqlPassword(loadDataTemplateVO.getPwd())
                .connMysqlDatabase(loadDataTemplateVO.getDatabase())
                .sql(loadDataTemplateVO.getSql())
                .build();
    }

    public static List<LoadDataTemplateVO> fromLoadDataTemplateDtos(final List<LoadDataTemplateDto> loadDataTemplateDtos) {
        if (null == loadDataTemplateDtos) {
            return null;
        }
        List<LoadDataTemplateVO> vos = loadDataTemplateDtos.stream().map(LoadDataTemplates::fromLoadDataTemplateDto).collect(Collectors.toList());
        while (vos.remove(null)) {

        }
        return vos;
    }

    public static LoadDataTemplateVO fromLoadDataTemplateDto(final LoadDataTemplateDto loadDataTemplateDto) {
        if (null == loadDataTemplateDto) {
            return null;
        }
        return LoadDataTemplateVO.builder()
                .field(loadDataTemplateDto.getFieldName())
                .connStrategy(loadDataTemplateDto.getConnStrategy())
                .host(loadDataTemplateDto.getConnMysqlAddr())
                .port(loadDataTemplateDto.getConnMysqlPort())
                .usr(loadDataTemplateDto.getConnMysqlUsername())
                .pwd(loadDataTemplateDto.getConnMysqlPassword())
                .database(loadDataTemplateDto.getConnMysqlDatabase())
                .sql(loadDataTemplateDto.getSql())
                .build();
    }
}
