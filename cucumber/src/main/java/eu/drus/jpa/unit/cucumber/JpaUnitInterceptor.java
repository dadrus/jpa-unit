package eu.drus.jpa.unit.cucumber;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.spi.FeatureResolver;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class JpaUnitInterceptor implements MethodInterceptor {

    private final JpaUnitHookExecutor executor;

    public JpaUnitInterceptor(final JpaUnitHookExecutor executor) {
        this.executor = executor;
    }

    @Override
    public Object intercept(final Object obj, final Method method, final Object[] args, final MethodProxy proxy) throws Throwable {
        if (hasCucumberAnnotation(method)) {
            return invokeWithJpaUnitHooks(obj, method, args, proxy);
        } else {
            return proxy.invokeSuper(obj, args);
        }
    }

    private Object invokeWithJpaUnitHooks(final Object obj, final Method method, final Object[] args, final MethodProxy proxy)
            throws Throwable {
        final FeatureResolver resolver = FeatureResolver.newFeatureResolver(method, obj.getClass())
                .withDefaultCleanupPhase(CleanupPhase.NONE).build();

        Object result = null;
        final TestMethodInvocationImpl invocation = new TestMethodInvocationImpl(obj, method, resolver);
        executor.processBefore(invocation);
        try {
            result = proxy.invokeSuper(obj, args);
        } catch (final Exception e) {
            invocation.setTestException(e);
            executor.processAfter(invocation);
            throw e;
        }
        executor.processAfter(invocation);

        return result;
    }

    private boolean hasCucumberAnnotation(final Method method) {
        final Annotation[] annotations = method.getDeclaredAnnotations();
        for (final Annotation annotation : annotations) {
            final Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType.getName().startsWith("cucumber.api")) {
                return true;
            }
        }
        return false;
    }

}
