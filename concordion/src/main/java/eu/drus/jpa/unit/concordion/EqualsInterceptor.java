package eu.drus.jpa.unit.concordion;

import eu.drus.jpa.unit.util.ReflectionUtils;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

public final class EqualsInterceptor {
	
	private EqualsInterceptor() {}

	@RuntimeType
	public static Object intercept(@This Object thiz, @Argument(0) Object other, @FieldValue("bean") Object bean) throws IllegalAccessException, NoSuchFieldException {			
        if (thiz == other) {
            return true;
        } else if (other instanceof EnhancedProxy) {
            return bean.equals(ReflectionUtils.getValue(other, "bean"));
        } else {
        	return bean.equals(other);
        }
	}
}
