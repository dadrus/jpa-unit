package eu.drus.jpa.unit.concordion;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class EqualsInterceptor implements MethodInterceptor {

    private final Object target;

    public EqualsInterceptor(final Object target) {
        this.target = target;
    }

    @Override
    public Object intercept(final Object proxy, final Method method, final Object[] args, final MethodProxy methodProxy) throws Throwable {
        final Object other = args[0];

        if (proxy == other) {
            return true;
        } else if (other instanceof Factory) {
            for (final Callback callback : ((Factory) other).getCallbacks()) {
                if (callback.getClass().isAssignableFrom(EqualsInterceptor.class)) {
                    return target.equals(((EqualsInterceptor) callback).target);
                }
            }
        }

        return target.equals(other);
    }

}
