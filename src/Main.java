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

    void main() {

        String pathInput = IO.readln("Introdu calea directorului: ");
        Path path = Paths.get(pathInput);

            if (Files.exists(path) && Files.isDirectory(path)) {
                boolean isNewPath = false;
                try (Scanner fileReader = new Scanner(new File("lastDirectory.txt"))){
                    Path oldPath = Paths.get(fileReader.nextLine());
                    IO.println(oldPath);
                    if(path.equals(oldPath)){
                        IO.println("Calea nu a fost schimbata");
                    }
                    else {
                        try(FileWriter fileWriter = new FileWriter("lastDirectory.txt")){
                            fileWriter.write(pathInput);
                            isNewPath = true;
                        }
                    }
                } catch (Exception e){
                    IO.println(e.getMessage());
                }
                Map<String, String> oldMapSnapshot = pushSnapshot("snapshot.txt");
                try (PrintWriter writer = new PrintWriter(new FileWriter("snapshot.txt"))) {
                    Files.walkFileTree(path, new FileVisitor(path, writer, oldMapSnapshot));
                    if (!oldMapSnapshot.isEmpty() && !isNewPath) {
                        System.out.println("\n--- FISIERE STERSE ---");
                        for (String fisierSters : oldMapSnapshot.keySet()) {
                            System.out.println("[STERS] " + fisierSters);
                        }
                    }
                    System.out.println("Snapshot salvat cu succes în snapshot.txt!");
                } catch (IOException e) {
                    IO.println("Nu a reusit scanarea: " + e.getMessage());
                }
            } else {
                IO.println("Calea nu este valida!");
            }

    }
}