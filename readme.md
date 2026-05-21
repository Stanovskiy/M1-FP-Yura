# Caesar-cipher cryptanalyzer

Starter project for the JavaRush "Module 1. Java Syntax" final project. Fork this repo, implement the missing pieces, and open a pull request back.

The canonical assignment spec is `src/main/resources/project-description.pdf` (Ukrainian).

## How to do this assignment

1. Fork this repo on GitHub.
2. Clone your fork locally.
3. Work through `CHECKLIST.md` top-to-bottom.
4. Run `./mvnw test` to see what still needs to pass.
5. When you're done, run `./mvnw package` and upload `target/GNEW-M1-FP-1.0-SNAPSHOT.jar` to your fork's GitHub Releases.
6. Open a PR back to the upstream repo. The PR template asks you the four questions the mentor will look at.

## What's already done

- `Cypher.encrypt` — Caesar shift over the A–Z/a–z alphabet, cyclical.
- `ArgumentsParser` — option-style CLI (`-e -k 5 -f path`).
- `Main` — wires ENCRYPT end-to-end.
- `EncryptedFileNamer` — produces `foo [ENCRYPTED].txt` and `foo [DECRYPTED].txt` output paths.
- A full test suite — most tests are red until you implement the missing pieces. The failures are the assignment.

## What you implement

- `Cypher.decrypt` — currently a stub that throws.
- `BruteForce.bruteForce` — currently a stub that throws.
- The `DECRYPT` and `BRUTEFORCE` branches in `Main` — currently throw `UnsupportedOperationException`.

See `CHECKLIST.md` for the test-driven view of these.

## How to run

```
./mvnw test                                   # run the whole test suite
./mvnw -Dtest=CypherTest test                 # run one test class
./mvnw -DukrainianLanguageTest=true test      # also run the Ukrainian-bonus tests
./mvnw package                                # build target/GNEW-M1-FP-1.0-SNAPSHOT.jar
```

## CLI

```
-e   encrypt
-d   decrypt
-bf  brute force
-k   key (signed integer, required for -e and -d)
-f   file path
```

Examples:

```
java -jar target/GNEW-M1-FP-1.0-SNAPSHOT.jar -e -k 5 -f /path/to/file.txt
java -jar target/GNEW-M1-FP-1.0-SNAPSHOT.jar -d -k 5 -f "/path/to/file [ENCRYPTED].txt"
java -jar target/GNEW-M1-FP-1.0-SNAPSHOT.jar -bf -f "/path/to/file [ENCRYPTED].txt"
```

Arguments may appear in any order: `-e -f path -k 5` works too.

> **Note:** The PDF spec describes a positional CLI (`ENCRYPT path key`). This starter uses option-style instead — both have their fans, and the tests are pinned to option-style. Your implementation should match the starter, not the PDF, for the CLI surface.
