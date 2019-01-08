package com.ctzen.jpamodelexp.pkgscan;

import com.google.common.collect.ImmutableSet;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import java.io.FileFilter;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

/**
 * Helper functions for package scanner.
 *
 * @author cchang
 */
public abstract class PackageScannerUtil {

    private PackageScannerUtil() {}

    // separators
    private static final char SEP_PATH = '/';           // the sane path separator
    private static final char SEP_WINDOZE_PATH = '\\';  // the insane ;-) path separator
    private static final char SEP_PACKAGE = '.';        // package name separator

    // file extensions (with initial . separator)
    static final String EXT_CLASS = ".class";
    static final String EXT_JAR = ".jar";

    /**
     * FileFilter that accepts directories and possible JPA class files.
     */
    static final FileFilter JPA_CLASS_FILE_FILTER = file ->
            file.isDirectory() || maybeJpaClassFile(file.getName());

    /**
     * JPA static metamodel class names ends with "_" (underscore)
     */
    private static final String METAMODEL_CLASS_NAME_SUFFIX = "_";

    /**
     * JPA static metamodel class files ends with "_.class"
     */
    private static final String METAMODEL_CLASS_FILE_SUFFIX = METAMODEL_CLASS_NAME_SUFFIX + EXT_CLASS;

    /**
     * Check if an annotation is declared (non-inherited) on a class.
     *
     * @param clz               class to check
     * @param annotationClass   annotation to find
     * @return true if annotationClass is declared on clz
     */
    private static boolean isDeclaredAnnotationPresent(Class<?> clz, Class<? extends Annotation> annotationClass) {
        return clz.getDeclaredAnnotation(annotationClass) != null;
    }

    /**
     * Check if any of the annotations is declared (non-inherited) on a class.
     *
     * @param clz               class to check
     * @param annotationClasses annotations to find
     * @return true if any of the annotationClasses is declared on clz
     */
    private static boolean isAnyDeclaredAnnotationPresent(Class<?> clz, Collection<Class<? extends Annotation>> annotationClasses) {
        return annotationClasses.stream()
                .anyMatch(annotationClass -> isDeclaredAnnotationPresent(clz, annotationClass));
    }

    /**
     * JPA annotations that can produce JPA static metamodels.
     */
    private static Set<Class<? extends Annotation>> JPA_STATIC_METAMODELABLE_ANNOTATIONS = ImmutableSet.of(
            Entity.class,
            MappedSuperclass.class,
            Embeddable.class
    );

    /**
     * Check if a class can be modeled by JPA static metamodel.
     *
     * @param clz   class to check
     * @return true if clz can be modeled by JPA static metamodel
     */
    static boolean isJpaStaticMetamodelable(Class<?> clz) {
        return isAnyDeclaredAnnotationPresent(clz, JPA_STATIC_METAMODELABLE_ANNOTATIONS);
    }

    /**
     * If a string starts with stripChar, remove the stripChar.
     *
     * @param s         string to check
     * @param stripChar char to strip if string starts with it
     * @return stripped string
     */
    private static String stripInitial(String s, char stripChar) {
        return s.isEmpty() || s.charAt(0) != stripChar ? s : s.substring(1);
    }

    /**
     * If a string ends with stripSuffix, remove the stripSuffix.
     *
     * @param s             string to check
     * @param stripSuffix   suffix to strip if s ends with it
     * @return stripped string
     */
    private static String stripSuffix(String s, String stripSuffix) {
        return s.endsWith(stripSuffix)
             ? s.substring(0, s.length() - stripSuffix.length())
             : s;
    }

    /**
     * Canonize a path.
     * Essentially unix-ize it (replace any windoze path separatorswith unix path
     * separators), and optionally remove any initial path separator.
     *
     * @param path                  path to sanitize
     * @param stripInitialPathSep   true to remove the initial path separator if present
     * @return sanitized path
     */
    static String sanitizePath(String path, boolean stripInitialPathSep) {
        path = path.replace(SEP_WINDOZE_PATH, SEP_PATH);
        return stripInitialPathSep ? stripInitial(path, SEP_PATH) : path;
    }

    /**
     * Check if a path may be a JPA class file.
     *
     * @param path  path to check
     * @return true if path ends with ".class" but not "_.class"
     */
    static boolean maybeJpaClassFile(String path) {
        return path.endsWith(EXT_CLASS) && !path.endsWith(METAMODEL_CLASS_FILE_SUFFIX);
    }

    /**
     * Check if a path may be a JPA class file under a package.
     *
     * @param path                  e.g. foo/bar/Fred.class or /foo/bar/Fred.class
     * @param packagePathPrefix     e.g. foo/bar/
     *                              use packageNameToPathPrefix()
     * @return true if path may be a JPA class under package
     */
    static boolean maybeJpaClassFileInPackage(String path, String packagePathPrefix) {
        path = sanitizePath(path, true);
        return path.startsWith(packagePathPrefix) && maybeJpaClassFile(path);
    }

    /**
     * Coverts a package name to path notation.
     * Essentially replaces "." with "/", and appends a "/".
     *
     * @param packageName   e.g. foo.bar
     * @return              e.g. foo/bar/
     */
    static String packageNameToPathPrefix(String packageName) {
        return packageName.replace(SEP_PACKAGE, SEP_PATH) + SEP_PATH;
    }

    /**
     * Converts a package name to resource name.
     *
     * @param packageName   package name to convert
     * @return converted resource name
     */
    static String packageNameToResourceName(String packageName) {
        String resourceName = packageName.replace(SEP_PACKAGE, SEP_PATH);
        return stripInitial(resourceName, SEP_PATH);
    }

    /**
     * Converts a class file path to class name.
     *
     * @param path  class file path
     * @return converted class name
     */
    static String classFileToClassName(String path) {
        return stripSuffix(sanitizePath(path, true), EXT_CLASS)
                .replace(SEP_PATH, SEP_PACKAGE);    // package-ize
    }

}
