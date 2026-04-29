import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class FileVisitor extends SimpleFileVisitor<Path> {
    private final Path startPath;
    private final Map<String, String> dateVechi;
    private final Map<String, String> fisiereNoi = new HashMap<>();
    private final Map<String, String> scanareCurenta = new HashMap<>();
    private final Criptare criptare = new Criptare();

    public int countOK = 0;
    public int countModificat = 0;

    public FileVisitor(Path startPath, Map<String, String> dateVechi) {
        this.startPath = startPath;
        this.dateVechi = dateVechi;
    }

    public Map<String, String> getFisiereNoi() { return fisiereNoi; }
    public Map<String, String> getScanareCurenta() { return scanareCurenta; }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.equals(startPath)) return FileVisitResult.CONTINUE;

        Path subSnap = Main.getSnapshotPath(dir);

        if (Files.exists(subSnap)) {
            Main.proceseazaDirector(dir);
            String subDirPath = dir.toAbsolutePath().normalize().toString() + java.io.File.separator;
            dateVechi.keySet().removeIf(key -> key.startsWith(subDirPath));
            return FileVisitResult.SKIP_SUBTREE;
        }

        int nivel = dir.getNameCount() - startPath.getNameCount();
        System.out.println("  ".repeat(nivel) + "\u001B[34m└── [D] " + dir.getFileName() + "\u001B[0m");
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        String absPath = file.toAbsolutePath().normalize().toString();
        String relPath = startPath.relativize(file).toString();
        String hash = criptare.calculeazaHash(file);

        scanareCurenta.put(absPath, hash);

        if (dateVechi.containsKey(absPath)) {
            if (hash.equals(dateVechi.get(absPath))) {
                System.out.println("[\u001B[32mOK\u001B[0m] " + relPath);
                countOK++;
            } else {
                System.out.println("[\u001B[31mMODIFICAT\u001B[0m] " + relPath);
                countModificat++;
            }
            dateVechi.remove(absPath);
        } else {
            fisiereNoi.put(absPath, hash);
        }
        return FileVisitResult.CONTINUE;
    }
}