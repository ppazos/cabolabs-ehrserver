
package com.cabolabs.swagger.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author cabolabs
 * @since 0.7
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiProperty {
    String description() default "";
    String type() default "";  // http://swagger.io/specification/#operationConsumes
    String format() default "";//http://swagger.io/specification/#operationConsumes

    /**
     * use as default Domain constraints, but it is possible to overwrite using keywords
     * in, min, max, between ... and ..., max size
     */
    String range() default "";
}
