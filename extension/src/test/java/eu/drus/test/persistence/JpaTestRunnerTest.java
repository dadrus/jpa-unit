package eu.drus.test.persistence;

import static eu.drus.test.persistence.core.metadata.TestCodeUtils.buildModel;
import static eu.drus.test.persistence.core.metadata.TestCodeUtils.compileModel;
import static eu.drus.test.persistence.core.metadata.TestCodeUtils.loadClass;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.mockito.ArgumentCaptor;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;

public class JpaTestRunnerTest {

    @ClassRule
    public static TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testClassWithoutPersistenceContextField() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaTestRunner.class);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final JpaTestRunner runner = new JpaTestRunner(cut);

        try {
            // WHEN
            runner.run(new RunNotifier());
            fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException e) {

            // THEN
            assertThat(e.getMessage(), containsString("EntityManagerFactory or EntityManager field annotated"));
        }
    }

    @Test
    public void testClassWithMultiplePersistenceContextFields() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaTestRunner.class);
        final JFieldVar emField = jClass.field(JMod.PRIVATE, EntityManager.class, "em");
        emField.annotate(PersistenceContext.class);
        final JFieldVar emfField = jClass.field(JMod.PRIVATE, EntityManagerFactory.class, "emf");
        emfField.annotate(PersistenceContext.class);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final JpaTestRunner runner = new JpaTestRunner(cut);

        try {
            // WHEN
            runner.run(new RunNotifier());
            fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException e) {

            // THEN
            assertThat(e.getMessage(), containsString("Only single field is allowed"));
        }
    }

    @Test
    public void testClassWithPersistenceContextFieldOfWrongType() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaTestRunner.class);
        final JFieldVar emField = jClass.field(JMod.PRIVATE, String.class, "em");
        emField.annotate(PersistenceContext.class);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final JpaTestRunner runner = new JpaTestRunner(cut);

        try {
            // WHEN
            runner.run(new RunNotifier());
            fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException e) {

            // THEN
            assertThat(e.getMessage(), containsString("is neither of type EntityManagerFactory, nor EntityManager"));
        }
    }

    @Test
    public void testClassWithPersistenceContextWithoutUnitNameSpecified() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaTestRunner.class);
        final JFieldVar emField = jClass.field(JMod.PRIVATE, EntityManager.class, "em");
        emField.annotate(PersistenceContext.class);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final JpaTestRunner runner = new JpaTestRunner(cut);

        try {
            // WHEN
            runner.run(new RunNotifier());
            fail("IllegalArgumentException expected");
        } catch (final PersistenceException e) {

            // THEN
            assertThat(e.getMessage(), containsString("No Persistence provider"));
        }
    }

    @Test
    public void testClassWithPersistenceContextWithUnknownUnitNameSpecified() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaTestRunner.class);
        final JFieldVar emField = jClass.field(JMod.PRIVATE, EntityManager.class, "em");
        final JAnnotationUse jAnnotation = emField.annotate(PersistenceContext.class);
        jAnnotation.param("unitName", "foo");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final JpaTestRunner runner = new JpaTestRunner(cut);

        try {
            // WHEN
            runner.run(new RunNotifier());
            fail("IllegalArgumentException expected");
        } catch (final PersistenceException e) {

            // THEN
            assertThat(e.getMessage(), containsString("No Persistence provider"));
        }
    }

    @Test
    public void testClassWithPersistenceContextWithKonfiguredUnitNameSpecified() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaTestRunner.class);
        final JFieldVar emField = jClass.field(JMod.PRIVATE, EntityManager.class, "em");
        final JAnnotationUse jAnnotation = emField.annotate(PersistenceContext.class);
        jAnnotation.param("unitName", "test-unit-1");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final JpaTestRunner runner = new JpaTestRunner(cut);

        final RunListener listener = mock(RunListener.class);
        final RunNotifier notifier = new RunNotifier();
        notifier.addListener(listener);

        // WHEN
        runner.run(notifier);

        // THEN
        final ArgumentCaptor<Description> descriptionCaptor = ArgumentCaptor.forClass(Description.class);
        verify(listener).testStarted(descriptionCaptor.capture());
        assertThat(descriptionCaptor.getValue().getClassName(), equalTo("ClassUnderTest"));
        assertThat(descriptionCaptor.getValue().getMethodName(), equalTo("testMethod"));

        verify(listener).testFinished(descriptionCaptor.capture());
        assertThat(descriptionCaptor.getValue().getClassName(), equalTo("ClassUnderTest"));
        assertThat(descriptionCaptor.getValue().getMethodName(), equalTo("testMethod"));

        verifyNoMoreInteractions(listener);
    }
}
