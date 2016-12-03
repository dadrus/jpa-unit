package eu.drus.test.persistence.rule.evaluation;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.test.persistence.JpaTestException;
import eu.drus.test.persistence.core.PersistenceUnitDescriptor;
import eu.drus.test.persistence.core.PersistenceUnitDescriptorLoader;
import eu.drus.test.persistence.core.metadata.FeatureResolver;
import eu.drus.test.persistence.core.metadata.FeatureResolverFactory;

@RunWith(MockitoJUnitRunner.class)
public class EvaluationRuleTest {

    @Mock
    private FeatureResolverFactory featureResolverFactory;

    @Mock
    private PersistenceUnitDescriptorLoader pudLoader;

    @Mock
    private PersistenceUnitDescriptor descriptor;

    @Mock
    private FeatureResolver resolver;

    @Mock
    private Statement base;

    @Mock
    private FrameworkMethod method;

    @Before
    public void setUp() throws Exception {

        when(pudLoader.loadPersistenceUnitDescriptors(any(Map.class))).thenReturn(Arrays.asList(descriptor));
        when(descriptor.getUnitName()).thenReturn("");
        when(descriptor.getProperties()).thenReturn(Collections.emptyMap());

        when(featureResolverFactory.createFeatureResolver(any(Method.class), any(Class.class))).thenReturn(resolver);
        when(resolver.getSeedData()).thenReturn(Collections.emptyList());
    }

    @Test
    public void testApplyRule() {
        // GIVEN
        final EvaluationRule rule = new EvaluationRule(featureResolverFactory, pudLoader, "", Collections.emptyMap());

        // WHEN
        final Statement stmt = rule.apply(base, method, this);

        // THEN
        assertThat(stmt, not(nullValue()));
    }

    @Test
    public void testApplyRuleForBadSeedData() {
        // GIVEN
        when(resolver.getSeedData()).thenReturn(Arrays.asList("not-existent.yaml"));
        final EvaluationRule rule = new EvaluationRule(featureResolverFactory, pudLoader, "", Collections.emptyMap());

        try {
            // WHEN
            rule.apply(base, method, this);
            fail("JpaTestException expected");
        } catch (final JpaTestException e) {
            // THEN
            assertThat(e.getMessage(), containsString("Failed to create statement"));
        }
    }

    @Test
    public void testCreateRuleWithErrorWhileLoadingPersistenceUnitDescriptors() throws IOException {
        // GIVEN
        when(pudLoader.loadPersistenceUnitDescriptors(any(Map.class))).thenThrow(IOException.class);

        try {
            // WHEN
            final EvaluationRule rule = new EvaluationRule(featureResolverFactory, pudLoader, "", Collections.emptyMap());
            fail("JpaTestException expected");
        } catch (final JpaTestException e) {
            // THEN
            assertThat(e.getMessage(), containsString("Error while loading persistence unit descriptors"));
            assertThat(e.getCause(), instanceOf(IOException.class));
        }
    }

    @Test
    public void testCreateRuleWithoutFindingProvidedPersistenceUnitName() throws IOException {
        // GIVEN
        when(pudLoader.loadPersistenceUnitDescriptors(any(Map.class))).thenReturn(Arrays.asList(mock(PersistenceUnitDescriptor.class)));

        try {
            // WHEN
            new EvaluationRule(featureResolverFactory, pudLoader, "", Collections.emptyMap());
            fail("JpaTestException expected");
        } catch (final JpaTestException e) {
            // THEN
            assertThat(e.getMessage(), containsString("No peristence unit found"));
        }
    }

    @Test
    public void testCreateRuleHavingMultipleUnitsForProvidedPersistenceUnitName() throws IOException {
        // GIVEN
        when(pudLoader.loadPersistenceUnitDescriptors(any(Map.class))).thenReturn(Arrays.asList(descriptor, descriptor));

        try {
            // WHEN
            new EvaluationRule(featureResolverFactory, pudLoader, "", Collections.emptyMap());
            fail("JpaTestException expected");
        } catch (final JpaTestException e) {
            // THEN
            assertThat(e.getMessage(), containsString("Multiple persistence units found"));
        }
    }
}
