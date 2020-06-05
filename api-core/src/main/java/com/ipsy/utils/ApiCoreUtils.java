package com.ipsy.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ApiCoreUtils {

  /**
   * object mapper for the entry.
   */
  public final static ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
      .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
      .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
      .findAndRegisterModules()
      .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
      .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)
      .setSerializationInclusion(JsonInclude.Include.NON_NULL);
  private static final String PREFIX = "ipsy";
  private static final String LINK = "_";
  /**
   * Configuration from the environment variables.
   */
  private final static Map<JavaType, Object> envCache = new HashMap<>();
  private static ObjectNode envNode = null;

  /**
   * add some new environment variables for testing.
   *
   * @param newenv the new environment variables
   * @throws IllegalAccessException exception
   * @throws NoSuchFieldException   exception
   */
  @SuppressWarnings("unchecked")
  public static void setEnv(Map<String, String> newenv)
      throws IllegalAccessException, NoSuchFieldException {
    Map<String, String> env = System.getenv();
    Field field = env.getClass().getDeclaredField("m");
    field.setAccessible(true);
    ((Map<String, String>) field.get(env)).putAll(newenv);
  }

  /**
   * get the declared field from the obj.
   *
   * @param obj      object
   * @param property property name
   * @return field
   */
  public static Field getField(Object obj, String property) {
    Class<?> clz = obj.getClass();
    while (clz != Object.class) {
      try {
        Field field = clz.getDeclaredField(property);
        field.setAccessible(true);
        return field;
      } catch (Exception e) {
        // Do nothing, we'll return null
        clz = clz.getSuperclass();
      }
    }
    return null;
  }

  /**
   * Fetch a property from an object.
   *
   * @param obj      The object who's property you want to fetch
   * @param property The property name
   * @return The value of the property or null if it does not exist.
   */
  @SuppressWarnings("unchecked")
  public static <T> T getProperty(Object obj, String property) {
    try {
      return (T) getField(obj, property).get(obj);
    } catch (Exception ignored) {
    }
    return null;
  }

  /**
   * set a property named `property`
   *
   * @param obj      the object
   * @param property the property name
   * @param value    the value
   */
  public static void setProperty(Object obj, String property, Object value) {
    try {
      getField(obj, property).set(obj, value);
    } catch (Exception ignored) {
    }
  }

  // generate the configuration class from the system environment.
  private synchronized static ObjectNode getEnvNode() {
    if (envNode == null) {
      envNode = new ObjectNode(OBJECT_MAPPER.getNodeFactory());
      System.getenv().entrySet().stream()
          .filter(it -> it.getKey().startsWith(PREFIX + LINK))
          .forEach(it -> {
            JsonNode node;
            try {
              node = OBJECT_MAPPER.readTree(it.getValue());
            } catch (IOException e) {
              node = new TextNode(it.getValue());
            }
            List<String> keys = Arrays.asList(it.getKey().split(LINK));
            keys.subList(1, keys.size() - 1).stream()
                .reduce(
                    envNode,
                    ObjectNode::with,
                    (prop1, prop2) -> null)
                .replace(keys.get(keys.size() - 1), node);
          });
    }
    return envNode;
  }

  public static void cleanupEnvConfig() {
    envCache.clear();
    envNode = null;
  }

  public static <T> T getEnvConfig(Class<T> tClass) {
    return getEnvConfig(OBJECT_MAPPER.constructType(tClass));
  }

  public static <T> T getEnvConfig(JavaType javaType) {
    if (javaType.getRawClass() == Void.class) {
      return null;
    }
    if (!envCache.containsKey(javaType)) {
      envCache.put(javaType, OBJECT_MAPPER.convertValue(getEnvNode(), javaType));
    }
    return (T) envCache.get(javaType);
  }

}
