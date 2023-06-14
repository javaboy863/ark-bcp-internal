package com.ark.bcp.domain.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ark.bcp.domain.vo.LoadDataGroovyScriptVO;
import com.ark.bcp.domain.vo.LoadDataLogInfoVO;
import com.ark.bcp.domain.vo.LoadDataTemplateVO;
import com.ark.bcp.domain.util.ZipCompressUtils;
import com.missfresh.risk.bcp.enums.LoadDataStrategyDefine;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InspectionEventSourceEntity extends EventSourceConfigEntity {
    private static final long serialVersionUID = 2539522988728115531L;

    private static final Logger logger = LoggerFactory.getLogger(InspectionEventSourceEntity.class);

    private Integer loadDataStrategy;
    private LoadDataGroovyScriptVO loadDataGroovyScriptVO;
    private List<LoadDataTemplateVO> loadDataTemplateVOList;
    /**
     * 日志解析配置信息
     */
    private LoadDataLogInfoVO loadDataLogInfoVo;

    private String cron;

    public void toDetailConfig() {
        JSONObject detailJson = new JSONObject();
        detailJson.put("loadDataStrategy", loadDataStrategy);
        detailJson.put("cron", cron);
        LoadDataStrategyDefine loadDataStrategyDefine = LoadDataStrategyDefine.fromStrategy(loadDataStrategy);
        if (null != loadDataStrategyDefine) {
            switch (loadDataStrategyDefine) {
                case GROOVY_SCRIPT: {
                    String unzipParam = JSON.toJSONString(loadDataGroovyScriptVO);
                    detailJson.put("loadDataStrategyParam", ZipCompressUtils.gzip(unzipParam));
                    break;
                }
                case TEMPLATE_MYSQL: {
                    String unzipParam = JSON.toJSONString(loadDataTemplateVOList);
                    detailJson.put("loadDataStrategyParam", ZipCompressUtils.gzip(unzipParam));
                    break;
                }
                case LOG_TYPE: {
                    String unzipParam = JSON.toJSONString(loadDataLogInfoVo);
                    detailJson.put("loadDataStrategyParam", ZipCompressUtils.gzip(unzipParam));
                    break;
                }
                default: {
                    break;
                }
            }
        }
        setDetailConf(detailJson.toJSONString());
    }

    public void parseDetailConfig() {
        if (StringUtils.isEmpty(getDetailConf())) {
            return;
        }
        JSONObject detailJson = JSON.parseObject(getDetailConf());
        setLoadDataStrategy(detailJson.getInteger("loadDataStrategy"));
        setCron(detailJson.getString("cron"));

        LoadDataStrategyDefine loadDataStrategyDefine = LoadDataStrategyDefine.fromStrategy(loadDataStrategy);
        if (null == loadDataStrategyDefine) {
            return;
        }
        switch (loadDataStrategyDefine) {
            case GROOVY_SCRIPT: {
                parseGroovyLoadDataStrategy(detailJson.getString("loadDataStrategyParam"));
                break;
            }
            case TEMPLATE_MYSQL: {
                parseTemplateDataStrategy(detailJson.getString("loadDataStrategyParam"));
                break;
            }
            case LOG_TYPE: {
                parseLogInfoDataStrategy(detailJson.getString("loadDataStrategyParam"));
                break;
            }
            default:
        }
    }

    private void parseGroovyLoadDataStrategy(final String loadDataStrategyParam) {
        String unzipedConf = null;
        try {
            unzipedConf = ZipCompressUtils.gunzip(loadDataStrategyParam);
            loadDataGroovyScriptVO = JSON.parseObject(unzipedConf, LoadDataGroovyScriptVO.class);
        } catch (Exception e) {
            logger.error("解析参数异常:{}", unzipedConf, e);
        }
    }

    private void parseTemplateDataStrategy(final String loadDataStrategyParam) {
        String unzipedConf = null;
        try {
            unzipedConf = ZipCompressUtils.gunzip(loadDataStrategyParam);
            loadDataTemplateVOList = JSON.parseArray(unzipedConf, LoadDataTemplateVO.class);
        } catch (Exception e) {
            logger.error("解析参数异常:{}", unzipedConf, e);
        }
    }

    /**
     * 解析日志配置信息
     * @param loadDataStrategyParam
     */
    private void parseLogInfoDataStrategy(final String loadDataStrategyParam) {
        String unzipedConf = null;
        try {
            unzipedConf = ZipCompressUtils.gunzip(loadDataStrategyParam);
            loadDataLogInfoVo = JSON.parseObject(unzipedConf, LoadDataLogInfoVO.class);
        } catch (Exception e) {
            logger.error("解析参数异常:{}", unzipedConf, e);
        }
    }
}
