import com.missfresh.risk.bcp.web.script.DynamicDubboInvoke; //泛化效用dubbo
import com.alibaba.fastjson.JSONObject;

class Demo {
    // 必须包含一个 此接口，否则执行不通过。
    /**
     * 必须包含一个 此接口，返回值，函数名，参数个数，参数类型必须完全一致。否则执行不通过。
     * @param jsonObject mq/kafka/http 消息的参数
     * @return true: 该检查命中了； false： 该检查没命中。
     * 如果执行体中抛出异常，会被捕获，并标记此次检查为异常检查。无论其他条件是否命中
     */
    Boolean handle(JSONObject jsonObject) {
        String orderNO = jsonObject.get("order");

        DynamicDubboInvoke.

        JSONObject retResult = new JSONObject()
        if (jsonObject.containsKey("a")) {
            return true;
        } else {
            return false;
        }
    }
}
