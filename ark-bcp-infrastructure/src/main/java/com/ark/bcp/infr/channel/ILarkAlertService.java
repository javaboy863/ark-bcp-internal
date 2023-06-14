

package com.ark.bcp.infr.channel;

import java.util.List;

/**
 */
@SuppressWarnings("AlibabaAbstractMethodOrInterfaceMethodMustUseJavadoc")
public interface ILarkAlertService {
    boolean alert(String title, List<String> contentLines);

    boolean alert(String url, String title, List<String> contentLines);

    boolean alert(String title, String body);

    boolean alert(String url, String title, String body);

    boolean alertByAppcode(String appcode, String body);
}
