package eu.drus.test.persistence.core.metadata;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.TestClass;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.test.persistence.annotation.ApplyScriptsAfter;
import eu.drus.test.persistence.annotation.ApplyScriptsBefore;
import eu.drus.test.persistence.annotation.Cleanup;
import eu.drus.test.persistence.annotation.CleanupUsingScripts;
import eu.drus.test.persistence.annotation.CustomColumnFilter;
import eu.drus.test.persistence.annotation.ExpectedDataSets;
import eu.drus.test.persistence.annotation.InitialDataSets;
import eu.drus.test.persistence.annotation.Transactional;

@RunWith(MockitoJUnitRunner.class)
public class MetadataExtractorTest {

    @Spy
    private MetadataExtractor metadataExtractor = new MetadataExtractor(new TestClass(MetadataExtractorTest.class));

    @Test
    public void testApplyScriptsAfter() {

        // WHEN
        final AnnotationInspector<ApplyScriptsAfter> ai = metadataExtractor.applyScriptsAfter();

        // THEN
        assertThat(ai, notNullValue());
    }

    @Test
    public void testApplyScriptsBefore() {

        // WHEN
        final AnnotationInspector<ApplyScriptsBefore> ai = metadataExtractor.applyScriptsBefore();

        // THEN
        assertThat(ai, notNullValue());
    }

    @Test
    public void testCleanup() {

        // WHEN
        final AnnotationInspector<Cleanup> ai = metadataExtractor.cleanup();

        // THEN
        assertThat(ai, notNullValue());
    }

    @Test
    public void testCleanupUsingScripts() {

        // WHEN
        final AnnotationInspector<CleanupUsingScripts> ai = metadataExtractor.cleanupUsingScripts();

        // THEN
        assertThat(ai, notNullValue());
    }

    @Test
    public void testCustomColumnFilter() {

        // WHEN
        final AnnotationInspector<CustomColumnFilter> ai = metadataExtractor.customColumnFilter();

        // THEN
        assertThat(ai, notNullValue());
    }

    @Test
    public void testExpectedDataSets() {

        // WHEN
        final AnnotationInspector<ExpectedDataSets> ai = metadataExtractor.expectedDataSets();

        // THEN
        assertThat(ai, notNullValue());
    }

    @Test
    public void testInitialDataSets() {

        // WHEN
        final AnnotationInspector<InitialDataSets> ai = metadataExtractor.initialDataSets();

        // THEN
        assertThat(ai, notNullValue());
    }

    @Test
    public void testPersistenceContext() {

        // WHEN
        final AnnotationInspector<PersistenceContext> ai = metadataExtractor.persistenceContext();

        // THEN
        assertThat(ai, notNullValue());
    }

    @Test
    public void testTransactional() {

        // WHEN
        final AnnotationInspector<Transactional> ai = metadataExtractor.transactional();

        // THEN
        assertThat(ai, notNullValue());
    }
}
