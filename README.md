# Java File Integrity Manager

A lightweight, console-based Java application that monitors file system integrity using SHA-256 cryptographic hashing. It scans directories, records snapshots of file hashes, and detects changes between scans — functioning as a local tripwire/HIDS (Host-based Intrusion Detection System) for your file system.

## Features

- **Directory Scanning** — Recursively walks directory trees using Java NIO.2 and computes SHA-256 hashes for every file
- **Change Detection** — Compares current file state against a stored snapshot and reports:
  - **OK** — File unchanged
  - **MODIFIED** — File contents changed
  - **NEW** — File added since last scan
  - **DELETED** — File removed since last scan
  - **RENAMED** — File moved/renamed (detected by matching hashes between deleted and new files)
- **Snapshot Persistence** — Snapshots are stored as pipe-delimited text files (`<path>|<sha256-hash>`) in the `snapshots/` directory
- **Sub-Directory Granularity** — If a sub-directory has its own snapshot, it is scanned independently with recursive processing
- **Recents** — Tracks the last 5 scanned directories for quick re-scanning
- **Favorites** — Persistent bookmarking of frequently scanned directories, manageable via an interactive sub-menu
- **Color-Coded Console Output** — ANSI escape codes highlight status for quick visual scanning

## Note

This project was built on **Fedora KDE** and targets the Linux filesystem structure. Keep this in mind if running on other operating systems, as path handling and console behavior may differ.

## Requirements

- **Java 14+** (uses `String.repeat()` and modern switch expressions)
- No external dependencies — uses only JDK standard library APIs

## Building & Running

### Command Line

```bash
# Compile
javac -d out src/*.java

# Run
mkdir -p snapshots
java -cp out Main
```

### IntelliJ IDEA

1. Open the project folder in IntelliJ IDEA
2. Ensure a JDK 14+ is configured
3. Run `Main.main()`

> **Note:** The project references an `IO.readln()` utility class. If this class is missing from your setup, create an `IO.java` file in `src/` with a static `readln(String prompt)` method wrapping `Scanner.nextLine()`.

## Project Structure

```
src/
  Main.java          — Application entry point, menu system, snapshot logic
  FileVisitor.java    — Custom SimpleFileVisitor for NIO.2 directory walking
  Criptare.java       — SHA-256 hash computation
snapshots/            — Runtime snapshot storage (gitignored)
lastDirectory.txt     — Last scanned directory record
```

## How Snapshots Work

1. Point the tool at a directory
2. It walks the tree, hash every file, and saves the results to `snapshots/<path-encoded>.txt`
3. On subsequent scans, it compares current hashes against the snapshot
4. Any differences are reported with归类 status (modified, new, deleted, renamed)
5. The snapshot file is updated after each scan

## License

This project is open source. Feel free to use and modify.
