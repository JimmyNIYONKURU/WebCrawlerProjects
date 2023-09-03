package com.udacity.webcrawler.profiler;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.lang.reflect.InvocationHandler;
/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {
  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }
  private Boolean profiledClass(Class<?> klass) {
    List<Method> methods = new ArrayList<>(Arrays.asList(klass.getDeclaredMethods()));
    if (methods.isEmpty()) {
      return false;
    }
    return methods.stream().anyMatch(x -> x.getAnnotation(Profiled.class) != null);
  }
  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);
    Objects.requireNonNull(delegate);
    if (!profiledClass(klass)) {
      throw new IllegalArgumentException(klass.getName() + "doesn't have profiled methods.");
    }
    ClassLoader classLoader = klass.getClassLoader();
    Class<?>[] interfaces = {klass};
    InvocationHandler handler = new ProfilingMethodInterceptor(clock, delegate, state);
    return (T) java.lang.reflect.Proxy.newProxyInstance(classLoader, interfaces, handler);
  }
  @Override
  public void writeData(Path path) throws IOException {
    try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
      writeData(writer);
    }
  }
  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
  private static final DateTimeFormatter RFC_1123_DATE_TIME = DateTimeFormatter.RFC_1123_DATE_TIME;
}
