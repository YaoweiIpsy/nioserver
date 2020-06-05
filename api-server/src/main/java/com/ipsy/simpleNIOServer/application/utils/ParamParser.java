package com.ipsy.simpleNIOServer.application.utils;

import static com.ipsy.utils.ExceptionUtils.ignoreException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ParamParser {

  private static final Map<Class<?>, ParseValue> map = new HashMap<Class<?>, ParseValue>() {{
    put(String.class, value -> value);
    put(Integer.class, Integer::valueOf);
    put(Long.class, Long::valueOf);
    put(Double.class, Double::valueOf);
    put(Float.class, Float::valueOf);
    put(Boolean.class, Boolean::valueOf);
    put(Byte.class, Byte::valueOf);
    put(int.class, Integer::parseInt);
    put(long.class, Long::parseLong);
    put(double.class, Double::parseDouble);
    put(float.class, Float::parseFloat);
    put(boolean.class, Boolean::parseBoolean);
    put(byte.class, Byte::parseByte);
  }};
  private static final Map<Class<?>, ParseValue> defaultValueMap = new HashMap<Class<?>, ParseValue>(
      8) {{
    put(int.class, value -> 0);
    put(long.class, value -> 0L);
    put(double.class, value -> 0.0d);
    put(float.class, value -> 0.0f);
    put(boolean.class, value -> false);
    put(byte.class, value -> 0);
  }};
  private static final Map<Class<?>, ParseArrayValue> arrayMap = new HashMap<Class<?>, ParseArrayValue>() {{
    put(int.class, (paramList) -> paramList.stream().mapToInt(Integer::valueOf).toArray());
    put(long.class, (paramList) -> paramList.stream().mapToLong(Long::valueOf).toArray());
    put(double.class, (paramList) -> paramList.stream().mapToDouble(Double::valueOf).toArray());
    put(String.class, (paramList) -> paramList.toArray(new String[0]));
  }};

  public static Object parseValue(Class<?> c, String value) {
    try {
      return map.get(c).parseValue(value);
    } catch (Exception ignored) {
    }
    return ignoreException(() -> defaultValueMap.get(c).parseValue(value));
  }

  public static Object parseArray(Class<?> c, List<String> paramList) {
    return ignoreException(() -> arrayMap.get(c).parseValue(paramList));
  }

  @FunctionalInterface
  interface ParseValue {

    Object parseValue(String value);
  }

  @FunctionalInterface
  interface ParseArrayValue {

    Object parseValue(List<String> paramList);
  }
}
