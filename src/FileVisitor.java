import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class FileVisitor extends SimpleFileVisitor<Path> {
    private Path startPath;
    private final PrintWriter writer;
    private Map<String, String> mapSnapshotVechi;
    private Map<String, String> fisiereNoi = new HashMap<>();
    private Criptare criptare = new Criptare();

    public int countOK = 0;
    public int countModificat = 0;

    public static final String RESET = "\u001B[0m";
    public static final String VERDE = "\u001B[32m";
    public static final String ROSU = "\u001B[31m";
    public static final String GALBEN = "\u001B[33m";
    public static final String ALBASTRU = "\u001B[34m";

    public FileVisitor(Path startPath, PrintWriter writer, Map<String, String> mapSnapshotVechi) {
        this.startPath = startPath;
        this.writer = writer;
        this.mapSnapshotVechi = mapSnapshotVechi;
    }

    public Map<String, String> getFisiereNoi() {
        return fisiereNoi;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        String relativePath = startPath.relativize(file).toString();
        String hashActual = criptare.calculeazaHash(file);

        if (mapSnapshotVechi.containsKey(relativePath)) {
            String hashVechi = mapSnapshotVechi.get(relativePath);

            if (hashActual.equals(hashVechi)) {
                System.out.println("[" + VERDE + "OK" + RESET + "] " + relativePath);
                countOK++;
            } else {
                System.out.println("[" + ROSU + "MODIFICAT!!!" + RESET + "] " + relativePath);
                countModificat++;
            }
            mapSnapshotVechi.remove(relativePath);
        } else {
            fisiereNoi.put(relativePath, hashActual);
        }

        writer.println(relativePath + "|" + hashActual);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        int nivel = dir.getNameCount() - startPath.getNameCount();
        String indent = "  ".repeat(Math.max(0, nivel));
        System.out.println(indent + ALBASTRU + "└── [D] " + dir.getFileName() + RESET);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException e) {
        System.err.println(ROSU + "[EROARE ACCES] " + RESET + file.getFileName());
        return FileVisitResult.CONTINUE;
    }
}