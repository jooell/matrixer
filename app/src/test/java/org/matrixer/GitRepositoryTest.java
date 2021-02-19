package org.matrixer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.*;

class GitRepositoryTest {

    final static String TEST_REPO_URL = "https://github.com/ikristola/matrixer-test";
    final static String TMP_DIR = System.getProperty("java.io.tmpdir");
    final static Path TARGET_DIR = Path.of(TMP_DIR, File.separator, "matrixer-test");

    @BeforeAll
    static void cleanUp() {
        System.out.println("Cleaning up!");
        removeDirectory(TARGET_DIR);
    }

    @AfterEach
    void cleanUpAfter() {
        System.out.println("Cleaning up");
        removeDirectory(TARGET_DIR);
    }

    @Test
    void throwsExceptionIfTargetAlreadyExists() {
        createDirectory(TARGET_DIR);
        File target = temporaryTargetFile();
        assertThrows(Exception.class, () -> GitRepository.clone(TEST_REPO_URL, target));
    }

    @Test
    void downloadsRepositoryIfURLisValid() {
        File target = temporaryTargetFile();
        assertDoesNotThrow(() -> GitRepository.clone(TEST_REPO_URL, target));
        assertTrue(directoryContainsFile(target, ".git"));
    }

    @Test
    void throwsExceptionIfURLIsNotAGitRepository() {
        File target = temporaryTargetFile();
        assertThrows(GitAPIException.class, () -> GitRepository.clone("localhost", target));
    }

    @Test
    void canCloneLocalGitRepository() {
        File target = temporaryTargetFile();
        String projectDir = projectDirectory().toString();
        assertDoesNotThrow(() -> GitRepository.clone(projectDir, target));
        assertTrue(directoryContainsFile(target, ".git"));
    }

    @Test
    void throwsExceptionIfLocalPathIsNotAGitDirectory() {
        File target = temporaryTargetFile();
        assertThrows(GitAPIException.class, () -> GitRepository.clone(TMP_DIR, target));
    }

    static File projectDirectory() {
        return new File(System.getProperty("user.dir")).getParentFile();
    }

    static File temporaryTargetFile() {
        return TARGET_DIR.toFile();
    }

<<<<<<< HEAD
    private void createDirectory(Path dir) {
        FileUtils.createDirectory(dir);
=======
    private static void createDirectory(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
>>>>>>> 54b19bf (Clean up beforeall and after each in GitRepositoryTest)
    }

    private static boolean directoryContainsFile(File dir, String fname) {
        return new File(dir, fname).exists();
    }

    private static void removeDirectory(Path dir) {
        try {
            if (dir.toFile().exists()) {
                Files.walk(dir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (IOException e) {
            throw new RuntimeException("Clean up: Failed to remove dir: " + e.getMessage());
        }
    }
}
