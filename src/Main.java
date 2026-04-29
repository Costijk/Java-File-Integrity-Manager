import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        String pathInput = IO.readln("Introdu calea directorului: ");
        Path pathTarget = Paths.get(pathInput).toAbsolutePath().normalize();

        if (Files.exists(pathTarget) && Files.isDirectory(pathTarget)) {
            proceseazaDirector(pathTarget);
        } else {
            System.out.println("Eroare: Calea nu este un director!");
        }
    }

    public static void proceseazaDirector(Path target) throws IOException {
        Path snapshotPath = getSnapshotPath(target);
        Map<String, String> oldData = incarcaSnapshot(snapshotPath);

        FileVisitor visitor = new FileVisitor(target, oldData);
        Files.walkFileTree(target, visitor);

        Map<String, String> noi = visitor.getFisiereNoi();
        analyzeChanges(noi, oldData, target);

        salveazaSnapshot(snapshotPath, visitor.getScanareCurenta());
    }

    public static Path getSnapshotPath(Path folder) {
        Path dirSnapshots = Paths.get("snapshots");
        if (!Files.exists(dirSnapshots)) {
            try { Files.createDirectories(dirSnapshots); } catch (IOException ignored) {}
        }
        String name = folder.toAbsolutePath().normalize().toString()
                .replace("/", ".").replace("\\", ".").replace(":", "");
        if (name.startsWith(".")) name = name.substring(1);
        return dirSnapshots.resolve(name + ".txt");
    }

    private static Map<String, String> incarcaSnapshot(Path path) throws IOException {
        Map<String, String> data = new HashMap<>();
        if (!Files.exists(path)) return data;
        for (String linie : Files.readAllLines(path)) {
            String[] parti = linie.split("\\|");
            if (parti.length == 2) data.put(parti[0], parti[1]);
        }
        return data;
    }

    private static void salveazaSnapshot(Path path, Map<String, String> date) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(path.toFile()))) {
            date.forEach((k, v) -> writer.println(k + "|" + v));
        }
    }

    private static void analyzeChanges(Map<String, String> noi, Map<String, String> sterse, Path startPath) {
        String GALBEN = "\u001B[33m", ALBASTRU = "\u001B[34m", ROSU = "\u001B[31m", RESET = "\u001B[0m";
        Iterator<Map.Entry<String, String>> it = noi.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, String> n = it.next();
            String vPath = null;
            for (Map.Entry<String, String> s : sterse.entrySet()) {
                if (s.getValue().equals(n.getValue())) { vPath = s.getKey(); break; }
            }
            if (vPath != null) {
                System.out.println(GALBEN + "[REDENUMIT] " + RESET + startPath.relativize(Paths.get(vPath)) + " -> " + startPath.relativize(Paths.get(n.getKey())));
                sterse.remove(vPath);
                it.remove();
            }
        }

        noi.forEach((k, v) -> System.out.println(ALBASTRU + "[NOU] " + RESET + startPath.relativize(Paths.get(k))));
        sterse.forEach((k, v) -> System.out.println(ROSU + "[STERS] " + RESET + startPath.relativize(Paths.get(k))));
    }
}