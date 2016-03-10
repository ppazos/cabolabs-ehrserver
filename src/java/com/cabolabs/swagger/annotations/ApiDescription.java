package com.cabolabs.swagger.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author Derk Muenchhausen
 * @author Stephan Linkel
 * @since 0.1
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiDescription {
    String name() default "";           // if empty the Controller name will be used
    String plural() default "";         // if empty the Controller name will be used
    String description() default "";
    String title() default ""; // Is required for specification swagger 2.0
    String version() default ""; //  Is required for specification swagger 2.0
    String host () default ""; //
    String schemes() default "";//Values MUST be from the list: "http", "https", "ws", "wss"
    String basePath() default "";//The value MUST start with a leading slash (/)
    String produces() default "";//Values MUST be from the list: http://swagger.io/specification/#mimeTypes
    //Definitions
    String nameElementDefinitions() default "";
    String typeElementDefinitions() default "";
}
