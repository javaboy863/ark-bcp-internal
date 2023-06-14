package com.ark.bcp.web.web.controller;

import com.alibaba.fastjson.JSON;
import com.missfresh.domain.Result;
import com.missfresh.risk.bcp.api.EventSourceConfigService;
import com.missfresh.risk.bcp.dto.EventSourceConfigDto;
import com.missfresh.risk.bcp.dto.PageResultDto;
import com.mryx.grampus.ccs.dto.CcsLoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 */
@RestController
@RequestMapping("/risk/bcp/bg/event")
@Slf4j
public class EventSourceConfigController {

    @Resource
    private EventSourceConfigService eventSourceConfigService;

    @RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST)
    public Result saveOrUpdate(@RequestBody EventSourceConfigDto eventSourceConfigDto) {
        log("saveOrUpdate dto:{}",eventSourceConfigDto);
        //保存用户
        if (isSaveUser(eventSourceConfigDto)){
            return eventSourceConfigService.saveEventSource(eventSourceConfigDto);
        }
        //更新用户
        return updateUser(eventSourceConfigDto);
    }

    private Result updateUser(EventSourceConfigDto eventSourceConfigDto) {
        log("ccs user:{}", JSON.toJSONString(getLoginUserName()));
        setUpdater(eventSourceConfigDto, getLoginUserName());
        return eventSourceConfigService.updateEventSource(eventSourceConfigDto);
    }

    private void setUpdater(EventSourceConfigDto eventSourceConfigDto, String loginUserName) {
        eventSourceConfigDto.setUpdateUser(loginUserName);
    }

    private boolean isSaveUser(EventSourceConfigDto eventSourceConfigDto) {
        if (eventSourceConfigDto.getId() == null) {
            eventSourceConfigDto.setCreateUser(getLoginUserName());
            return true;
        }
        return false;
    }

    private String getLoginUserName() {
        return null == CcsLoginUser.get() ? "null" : CcsLoginUser.get().getOauthName();
    }

    @RequestMapping(value = "/disable", method = RequestMethod.POST)
    public Result disableEventSource(@RequestBody EventSourceConfigDto eventSourceConfigDto) {
        log("disable eventSource:{}",eventSourceConfigDto.getId());
        buildEventSourceConfig(eventSourceConfigDto);
        return eventSourceConfigService.disable(eventSourceConfigDto);
    }

    @RequestMapping(value = "/enable", method = RequestMethod.POST)
    public Result enableEventSource(@RequestBody EventSourceConfigDto eventSourceConfigDto) {
        log("enable eventSource:{}",eventSourceConfigDto.getId());
        buildEventSourceConfig(eventSourceConfigDto);
        return eventSourceConfigService.enable(eventSourceConfigDto);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public Result delete(@RequestBody EventSourceConfigDto eventSourceConfigDto) {
        log("delete eventSource:{}",eventSourceConfigDto.getId());
        buildEventSourceConfig(eventSourceConfigDto);
        return eventSourceConfigService.delate(eventSourceConfigDto);
    }

    private void buildEventSourceConfig(@RequestBody EventSourceConfigDto eventSourceConfigDto) {
        String loginUserName = (getLoginUserName());
        setUpdater(eventSourceConfigDto, loginUserName);
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public Result<EventSourceConfigDto> info(Long id) {
        log("info eventSource:{}",id);
        return eventSourceConfigService.info(id);
    }

    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public Result<PageResultDto<EventSourceConfigDto>> page(@RequestBody EventSourceConfigDto eventSourceConfigDto) {
        log("page query:{}",eventSourceConfigDto);
        return eventSourceConfigService.page(eventSourceConfigDto);
    }

    @RequestMapping(value = "/queryByName", method = RequestMethod.GET)
    public Result<List<EventSourceConfigDto>> query(String sug) {
        log("sug query:{}",sug);
        return eventSourceConfigService.queryByName(sug);
    }

    private void log(String str,Object o) {
        log.info("sug query:{}", o);
    }
}
