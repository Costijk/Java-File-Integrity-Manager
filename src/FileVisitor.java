import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import static java.nio.file.FileVisitResult.CONTINUE;

public class FileVisitor extends SimpleFileVisitor<Path> {
    private Path startPath;
    private final PrintWriter writer;
    private Map<String, String> mapSnapshot;
    Criptare criptare = new Criptare();

    public FileVisitor(Path startPath, PrintWriter writer, Map<String, String> mapSnapshot){
        this.startPath = startPath;
        this.writer = writer;
        this.mapSnapshot = mapSnapshot;
    }

    private void printCuIndentare(Path caleaCurenta, String prefix) {
        int nivel = caleaCurenta.getNameCount() - startPath.getNameCount();
        String spatii = "  ".repeat(nivel);

        IO.print(spatii + prefix + caleaCurenta.getFileName());
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        String relativePath = startPath.relativize(file).toString();
        String hashActual = criptare.calculeazaHash(file);
        String owner = "Unknown";
        try { owner = Files.getOwner(file).getName(); } catch (Exception e) {}
        if (mapSnapshot.containsKey(relativePath)) {
            String hashVechi = mapSnapshot.get(relativePath);

            if (hashActual.equals(hashVechi)) {
                System.out.println("[OK] " + relativePath + " (Owner: " + owner + ")");
            } else {
                System.out.println("[MODIFICAT!!!] " + relativePath + " (Atentie! Hash diferit)");
            }
            mapSnapshot.remove(relativePath);
        } else {
            System.out.println("[NOU] " + relativePath + " (Fisier adaugat recent)");
        }
        writer.println(relativePath + "|" + hashActual);

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        int nivel = dir.getNameCount() - startPath.getNameCount();
       // IO.print("  ".repeat(nivel) + "[D] " + dir.getFileName());
        //IO.println();
        return  FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException e){
        IO.println("Nu am voie sa intru aici: " + file.getFileName());

        return FileVisitResult.CONTINUE;
    }
}
