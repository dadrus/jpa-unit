package eu.drus.test.persistence.core.metadata;

import static eu.drus.test.persistence.core.metadata.TestCodeUtils.buildModel;
import static eu.drus.test.persistence.core.metadata.TestCodeUtils.compileModel;
import static eu.drus.test.persistence.core.metadata.TestCodeUtils.loadClass;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;

public class FeatureResolverFactoryTest {

    private static final FeatureResolverFactory FACTORY = new FeatureResolverFactory();

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testCreateFeatureResolver() throws Exception {

        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "test");

        buildModel(testFolder.getRoot(), jCodeModel);

        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final Method method = cut.getDeclaredMethod(jMethod.name());

        // WHEN
        final FeatureResolver resolver = FACTORY.createFeatureResolver(method, cut);

        // THEN
        assertThat(resolver, notNullValue());
    }

}
