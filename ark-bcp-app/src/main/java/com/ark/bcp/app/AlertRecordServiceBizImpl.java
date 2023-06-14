package com.ark.bcp.app;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.missfresh.domain.ErrorCodeEnum;
import com.missfresh.domain.Result;
import com.missfresh.risk.bcp.api.AlertRecordService;
import com.missfresh.risk.bcp.domain.entity.CheckFailRecordEntity;
import com.missfresh.risk.bcp.domain.entity.CheckRuleConfigEntity;
import com.missfresh.risk.bcp.domain.exception.CheckParameterException;
import com.missfresh.risk.bcp.domain.service.CheckFailRecordService;
import com.missfresh.risk.bcp.domain.service.RuleConfigDomainService;
import com.missfresh.risk.bcp.domain.util.ResultUtils;
import com.missfresh.risk.bcp.dto.AlertRecordDto;
import com.missfresh.risk.bcp.dto.AlertRecordPageQueryDto;
import com.missfresh.risk.bcp.dto.PageResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 */
@Service(value = "alertRecordServiceBiz")
public class AlertRecordServiceBizImpl implements AlertRecordService {
    private static final Logger logger = LoggerFactory.getLogger(AlertRecordService.class);

    @Resource
    private CheckFailRecordService checkFailRecordService;

    @Resource
    private RuleConfigDomainService ruleConfigDomainService;

    /**
     * 分页获取记录.
     *
     * @param pageQueryDto ""
     * @return ""
     */
    @Override
    public Result<PageResultDto<AlertRecordDto>> queryByPage(AlertRecordPageQueryDto pageQueryDto) {
        //入参检查
        checkRequest(pageQueryDto);

        //将入参转换为查询实体
        CheckFailRecordEntity checkFailRecordEntity = convertByAlertRecordPageQueryDto(pageQueryDto);

        //查询列表
        List<CheckFailRecordEntity> list = checkFailRecordService.selectBcpCheckFailRecordListPage(checkFailRecordEntity,
                pageQueryDto.getFromTime(), pageQueryDto.getEndTime());

        //查询总条数
        Long total = checkFailRecordService.selectBcpCheckFailRecordListPageTotal(checkFailRecordEntity,
                pageQueryDto.getFromTime(), pageQueryDto.getEndTime());

        //将查询结果转换为AlertRecordDto
        List<AlertRecordDto> alertRecordDtoList = convertByCheckFailRecordEntity(list);

        //填充规则名
        fillRuleName(alertRecordDtoList);

        //返回值组装
        return ResultUtils.wrapSuccess(getQueryByPageRes(total,alertRecordDtoList));
    }

    /**
     * 更新报警信息.
     *
     * @param alertRecordDto ""
     * @return ""
     */
    @Override
    public Result updateAlertRecord(AlertRecordDto alertRecordDto) {
        logger.info("updateAlertRecord:{}", JSON.toJSONString(alertRecordDto));
        //入参检查
        updateAlertRecordCheckRequest(alertRecordDto);

        //将入参转换为CheckFailRecordEntity
        CheckFailRecordEntity checkFailRecordEntity = convertByAlertRecordDto(alertRecordDto);

        //更新操作
        int effectRows = checkFailRecordService.updateBcpCheckFailRecordByIdSelective(checkFailRecordEntity);

        return getUpdateAlertRecordRes(effectRows);
    }

    /**
     * 单个报警详细信息.
     *
     * @param id ""
     * @return ""
     */
    @Override
    public Result<AlertRecordDto> detail(Long id) {
        //入参检查
        checkDetailRequest(id);

        //查询操作
        CheckFailRecordEntity checkFailRecordEntity = checkFailRecordService.getById(id);

        //将CheckFailRecordEntity 转为AlertRecordDto
        AlertRecordDto alertRecordDto = convertByCheckFailRecordEntity(checkFailRecordEntity);

        return ResultUtils.wrapSuccess(alertRecordDto);
    }



    /**
     * 填充规则名
     * @param alertRecordDtoList
     */
    private void fillRuleName(List<AlertRecordDto> alertRecordDtoList) {
        //提取id
        Set<Long> ruleIds = extractId(alertRecordDtoList);
        if (CollectionUtils.isEmpty(ruleIds)) {
            return;
        }

        //根据规则id查询对应的内容转化为Map
        Map<Long, CheckRuleConfigEntity> ruleConfigEntityMap = queryRuleConfigEntityMap(ruleIds);

        alertRecordDtoList.forEach(new Consumer<AlertRecordDto>() {
            @Override
            public void accept(AlertRecordDto alertRecordDto) {
                if (null != alertRecordDto.getRuleId() && ruleConfigEntityMap.containsKey(alertRecordDto.getRuleId())) {
                    alertRecordDto.setRuleName(ruleConfigEntityMap.get(alertRecordDto.getRuleId()).getRuleName());
                }
            }
        });
    }


    /**
     * 检查入参
     * @param pageQueryDto
     * @return
     */
    private void checkRequest(AlertRecordPageQueryDto pageQueryDto){
        if(pageQueryDto == null){
            throw new CheckParameterException("入参格式不正确");
        }
    }
    /**
     * 检查入参
     * @param alertRecordDto
     * @return
     */
    private void updateAlertRecordCheckRequest(AlertRecordDto alertRecordDto){
        if(alertRecordDto == null){
            throw new CheckParameterException("入参格式不正确");
        }
    }

