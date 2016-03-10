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
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DeleteMethod {
	String pathApiRest() default ""; 
	String summary() default ""; 
	String description() default ""; 
	String tags() default "";
	String domainClass() default "";
}
