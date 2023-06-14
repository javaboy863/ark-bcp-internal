

package com.ark.bcp.app.utils;
import com.missfresh.risk.bcp.domain.vo.LoadDataLogInfoVO;
import com.missfresh.risk.bcp.dto.LogInfoDto;
import org.springframework.beans.BeanUtils;

/**
 */
public class LoadDataLogInfoVOs {

    public static LogInfoDto toLogInfoDto(final LoadDataLogInfoVO loadDataLogInfoVO) {
        if (null == loadDataLogInfoVO) {
            return null;
        }
        LogInfoDto logInfoDto = new LogInfoDto();
        BeanUtils.copyProperties(loadDataLogInfoVO,logInfoDto);
        return logInfoDto;
    }

    public static LoadDataLogInfoVO fromLoadDataLoginfoDtos(final LogInfoDto logInfoDtos) {
        if (null == logInfoDtos) {
            return null;
        }
        LoadDataLogInfoVO vos = new LoadDataLogInfoVO();
        BeanUtils.copyProperties(logInfoDtos,vos);
        return vos;
    }

    public static LoadDataLogInfoVO fromLoadDataLogInfoDto(final LogInfoDto logInfoDto) {
        if (null == logInfoDto) {
            return null;
        }
        return LoadDataLogInfoVO.builder()
                .logInfoDetailDtos(logInfoDto.getLogInfoDetailDtos())
                .beforeMinute(logInfoDto.getBeforeMinute())
                .timeRegion(logInfoDto.getTimeRegion())
                .build();
    }
}
