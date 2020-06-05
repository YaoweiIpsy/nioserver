package com.ipsy.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * To simplify exception code.
 */
@Slf4j
public final class ExceptionUtils {

  private static final java.util.function.Consumer<Exception> NULL_EXCEPTION_CONSUMER = null;

  // --- function ----
  public static <T, R> R ignoreException(Function<T, R> function, T t,
      java.util.function.Consumer<Exception> exceptionConsumer) {
    try {
      return function.apply(t);
    } catch (Exception e) {
      if (exceptionConsumer != null) {
        exceptionConsumer.accept(e);
      }
    }
    return null;
  }

  public static <T, R> R ignoreException(Function<T, R> function, T t, String loggingMessage) {
    return ignoreException(function, t, (e) -> log.error("{}: {}", loggingMessage, e));
  }

  public static <T, R> R ignoreException(Function<T, R> function, T t) {
    return ignoreException(function, t, NULL_EXCEPTION_CONSUMER);
  }

  public static <T, R> R requireNoException(Function<T, R> function, T t,
      java.util.function.Consumer<Exception> exceptionConsumer) {
    return ignoreException(function, t, (e) -> {
      if (exceptionConsumer != null) {
        exceptionConsumer.accept(e);
      }
      throw new RuntimeException(e);
    });
  }

  public static <T, R> R requireNoException(Function<T, R> function, T t, String loggingMessage) {
    return requireNoException(function, t, (e) -> log.error("{}: {}", loggingMessage, e));
  }

  public static <T, R> R requireNoException(Function<T, R> function, T t) {
    return requireNoException(function, t, NULL_EXCEPTION_CONSUMER);
  }

  // --- consumer ---
  public static <T> void ignoreException(Consumer<T> function, T t,
      java.util.function.Consumer<Exception> exceptionConsumer) {
    try {
      function.accept(t);
    } catch (Exception e) {
      if (exceptionConsumer != null) {
        exceptionConsumer.accept(e);
      }
    }
  }

  public static <T> void ignoreException(Consumer<T> function, T t, String loggingMessage) {
    ignoreException(function, t, (e) -> log.error("{}: {}", loggingMessage, e));
  }

  public static <T> void ignoreException(Consumer<T> function, T t) {
    ignoreException(function, t, NULL_EXCEPTION_CONSUMER);
  }

  public static <T> void requireNoException(Consumer<T> function, T t,
      java.util.function.Consumer<Exception> exceptionConsumer) {
    ignoreException(function, t, (e) -> {
      if (exceptionConsumer != null) {
        exceptionConsumer.accept(e);
      }
      throw new RuntimeException(e);
    });
  }

  public static <T> void requireNoException(Consumer<T> function, T t, String loggingMessage) {
    requireNoException(function, t, (e) -> log.error("{}: {}", loggingMessage, e));
  }

  public static <T> void requireNoException(Consumer<T> function, T t) {
    requireNoException(function, t, NULL_EXCEPTION_CONSUMER);
  }

  // --- Supplier ---
  public static <R> R ignoreException(Supplier<R> function,
      java.util.function.Consumer<Exception> exceptionConsumer) {
    try {
      return function.get();
    } catch (Exception e) {
      if (exceptionConsumer != null) {
        exceptionConsumer.accept(e);
      }
    }
    return null;
  }

  public static <R> R ignoreException(Supplier<R> function, String loggingMessage) {
    return ignoreException(function, (e) -> log.error("{}: {}", loggingMessage, e));
  }

  public static <R> R ignoreException(Supplier<R> function) {
    return ignoreException(function, NULL_EXCEPTION_CONSUMER);
  }

  public static <R> R requireNoException(Supplier<R> function,
      java.util.function.Consumer<Exception> exceptionConsumer) {
    return ignoreException(function, (e) -> {
      if (exceptionConsumer != null) {
        exceptionConsumer.accept(e);
      }
      throw new RuntimeException(e);
    });
  }

  public static <R> R requireNoException(Supplier<R> function, String loggingMessage) {
    return requireNoException(function, (e) -> log.error("{}: {}", loggingMessage, e));
  }

  public static <R> R requireNoException(Supplier<R> function) {
    return requireNoException(function, NULL_EXCEPTION_CONSUMER);
  }

  // --- SimpleFunction ---
  public static void ignoreException(SimpleFunction function,
      java.util.function.Consumer<Exception> exceptionConsumer) {
    try {
      function.call();
    } catch (Exception e) {
      if (exceptionConsumer != null) {
        exceptionConsumer.accept(e);
      }
    }
  }

  public static void ignoreException(SimpleFunction function, String loggingMessage) {
    ignoreException(function, (e) -> log.error("{}: {}", loggingMessage, e));
  }

  public static void ignoreException(SimpleFunction function) {
    ignoreException(function, NULL_EXCEPTION_CONSUMER);
  }

  public static void requireNoException(SimpleFunction function,
      java.util.function.Consumer<Exception> exceptionConsumer) {
    ignoreException(function, (e) -> {
      if (exceptionConsumer != null) {
        exceptionConsumer.accept(e);
      }
      throw new RuntimeException(e);
    });
  }

  public static void requireNoException(SimpleFunction function, String loggingMessage) {
    requireNoException(function, (e) -> log.error("{}: {}", loggingMessage, e));
  }

  public static void requireNoException(SimpleFunction function) {
    requireNoException(function, NULL_EXCEPTION_CONSUMER);
  }

  /**
   * Require no exception
   */
  public interface Function<T, R> {

    R apply(T t) throws Exception;
  }

  public interface Consumer<T> {

    void accept(T t) throws Exception;
  }

  public interface Supplier<R> {

    R get() throws Exception;
  }

  public interface SimpleFunction {

    void call() throws Exception;
  }

}
