import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class FileVisitor extends SimpleFileVisitor<Path> {
    private final Path startPath;
    private final Map<String, String> oldData;
    private final Map<String, String> newFiles = new HashMap<>();
    private final Map<String, String> currentScan = new HashMap<>();
    private final HashCalculator hashCalculator = new HashCalculator();

    public int countOK = 0;
    public int countModified = 0;

    public FileVisitor(Path startPath, Map<String, String> oldData) {
        this.startPath = startPath;
        this.oldData = oldData;
    }

    public Map<String, String> getNewFiles() { return newFiles; }
    public Map<String, String> getCurrentScan() { return currentScan; }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.equals(startPath)) return FileVisitResult.CONTINUE;

        Path subSnapshot = Main.getSnapshotPath(dir);

        if (Files.exists(subSnapshot)) {
            Main.processDirectory(dir);
            String subDirPath = dir.toAbsolutePath().normalize().toString() + java.io.File.separator;
            oldData.keySet().removeIf(key -> key.startsWith(subDirPath));
            return FileVisitResult.SKIP_SUBTREE;
        }

        int level = dir.getNameCount() - startPath.getNameCount();
        System.out.println("  ".repeat(level) + "\u001B[34m└── [D] " + dir.getFileName() + "\u001B[0m");
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        String absPath = file.toAbsolutePath().normalize().toString();
        String relPath = startPath.relativize(file).toString();
        String hash = hashCalculator.calculateHash(file);

        currentScan.put(absPath, hash);

        if (oldData.containsKey(absPath)) {
            if (hash.equals(oldData.get(absPath))) {
                System.out.println("[\u001B[32mOK\u001B[0m] " + relPath);
                countOK++;
            } else {
                System.out.println("[\u001B[31mMODIFIED\u001B[0m] " + relPath);
                countModified++;
            }
            oldData.remove(absPath);
        } else {
            newFiles.put(absPath, hash);
        }
        return FileVisitResult.CONTINUE;
    }
}