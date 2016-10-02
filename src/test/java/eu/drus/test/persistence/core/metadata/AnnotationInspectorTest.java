package eu.drus.test.persistence.core.metadata;

import static eu.drus.test.persistence.core.metadata.TestCodeUtils.compileModel;
import static eu.drus.test.persistence.core.metadata.TestCodeUtils.loadClass;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.persistence.PersistenceContext;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.model.TestClass;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;

import eu.drus.test.persistence.annotation.ApplyScriptsAfter;
import eu.drus.test.persistence.annotation.Cleanup;
import eu.drus.test.persistence.annotation.InitialDataSets;

public class AnnotationInspectorTest {

    @ClassRule
    public static TemporaryFolder testFolder = new TemporaryFolder();

    private static Class<?> cut;

    @BeforeClass
    public static void generateModel() throws Exception {
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        JAnnotationUse jAnnotationUse = jClass.annotate(InitialDataSets.class);
        jAnnotationUse.param("value", "Script.file");
        jClass.annotate(Cleanup.class);
        final JFieldVar jField = jClass.field(JMod.PRIVATE, String.class, "testField");
        jField.annotate(PersistenceContext.class);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jAnnotationUse = jMethod.annotate(InitialDataSets.class);
        jAnnotationUse.param("value", "InitialDataSets.file");
        jAnnotationUse = jMethod.annotate(ApplyScriptsAfter.class);
        jAnnotationUse.param("value", "ApplyScriptsAfter.file");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        cut = loadClass(testFolder.getRoot(), jClass.name());
    }

    @Test
    public void testFieldAnnotationInspection() throws Exception {
        // GIVEN
        final Field field = cut.getDeclaredField("testField");
        final Method method = cut.getDeclaredMethod("testMethod");

        // WHEN
        final AnnotationInspector<PersistenceContext> ai = new AnnotationInspector<>(new TestClass(cut), PersistenceContext.class);

        // THEN
        assertThat(ai.fetchFromField(field), notNullValue());
        assertThat(ai.fetchFromMethod(method), nullValue());
        assertThat(ai.fetchUsingFirst(method), nullValue());
        assertThat(ai.fetchAll().size(), equalTo(1));
        assertThat(ai.getAnnotatedFields().size(), equalTo(1));
        assertThat(ai.getAnnotatedFields(), hasItem(field));
        assertThat(ai.getAnnotatedMethods().isEmpty(), equalTo(Boolean.TRUE));
        assertThat(ai.getAnnotationOnClassLevel(), nullValue());
        assertThat(ai.isDefinedOnField(field), equalTo(Boolean.TRUE));
        assertThat(ai.isDefinedOnAnyField(), equalTo(Boolean.TRUE));
        assertThat(ai.isDefinedOnMethod(method), equalTo(Boolean.FALSE));
        assertThat(ai.isDefinedOnAnyMethod(), equalTo(Boolean.FALSE));
        assertThat(ai.isDefinedOnClassLevel(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testMethodAnnotationInspection() throws Exception {
        // GIVEN
        final Field field = cut.getDeclaredField("testField");
        final Method method = cut.getDeclaredMethod("testMethod");

        // WHEN
        final AnnotationInspector<ApplyScriptsAfter> ai = new AnnotationInspector<>(new TestClass(cut), ApplyScriptsAfter.class);

        // THEN
        assertThat(ai.fetchFromField(field), nullValue());
        assertThat(ai.fetchFromMethod(method), notNullValue());
        assertThat(ai.fetchUsingFirst(method), notNullValue());
        assertThat(ai.fetchAll().size(), equalTo(1));
        assertThat(ai.getAnnotatedFields().isEmpty(), equalTo(Boolean.TRUE));
        assertThat(ai.getAnnotatedMethods().size(), equalTo(1));
        assertThat(ai.getAnnotatedMethods(), hasItem(method));
        assertThat(ai.getAnnotationOnClassLevel(), nullValue());
        assertThat(ai.isDefinedOnField(field), equalTo(Boolean.FALSE));
        assertThat(ai.isDefinedOnAnyField(), equalTo(Boolean.FALSE));
        assertThat(ai.isDefinedOnMethod(method), equalTo(Boolean.TRUE));
        assertThat(ai.isDefinedOnAnyMethod(), equalTo(Boolean.TRUE));
        assertThat(ai.isDefinedOnClassLevel(), equalTo(Boolean.FALSE));
    }

    @Test
    public void testClassAnnotationInspection() throws Exception {
        // GIVEN
        final Field field = cut.getDeclaredField("testField");
        final Method method = cut.getDeclaredMethod("testMethod");

        // WHEN
        final AnnotationInspector<Cleanup> ai = new AnnotationInspector<>(new TestClass(cut), Cleanup.class);

        // THEN
        assertThat(ai.fetchFromField(field), nullValue());
        assertThat(ai.fetchFromMethod(method), nullValue());
        assertThat(ai.fetchUsingFirst(method), notNullValue());
        assertThat(ai.fetchAll().size(), equalTo(1));
        assertThat(ai.getAnnotatedFields().isEmpty(), equalTo(Boolean.TRUE));
        assertThat(ai.getAnnotatedMethods().isEmpty(), equalTo(Boolean.TRUE));
        assertThat(ai.getAnnotationOnClassLevel(), notNullValue());
        assertThat(ai.isDefinedOnField(field), equalTo(Boolean.FALSE));
        assertThat(ai.isDefinedOnAnyField(), equalTo(Boolean.FALSE));
        assertThat(ai.isDefinedOnMethod(method), equalTo(Boolean.FALSE));
        assertThat(ai.isDefinedOnAnyMethod(), equalTo(Boolean.FALSE));
        assertThat(ai.isDefinedOnClassLevel(), equalTo(Boolean.TRUE));
    }

    @Test
    public void testClassAndMethodAnnotationInspection() throws Exception {
        // GIVEN
        final Field field = cut.getDeclaredField("testField");
        final Method method = cut.getDeclaredMethod("testMethod");

        // WHEN
        final AnnotationInspector<InitialDataSets> ai = new AnnotationInspector<>(new TestClass(cut), InitialDataSets.class);

        // THEN
        assertThat(ai.fetchFromField(field), nullValue());
        InitialDataSets ids = ai.fetchFromMethod(method);
        assertThat(ids, notNullValue());
        assertThat(ids.value()[0], equalTo("InitialDataSets.file"));
        ids = ai.fetchUsingFirst(method);
        assertThat(ids, notNullValue());
        assertThat(ids.value()[0], equalTo("InitialDataSets.file"));
        assertThat(ai.fetchAll().size(), equalTo(2));
        assertThat(ai.getAnnotatedFields().isEmpty(), equalTo(Boolean.TRUE));
        assertThat(ai.getAnnotatedMethods().size(), equalTo(1));
        ids = ai.getAnnotationOnClassLevel();
        assertThat(ids, notNullValue());
        assertThat(ids.value()[0], equalTo("Script.file"));
        assertThat(ai.isDefinedOnField(field), equalTo(Boolean.FALSE));
        assertThat(ai.isDefinedOnAnyField(), equalTo(Boolean.FALSE));
        assertThat(ai.isDefinedOnMethod(method), equalTo(Boolean.TRUE));
        assertThat(ai.isDefinedOnAnyMethod(), equalTo(Boolean.TRUE));
        assertThat(ai.isDefinedOnClassLevel(), equalTo(Boolean.TRUE));
    }
}
