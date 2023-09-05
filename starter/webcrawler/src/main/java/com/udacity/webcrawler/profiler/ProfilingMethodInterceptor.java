package com.udacity.webcrawler.profiler;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler
{
  private final Clock clock;
  private final Object delegate;
  private final ProfilingState state;
  ProfilingMethodInterceptor(Clock clock, Object delegate, ProfilingState state)
  {
    this.clock = Objects.requireNonNull(clock);
    this.delegate = Objects.requireNonNull(delegate);
    this.state = Objects.requireNonNull(state);
  }
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
  {
    Profiled annotation = method.getAnnotation(Profiled.class);
    Object result;
    Instant start = null;
    if (annotation != null)
    {
      try
      {
        start = clock.instant();
        result = method.invoke(delegate, args);
      } catch (InvocationTargetException ex)
      {
        throw ex.getTargetException();
      } catch (IllegalAccessException e)
      {
        throw new RuntimeException(e);
      }
      finally
      {
        Duration duration = Duration.between(start, clock.instant());
        state.record(delegate.getClass(), method, duration);
      }
      return result;
    }
    else
    {
      return method.invoke(delegate, args);
    }
  }
}