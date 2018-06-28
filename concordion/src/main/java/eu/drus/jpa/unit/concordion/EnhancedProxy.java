package eu.drus.jpa.unit.concordion;

import static net.bytebuddy.implementation.MethodDelegation.to;
import static net.bytebuddy.implementation.MethodDelegation.toField;
import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.isClone;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.isEquals;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.not;

import java.lang.reflect.Modifier;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.spi.DecoratorExecutor;
import eu.drus.jpa.unit.util.ReflectionUtils;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

public interface EnhancedProxy {

	static Object create(final Object bean, DecoratorExecutor executor) {
		try {
			Object proxy = new ByteBuddy()
				.subclass(bean.getClass())
				.implement(EnhancedProxy.class)
				.defineField("bean", bean.getClass(), Modifier.PRIVATE)
				.defineField("executor", DecoratorExecutor.class, Modifier.PRIVATE)
				.method(isEquals())
					.intercept(to(EqualsInterceptor.class))
				.method(not(isDeclaredBy(Object.class).or(isAnnotatedWith(nameStartsWith("org.concordion.api")))))
					.intercept(to(ConcordionInterceptor.class))
				.method(isDeclaredBy(Object.class).and(not(isEquals().or(isClone()))))
					.intercept(toField("bean"))
				.make()
				.load(bean.getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
				.getLoaded()
				.newInstance();
			
			ReflectionUtils.injectValue(proxy, "bean", bean);
			ReflectionUtils.injectValue(proxy, "executor", executor);
	    	
	        return proxy;
		} catch (Exception e) {
			throw new JpaUnitException("Failed to create proxy for " + bean, e);
		}
	}
}
