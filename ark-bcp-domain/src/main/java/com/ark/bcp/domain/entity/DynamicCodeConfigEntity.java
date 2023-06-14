
package com.ark.bcp.domain.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ark.bcp.domain.util.ZipCompressUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 */

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DynamicCodeConfigEntity implements Serializable {
    private static final long serialVersionUID = 1844650914850403734L;

    /**
     * 编号，主键
     **/
    private Long id;
    /**
     * 代码id
     **/
    private Long conditionId;

    /**
     * 事件id
     */
    private Long eventId;
    /**
     * 代码分类, java/ groovy/ql
     **/
    private Integer type;

    /**
     * 脚本名称
     */
    private String name;
    /**
     * 代码内容
     **/
    private String scriptContent;

    /**
     * 是否可用,0,正常，1：删除 是已经删除
     **/
    private Integer isDelete;
    /**
     * 数据版本号
     **/
    private Integer version;
    /**
     * 创建时间
     **/
    private Date createdTime;
    /**
     * 修改时间
     **/
    private Date updatedTime;
    /**
     * 修改人
     **/
    private String updatedBy;
    /**
     * 创建人
     **/
    private String createdBy;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DynamicCodeConfigEntity that = (DynamicCodeConfigEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void zipCompressSrc() {
        final int compressThreshold = 4000;
        if (!StringUtils.isEmpty(scriptContent) && scriptContent.length() > compressThreshold) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ziped", ZipCompressUtils.gzip(scriptContent));
            scriptContent = jsonObject.toJSONString();
        }
    }

    public void unzipCompressSrc() {
        try {
            final String compressKey = "ziped";
            if (!StringUtils.isEmpty(scriptContent) && scriptContent.contains(compressKey)) {
                JSONObject jsonObject = JSON.parseObject(scriptContent);
                if (jsonObject.containsKey(compressKey)) {
                    scriptContent = ZipCompressUtils.gunzip(jsonObject.getString(compressKey));
                }
            }
        } catch (Exception e) {
            // donothing
        }
    }
}
