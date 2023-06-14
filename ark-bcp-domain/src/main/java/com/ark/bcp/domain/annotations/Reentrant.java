
package com.ark.bcp.domain.annotations;

import java.lang.annotation.*;


/**
 * 标记方法可重入.
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Reentrant {
}
