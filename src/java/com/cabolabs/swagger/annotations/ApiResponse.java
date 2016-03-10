package com.cabolabs.swagger.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * An ApiResponse represents a type of response from a server.  This can be used to
 * describe both success codes as well as errors.
 * If your Api has different response classes, you can describe them here by associating
 * a response class with a response code.  Note, Swagger does not allow multiple response
 * types for a single response code.
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
