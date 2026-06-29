import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {

    // Files where we save history and favorites
    private static final Path RECENTS_FILE = Paths.get("snapshots", "recents.txt");
    private static final Path FAVORITES_FILE = Paths.get("snapshots", "favorites.txt");

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Paths.get("snapshots"));

        while (true) {
            System.out.println("\n\u001B[36m=== INTEGRITY MENU ===\u001B[0m");
            System.out.println("1. Scan a new directory");
            System.out.println("2. Scan from recents");
            System.out.println("3. Favorites");
            System.out.println("0. Exit");

            String opt = IO.readln("\nChoose an option: ");

            if (opt.equals("0")) {
                System.out.println("\u001B[32mGoodbye!\u001B[0m");
                break;
            } else if (opt.equals("1")) {
                String pathInput = IO.readln("Enter directory path: ");
                Path pathTarget = Paths.get(pathInput).toAbsolutePath().normalize();
                runScan(pathTarget);
            } else if (opt.equals("2")) {
                displayAndScanList(RECENTS_FILE, "RECENTS");
            } else if (opt.equals("3")) {
                favoritesMenu(); // Enter favorites sub-menu
            } else {
                System.out.println("\u001B[31mInvalid option!\u001B[0m");
            }
        }
    }

    // --- FAVORITES SUB-MENU ---

    private static void favoritesMenu() throws IOException {
        while (true) {
            System.out.println("\n\u001B[36m--- FAVORITES MENU ---\u001B[0m");
            System.out.println("1. Scan a directory from favorites");
            System.out.println("2. Add a new directory");
            System.out.println("3. Remove a directory from favorites");
            System.out.println("0. Back to main menu");

            String opt = IO.readln("Choose an option: ");

            if (opt.equals("0")) {
                break;
            } else if (opt.equals("1")) {
                displayAndScanList(FAVORITES_FILE, "FAVORITES");
            } else if (opt.equals("2")) {
                addToFavorites();
            } else if (opt.equals("3")) {
                removeFromFavorites();
            } else {
                System.out.println("\u001B[31mInvalid option!\u001B[0m");
            }
        }
    }

    private static void addToFavorites() throws IOException {
        String pathInput = IO.readln("Enter directory path to add: ");
        Path target = Paths.get(pathInput).toAbsolutePath().normalize();

        if (Files.exists(target) && Files.isDirectory(target)) {
            List<String> favorites = new ArrayList<>();
            if (Files.exists(FAVORITES_FILE)) {
                favorites = Files.readAllLines(FAVORITES_FILE);
            }

            String path = target.toString();
            if (!favorites.contains(path)) {
                favorites.add(path);
                try (PrintWriter writer = new PrintWriter(new FileWriter(FAVORITES_FILE.toFile(), true))) {
                    writer.println(path);
                }
                System.out.println("\u001B[32m[+] " + target.getFileName() + " added!\u001B[0m");
            } else {
                System.out.println("\u001B[33mDirectory is already in the list.\u001B[0m");
            }
        } else {
            System.out.println("\u001B[31mError: Path is not a valid directory!\u001B[0m");
        }
    }

    private static void removeFromFavorites() throws IOException {
        if (!Files.exists(FAVORITES_FILE)) {
            System.out.println("\u001B[33mList is empty.\u001B[0m");
            return;
        }

        List<String> favorites = Files.readAllLines(FAVORITES_FILE);
        if (favorites.isEmpty()) {
            System.out.println("\u001B[33mList is empty.\u001B[0m");
            return;
        }

        System.out.println("\n\u001B[35m--- REMOVE FROM FAVORITES ---\u001B[0m");
        for (int i = 0; i < favorites.size(); i++) {
            System.out.println((i + 1) + ". " + favorites.get(i));
        }
        System.out.println("0. Cancel");

        String choice = IO.readln("Choose number to remove: ");
        try {
            int index = Integer.parseInt(choice);
            if (index == 0) return;
            if (index > 0 && index <= favorites.size()) {
                String removed = favorites.remove(index - 1);

                // Save updated list back to file
                try (PrintWriter writer = new PrintWriter(new FileWriter(FAVORITES_FILE.toFile()))) {
                    for (String f : favorites) {
                        writer.println(f);
                    }
                }
                System.out.println("\u001B[32m[-] Removed: " + removed + "\u001B[0m");
            } else {
                System.out.println("\u001B[31mInvalid number!\u001B[0m");
            }
        } catch (NumberFormatException e) {
            System.out.println("\u001B[31mPlease enter a valid number!\u001B[0m");
        }
    }

    // --- SCAN AND RECENTS LOGIC ---

    private static void runScan(Path target) throws IOException {
        if (Files.exists(target) && Files.isDirectory(target)) {
            processDirectory(target);
            saveToRecents(target);
        } else {
            System.out.println("\u001B[31mError: Path is not a valid directory!\u001B[0m");
        }
    }

    private static void displayAndScanList(Path listFile, String title) throws IOException {
        if (!Files.exists(listFile)) {
            System.out.println("\u001B[33m" + title.toLowerCase() + " list is empty.\u001B[0m");
            return;
        }

        List<String> list = Files.readAllLines(listFile);
        if (list.isEmpty()) {
            System.out.println("\u001B[33m" + title.toLowerCase() + " list is empty.\u001B[0m");
            return;
        }

        System.out.println("\n\u001B[35m--- " + title + " DIRECTORIES ---\u001B[0m");
        for (int i = 0; i < list.size(); i++) {
            System.out.println((i + 1) + ". " + list.get(i));
        }
        System.out.println("0. Back");

        String choice = IO.readln("Choose number: ");
        try {
            int index = Integer.parseInt(choice);
            if (index == 0) return;
            if (index > 0 && index <= list.size()) {
                Path target = Paths.get(list.get(index - 1));
                runScan(target);
            } else {
                System.out.println("\u001B[31mInvalid number!\u001B[0m");
            }
        } catch (NumberFormatException e) {
            System.out.println("\u001B[31mPlease enter a valid number!\u001B[0m");
        }
    }

    private static void saveToRecents(Path target) throws IOException {
        List<String> recents = new ArrayList<>();
        if (Files.exists(RECENTS_FILE)) {
            recents = Files.readAllLines(RECENTS_FILE);
        }

        String path = target.toString();
        recents.remove(path);
        recents.add(0, path);

        if (recents.size() > 5) {
            recents = recents.subList(0, 5);
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(RECENTS_FILE.toFile()))) {
            for (String r : recents) {
                writer.println(r);
            }
        }
    }

    // --- SNAPSHOT PROCESSING LOGIC ---

    public static void processDirectory(Path target) throws IOException {
        Path snapshotPath = getSnapshotPath(target);
        Map<String, String> oldData = loadSnapshot(snapshotPath);

        FileVisitor visitor = new FileVisitor(target, oldData);
        Files.walkFileTree(target, visitor);

        Map<String, String> newEntries = visitor.getNewFiles();
        analyzeChanges(newEntries, oldData, target);

        saveSnapshot(snapshotPath, visitor.getCurrentScan());
        System.out.println("\n\u001B[32mCompleted: " + target.getFileName() + "\u001B[0m");
    }

    public static Path getSnapshotPath(Path folder) {
        Path snapshotsDir = Paths.get("snapshots");
        String name = folder.toAbsolutePath().normalize().toString()
                .replace("/", ".").replace("\\", ".").replace(":", "");
        if (name.startsWith(".")) name = name.substring(1);
        return snapshotsDir.resolve(name + ".txt");
    }

    private static Map<String, String> loadSnapshot(Path path) throws IOException {
        Map<String, String> data = new HashMap<>();
        if (!Files.exists(path)) return data;
        for (String line : Files.readAllLines(path)) {
            String[] parts = line.split("\\|");
            if (parts.length == 2) data.put(parts[0], parts[1]);
        }
        return data;
    }

    private static void saveSnapshot(Path path, Map<String, String> data) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(path.toFile()))) {
            data.forEach((k, v) -> writer.println(k + "|" + v));
        }
    }

    private static void analyzeChanges(Map<String, String> newEntries, Map<String, String> deleted, Path startPath) {
        String YELLOW = "\u001B[33m", BLUE = "\u001B[34m", RED = "\u001B[31m", RESET = "\u001B[0m";
        Iterator<Map.Entry<String, String>> it = newEntries.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, String> n = it.next();
            String oldPath = null;
            for (Map.Entry<String, String> d : deleted.entrySet()) {
                if (d.getValue().equals(n.getValue())) { oldPath = d.getKey(); break; }
            }
            if (oldPath != null) {
                System.out.println(YELLOW + "[RENAMED] " + RESET + startPath.relativize(Paths.get(oldPath)) + " -> " + startPath.relativize(Paths.get(n.getKey())));
                deleted.remove(oldPath);
                it.remove();
            }
        }

        newEntries.forEach((k, v) -> System.out.println(BLUE + "[NEW] " + RESET + startPath.relativize(Paths.get(k))));
        deleted.forEach((k, v) -> System.out.println(RED + "[DELETED] " + RESET + startPath.relativize(Paths.get(k))));
    }
}