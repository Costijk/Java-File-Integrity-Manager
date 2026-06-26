import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {

    // Fisierele unde vom salva istoricul si favoritele
    private static final Path RECENTS_FILE = Paths.get("snapshots", "recents.txt");
    private static final Path FAVORITES_FILE = Paths.get("snapshots", "favorites.txt");

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Paths.get("snapshots"));

        while (true) {
            System.out.println("\n\u001B[36m=== MENIU INTEGRITATE ===\u001B[0m");
            System.out.println("1. Scaneaza un director nou");
            System.out.println("2. Scaneaza din recente");
            System.out.println("3. Favorite");
            System.out.println("0. Iesire");

            String opt = IO.readln("\nAlege o optiune: ");

            if (opt.equals("0")) {
                System.out.println("\u001B[32mLa revedere!\u001B[0m");
                break;
            } else if (opt.equals("1")) {
                String pathInput = IO.readln("Introdu calea directorului: ");
                Path pathTarget = Paths.get(pathInput).toAbsolutePath().normalize();
                ruleazaScanare(pathTarget);
            } else if (opt.equals("2")) {
                afiseazaSiScaneazaLista(RECENTS_FILE, "RECENTE");
            } else if (opt.equals("3")) {
                meniuFavorite(); // Intram in sub-meniul de favorite
            } else {
                System.out.println("\u001B[31mOptiune invalida!\u001B[0m");
            }
        }
    }

    // --- SUB-MENIU FAVORITE ---

    private static void meniuFavorite() throws IOException {
        while (true) {
            System.out.println("\n\u001B[36m--- MENIU FAVORITE ---\u001B[0m");
            System.out.println("1. Scaneaza un director din favorite");
            System.out.println("2. Adauga un director nou");
            System.out.println("3. Sterge un director din favorite");
            System.out.println("0. Inapoi la meniul principal");

            String opt = IO.readln("Alege o optiune: ");

            if (opt.equals("0")) {
                break;
            } else if (opt.equals("1")) {
                afiseazaSiScaneazaLista(FAVORITES_FILE, "FAVORITE");
            } else if (opt.equals("2")) {
                adaugaInFavorite();
            } else if (opt.equals("3")) {
                stergeDinFavorite();
            } else {
                System.out.println("\u001B[31mOptiune invalida!\u001B[0m");
            }
        }
    }

    private static void adaugaInFavorite() throws IOException {
        String pathInput = IO.readln("Introdu calea directorului de adaugat: ");
        Path target = Paths.get(pathInput).toAbsolutePath().normalize();

        if (Files.exists(target) && Files.isDirectory(target)) {
            List<String> favorite = new ArrayList<>();
            if (Files.exists(FAVORITES_FILE)) {
                favorite = Files.readAllLines(FAVORITES_FILE);
            }

            String calea = target.toString();
            if (!favorite.contains(calea)) {
                favorite.add(calea);
                try (PrintWriter writer = new PrintWriter(new FileWriter(FAVORITES_FILE.toFile(), true))) {
                    writer.println(calea);
                }
                System.out.println("\u001B[32m[+] " + target.getFileName() + " a fost adaugat!\u001B[0m");
            } else {
                System.out.println("\u001B[33mDirectorul este deja in lista.\u001B[0m");
            }
        } else {
            System.out.println("\u001B[31mEroare: Calea nu este un director valid!\u001B[0m");
        }
    }

    private static void stergeDinFavorite() throws IOException {
        if (!Files.exists(FAVORITES_FILE)) {
            System.out.println("\u001B[33mLista este goala.\u001B[0m");
            return;
        }

        List<String> favorite = Files.readAllLines(FAVORITES_FILE);
        if (favorite.isEmpty()) {
            System.out.println("\u001B[33mLista este goala.\u001B[0m");
            return;
        }

        System.out.println("\n\u001B[35m--- STERGE DIN FAVORITE ---\u001B[0m");
        for (int i = 0; i < favorite.size(); i++) {
            System.out.println((i + 1) + ". " + favorite.get(i));
        }
        System.out.println("0. Anulare");

        String alegere = IO.readln("Alege numarul pentru a-l sterge: ");
        try {
            int index = Integer.parseInt(alegere);
            if (index == 0) return;
            if (index > 0 && index <= favorite.size()) {
                String sters = favorite.remove(index - 1);

                // Salvam lista actualizata inapoi in fisier
                try (PrintWriter writer = new PrintWriter(new FileWriter(FAVORITES_FILE.toFile()))) {
                    for (String f : favorite) {
                        writer.println(f);
                    }
                }
                System.out.println("\u001B[32m[-] Eliminat: " + sters + "\u001B[0m");
            } else {
                System.out.println("\u001B[31mNumar invalid!\u001B[0m");
            }
        } catch (NumberFormatException e) {
            System.out.println("\u001B[31mTe rog introdu un numar valid!\u001B[0m");
        }
    }

    // --- LOGICA DE SCANARE SI RECENTE ---

    private static void ruleazaScanare(Path target) throws IOException {
        if (Files.exists(target) && Files.isDirectory(target)) {
            proceseazaDirector(target);
            salveazaInRecente(target);
        } else {
            System.out.println("\u001B[31mEroare: Calea nu este un director valid!\u001B[0m");
        }
    }

    private static void afiseazaSiScaneazaLista(Path fisierLista, String titlu) throws IOException {
        if (!Files.exists(fisierLista)) {
            System.out.println("\u001B[33mLista de " + titlu.toLowerCase() + " este goala.\u001B[0m");
            return;
        }

        List<String> lista = Files.readAllLines(fisierLista);
        if (lista.isEmpty()) {
            System.out.println("\u001B[33mLista de " + titlu.toLowerCase() + " este goala.\u001B[0m");
            return;
        }

        System.out.println("\n\u001B[35m--- DIRECTOARE " + titlu + " ---\u001B[0m");
        for (int i = 0; i < lista.size(); i++) {
            System.out.println((i + 1) + ". " + lista.get(i));
        }
        System.out.println("0. Inapoi");

        String alegere = IO.readln("Alege numarul: ");
        try {
            int index = Integer.parseInt(alegere);
            if (index == 0) return;
            if (index > 0 && index <= lista.size()) {
                Path target = Paths.get(lista.get(index - 1));
                ruleazaScanare(target);
            } else {
                System.out.println("\u001B[31mNumar invalid!\u001B[0m");
            }
        } catch (NumberFormatException e) {
            System.out.println("\u001B[31mTe rog introdu un numar valid!\u001B[0m");
        }
    }

    private static void salveazaInRecente(Path target) throws IOException {
        List<String> recente = new ArrayList<>();
        if (Files.exists(RECENTS_FILE)) {
            recente = Files.readAllLines(RECENTS_FILE);
        }

        String calea = target.toString();
        recente.remove(calea);
        recente.add(0, calea);

        if (recente.size() > 5) {
            recente = recente.subList(0, 5);
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(RECENTS_FILE.toFile()))) {
            for (String r : recente) {
                writer.println(r);
            }
        }
    }

    // --- LOGICA DE PROCESARE A SNAPSHOT-URILOR ---

    public static void proceseazaDirector(Path target) throws IOException {
        Path snapshotPath = getSnapshotPath(target);
        Map<String, String> oldData = incarcaSnapshot(snapshotPath);

        FileVisitor visitor = new FileVisitor(target, oldData);
        Files.walkFileTree(target, visitor);

        Map<String, String> noi = visitor.getFisiereNoi();
        analyzeChanges(noi, oldData, target);

        salveazaSnapshot(snapshotPath, visitor.getScanareCurenta());
        System.out.println("\n\u001B[32mFinalizat: " + target.getFileName() + "\u001B[0m");
    }

    public static Path getSnapshotPath(Path folder) {
        Path dirSnapshots = Paths.get("snapshots");
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