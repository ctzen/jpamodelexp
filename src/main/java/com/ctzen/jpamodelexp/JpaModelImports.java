package com.ctzen.jpamodelexp;

import com.google.common.collect.ImmutableSet;

import java.util.*;
import java.util.stream.Stream;

/**
 * Holds import classes for a {@link JpaModel}.
 *
 * @author cchang
 */
class JpaModelImports {

    private static final Set<String> IMPLICIT_PACKAGES = ImmutableSet.of(
            "java.lang"
    );

    JpaModelImports(String packageName, Class<?>... classes) {
        this.packageName = packageName;
        add(Arrays.asList(classes));
    }

    private final String packageName;

    private final Set<Class<?>> classes = new TreeSet<>(Comparator.comparing(Class::getCanonicalName));

    void add(Class<?> clz) {
        classes.add(clz);
    }

    void add(Collection<Class<?>> classes) {
        this.classes.addAll(classes);
    }

    void finalizeImports() {
        Map<String, Set<Class<?>>> conflictsMap = new HashMap<>();
        classes.forEach(clz -> conflictsMap.computeIfAbsent(clz.getSimpleName(), k -> new HashSet<>()).add(clz));
        conflictsMap.values().forEach(this::resolveConflict);   // resolve any conflict
        classes.removeIf(this::isImplicit);                     // remove classes from implicit packages
    }

    private void resolveConflict(Set<Class<?>> conflicts) {
        if (conflicts.size() > 1) {                                 // is conflicted only if a simple name came from more than 1 package
            if (conflicts.stream().anyMatch(this::isImplicit)) {    // if any of the conflicted class came from implicit packages
                classes.removeAll(conflicts);                       // don't import any of them
            }
            else {
                // conflict NOT involving any implicit package
                // keep one to import and don't import the rest

                // It shouldn't matter which one to keep but java.util.Date and java.sql.Date are used for unit testing
                // and the annotation processor prefers to import java.util.Date, so we handle this special case for unit testing
                if (conflicts.size() == 2 && conflicts.stream().allMatch(clz -> clz.getCanonicalName().matches("java\\.(util|sql)\\.Date"))) {
                    classes.remove(conflicts.stream().filter(clz -> clz.getCanonicalName().equals("java.sql.Date")).findFirst().get());
                    return;
                }

                Iterator<Class<?>> i = conflicts.iterator();
                i.next();
                i.remove();
                classes.removeAll(conflicts);
            }
        }
    }

    private boolean isImplicit(Class<?> clz) {
        Package pkg = clz.getPackage();
        return pkg == null                                  // no package
            || pkg.getName().equals(packageName)            // same package
            || IMPLICIT_PACKAGES.contains(pkg.getName());   // implicit package
    }

    boolean isEmpty() {
        return classes.isEmpty();
    }

    Stream<Class<?>> stream() {
        return classes.stream();
    }

    boolean isImported(Class<?> clz) {
        return isImplicit(clz) || classes.contains(clz);
    }

}
