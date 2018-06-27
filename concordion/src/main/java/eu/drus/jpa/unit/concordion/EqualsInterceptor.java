package eu.drus.jpa.unit.concordion;

import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

public final class EqualsInterceptor {
	
	private EqualsInterceptor() {}

	@RuntimeType
	public static Object intercept(@This Object thiz, @Argument(0) Object other, @FieldValue("bean") Object bean) throws Exception {			
        if (thiz == other) {
            return true;
        } else if (other instanceof EnhancedProxy) {
            return bean.equals(other.getClass().getDeclaredField("bean").get(other));
        } else {
        	return bean.equals(other);
        }
	}
}
