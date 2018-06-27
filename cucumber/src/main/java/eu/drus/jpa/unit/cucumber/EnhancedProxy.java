package eu.drus.jpa.unit.cucumber;

import static net.bytebuddy.implementation.MethodDelegation.to;
import static net.bytebuddy.implementation.MethodDelegation.toField;
import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.isEquals;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.not;

import java.lang.reflect.Modifier;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.spi.DecoratorExecutor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

public interface EnhancedProxy {

	static Object create(final Object bean, DecoratorExecutor executor) {
		try {
			Object proxy = new ByteBuddy()
				.subclass(bean.getClass())
				.implement(EnhancedProxy.class)
				.defineField("bean", bean.getClass(), Modifier.PUBLIC)
				.defineField("executor", DecoratorExecutor.class, Modifier.PUBLIC)
				.method(isEquals())
					.intercept(to(EqualsInterceptor.class))
				.method(isAnnotatedWith(nameStartsWith("cucumber.api")))
					.intercept(to(CucumberInterceptor.class))
				.method(not(isEquals().or(isClone()).or(isAnnotatedWith(nameStartsWith("cucumber.api")))))
					.intercept(toField("bean"))
				.make()
				.load(bean.getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
				.getLoaded()
				.newInstance();
			
			proxy.getClass().getDeclaredField("bean").set(proxy, bean);
			proxy.getClass().getDeclaredField("executor").set(proxy, executor);
	    	
	        return proxy;
		} catch (Exception e) {
			throw new JpaUnitException("Failed to create proxy for " + bean, e);
		}
	}
}
