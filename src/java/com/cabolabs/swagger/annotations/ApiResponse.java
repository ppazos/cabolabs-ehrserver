package com.cabolabs.swagger.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author cabolabs
 * @since 0.7
 * 
 *Recoge el tipo de respuestas que da al servidor para una operación de la api. Esto se puede
 *usar tanto para recoger las respuestas correctas como las erroneas.
 *
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiResponse {
  /** Response code to describe */
  int code();

  /** Human-readable message to accompany the response */
  String message();

  /** Optional response class to describe the payload of the message */
  Class<?> response() default Void.class;

  String typeSchema() default ""; // for example array

  String nameItemsSchema() default "";// for example "$ref": "#/definitions/Product"

  String valueItemsSchema() default "";// for example #/definitions/Product"

}
