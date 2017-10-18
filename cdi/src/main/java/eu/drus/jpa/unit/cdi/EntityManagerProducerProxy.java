package eu.drus.jpa.unit.cdi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;
import javax.persistence.EntityManager;

class EntityManagerProducerProxy implements Producer<EntityManager> {
    private Producer<EntityManager> proxied;

    public EntityManagerProducerProxy(final Producer<EntityManager> proxied) {
        this.proxied = proxied;
    }

    @Override
    public EntityManager produce(final CreationalContext<EntityManager> ctx) {
        return (EntityManager) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] {
                EntityManager.class, Disposable.class
        }, new EntityManagerInvocationHandler(ctx));
    }

    @Override
    public void dispose(final EntityManager instance) {
        ((Disposable) instance).dispose();
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return proxied.getInjectionPoints();
    }

    @FunctionalInterface
    private static interface Disposable {
        void dispose();
    }

    private class EntityManagerInvocationHandler implements InvocationHandler {

        private final CreationalContext<EntityManager> ctx;
        private EntityManager instance;
        private boolean delegateUsed = false;

        private EntityManagerInvocationHandler(final CreationalContext<EntityManager> ctx) {
            this.ctx = ctx;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (method.equals(Disposable.class.getDeclaredMethods()[0])) {
                if (delegateUsed && instance != null) {
                    proxied.dispose(instance);
                    instance = null;
                    delegateUsed = false;
                }
                return null;
            }

            final EntityManager oldInstance = instance;
            instance = EntityManagerHolder.INSTANCE.getEntityManager();

            if (instance == null && oldInstance == null) {
                instance = proxied.produce(ctx);
                delegateUsed = true;
            } else if (instance == null) {
                instance = oldInstance;
            }

            return method.invoke(instance, args);
        }
    }
}