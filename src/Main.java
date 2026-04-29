import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {

    void main() throws IOException {
        String pathInput = IO.readln("Introdu calea directorului: ");
        Path pathTarget = Paths.get(pathInput).toAbsolutePath().normalize();

        Path dirSnapshots = Paths.get("snapshots");
        if (!Files.exists(dirSnapshots)) Files.createDirectories(dirSnapshots);


        String snapshotName = pathTarget.toString().replace('/', '.').replace(':', '.');
        if (snapshotName.startsWith(".")) snapshotName = snapshotName.substring(1);
        Path currentSnapshotPath = dirSnapshots.resolve(snapshotName + ".txt");


        Map<String, String> oldData = incarcaDateInteligente(pathTarget, dirSnapshots);

        if (Files.exists(pathTarget) && Files.isDirectory(pathTarget)) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(currentSnapshotPath.toFile()))) {

                FileVisitor visitor = new FileVisitor(pathTarget, writer, oldData);
                Files.walkFileTree(pathTarget, visitor);
                Map<String, String> noi = visitor.getFisiereNoi();
                analyzeChanges(noi, oldData);
                printSummary(visitor.countOK, visitor.countModificat, noi.size(), oldData.size());

                System.out.println("\nSnapshot salvat: " + currentSnapshotPath);
            }
        } else {
            System.out.println("Director invalid!");
        }
    }

    private Map<String, String> incarcaDateInteligente(Path target, Path folderSnap) throws IOException {
        File[] files = folderSnap.toFile().listFiles((d, n) -> n.endsWith(".txt"));
        if (files == null) return new HashMap<>();

        File bestFit = null;
        String targetStr = target.toString().replace('/', '.').replace(':', '.');
        if (targetStr.startsWith(".")) targetStr = targetStr.substring(1);

        for (File f : files) {
            String snapName = f.getName().replace(".txt", "");
            if (targetStr.startsWith(snapName)) {
                if (bestFit == null || snapName.length() > bestFit.getName().length()) {
                    bestFit = f;
                }
            }
        }

        if (bestFit != null) {
            System.out.println("\u001B[35m[INFO] Date moștenite din: " + bestFit.getName() + "\u001B[0m");
            return adaptSnapshot(bestFit.toPath(), target);
        }
        return new HashMap<>();
    }

    private Map<String, String> adaptSnapshot(Path snapFile, Path target) throws IOException {
        Map<String, String> adapted = new HashMap<>();
        String snapRootStr = snapFile.getFileName().toString().replace(".txt", "").replace('.', '/');
        if (!snapRootStr.startsWith("/")) snapRootStr = "/" + snapRootStr;
        Path snapRoot = Paths.get(snapRootStr);

        for (String line : Files.readAllLines(snapFile)) {
            String[] parts = line.split("\\|");
            if (parts.length == 2) {
                Path fileAbs = snapRoot.resolve(parts[0]);
                if (fileAbs.startsWith(target)) {
                    adapted.put(target.relativize(fileAbs).toString(), parts[1]);
                }
            }
        }
        return adapted;
    }

    private void analyzeChanges(Map<String, String> noi, Map<String, String> sterse) {
        Iterator<Map.Entry<String, String>> it = noi.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> n = it.next();
            String vechiPath = null;
            for (Map.Entry<String, String> s : sterse.entrySet()) {
                if (s.getValue().equals(n.getValue())) {
                    vechiPath = s.getKey();
                    break;
                }
            }
            if (vechiPath != null) {
                System.out.println("\u001B[33m[REDENUMIT] " + vechiPath + " -> " + n.getKey() + "\u001B[0m");
                sterse.remove(vechiPath);
                it.remove();
            }
        }
        noi.forEach((k, v) -> System.out.println("\u001B[34m[NOU] " + k + "\u001B[0m"));
        sterse.forEach((k, v) -> System.out.println("\u001B[31m[STERS] " + k + "\u001B[0m"));
    }

    private void printSummary(int ok, int mod, int noi, int sterse) {
        System.out.println("\n--- SUMAR INTEGRITATE ---");
        System.out.println("Intacte: " + ok + " | Modificate: " + mod + " | Noi: " + noi + " | Șterse: " + sterse);
    }
}