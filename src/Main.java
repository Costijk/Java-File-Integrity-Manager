import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.*;

public class Main {
    private Map<String, String> pushSnapshot(String fileName) {
        Map<String, String> savedData = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(fileName));
            for (String line : lines) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    savedData.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            IO.println("Nu s-a gasit un snapshot anterior~ Se va crea unul nou.");
        }

        return savedData;
    }

    void main() throws IOException {
        String pathInput = IO.readln("Introdu calea directorului de scanat: ");
        Path path = Paths.get(pathInput);

        // Configurare culori ANSI
        String VERDE = "\u001B[32m";
        String ROSU = "\u001B[31m";
        String GALBEN = "\u001B[33m";
        String ALBASTRU = "\u001B[34m";
        String RESET = "\u001B[0m";

        String folderSnapshots = "snapshots";
        Path dirSnapshots = Paths.get(folderSnapshots);

        if (!Files.exists(dirSnapshots)) {
            Files.createDirectories(dirSnapshots);
            IO.println("Directorul '" + folderSnapshots + "' a fost creat.");
        }

        String snapshotName = pathInput.replace('/', '.').replace('\\', '.');
        if (snapshotName.startsWith(".")) snapshotName = snapshotName.substring(1);

        Path snapshotFilePath = dirSnapshots.resolve(snapshotName + ".txt");
        String finalSnapshotPath = snapshotFilePath.toString();

        if (Files.exists(path) && Files.isDirectory(path)) {
            Map<String, String> oldMapSnapshot = pushSnapshot(finalSnapshotPath);

            try (PrintWriter writer = new PrintWriter(new FileWriter(finalSnapshotPath))) {
                IO.println("\n" + ALBASTRU + "--- Începe Verificarea de Integritate ---" + RESET);

                FileVisitor visitor = new FileVisitor(path, writer, oldMapSnapshot);
                Files.walkFileTree(path, visitor);

                Map<String, String> fisiereNoi = visitor.getFisiereNoi();

                IO.println("\n" + ALBASTRU + "--- Analiză Finală ---" + RESET);

                Iterator<Map.Entry<String, String>> itNoi = fisiereNoi.entrySet().iterator();
                while (itNoi.hasNext()) {
                    Map.Entry<String, String> nou = itNoi.next();
                    String caleVeche = null;
                    for (Map.Entry<String, String> vechi : oldMapSnapshot.entrySet()) {
                        if (vechi.getValue().equals(nou.getValue())) {
                            caleVeche = vechi.getKey();
                            break;
                        }
                    }

                    if (caleVeche != null) {
                        IO.println("[" + GALBEN + "REDENUMIT/MUTAT" + RESET + "] " + caleVeche + " -> " + nou.getKey());
                        oldMapSnapshot.remove(caleVeche);
                        itNoi.remove();
                    }
                }
                for (String nume : fisiereNoi.keySet()) {
                    IO.println("[" + ALBASTRU + "NOU" + RESET + "] " + nume);
                }

                if (!oldMapSnapshot.isEmpty()) {
                    for (String fisierSters : oldMapSnapshot.keySet()) {
                        IO.println("[" + ROSU + "STERS" + RESET + "] " + fisierSters);
                    }
                }

                IO.println("\n");
            }
        }
    }
}