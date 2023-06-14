package com.ark.bcp.domain.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.missfresh.domain.ErrorCodeEnum;
import com.missfresh.domain.Result;
import com.ark.bcp.domain.script.SqlExecuteInvoker;
import com.ark.bcp.domain.util.ResultUtils;
import com.missfresh.risk.bcp.dto.LoadDataTemplateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 */
@Service
public class InspectionLoadDataTemplateDomainService {
    private static Logger logger = LoggerFactory.getLogger(InspectionLoadDataTemplateDomainService.class);
    private static Map<Integer, Function<LoadDataTemplateDto, Result<?>>> loadTemplates = Maps.newHashMap();

    private static final Integer LOAD_TEMPLATE_MYSQL = 1;

    static {
        loadTemplates.put(LOAD_TEMPLATE_MYSQL, new MysqlLocalRunFunction());
    }

    public Result<?> mockTemplateDataLoader(final LoadDataTemplateDto loadDataTemplateDto) {
        if (null == loadDataTemplateDto || null == loadDataTemplateDto.getConnStrategy()) {
            return ResultUtils.wrapFailure(ErrorCodeEnum.ERR_PARAM);
        }
        if (!loadTemplates.containsKey(loadDataTemplateDto.getConnStrategy())) {
            return ResultUtils.wrapFailure(-1, "不是别该连接策略");
        }
        return loadTemplates.get(loadDataTemplateDto.getConnStrategy()).apply(loadDataTemplateDto);
    }

    static class MysqlLocalRunFunction implements Function<LoadDataTemplateDto, Result<?>> {
        /**
         * Applies this function to the given argument.
         *
         * @param loadDataTemplateDto the function argument
         * @return the function result
         */
        @Override
        public Result<?> apply(LoadDataTemplateDto loadDataTemplateDto) {
            try {
                List<JSONObject> lines = SqlExecuteInvoker.mysqlQuery(
                        loadDataTemplateDto.getConnMysqlAddr(),
                        Integer.valueOf(loadDataTemplateDto.getConnMysqlPort()),
                        loadDataTemplateDto.getConnMysqlDatabase(),
                        loadDataTemplateDto.getConnMysqlUsername(),
                        loadDataTemplateDto.getConnMysqlPassword(),
                        loadDataTemplateDto.getSql()
                );
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("result", lines);
                return ResultUtils.wrapSuccess(jsonObject);
            } catch (Exception e) {
                logger.error("执行sql异常", e);
                return ResultUtils.wrapFailure(-1, e.getMessage());
            }
        }
    }
}
