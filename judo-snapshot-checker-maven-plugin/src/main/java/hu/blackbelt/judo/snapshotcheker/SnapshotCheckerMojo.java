package hu.blackbelt.judo.snapshotcheker;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Mojo(name = "checkSnapshots", defaultPhase = LifecyclePhase.VERIFY)
public class SnapshotCheckerMojo extends AbstractMojo {

    @Parameter(property = "sources")
    private List<String> sources;

    @Parameter(property = "snapshotPostfix", defaultValue = ".snapshot")
    private String snapshotPostfix = ".snapshot";

    @Parameter(property = "forceSnapshotOverwrite", defaultValue = "false")
    private Boolean forceSnapshotOverwrite = false;

    @Parameter(property = "sourceDirectory", required = true)
    private String sourceDirectory;

    @Parameter(property = "snapshotDirectory", required = true)
    private String snapshotDirectory;

    @Parameter(property = "createSnapshotIfNotExists", defaultValue = "true")
    private Boolean createSnapshotIfNotExists = true;

    private static final int CONTEXT_SIZE = 3;

    @Override
    public void execute() throws MojoExecutionException {
        Path sourcePath = Paths.get(sourceDirectory);
        Path snapshotPath = Paths.get(snapshotDirectory);

        if (sources == null || sources.isEmpty()) {
            throw new MojoExecutionException("No sources configured for snapshot checking.");
        }

        for (String source : sources) {
            Path sourceFilePath = sourcePath.resolve(source);
            Path snapshotFilePath = snapshotPath.resolve(source + snapshotPostfix);
            File sourceFile = sourceFilePath.toFile();
            File snapshotFile = snapshotFilePath.toFile();

            if (!sourceFile.exists()) {
                throw new MojoExecutionException("Source file does not exist: " + sourceFile.getPath());
            }

            try {
                if (!snapshotFile.exists()) {
                    if (createSnapshotIfNotExists) {
                        createSnapshotFileFromSource(sourceFile, snapshotFile);
                        getLog().info("Created snapshot file: " + snapshotFile.getPath());
                    } else {
                        throw new MojoExecutionException("Snapshot file does not exist: " + snapshotFile.getPath());
                    }
                } else {
                    if (forceSnapshotOverwrite) {
                        createSnapshotFileFromSource(sourceFile, snapshotFile);
                        getLog().info("Overwritten snapshot file: " + snapshotFile.getPath());
                    }
                    compareFiles(sourceFile, snapshotFile);
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Error processing files: " + sourceFile.getPath() + " and " + snapshotFile.getPath(), e);
            }
        }
    }

    private void createSnapshotFileFromSource(File sourceFile, File snapshotFile) throws IOException {
        Files.createDirectories(snapshotFile.getParentFile().toPath());
        Files.copy(sourceFile.toPath(), snapshotFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void compareFiles(File sourceFile, File snapshotFile) throws IOException, MojoExecutionException {
        List<String> sourceLines = Files.readAllLines(sourceFile.toPath());
        List<String> snapshotLines = Files.readAllLines(snapshotFile.toPath());

        Patch<String> diff = DiffUtils.diff(snapshotLines, sourceLines);

        if (!diff.getDeltas().isEmpty()) {
            displayGitLikeDiff(snapshotLines, sourceLines, "[SNAPSHOT] " + snapshotFile.getPath().substring(snapshotDirectory.length()), "[SOURCE] " + sourceFile.getPath().substring(sourceDirectory.length()));
            throw new MojoExecutionException("Snapshot changes detected in " + sourceFile.getPath());
        }
    }

    private void displayGitLikeDiff(List<String> original, List<String> revised, String snapshotPath, String sourcePath) {
        Patch<String> patch = DiffUtils.diff(original, revised);
        List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(snapshotPath, sourcePath, original, patch, CONTEXT_SIZE);
        for (String line : unifiedDiff) {
            System.out.println(line);
        }
    }

    public static class FilePair {
        private String sourceFile;
        private String snapshotFile;

        public String getSourceFile() {
            return sourceFile;
        }

        public void setSourceFile(String sourceFile) {
            this.sourceFile = sourceFile;
        }

        public String getSnapshotFile() {
            return snapshotFile;
        }

        public void setSnapshotFile(String snapshotFile) {
            this.snapshotFile = snapshotFile;
        }
    }
}
