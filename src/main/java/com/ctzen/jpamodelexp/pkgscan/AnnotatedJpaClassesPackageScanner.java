package com.ctzen.jpamodelexp.pkgscan;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import static com.ctzen.jpamodelexp.pkgscan.PackageScannerUtil.*;

/**
 * Scans packages for annotated JPA classes.
 *
 * As of this writing, this scanner os capable of handing "file:", and "jar:file:" resources.
 *
 * By default, only the "file:" resources are scanned, use any of the setUrlProtocols() functions to
 * change this behaviour.
 *
 * @author cchang
 */
public class AnnotatedJpaClassesPackageScanner {

    public AnnotatedJpaClassesPackageScanner() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public AnnotatedJpaClassesPackageScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
        setUrlProtocols(UrlProtocol.FILE);
    }

    private final ClassLoader classLoader;

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className, true, classLoader);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Enumeration<URL> getResources(String resourceName) {
        try {
            return classLoader.getResources(resourceName);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final Set<UrlProtocol> urlProtocols = new HashSet<>();

    public void setUrlProtocols(Collection<UrlProtocol> urlProtocols) {
        this.urlProtocols.clear();
        this.urlProtocols.addAll(urlProtocols);
    }

    public void setUrlProtocols(UrlProtocol... urlProtocols) {
        setUrlProtocols(Arrays.asList(urlProtocols));
    }

    private boolean scanProtocol(UrlProtocol urlProtocol) {
        return urlProtocols.isEmpty()
            || (urlProtocol != null && urlProtocols.contains(urlProtocol));
    }

    private final Set<String> packageNames = new HashSet<>();

    public void addPackages(Collection<String> packageNames) {
        this.packageNames.addAll(packageNames);
    }

    public void addPackages(String... packageNames) {
        addPackages(Arrays.asList(packageNames));
    }

    private final Set<Class<?>> jpaClasses = new HashSet<>();

    private void addJpaClassFile(String path) {
        Class<?> clz = loadClass(classFileToClassName(path));
        if (isJpaStaticMetamodelable(clz)) {
            jpaClasses.add(clz);
        }
    }

    public Set<Class<?>> scan() {
        jpaClasses.clear();
        packageNames.forEach(this::scan);
        return jpaClasses;
    }

    private void scan(String packageName) {
        String resourceName = packageNameToResourceName(packageName);
        Enumeration<URL> resources = getResources(resourceName);
        while(resources.hasMoreElements()) {
            scan(packageName, resources.nextElement());
        }
    }

    private void unhandled(URL url) {
        throw new IllegalArgumentException("Unhandled: " + url);
    }

    private void scan(String packageName, URL url) {
        UrlProtocol urlProtocol = UrlProtocol.decode(url.getProtocol());
        if (scanProtocol(urlProtocol)) {
            if (urlProtocol != null) {
                switch(urlProtocol) {
                    case FILE:
                        scanFile(packageName, url);
                        return;
                    case JAR:
                        scanJar(packageName, url);
                        return;
                }
            }
            unhandled(url);
        }
    }

    private void scanFile(String packageName, URL url) {
        File dir = new File(url.getPath());
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + url);
        }
        scan(dir.listFiles(JPA_CLASS_FILE_FILTER), dir.getPath().length() - packageName.length());
    }

    private void scan(File[] files, int substringIndex) {
        if (files != null) {
            Arrays.stream(files).filter(file -> !file.isDirectory())
                    .forEach(file -> addJpaClassFile(file.getPath().substring(substringIndex)));
            Arrays.stream(files).filter(File::isDirectory)
                    .forEach(dir -> scan(dir.listFiles(JPA_CLASS_FILE_FILTER), substringIndex));
        }
    }

    private static final Pattern REX_JAR_FILE =
            Pattern.compile(UrlProtocol.FILE.getProtocol() + ":(.+\\" + EXT_JAR + ")(!.*)?");

    private void scanJar(String packageName, URL url) {
        // jar:file:/path/file.jar!/jpamodelexp/test
        String path = url.getPath();
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("No path: " + url);
        }
        Matcher m = REX_JAR_FILE.matcher(path);
        if (!m.matches()) {
            unhandled(url);
        }
        JarFile jarFile;
        try {
            jarFile = new JarFile(new File(m.group(1)));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        String packagePath = packageNameToPathPrefix(packageName);
        jarFile.stream()
                .map(ZipEntry::getName)
                .filter(name -> maybeJpaClassFileInPackage(name, packagePath))
                .forEach(this::addJpaClassFile);
    }

}
