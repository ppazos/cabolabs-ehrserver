package com.cabolabs.swagger.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiOperation {
  /** Brief description of the operation  */
  String value();

  /** long description of the operation */
  String notes() default "";

  String sample() default "";

  /** authorizations required by this Api */
  // String authorizations() default "";
}
