package com.cabolabs.swagger.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author cabolabs
 * @since 0.7
 * Representa un parametro de una operación de la api
 *
 */
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiParam {
  /** Name of the parameter */
  String name();

  /** Description of the parameter */
  String value() default "";

  /** Default value  - if e.g. no JAX-RS @DefaultValue is given */
  String defaultValue() default "";

  /** Description of values this endpoint accepts */
  String allowableValues() default "";

  /** specifies if the parameter is required or not */
  boolean required() default false;

  String in() default "";//Required. The location of the parameter. Possible values are "query", "header", "path", "formData" or "body".

  String type() default "";//Required. The type of the parameter. The value MUST be one of "string", "number", "integer", "boolean", "array" or "file"

  String format() default "";//The extending format for the previously mentioned type. See http://swagger.io/specification/#dataTypeFormat
  String items() default "";
  String typeCodeProperties() default "";
  String formatCodeProperties() default "";
  String messageProperties() default "";
  String fieldsProperties() default "";
}