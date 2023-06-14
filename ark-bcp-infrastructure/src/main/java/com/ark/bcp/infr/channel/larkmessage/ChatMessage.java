
package com.ark.bcp.infr.channel.larkmessage;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 */
@Data
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = -5303725201221774810L;
    @JSONField(name = "msg_type")
    private String msgType;
    private Content content;

    @Data
    public static class Content implements Serializable {
        private static final long serialVersionUID = 8061221477478160612L;
        private String text;
        private JSONObject post;

        public static JSONArray makeLine(JSONObject... items) {
            if (null == items) {
                return null;
            }
            JSONArray line = new JSONArray();
            for (JSONObject item : items) {
                line.add(item);
            }
            return line;
        }

        public static JSONObject makeTextContextItem(String text, boolean unEscape) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("tag", "text");
            jsonObject.put("un_escape", true);
            jsonObject.put("text", text);
            return jsonObject;
        }

    }
}
