

package com.ark.bcp.infr.channel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ark.bcp.infr.channel.larkmessage.ChatMessage;
import com.ark.bcp.infr.channel.larkmessage.LarkMessageBody;
import com.google.common.base.Joiner;
import com.missfresh.risk.bcp.domain.exception.FailfastException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;

/**
 */
@Service
public class LarkAlertServiceImpl implements ILarkAlertService {
    private static final Logger logger = LoggerFactory.getLogger(LarkAlertServiceImpl.class);
    private static final String LARK_RBT_NOTIFY_V1_URL = "/open-apis/bot/hook/";
    private static final String LARK_RBT_NOTIFY_V2_URL = "/open-apis/bot/v2/hook/";


    @Value("${lark.default.notice.roboturl}")
    private String larkDefaultRobotUrl;

    @Value("${lark.alert.url}")
    private String alertUrl;

    @Resource
    private RestTemplate restTemplate;

    @Override
    public boolean alert(String title, List<String> contentLines) {
        return alert(larkDefaultRobotUrl, title, contentLines);
    }

    @Override
    public boolean alert(String url, String title, List<String> contentLines) {
        if (StringUtils.isEmpty(title) || CollectionUtils.isEmpty(contentLines)) {
            logger.warn("报警标题不能为空");
            return false;
        }
        return alert(url, title, Joiner.on("\n").join(contentLines));
    }

    @Override
    public boolean alert(String title, String body) {
        return alert(larkDefaultRobotUrl, title, body);
    }

    @Override
    public boolean alert(String url, String title, String body) {
        if (StringUtils.isEmpty(body) || StringUtils.isEmpty(title)) {
            return false;
        }

        String larkUrl = url;
        if (StringUtils.isEmpty(larkUrl)) {
            larkUrl = larkDefaultRobotUrl;
        }
        String postBody = null;
        if (larkUrl.contains(LARK_RBT_NOTIFY_V1_URL)) {
            LarkMessageBody alertContent = new LarkMessageBody();
            alertContent.setTitle(title);
            alertContent.setText(body);
            postBody = JSON.toJSONString(alertContent);
        } else if (larkUrl.contains(LARK_RBT_NOTIFY_V2_URL)) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setMsgType("post");

            JSONObject postContext = new JSONObject();
            JSONObject zhchContext = new JSONObject();
            postContext.put("zh_cn", zhchContext);
            JSONArray realContext = new JSONArray();
            zhchContext.put("title", title);
            zhchContext.put("content", realContext);

            realContext.add(
                    ChatMessage.Content.makeLine(
                            ChatMessage.Content.makeTextContextItem(body, true)
                    )
            );
            ChatMessage.Content content = new ChatMessage.Content();
            content.setPost(postContext);

            chatMessage.setContent(content);
            postBody = JSON.toJSONString(chatMessage);
        } else {
            logger.info("不支持的larkurl,{}", larkUrl);
            throw new FailfastException(null, "不支持的larkurl:" + larkUrl);
        }
        return postBody(larkUrl, postBody);
    }

    @Override
    public boolean alertByAppcode(String appcode, String body) {
        if (StringUtils.isEmpty(body)) {
            return false;
        }
        return postBody(alertUrl + appcode, body);
    }

    private boolean postBody(String url, String postBody) {
        try {
            logger.info("发送报警消息:{} to {}", postBody, url);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            HttpEntity<String> request = new HttpEntity<>(postBody, headers);
            ResponseEntity<String> postForEntity = restTemplate.postForEntity(url, request, String.class);
            logger.info("发送报警消息,返回:{},{}", postForEntity.getStatusCodeValue(), postForEntity.getBody());
            if (!postForEntity.getStatusCode().is2xxSuccessful()) {
                throw new FailfastException(null, "调用" + url + " 返回码:" + postForEntity.getStatusCodeValue());
            }
            return true;
        } catch (Exception e) {
            logger.error("发送报警异常", e);
            throw new FailfastException(null, e.getMessage());
        }
    }
}
