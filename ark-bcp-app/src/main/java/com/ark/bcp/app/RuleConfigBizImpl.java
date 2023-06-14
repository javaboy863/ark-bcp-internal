package com.ark.bcp.app;


import com.missfresh.domain.ErrorCodeEnum;
import com.missfresh.domain.Result;
import com.missfresh.risk.bcp.api.RuleConfigService;
import com.ark.bcp.app.service.rules.RuleConfigInnerService;
import com.missfresh.risk.bcp.domain.util.ResultUtils;
import com.missfresh.risk.bcp.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 */
@Service
@Slf4j
public class RuleConfigBizImpl implements RuleConfigService {

    @Resource
    private RuleConfigInnerService ruleConfigInnerService;


    @Override
    public Result saveRuleConfig(RuleConfigDto ruleConfigDto) {
        try {
            // 带事务保存所有信息到数据库
            Result<Long> resultRuleId = ruleConfigInnerService.saveRuleConfig(ruleConfigDto);
            // 在事务提交之后再通知！！
            if (ResultUtils.resultIsAvaliable(resultRuleId)) {
                ruleConfigInnerService.notifySaveRuleConfig(resultRuleId.getData());
            }
            return resultRuleId;
        } catch (Exception e) {
            log.error("保存规则异常", e);
        }
        return ResultUtils.wrapFailure(ErrorCodeEnum.ERR_SERVER_ERROR);
    }

    /**
     * 修改规则配置
     * @param ruleConfigDto
     * @return
     */
    @Override
    public Result updateRuleConfig(RuleConfigDto ruleConfigDto) {
        try {
            // 带事务更新规则配置到数据库
            Result result = ruleConfigInnerService.updateRuleConfig(ruleConfigDto);
            // 在事务提交之后再通知！！
            if (ResultUtils.resultIsAvaliable(result)) {
                ruleConfigInnerService.notifyUpdateRuleConfig(ruleConfigDto.getId());
            }
            return result;
        } catch (Exception e) {
            log.error("更新规则异常", e);
        }
        return ResultUtils.wrapFailure(ErrorCodeEnum.ERR_SERVER_ERROR);
    }

    /**
     * 查询规则配置列表
     * @param queryDto
     * @return
     */
    @Override
    public Result<PageResultDto<RuleConfigPageResultDto>> queryRuleConfigList(RuleConfigPageQueryDto queryDto) {
        return ruleConfigInnerService.queryRuleConfigList(queryDto);
    }

    /**
     * 删除规则配置
     * @param ruleConfigDto
     * @return
     */
    @Override
    public Result deleteRuleConfig(RuleConfigDto ruleConfigDto) {
        return ruleConfigInnerService.deleteRuleConfig(ruleConfigDto);
    }

    /**
     * 禁用规则配置
     * @param ruleConfigDto
     * @return
     */
    @Override
    public Result disableRuleConfig(RuleConfigDto ruleConfigDto) {
        return ruleConfigInnerService.disableRuleConfig(ruleConfigDto);
    }

    /**
     * 启用规则配置
     * @param ruleConfigDto
     * @return
     */
    @Override
    public Result enableRuleConfig(RuleConfigDto ruleConfigDto) {
        return ruleConfigInnerService.enableRuleConfig(ruleConfigDto);
    }

    /**
     * 规则配置详情
     * @param ruleId
     * @return
     */
    @Override
    public Result<RuleConfigInfoDto> ruleConfigInfo(Long ruleId) {
        return ruleConfigInnerService.ruleConfigInfo(ruleId);
    }

    /**
     * 查询规则配置列表
     * @param ruleConfigDto
     * @return
     */
    @Override
    public Result<List<RuleConfigPageResultDto>> searchRuleConfigList(RuleConfigDto ruleConfigDto) {
        return ruleConfigInnerService.searchRuleConfigList(ruleConfigDto);
    }

    /**
     * mock  测试
     * @param ruleConfigDto
     * @return
     */
    @Override
    public Result<List<MokeResultDto>> mockTest(RuleConfigDto ruleConfigDto) {
        return ruleConfigInnerService.mockTest(ruleConfigDto);
    }
}