    /**
     *  入参检查
     * @param id
     * @return
     */
    private void checkDetailRequest(Long id){

        if (null == id || 0 == id) {
            throw new CheckParameterException("入参格式不正确");
        }
    }

    /**
     * 将CheckFailRecordEntity转换为AlertRecordDto
     * @return
     */
    private AlertRecordDto convertByCheckFailRecordEntity(CheckFailRecordEntity checkFailRecordEntity){

        AlertRecordDto alertRecordDto = new AlertRecordDto();
        alertRecordDto.setId(checkFailRecordEntity.getId());
        alertRecordDto.setCreateTime(checkFailRecordEntity.getCreateTime());
        alertRecordDto.setAlertMsg(checkFailRecordEntity.getReason());
        alertRecordDto.setHandleMsg(checkFailRecordEntity.getHandleMsg());
        alertRecordDto.setRuleId(checkFailRecordEntity.getRuleId());
        alertRecordDto.setStatus(checkFailRecordEntity.getStatus());

        return alertRecordDto;
    }
    /**
     * 将AlertRecordDto 转换为CheckFailRecordEntity
     * @param alertRecordDto
     * @return
     */
    private CheckFailRecordEntity convertByAlertRecordDto(AlertRecordDto alertRecordDto){

        CheckFailRecordEntity checkFailRecordEntity = new CheckFailRecordEntity();
        checkFailRecordEntity.setId(alertRecordDto.getId());
        checkFailRecordEntity.setRuleId(alertRecordDto.getRuleId());
        checkFailRecordEntity.setStatus(alertRecordDto.getStatus());
        checkFailRecordEntity.setHandleMsg(alertRecordDto.getHandleMsg());

        return checkFailRecordEntity;
    }

    /**
     * 将AlertRecordPageQueryDto 转换为CheckFailRecordEntity
     * @param pageQueryDto
     * @return
     */
    private CheckFailRecordEntity convertByAlertRecordPageQueryDto(AlertRecordPageQueryDto pageQueryDto){
        CheckFailRecordEntity checkFailRecordEntity = new CheckFailRecordEntity();
        checkFailRecordEntity.setRuleId(pageQueryDto.getRuleId());
        checkFailRecordEntity.setStatus(pageQueryDto.getStatus());
        checkFailRecordEntity.setPageNo(pageQueryDto.getPageNo());
        checkFailRecordEntity.setPageSize(pageQueryDto.getPageSize());

        return checkFailRecordEntity;
    }

    /**
     * 将CheckFailRecordEntity list 转换为AlertRecordDto list
     * @param list
     * @return
     */
    private List<AlertRecordDto> convertByCheckFailRecordEntity(List<CheckFailRecordEntity> list){

        List<AlertRecordDto> alertRecordDtoList = list.stream().map(
                new Function<CheckFailRecordEntity, AlertRecordDto>() {
                    @Override
                    public AlertRecordDto apply(CheckFailRecordEntity checkFailRecordEntity) {
                        AlertRecordDto alertRecordDto = new AlertRecordDto();
                        alertRecordDto.setId(checkFailRecordEntity.getId());
                        alertRecordDto.setCreateTime(checkFailRecordEntity.getCreateTime());
                        alertRecordDto.setAlertMsg(checkFailRecordEntity.getReason());
                        alertRecordDto.setHandleMsg(checkFailRecordEntity.getHandleMsg());
                        alertRecordDto.setRuleId(checkFailRecordEntity.getRuleId());
                        alertRecordDto.setStatus(checkFailRecordEntity.getStatus());
                        return alertRecordDto;
                    }
                }
        ).collect(Collectors.toList());

        return alertRecordDtoList;
    }

    /**
     * 提取Id 集合
     * @return
     */
    private Set<Long> extractId(List<AlertRecordDto> alertRecordDtoList){
        if (CollectionUtils.isEmpty(alertRecordDtoList)) {
            return null;
        }
        Set<Long> ruleIds = alertRecordDtoList.stream().map(
                new Function<AlertRecordDto, Long>() {
                    @Override
                    public Long apply(AlertRecordDto alertRecordDto) {
                        return alertRecordDto.getRuleId();
                    }
                }
        ).collect(Collectors.toSet());

        return ruleIds;
    }

    /**
     * 根据id 获取ruleConfigEntity
     * @param ruleIds
     * @return
     */
    private Map<Long, CheckRuleConfigEntity> queryRuleConfigEntityMap(Set<Long> ruleIds){
        Map<Long, CheckRuleConfigEntity> ruleConfigEntityMap = Maps.newConcurrentMap();

        ruleIds.forEach(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) {
                CheckRuleConfigEntity entity = ruleConfigDomainService.selectCheckRuleConfigById(aLong);
                if (null != entity) {
                    ruleConfigEntityMap.put(aLong, entity);
                }
            }
        });

        return ruleConfigEntityMap;
    }

    /**
     * 返回值组装
     * @param effectRows
     * @return
     */
    private Result getUpdateAlertRecordRes(int effectRows){
        return 0 == effectRows
                ? ResultUtils.wrapFailure(ErrorCodeEnum.ERR_PARAM, "更新失败")
                : ResultUtils.wrapSuccess();
    }

    /**
     * 返回值组装
     * @param total
     * @param alertRecordDtoList
     * @return
     */
    private PageResultDto<AlertRecordDto> getQueryByPageRes(Long total,List<AlertRecordDto> alertRecordDtoList){
        PageResultDto<AlertRecordDto> data = PageResultDto.<AlertRecordDto>builder()
                .total(total)
                .dataList(alertRecordDtoList).build();

        return data;
    }
}
