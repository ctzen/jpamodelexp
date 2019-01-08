package com.ctzen.jpamodelexp;

import com.ctzen.jpamodelexp.jpa.AnnotatedClassesJpaMetamodelBuilder;
import com.ctzen.jpamodelexp.pkgscan.AnnotatedJpaClassesPackageScanner;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import org.testng.Reporter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author cchang
 */
@Test
public class JpaModelExporterTest {

    private static final String EXPORT_PATH = "build/generated";

    private static final String ANNOTATION_PROCESSOR_GENERATED_PATH = "build/classes/java/test";

    private static final String[] EXPORT_PACKAGES = new String[] {
            "com.ctzen.jpamodelexp.entity"
    };

    private static final String LINE_SEP = "\n";

    private static final String METAMODEL_SOURCE_SUFFIX = "_.java";

    private static String relativePath(File dir, File file) {
        return file.getAbsolutePath().substring(dir.getAbsolutePath().length() + 1);
    }

    @BeforeClass
    public void setup() throws IOException {
        expDir = new File(EXPORT_PATH).getCanonicalFile();
        apgDir = new File(ANNOTATION_PROCESSOR_GENERATED_PATH).getCanonicalFile();
    }

    private File expDir;                                    // exporter generated JPA metamodel source base dir
    private final Set<File> expFiles = new TreeSet<>();     // exporter generated JPA metamodel source files

    private File apgDir;                                    // annotation processor generated JPA metamodel source base dir
    private final Set<File> apgFiles = new TreeSet<>();     // annotation processor generated JPA metamodel source files

    public void export() {
        AnnotatedJpaClassesPackageScanner scanner = new AnnotatedJpaClassesPackageScanner();
        scanner.addPackages(EXPORT_PACKAGES);
        AnnotatedClassesJpaMetamodelBuilder jmb = new AnnotatedClassesJpaMetamodelBuilder();
        jmb.addAnnotatedClasses(scanner.scan());
        JpaModelExporter exporter = new JpaModelExporter(expDir, jmb.build());
        exporter.setLineSep(LINE_SEP);
        exporter.export();
    }

    @Test(dependsOnMethods = "export")
    public void gather() {
        gatherMetamodelFiles(apgFiles, apgDir);
        reportGatheredFiles(apgFiles, apgDir, "annotation processor");
        gatherMetamodelFiles(expFiles, expDir);
        reportGatheredFiles(expFiles, expDir, "exporter");
        assertGatheredFiles(apgFiles, "annotation processor");
        assertGatheredFiles(expFiles, "exporter");
    }

    private void gatherMetamodelFiles(Set<File> files, File dir) {
        if (dir.isDirectory()) {
            File[] metamodelFiles = dir.listFiles(file -> {
                String name = file.getName();
                return !name.startsWith(".") && (file.isDirectory() || name.endsWith(METAMODEL_SOURCE_SUFFIX));
            });
            assertThat(metamodelFiles)
                    .as("Failed to gather metamodel source files from: %s", dir)
                    .isNotNull();
            Arrays.stream(metamodelFiles).forEach(file -> {
                if (file.isDirectory()) {
                    gatherMetamodelFiles(files, file);
                }
                else {
                    files.add(file);
                }
            });
        }
    }

    private void reportGatheredFiles(Set<File> files, File baseDir, String type) {
        Reporter.log(String.format("Number of %s generated metamodel source files in '%s': %d", type, baseDir, files.size()));
        int i = 0;
        for (File file : files) {
            Reporter.log(String.format("  %2d. %s", ++i, relativePath(baseDir, file)));
        }
    }

    private void assertGatheredFiles(Set<File> files, String type) {
        assertThat(files)
                .as("No %s generated metamodel source file", type)
                .isNotEmpty();
    }

    @DataProvider(name = "matchesData")
    public Iterator<Object[]> matchesData() {
        return new Iterator<Object[]>() {
            @Override
            public boolean hasNext() {
                return !apgFiles.isEmpty() || !expFiles.isEmpty();
            }
            @Override
            public Object[] next() {
                File apgFile = apgFiles.isEmpty() ? null : pop(apgFiles);
                File expFile = apgFile == null ? pop(expFiles) : findExpFile(apgFile);
                return new Object[] { apgFile, expFile };
            }
        };
    }

    private File findExpFile(File apgFile) {
        String apgPath = relativePath(apgDir, apgFile);
        File expFile = expFiles.stream()
                .filter(file -> apgPath.equals(relativePath(expDir, file)))
                .findAny().orElse(null);
        if (expFile != null) {
            expFiles.remove(expFile);
        }
        return expFile;
    }

    private File pop(Set<File> files) {
        Iterator<File> i = files.iterator();
        File file = i.next();
        i.remove();
        return file;
    }

    @Test(dependsOnMethods = "gather", dataProvider = "matchesData")
    public void matches(File apgFile, File expFile) {
        String relPath = apgFile == null
                ? relativePath(expDir, expFile)
                : relativePath(apgDir, apgFile);
        assertExists(new File(apgDir, relPath), "Annotation processor");
        assertExists(new File(expDir, relPath), "Exporter");
        assert apgFile != null; // apgFile cannot be null at this point, this assert suppresses IDEA inspection warning.
        List<String> apgLines = prepareMetamodelSource(fileLines(apgFile), null);
        List<String> expSrc = new LinkedList<>();
        List<String> expLines = prepareMetamodelSource(fileLines(expFile), expSrc);
        assertThat(expLines)
                .as("Mismatch: %s", relPath)
                .isEqualTo(apgLines);
        Reporter.log(Joiner.on('\n').join(expSrc));
    }

    private void assertExists(File file, String type) {
        assertThat(file)
                .as("%s generated metamodel source file missing", type)
                .exists();
    }

    private Stream<String> fileLines(File file) {
        try {
            return Files.lines(file.toPath());
        }
        catch (IOException e) {
            fail("Load file failed: %s (%s)", file, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static final Set<String> IGNORE_SOURCE_LINE_PREFIXES = ImmutableSet.of(
            "@Generated("
    );

    private List<String> prepareMetamodelSource(Stream<String> lineStream, List<String> src) {
        List<String> lines = new LinkedList<>();
        lineStream.forEach(line -> {
            if (src != null) {
                src.add(line);
            }
            line = line.trim();
            if (!line.isEmpty() && IGNORE_SOURCE_LINE_PREFIXES.stream().noneMatch(line::startsWith)) {
                lines.add(line);
            }
        });
        lines.sort(this::compareSrcLines);
        return lines;
    }

    private static final Set<String> SORT_BLOCK_PREFIXES = ImmutableSet.of(
            "import ",
            "public static volatile ",
            "public static final String "
    );

    private int compareSrcLines(String line1, String line2) {
        for (String prefix: SORT_BLOCK_PREFIXES) {
            if (line1.startsWith(prefix) && line2.startsWith(prefix)) {
                return line1.compareTo(line2);
            }
        }
        return 0;
    }

}
