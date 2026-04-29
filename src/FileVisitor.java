import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class FileVisitor extends SimpleFileVisitor<Path> {
    private Path startPath;
    private final PrintWriter writer;
    private Map<String, String> mapSnapshot;
    private Criptare criptare = new Criptare();

    // --- ASTA E PARTEA NOUĂ ---
    // Aici ținem minte fișierele care nu erau în snapshot-ul vechi
    private Map<String, String> fisiereNoi = new HashMap<>();

    public FileVisitor(Path startPath, PrintWriter writer, Map<String, String> mapSnapshot) {
        this.startPath = startPath;
        this.writer = writer;
        this.mapSnapshot = mapSnapshot;
    }

    public Map<String, String> getFisiereNoi() {
        return fisiereNoi;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        String relativePath = startPath.relativize(file).toString();
        String hashActual = criptare.calculeazaHash(file);

        String VERDE = "\u001B[32m";
        String ROSU = "\u001B[31m";
        String RESET = "\u001B[0m";

        if (mapSnapshot.containsKey(relativePath)) {
            String hashVechi = mapSnapshot.get(relativePath);

            if (hashActual.equals(hashVechi)) {
                System.out.println("[" + VERDE + "OK" + RESET + "] " + relativePath);
            } else {
                System.out.println("[" + ROSU + "MODIFICAT!!!" + RESET + "] " + relativePath);
            }
            mapSnapshot.remove(relativePath);
        } else {
            fisiereNoi.put(relativePath, hashActual);
        }


        writer.println(relativePath + "|" + hashActual);

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

        int nivel = dir.getNameCount() - startPath.getNameCount();
        String spatii = "  ".repeat(nivel);

        if (dir.equals(startPath)) {
            IO.println("[ROOT] " + dir.getFileName());
        } else {
            IO.println(spatii + "└── [D] " + dir.getFileName());
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException e) {
        System.err.println("Eroare accesare: " + file.getFileName());
        return FileVisitResult.CONTINUE;
    }
}