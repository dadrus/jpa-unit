package eu.drus.jpa.unit.decorator.jpa;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import eu.drus.jpa.unit.spi.Constants;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;
import eu.drus.jpa.unit.spi.TestInvocation;

@RunWith(MockitoJUnitRunner.class)
public class EntityManagerFactoryDecoratorTest {

    private static final HashMap<String, Object> PERSISTENCE_PROPERTIES = new HashMap<>();

    private static final String UNIT_NAME = "MY_UNIT_NAME";

    @Mock
    private ExecutionContext ctx;

    @Mock
    private TestInvocation invocation;

    @Mock
    private EntityManagerFactory factory;

    @Mock
    private PersistenceUnitDescriptor descriptor;

    private MockedStatic<Persistence> persistenceMock;

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    @Before
    public void prepareMocks() {
        persistenceMock = mockStatic(Persistence.class);
        persistenceMock.when(() -> Persistence.createEntityManagerFactory(eq(UNIT_NAME), eq(PERSISTENCE_PROPERTIES))).thenReturn(factory);

        when(invocation.getContext()).thenReturn(ctx);
        lenient().when(invocation.getTestClass()).thenReturn((Class) getClass());
        when(ctx.getDescriptor()).thenReturn(descriptor);
        when(descriptor.getUnitName()).thenReturn(UNIT_NAME);
        when(descriptor.getProperties()).thenReturn(PERSISTENCE_PROPERTIES);
    }

    @After
    public void releaseMocks() {
        persistenceMock.close();
    }

    @Test
    public void testRequiredPriority() {
        // GIVEN
        final EntityManagerFactoryDecorator decorator = new EntityManagerFactoryDecorator();

        // WHEN
        final int priority = decorator.getPriority();

        // THEN
        assertThat(priority, equalTo(2));
    }

    @Test
    public void testBeforeAll() throws Throwable {
        // GIVEN
        final EntityManagerFactoryDecorator decorator = new EntityManagerFactoryDecorator();

        // WHEN
        decorator.beforeAll(invocation);

        // THEN
        verify(ctx).storeData(eq(Constants.KEY_ENTITY_MANAGER_FACTORY), eq(factory));
    }

    @Test
    public void testAfterAll() throws Throwable {
        // GIVEN
        when(ctx.getData(Constants.KEY_ENTITY_MANAGER_FACTORY)).thenReturn(factory);

        final EntityManagerFactoryDecorator decorator = new EntityManagerFactoryDecorator();

        // WHEN
        decorator.afterAll(invocation);

        // THEN
        verify(ctx).storeData(eq(Constants.KEY_ENTITY_MANAGER_FACTORY), eq(null));
        verify(factory).close();
    }

}
