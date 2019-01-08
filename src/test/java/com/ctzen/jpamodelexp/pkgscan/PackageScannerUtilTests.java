package com.ctzen.jpamodelexp.pkgscan;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author cchang
 */
@Test
public class PackageScannerUtilTests {

    @DataProvider(name = "sanitizePathData")
    public Object[][] sanitizePathData() {
        return new Object[][] {
                { "", false, "" },
                { "", true, "" },
                { "/", false, "/" },
                { "/", true, "" },
                { "\\", false, "/" },
                { "\\", true, "" },
                { "x", false, "x" },
                { "x", true, "x" },
                { "a/b/c", false, "a/b/c" },
                { "a/b/c", true, "a/b/c" },
                { "a\\b\\c", false, "a/b/c" },
                { "a\\b\\c", true, "a/b/c" },
                { "/a/b/c", false, "/a/b/c" },
                { "/a/b/c", true, "a/b/c" },
                { "\\a\\b\\c", false, "/a/b/c" },
                { "\\a\\b\\c", true, "a/b/c" },
                { "/a/b/c/", false, "/a/b/c/" },
                { "/a/b/c/", true, "a/b/c/" },
                { "\\a\\b\\c\\", false, "/a/b/c/" },
                { "\\a\\b\\c\\", true, "a/b/c/" },
                { "/a\\b/c\\", false, "/a/b/c/" },
                { "\\a/b\\c/", true, "a/b/c/" },
        };
    }

    @Test(dataProvider = "sanitizePathData")
    public void sanitizePath(String path, boolean stripInitialPathSep, String expected) {
        assertThat(PackageScannerUtil.sanitizePath(path, stripInitialPathSep))
                .isEqualTo(expected);
    }

    @DataProvider(name = "maybeJpaClassFileData")
    public Object[][] maybeJpaClassFileData() {
        return new Object[][] {
            { "", false },
            { ".class", true },
            { "_.class", false },
            { "X.class", true },
            { "/a/b/X.class", true },
        };
    }

    @Test(dataProvider = "maybeJpaClassFileData")
    public void maybeJpaClassFile(String path, boolean expected) {
        assertThat(PackageScannerUtil.maybeJpaClassFile(path))
                .isEqualTo(expected);
    }

    @DataProvider(name = "maybeJpaClassFileInPackageData")
    public Object[][] maybeJpaClassFileInPackageData() {
        return new Object[][] {
            { "a/b/X.class", "a.b", true },
            { "/a/b/X.class", "a.b", true },
            { "a/b/X.class", "a", true },
            { "a/b/X.class", "a.c", false },
            { "a/bc/X.class", "a.b", false },
        };
    }

    @Test(dataProvider = "maybeJpaClassFileInPackageData")
    public void maybeJpaClassFileInPackage(String path, String packageName, boolean expected) {
        String packagePathPrefix =  PackageScannerUtil.packageNameToPathPrefix(packageName);
        assertThat(PackageScannerUtil.maybeJpaClassFileInPackage(path, packagePathPrefix))
                .isEqualTo(expected);
    }

}
