

package com.ark.bcp.domain.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;

/**
 */
@SuppressWarnings("AlibabaUndefineMagicConstant")
public final class ClassLoaderUtil {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ClassLoaderUtil.class);

    /**
     * class path
     */
    private static String classPath = "";

    /**
     * loader
     */
    private static ClassLoader loader = Thread.currentThread().getContextClassLoader();

    private ClassLoaderUtil() {
    }

    //
    // get class path
    //
    static {

        if (loader == null) {
            LOGGER.info("using system class loader!");
            loader = ClassLoader.getSystemClassLoader();
        }

        try {

            URL url = loader.getResource("");
            LOGGER.info("get classpath using getResource() url is :{}", url.toURI().toString());

            String scheme = url.toURI().getScheme();
            if ("file".equalsIgnoreCase(scheme)) {
                File file = new File(url.toURI());
                classPath = file.getAbsolutePath();
                classPath = URLDecoder.decode(classPath, "utf-8");
            } else if (url.toURI().toString().contains(".jar!")) {
                LOGGER.info("using config file inline jar!" + classPath);
                classPath = System.getProperty("user.dir");
                addCurrentWorkingDir2Classpath(classPath);
            } else {
                LOGGER.info("cannot get classpath using getResource(), now using user.dir");
                classPath = System.getProperty("user.dir");
                addCurrentWorkingDir2Classpath(classPath);
            }
        } catch (Exception e) {
            LOGGER.error("get classpath using getResource() error, now using user.dir", e);
            classPath = System.getProperty("user.dir");
            //
            addCurrentWorkingDir2Classpath(classPath);
        } finally {
            LOGGER.info("classpath: {}", classPath);
        }
    }

    /**
     * only support 1.7 or higher
     * http://stackoverflow.com/questions/252893/how-do-you-change-the-classpath-within-java
     */
    private static void addCurrentWorkingDir2Classpath(String path2Added) {

        // Add the conf dir to the classpath
        // Chain the current thread classloader
        URLClassLoader urlClassLoader;
        try {
            urlClassLoader = new URLClassLoader(new URL[]{new File(path2Added).toURI().toURL()},
                    loader);
            // Replace the thread classloader - assumes
            // you have permissions to do so
            Thread.currentThread().setContextClassLoader(urlClassLoader);
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
    }

    public static String getClassPath() {
        return classPath;
    }

    public static ClassLoader getLoader() {
        return loader;
    }
}
