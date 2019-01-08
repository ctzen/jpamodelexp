package com.ctzen.jpamodelexp;

import java.util.stream.Collectors;

/**
 * Generates JPA static metamodel from a {@link JpaModel}.
 *
 * @author cchang
 */
class JpaMetamodelSourcerer {

    JpaMetamodelSourcerer(JpaModel model, String lineSep) {
        this.model = model;
        this.lineSep = lineSep;
    }

    private final JpaModel model;

    private final String lineSep;

    String generate() {
        sb = new StringBuilder();
        // package
        if (model.hasPackage()) {
            addln("package %s;", model.getPackageName());
        }
        // imports
        JpaModelImports imports = model.getImports();
        if (!imports.isEmpty()) {
            if (model.hasPackage()) {
                ln();
            }
            imports.stream().forEach(clz -> addln("import %s;", clz.getCanonicalName()));
        }
        // class declaration
        ln();
        addln("@Generated(\"%s\")", JpaModelExporter.class.getCanonicalName());
        addln("@StaticMetamodel(%s.class)", model.getJpaClass().getSimpleName());
        add("public abstract class %s", model.getSimpleName());
        if (model.hasExtendz()) {
            add(" extends %s", model.getExtendz().getCanonicalName());
        }
        addln(" {");
        // class body
        if (model.hasAttributes()) {
            // attributes
            ln();
            model.attributeStream().forEach(jma -> {
                add("    public static volatile %s", jma.getJpaClass().getSimpleName());
                if (jma.hasGenerics()) {
                    add("<%s>", jma.genericsStream()
                            .map(clz -> imports.isImported(clz) ? clz.getSimpleName() : clz.getCanonicalName())
                            .collect(Collectors.joining(", ")));
                }
                addln(" %s;", jma.getName());
            });
            // attribute names
            ln();
            model.attributeStream().forEach(jma ->
                    addln("    public static final String %s = \"%s\";", jma.getConstName(), jma.getName()));
        }
        // class end
        ln();
        addln("}");
        return sb.toString();
    }

    private StringBuilder sb;

    private void ln() {
        sb.append(lineSep);
    }

    private void add(String format, Object... args) {
        sb.append(String.format(format, args));
    }

    private void addln(String format, Object... args) {
        add(format, args);
        ln();
    }

}
