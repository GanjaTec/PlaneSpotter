package test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import planespotter.constants.Paths;
import planespotter.model.io.FileWizard;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileWizardTest {

    private FileWizard fileWizard;

    @BeforeEach
    void setUp() {
        fileWizard = FileWizard.getFileWizard();
    }

    @AfterEach
    void tearDown() {
        File logs = new File("logs\\");
        File[] files = logs.listFiles();
        if (files != null) {
            String name;
            for (File file : files) {
                name = file.getName();
                if (name.startsWith("test_log")) {
                    file.delete();
                }
            }
        }
    }

    @Test
    void saveConfig() {
        assertDoesNotThrow(() -> fileWizard.saveConfig());
    }

    @Test
    void loadPlsFile() {
    }

    @Test
    void savePlsFile() {

    }

    @Test
    void saveLogFile() {
        assertDoesNotThrow(() -> fileWizard.saveLogFile("test_log", "das ist ein test-log!\nnoch eine Zeile!"));
    }

    @Test
    void getFileWizard() {
        assertNotNull(FileWizard.getFileWizard());
    }
}