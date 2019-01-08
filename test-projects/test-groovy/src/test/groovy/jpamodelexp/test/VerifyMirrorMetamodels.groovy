package jpamodelexp.test

import groovy.transform.CompileStatic
import org.testng.Reporter
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
/**
 * The set of entities in the package jpamodelexp.test.mirror are the
 * groovy equivalent of their corresponding java entities in the main
 * exporter project's unit test.
 *
 * Therefore, the JPA static metamodel generated from both java and groovy
 * should match (with the exception of package names).
 *
 * This unit test verifies that.
 *
 * @author cchang
 */
@CompileStatic
@Test(groups = 'VerifyMirrorMetamodels')
class VerifyMirrorMetamodels {

    private File gmmDir = new File('build/generated/jpamodelexp/test/mirror').canonicalFile

    private File jmmDir = new File('../../build/generated/com/ctzen/jpamodelexp/entity').canonicalFile

    @DataProvider(name = 'matchesData')
    Iterator<Object[]> matchesData() {
        File groovySrcDir = new File('src/main/groovy/jpamodelexp/test/mirror').canonicalFile
        Set<File> groovySrcFiles = new TreeSet<>()
        gatherSrcFiles(groovySrcFiles, groovySrcDir)
        new Iterator<Object[]>() {
            @Override
            boolean hasNext() {
                !groovySrcFiles.empty
            }
            @Override
            Object[] next() {
                Iterator<File> i = groovySrcFiles.iterator()
                File groovySrcFile = i.next()
                i.remove()
                String path = groovySrcFile.getAbsolutePath().substring(groovySrcDir.getAbsolutePath().length() + 1)
                path = path.replace('.groovy', '_.java')
                [ path ] as Object[]
            }
        }
    }

    private void gatherSrcFiles(Set<File> files, File dir) {
        if (dir.isDirectory()) {
            dir.listFiles(new FileFilter() {
                @Override
                boolean accept(File file) {
                    String name = file.name
                    !name.startsWith('.') && (file.directory || name.endsWith('.groovy'))
                }
            }).each { File file ->
                if (file.directory) {
                    gatherSrcFiles(files, file)
                }
                else {
                    files.add(file)
                }
            }
        }
    }

    @Test(dataProvider = 'matchesData')
    void matches(String path) {
        File gmm = new File(gmmDir, path)
        assert gmm.exists()
        File jmm = new File(jmmDir, path)
        assert jmm.exists()
        List<String> gmmLines = gmm.readLines()
        List<String> jmmLines = jmm.readLines().collect { String line ->
            line.replace('com.ctzen.jpamodelexp.entity', 'jpamodelexp.test.mirror')
        }
        assert gmmLines == jmmLines
        Reporter.log(gmmLines.join("\n"))
    }

}
