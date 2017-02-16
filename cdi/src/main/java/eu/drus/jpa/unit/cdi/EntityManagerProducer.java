package eu.drus.jpa.unit.cdi;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;
import javax.persistence.EntityManager;

class EntityManagerProducer implements Producer<EntityManager> {
    private static final EntityManagerHolder HOLDER = EntityManagerHolder.getInstance();

    private Producer<EntityManager> delegate;
    private boolean delegateUsed = false;

    public EntityManagerProducer(final Producer<EntityManager> delegate) {
        this.delegate = delegate;
    }

    @Override
    public EntityManager produce(final CreationalContext<EntityManager> ctx) {
        return (EntityManager) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] {
                EntityManager.class
        }, (final Object proxy, final Method method, final Object[] args) -> {
            EntityManager em = HOLDER.getEntityManager();
            if (em == null) {
                em = delegate.produce(ctx);
                delegateUsed = true;
            }

            return method.invoke(em, args);
        });
    }

    @Override
    public void dispose(final EntityManager instance) {
        if (delegateUsed) {
            delegate.dispose(instance);
        }
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return delegate.getInjectionPoints();
    }
}