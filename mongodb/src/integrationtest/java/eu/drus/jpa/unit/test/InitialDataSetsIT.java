package eu.drus.jpa.unit.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.api.DataSeedStrategy;
import eu.drus.jpa.unit.api.InitialDataSets;
import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.test.model.Customer;
import eu.drus.jpa.unit.test.util.MongodManager;

@RunWith(JpaUnitRunner.class)
@Cleanup(phase = CleanupPhase.NONE)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InitialDataSetsIT {

    @BeforeClass
    public static void startMongod() {
        MongodManager.startServer();
    }

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;

    @Test
    @InitialDataSets("datasets/initial-data.json")
    public void test1() {
        // this test uses the default INSERT DataSeedStrategy
        final TypedQuery<Customer> query = manager.createQuery("SELECT c FROM Customer c WHERE c.name='John'", Customer.class);
        final Customer entity = query.getSingleResult();

        assertNotNull(entity);
        assertThat(entity.getName(), equalTo("John"));
        assertThat(entity.getSurname(), equalTo("Doe"));

        final Customer customer = new Customer("Max", "Payne");
        manager.persist(customer);
    }

    @Test
    public void test2() {
        // Since cleanup is disabled 2 Customer entities do exist.

        final TypedQuery<Customer> query = manager.createQuery("SELECT c FROM Customer c", Customer.class);
        final List<Customer> depositors = query.getResultList();

        assertThat(depositors.size(), equalTo(2));
    }

    @Test
    @InitialDataSets(value = "datasets/initial-data.json", seedStrategy = DataSeedStrategy.CLEAN_INSERT)
    public void test3() {
        // Since the given data is already present (thanks to the disabled cleanup) usage of INSERT
        // DataSeedStrategy would lead to a constraint violations. But usage of clean insert, will
        // delete ALL data within the tables referenced by the given dataset, insert the
        // corresponding data from the data set and retain other data.
        // Thus: Max Payne entity is deleted

        final TypedQuery<Customer> query = manager.createQuery("SELECT c FROM Customer c", Customer.class);
        final List<Customer> depositors = query.getResultList();

        assertThat(depositors.size(), equalTo(1));

        // insert a new customer and update e.g a name of the previously stored customer and let's
        // see what REFRESH DataSeedStrategy will do (see next test methods)
        final Customer depositor = new Customer("Max", "Payne");
        manager.persist(depositor);

        depositors.get(0).setName("Foo");
    }

    @Test
    public void test4() {
        // Just to verify the surname update has been persisted
        final TypedQuery<Customer> query = manager.createQuery("SELECT c FROM Customer c WHERE c.surname='Doe'", Customer.class);
        final Customer entity = query.getSingleResult();

        assertNotNull(entity);
        assertThat(entity.getName(), equalTo("Foo"));
        assertThat(entity.getSurname(), equalTo("Doe"));
    }

    @Test
    @InitialDataSets(value = "datasets/initial-data2.json", seedStrategy = DataSeedStrategy.REFRESH)
    public void test5() {
        // Since the given data is already present (thanks to the disabled cleanup) usage of REFRESH
        // will update data referenced by the given dataset, add new data and retain other data.
        // Thus: Max Payne entity is still there, the name of Foo is changed back to John and a new
        // entity Jack is added

        TypedQuery<Customer> query = manager.createQuery("SELECT c FROM Customer c WHERE c.surname='Doe'", Customer.class);
        Customer entity = query.getSingleResult();
        assertThat(entity.getName(), equalTo("John")); // not Foo!!!
        entity.setName("Foo"); // change it to Foo for the next test again

        query = manager.createQuery("SELECT c FROM Customer c WHERE c.name='Max'", Customer.class);
        entity = query.getSingleResult();
        assertThat(entity.getSurname(), equalTo("Payne"));

        query = manager.createQuery("SELECT c FROM Customer c WHERE c.name='Jack'", Customer.class);
        entity = query.getSingleResult();
        assertThat(entity.getSurname(), equalTo("Sparrow"));

        // Let see what UPDATE will bring if we delete an object referenced in the dataset
        manager.remove(entity);
    }

    @Test
    @Cleanup
    @InitialDataSets(value = "datasets/initial-data2.json", seedStrategy = DataSeedStrategy.UPDATE)
    public void test6() {
        // Since the given data is already present (thanks to the disabled cleanup) usage of UPDATE
        // will update data referenced by the given dataset and present in the database. All rows
        // not present in the database are ignored.
        // Thus John is John again, Max is still there, and Jack is still absent

        TypedQuery<Customer> query = manager.createQuery("SELECT c FROM Customer c WHERE c.surname='Doe'", Customer.class);
        Customer entity = query.getSingleResult();
        assertThat(entity.getName(), equalTo("John")); // not Foo!!!

        query = manager.createQuery("SELECT c FROM Customer c WHERE c.name='Max'", Customer.class);
        entity = query.getSingleResult();
        assertThat(entity.getSurname(), equalTo("Payne"));

        query = manager.createQuery("SELECT c FROM Customer c WHERE c.name='Jack'", Customer.class);
        assertTrue(query.getResultList().isEmpty());
    }
}
