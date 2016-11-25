package eu.drus.test.persistence.core.metadata;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.PersistenceContext;

import org.junit.runners.model.TestClass;

import eu.drus.test.persistence.annotation.ApplyScriptsAfter;
import eu.drus.test.persistence.annotation.ApplyScriptsBefore;
import eu.drus.test.persistence.annotation.Cleanup;
import eu.drus.test.persistence.annotation.CleanupUsingScripts;
import eu.drus.test.persistence.annotation.CustomColumnFilter;
import eu.drus.test.persistence.annotation.ExpectedDataSets;
import eu.drus.test.persistence.annotation.InitialDataSets;
import eu.drus.test.persistence.annotation.Transactional;

public class MetadataExtractor {
    private final TestClass testClass;

    private final Map<Class<?>, AnnotationInspector<?>> inspectors = new HashMap<>();

    public MetadataExtractor(final TestClass testClass) {
        this.testClass = testClass;
    }

    private <K extends Annotation> void register(final TestClass testClass, final Class<K> annotation) {
        inspectors.put(annotation, new AnnotationInspector<>(testClass, annotation));
    }

    @SuppressWarnings("unchecked")
    private <K extends Annotation> AnnotationInspector<K> using(final Class<K> annotation) {
        if (inspectors.get(annotation) == null) {
            register(testClass, annotation);
        }
        return (AnnotationInspector<K>) inspectors.get(annotation);
    }

    public AnnotationInspector<Transactional> transactional() {
        return using(Transactional.class);
    }

    public AnnotationInspector<InitialDataSets> initialDataSets() {
        return using(InitialDataSets.class);
    }

    public AnnotationInspector<ExpectedDataSets> expectedDataSets() {
        return using(ExpectedDataSets.class);
    }

    public AnnotationInspector<CustomColumnFilter> customColumnFilter() {
        return using(CustomColumnFilter.class);
    }

    public AnnotationInspector<Cleanup> cleanup() {
        return using(Cleanup.class);
    }

    public AnnotationInspector<CleanupUsingScripts> cleanupUsingScripts() {
        return using(CleanupUsingScripts.class);
    }

    public AnnotationInspector<PersistenceContext> persistenceContext() {
        return using(PersistenceContext.class);
    }

    public AnnotationInspector<ApplyScriptsBefore> applyScriptsBefore() {
        return using(ApplyScriptsBefore.class);
    }

    public AnnotationInspector<ApplyScriptsAfter> applyScriptsAfter() {
        return using(ApplyScriptsAfter.class);
    }
}
