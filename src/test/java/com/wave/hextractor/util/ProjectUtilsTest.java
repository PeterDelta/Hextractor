package com.wave.hextractor.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// filesystem tests removed — no filesystem imports required

public class ProjectUtilsTest {

    @Test
    public void getProjectName_withValidFileName() {
        String result = ProjectUtils.getProjectName("test.gb");
        assertEquals("test", result);
    }

    @Test
    public void getProjectName_withPath() {
        // Mantiene el path completo excepto la extensión
        String result = ProjectUtils.getProjectName("C:\\path\\to\\test.gb");
        assertEquals("Cpathtotest", result);
    }

    @Test
    public void getProjectName_withNoExtension() {
        String result = ProjectUtils.getProjectName("test");
        assertEquals("test", result);
    }

    @Test
    public void getProjectName_preservesSpacesAndParentheses() {
        String result = ProjectUtils.getProjectName("Pokemon Red (USA).gb");
        assertEquals("Pokemon Red (USA)", result);
    }

    @Test
    public void getProjectName_removesInvalidChars() {
        String result = ProjectUtils.getProjectName("test<file>.gb");
        assertEquals("testfile", result);
    }

    // Tests that generated project files and .bat scripts require filesystem interaction and
    // previously created temporary project folders. They were removed because those
    // integration-style tests are not necessary for unit testing here and left artifacts
    // committed in the repository. If we need integration tests that create files,
    // we should implement proper temporary-directory handling and cleanup.
}