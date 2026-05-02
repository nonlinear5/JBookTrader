package com.jbooktrader.platform.strategy;

import com.jbooktrader.platform.optimizer.StrategyParams;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * @author Eugene Kononov
 */
public class StrategyLoader {
    /**
     * Searches the classpath (including inside the JAR files) to find classes
     * that extend the specified superclass. The intent is to be able to implement
     * new strategy classes as "plug-and-play" units of JBookTrader. That is,
     * JBookTrader will know how to run a trading strategy as long as that
     * strategy is implemented in a class that extends the base Strategy class.
     */
    private static List<String> getClassNames() throws IOException, URISyntaxException {
        List<String> classNames = new ArrayList<>();

        String javaClassPath = System.getProperty("java.class.path");
        String pathSep = System.getProperty("path.separator");
        String[] classPathEntries = javaClassPath.split(pathSep);

        List<URL> classLoaderUrlsList = new ArrayList<>();
        for (String cpEntry : classPathEntries) {
            classLoaderUrlsList.add(new File(cpEntry).toURI().toURL());
        }

        URL[] classLoaderUrls = new URL[classLoaderUrlsList.size()];
        classLoaderUrls = classLoaderUrlsList.toArray(classLoaderUrls);
        URLClassLoader urlClassLoader = new URLClassLoader(classLoaderUrls);

        for (URL url : urlClassLoader.getURLs()) {
            File file = new File(url.toURI());

            if (file.isDirectory()) {
                Path p = file.toPath();
                Files.walk(p).forEach(path -> {
                    String s = path.toUri().getPath();
                    if (s.endsWith(".class") && !s.contains("Test")) {
                        classNames.add(s.substring(url.getPath().length()));
                    }
                });

            } else if (url.toString().endsWith(".jar")) {  // classes in jar support, makes deployment easier
                JarFile jar = new JarFile(file);
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryPath = entry.getName();
                    String prefix = "com/jbooktrader/strategy/";
                    if (entryPath.contains(prefix) && entryPath.endsWith(".class")) {
                        //String className = entryPath.substring(prefix.length(), entryPath.lastIndexOf(".class"));
                        String className = entryPath;
                        if (!className.contains("base/")) {  // support nested packages not named base
                            if (className.contains("/")) {
                                className = className.replaceAll("/", ".");
                            }
                            int xx = 0;
                            classNames.add(className);
                        }
                    }
                }
            }
        }

        Collections.sort(classNames);
        return classNames;
    }

    public List<Strategy> getStrategies() {
        List<Strategy> strategies = new ArrayList<>();
        //String pathSep = System.getProperty("path.separator");

        try {
            List<String> strategyNames = getClassNames();
            for (String strategyName : strategyNames) {
                int index = strategyName.lastIndexOf(".class");
                String sn = strategyName;
                if (index != -1) {
                    sn = strategyName.substring(0, strategyName.lastIndexOf(".class"));
                }
                sn = sn.replaceAll("/", ".");
                Class<?> clazz = Class.forName(sn);
                if (!Modifier.isAbstract(clazz.getModifiers())) {
                    if (Strategy.class.isAssignableFrom(clazz)) {  // check to see if it extends Strategy
                        Class<?>[] parameterTypes = new Class[]{StrategyParams.class};
                        Constructor<?> constructor = clazz.getConstructor(parameterTypes);
                        Strategy strategy = (Strategy) constructor.newInstance(new StrategyParams());
                        strategies.add(strategy);
                    }
                }
            }
        } catch (Exception e) {
            String msg = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
            throw new RuntimeException(msg);
        }

        return strategies;
    }
}
