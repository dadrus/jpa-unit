package eu.drus.jpa.unit.cdi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;
import javax.persistence.EntityManager;

class EntityManagerProducer implements Producer<EntityManager> {
    private Producer<EntityManager> delegate;

    public EntityManagerProducer(final Producer<EntityManager> delegate) {
        this.delegate = delegate;
    }

    @Override
    public EntityManager produce(final CreationalContext<EntityManager> ctx) {
        return (EntityManager) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] {
                EntityManager.class, Disposable.class
        }, new EntityManagerHandler(ctx));
    }

    @Override
    public void dispose(final EntityManager instance) {
        ((Disposable) instance).dispose();
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return delegate.getInjectionPoints();
    }

    @FunctionalInterface
    private static interface Disposable {
        void dispose();
    }

    private class EntityManagerHandler implements InvocationHandler {

        private final CreationalContext<EntityManager> ctx;
        private EntityManager instance;
        private boolean delegateUsed = false;

        private EntityManagerHandler(final CreationalContext<EntityManager> ctx) {
            this.ctx = ctx;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (method.equals(Disposable.class.getDeclaredMethods()[0])) {
                if (delegateUsed) {
                    delegate.dispose(instance);
                }
                return null;
            }

            if (instance == null) {
                instance = EntityManagerHolder.INSTANCE.getEntityManager();
            }

            if (instance == null) {
                instance = delegate.produce(ctx);
                delegateUsed = true;
            }

            return method.invoke(instance, args);
        }
    }
}