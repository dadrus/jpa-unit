package eu.drus.jpa.unit.decorator.jpa;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;

public class EntityManagerFactoryDecorator implements TestClassDecorator {

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void beforeAll(final ExecutionContext ctx, final Object target) throws Throwable {
        final String unitName = (String) ctx.getData("unitName");
        final EntityManagerFactory emf = Persistence.createEntityManagerFactory(unitName); // FIXME
        ctx.storeData("emf", emf);
    }

    @Override
    public void afterAll(final ExecutionContext ctx, final Object target) throws Throwable {
        final EntityManagerFactory emf = (EntityManagerFactory) ctx.getData("emf");
        ctx.storeData("emf", null);
        emf.close();
    }

}
