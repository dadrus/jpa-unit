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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.model.TestClass;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;

public class AnnotationInspectorTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testFieldAnnotationInspection() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JFieldVar jField = jClass.field(JMod.PRIVATE, String.class, "testField");
        jField.annotate(PersistenceContext.class);
        jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");

        jCodeModel.build(testFolder.getRoot());

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Field field = cut.getDeclaredField("testField");
        final Method method = cut.getDeclaredMethod("testMethod");

        final AnnotationInspector<PersistenceContext> ai = new AnnotationInspector<>(new TestClass(cut), PersistenceContext.class);

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
        // TODO
    }

    @Test
    public void testClassAnnotationInspection() throws Exception {
        // TODO
    }

    @Test
    public void testClassAndMethodAnnotationInspection() throws Exception {
        // TODO
    }
}
