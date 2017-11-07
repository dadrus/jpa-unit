package eu.drus.jpa.unit.neo4j.test.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.sun.codemodel.JCodeModel;

public final class TestCodeUtils {

    private TestCodeUtils() {}

    public static void buildModel(final File destinationFolder, final JCodeModel jCodeModel) throws IOException {
        jCodeModel.build(destinationFolder, new PrintStream(new ByteArrayOutputStream()));
    }

    public static void compileModel(final File destinationFolder) throws IOException {

        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        final File[] javaFiles = destinationFolder.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".java");
            }
        });

        final Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(javaFiles);
        compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();
        fileManager.close();
    }

    public static Class<?> loadClass(final File classFolder, final String className) throws MalformedURLException, ClassNotFoundException {
        final ClassLoader cl = new URLClassLoader(new URL[] {
                classFolder.toURI().toURL()
        });

        return Class.forName(className, false, cl);
    }
}
