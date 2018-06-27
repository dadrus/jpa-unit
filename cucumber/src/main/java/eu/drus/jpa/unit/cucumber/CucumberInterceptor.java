package eu.drus.jpa.unit.cucumber;

import java.lang.reflect.Method;

import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.spi.DecoratorExecutor;
import eu.drus.jpa.unit.spi.FeatureResolver;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

public final class CucumberInterceptor {
	
	private CucumberInterceptor() {}
    
    @RuntimeType
	public static Object intercept(@FieldValue("executor") DecoratorExecutor executor, @FieldValue("bean") Object bean, @Origin Method method, @AllArguments Object[] args) throws Exception {
		
        final FeatureResolver resolver = FeatureResolver.newFeatureResolver(bean.getClass()).withTestMethod(method)
                .withDefaultCleanupPhase(CleanupPhase.NONE).build();

        Object result = null;
        final TestInvocationImpl invocation = new TestInvocationImpl(bean, method, resolver);
        executor.processBefore(invocation);
        try {
            result = method.invoke(bean, args);
        } catch (final Exception e) {
        	Exception cause = (Exception)e.getCause();
            invocation.setTestException(cause);
            executor.processAfter(invocation);
            throw cause;
        }
        executor.processAfter(invocation);

        return result;
    }

}
