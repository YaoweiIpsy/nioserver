package com.ipsy.simpleNIOServer.application.models;

import static com.ipsy.utils.ExceptionUtils.ignoreException;

import com.ipsy.simpleNIOServer.application.annotations.Controller;
import com.ipsy.simpleNIOServer.application.annotations.RequestBody;
import com.ipsy.simpleNIOServer.application.utils.ParamParser;
import com.ipsy.utils.ApiCoreUtils;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Action {

  private static final String DELIMITER = "\\?";
  private Controller controller;
  private Method method;
  private Object target;

  public Object[] mapping(String uriPattern, FullHttpRequest request) {
    Map<String, String> parameters = new HashMap<>();
    Pattern pattern = Pattern.compile("\\{(\\w+)\\}");

    String path = request.uri().split(DELIMITER)[0];
    String[] paths = path.split("/");
    String[] mappingPaths = uriPattern.split("/");
    if (paths.length != mappingPaths.length) {
      return null;
    }
    for (int i = 0; i < paths.length; ++i) {
      Matcher matcher = pattern.matcher(mappingPaths[i]);
      if (!matcher.matches()) {
        if (!paths[i].equals(mappingPaths[i])) {
          return null;
        }
      } else {
        parameters.put(matcher.group(1), paths[i]);
      }
    }
    Class<?>[] classes = method.getParameterTypes();
    Object[] objects = new Object[classes.length];
    for (int i = 0; i < classes.length; i++) {
      Class<?> aClass = classes[i];
      //handle @RequestBody from the request.context
      Annotation[] parameterAnnotation = method.getParameterAnnotations()[i];
      if (parameterAnnotation.length > 0) {
        for (Annotation annotation : parameterAnnotation) {
          if (annotation.annotationType() == RequestBody.class &&
              request.headers().get(HttpHeaderNames.CONTENT_TYPE.toString())
                  .equals(HttpHeaderValues.APPLICATION_JSON.toString())) {
            objects[i] = ignoreException(
                () -> ApiCoreUtils.OBJECT_MAPPER.readValue(request.content().array(), aClass));
          }
        }
      } else {
        String paramName = method.getParameters()[i].getName();
        objects[i] = ParamParser
            .parseValue(aClass, ignoreException(() -> parameters.get(paramName)));
      }
    }
    return objects;
  }

  public Object call(Object... params) throws InvocationTargetException, IllegalAccessException {
    return method.invoke(target, params);
  }
}
