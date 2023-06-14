

package com.ark.bcp.domain.util;


import java.io.IOException;
import java.util.Base64;

/**
 */
@SuppressWarnings("AlibabaCommentsMustBeJavadocFormat")
public class Base64Utils {

    //字节数组转Base64编码
    public static String byte2Base64(byte[] bytes) {
        Base64.Encoder encoder = Base64.getMimeEncoder();
        return encoder.encodeToString(bytes);
        /*BASE64Encoder base64Encoder = new BASE64Encoder();
        return base64Encoder.encode(bytes);*/
    }

    //Base64编码转字节数组w
    public static byte[] base642Byte(String base64Key) throws IOException {
        Base64.Decoder decoder = Base64.getMimeDecoder();
        return decoder.decode(base64Key);
        /*BASE64Decoder base64Decoder = new BASE64Decoder();
        return base64Decoder.decodeBuffer(base64Key);*/
    }
}
