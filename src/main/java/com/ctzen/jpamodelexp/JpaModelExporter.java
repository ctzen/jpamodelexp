package com.ctzen.jpamodelexp;

import javax.persistence.metamodel.Metamodel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Pattern;

/**
 * JPA static metamodel exporter.
 *
 * Essentially reverse engineer {@link Metamodel#getManagedTypes()}
 * into JPA static metamodel sources.
 *
 * @author cchang
 */
public class JpaModelExporter {

    public JpaModelExporter(File exportDir, JpaMetamodel jpaMetamodel) {
        this.exportDir = exportDir;
        this.jpaMetamodel = jpaMetamodel;
        stdout("Export dir: %s", exportDir.getAbsolutePath());
    }

    private final File exportDir;

    private final JpaMetamodel jpaMetamodel;

    private String lineSep = System.getProperty("line.separator");

    /**
     * @return  line separator to use when exporting JPA static metamodel source files
     */
    public String getLineSep() {
        return lineSep;
    }

    /**
     * @param lineSep   explicitly set the line separator to use when exporting JPA static metamodel source files.
     *                  default is System.getProperty("line.separator")
     */
    public void setLineSep(String lineSep) {
        this.lineSep = lineSep;
    }

    private void stdout(String format, Object... args) {
        System.out.println(String.format(format, args));
    }

    private final Set<Pattern> filters = new HashSet<>();

    /**
     * Adds a regex filter.
     * Only classes' fqcn that matches the regex are exported.
     *
     * @param filter    regex filter
     */
    public void filter(Pattern filter) {
        filters.add(filter);
    }

    /**
     * Adds class name filters
     * Only classes's fqcn that matches the class names are exported.
     *
     * @param classNames    class names
     */
    public void filterClasses(Collection<String> classNames) {
        classNames.forEach(className -> filter(Pattern.compile(Pattern.quote(className))));
    }

    /**
     * Adds class name filters
     * Only classes's fqcn that matches the class names are exported.
     *
     * @param classNames    class names
     */
    public void filterClasses(String... classNames) {
        filterClasses(Arrays.asList(classNames));
    }

    /**
     * Adds package name filters
     * Only classes in the package names (or sub-packages) are exported.
     *
     * @param packageNames  package names
     */
    public void filterPackages(Collection<String> packageNames) {
        packageNames.forEach(packageName -> filter(Pattern.compile(Pattern.quote(packageName + ".") + ".+")));
    }

    /**
     * Adds package name filters
     * Only classes in the package names (or sub-packages) are exported.
     *
     * @param packageNames  package names
     */
    public void filterPackages(String... packageNames) {
        filterPackages(Arrays.asList(packageNames));
    }

    public void export() {
        try {
            createModels(jpaMetamodel.getMetamodel());
        }
        finally {
            jpaMetamodel.close();
        }
        models.stream()
                .filter(model -> filters.isEmpty()
                        || filters.stream().anyMatch(filter -> filter.matcher(model.getJpaClass().getCanonicalName()).matches()))
                .forEach(this::writeSource);
    }

    private final Set<JpaModel> models = new TreeSet<>();

    private JpaModel findModelByJpaClass(Class<?> clz) {
        return clz == null ? null : models.stream().filter(model -> model.isModelOf(clz)).findAny().orElse(null);
    }

    private void createModels(Metamodel metamodel) {
        metamodel.getManagedTypes().forEach(type -> models.add(new JpaModel(type)));
        models.forEach(this::processExtendz);
        models.forEach(JpaModel::finalizeModel);
    }

    private void processExtendz(JpaModel model) {
        Class<?> superclass = model.getJpaClass().getSuperclass();
        if (superclass != null) {
            model.setExtendz(findModelByJpaClass(superclass));
        }
    }

    private void writeSource(JpaModel model) {
        stdout("Export: %s", model.getCanonicalName());
        String src = new JpaMetamodelSourcerer(model, lineSep).generate();
        File file = new File(exportDir, model.getCanonicalName().replace('.', '/') + ".java");
        File dir = file.getParentFile();
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Make directories failed: " + dir);
            }
        }
        if (!dir.exists()) {
            throw new IllegalStateException("Need directory: " + dir);
        }
        try {
            Files.write(file.toPath(), src.getBytes(),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException e) {
            throw new RuntimeException("Write failed: " + file, e);
        }
    }

}
