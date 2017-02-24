package eu.drus.jpa.unit.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.test.model.DepositorRepository;

@RunWith(CdiTestRunner.class)
public class CdiBootstrappingWithJpaTest {

    @Inject
    private DepositorRepository repository;

    @Test
    public void testEntityManagerIsInjected() {
        assertNotNull(repository);
        assertThat(repository.findAll().isEmpty(), is(Boolean.TRUE));
    }
}
