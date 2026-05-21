# Starter Project Improvements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the medium-overhaul described in `docs/superpowers/specs/2026-05-21-starter-improvements-design.md`: visible TODO seams, expanded test coverage, runnable-jar plugin, Maven wrapper, CHECKLIST.md, PR template, README rewrite.

**Architecture:** Stay within the existing four-package layout (`crypto`, `file`, `runner`, `language`). New code adds (a) a `BruteForce` stub class, (b) an `EncryptedFileNamer` helper that owns filename suffix rules, (c) a minimal `Language` base class with one `EnglishLanguage` subclass. `Main`'s single `if` becomes a `switch` with `UnsupportedOperationException` stubs for DECRYPT/BRUTEFORCE. Test fixtures move from inline strings to `src/test/resources/`. Three new unit-test classes (`CypherTest`, `ArgumentsParserTest`, `EncryptedFileNamerTest`) augment the existing end-to-end `MainTest`.

**Tech Stack:** Java 17, Maven (with wrapper added in this work), JUnit Jupiter 5.9.2, GitHub Actions.

---

## Task 1: Stash uncommitted Cypher.java experiments

**Files:**
- Stash: `src/main/java/ua/com/javarush/gnew/crypto/Cypher.java`

Working copy has uncommitted changes that comment out `Math.negateExact(key)`. That breaks the existing `encrypt("A", 1, "B")` test in `MainTest`. Stash the changes so we work against a known-good baseline. The stash can be recovered with `git stash pop` later.

- [ ] **Step 1: Check git status**

Run: `git status src/main/java/ua/com/javarush/gnew/crypto/Cypher.java`
Expected: ` M src/main/java/ua/com/javarush/gnew/crypto/Cypher.java`

- [ ] **Step 2: Stash the working-copy modifications**

Run: `git stash push -m "starter-improvements: pre-baseline Cypher experiments" -- src/main/java/ua/com/javarush/gnew/crypto/Cypher.java`
Expected: `Saved working directory and index state On main: starter-improvements: pre-baseline Cypher experiments`

- [ ] **Step 3: Verify Cypher.java now matches HEAD**

Run: `git diff src/main/java/ua/com/javarush/gnew/crypto/Cypher.java`
Expected: empty output (no diff)

- [ ] **Step 4: Verify the baseline test suite still passes encrypt-side tests**

Run: `mvn -q -Dtest='MainTest$EnglishTests#encrypt' test`
Expected: BUILD SUCCESS (the parameterized `encrypt("A", 1, "B")` etc. all pass)

No commit for this task — stashing isn't a code change.

---

## Task 2: Extract test fixtures to `src/test/resources/`

**Files:**
- Create: `src/test/resources/hamlet.txt`
- Create: `src/test/resources/orwell.txt`
- Modify: `src/test/java/ua/com/javarush/gnew/MainTest.java` (remove `HAMLET_EN` and `ORWELL_UA` string constants; load from resources)

Reduces `MainTest` from ~350 lines to ~200. Pure refactor — same tests, same outcomes.

- [ ] **Step 1: Create `src/test/resources/hamlet.txt`** with the exact contents of the current `HAMLET_EN` constant (lines 22–60 of `MainTest.java`, preserving the trailing dot and all whitespace).

```
THE TRAGEDY OF HAMLET, PRINCE OF DENMARK


by William Shakespeare



Dramatis Personae

  Claudius, King of Denmark.
  Marcellus, Officer.
  Hamlet, son to the former, and nephew to the present king.
  Polonius, Lord Chamberlain.
  Horatio, friend to Hamlet.
  Laertes, son to Polonius.
  Voltemand, courtier.
  Cornelius, courtier.
  Rosencrantz, courtier.
  Guildenstern, courtier.
  Osric, courtier.
  A Gentleman, courtier.
  A Priest.
  Marcellus, officer.
  Bernardo, officer.
  Francisco, a soldier
  Reynaldo, servant to Polonius.
  Players.
  Two Clowns, gravediggers.
  Fortinbras, Prince of Norway.  
  A Norwegian Captain.
  English Ambassadors.

  Getrude, Queen of Denmark, mother to Hamlet.
  Ophelia, daughter to Polonius.

  Ghost of Hamlet's Father.

  Lords, ladies, Officers, Soldiers, Sailors, Messengers, Attendants.
```

Important: line 60 of `MainTest.java` is `"...Lords, ladies, Officers, Soldiers, Sailors, Messengers, Attendants."""` — no trailing newline. Make sure the resource file has no trailing newline either (use `printf` instead of `echo`, or strip via a text editor).

- [ ] **Step 2: Create `src/test/resources/orwell.txt`** with the exact contents of the current `ORWELL_UA` constant (lines 62–93 of `MainTest.java`). The original ends with `скінчилася.\n` — preserve the single trailing newline.

- [ ] **Step 3: Add a fixture loader helper to `MainTest`**

Add to `MainTest` (alongside the existing `createTestFile` helper, near line 107):

```java
private static String loadResource(String resourceName) {
    try {
        return Files.readString(Path.of(
                MainTest.class.getResource("/" + resourceName).toURI()
        ));
    } catch (IOException | java.net.URISyntaxException e) {
        throw new RuntimeException("Failed to load test resource: " + resourceName, e);
    }
}
```

- [ ] **Step 4: Replace `HAMLET_EN` and `ORWELL_UA` constant definitions** (lines 22–93)

Replace the two `private static final String HAMLET_EN = """..."""` and `private static final String ORWELL_UA = """..."""` blocks with:

```java
private static final String HAMLET_EN = loadResource("hamlet.txt");
private static final String ORWELL_UA = loadResource("orwell.txt");
```

- [ ] **Step 5: Run the full English+File test suite to verify no regression**

Run: `mvn -q -Dtest='MainTest$FileTests,MainTest$EnglishTests' test`
Expected: same pass/fail mix as before the refactor (encrypt tests pass; decrypt/BF tests fail with "No new file was created" — this is the baseline failure mode).

- [ ] **Step 6: Commit**

```bash
git add src/test/resources/hamlet.txt src/test/resources/orwell.txt src/test/java/ua/com/javarush/gnew/MainTest.java
git commit -m "$(cat <<'EOF'
Extract MainTest fixtures to src/test/resources/

HAMLET_EN and ORWELL_UA were ~100 lines of embedded strings, making
MainTest hard to navigate. Move them to plain .txt files loaded via
the classpath. Same content, same tests, same outcomes.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 3: Create `EncryptedFileNamer` + tests (TDD)

**Files:**
- Create: `src/main/java/ua/com/javarush/gnew/file/EncryptedFileNamer.java`
- Create: `src/test/java/ua/com/javarush/gnew/file/EncryptedFileNamerTest.java`

Helper that owns the `[ENCRYPTED]` / `[DECRYPTED]` filename suffix rules. Currently this lives inline in `Main` with a brittle `.length() - 4`.

- [ ] **Step 1: Write the failing test class**

Create `src/test/java/ua/com/javarush/gnew/file/EncryptedFileNamerTest.java`:

```java
package ua.com.javarush.gnew.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EncryptedFileNamerTest {

    private final EncryptedFileNamer namer = new EncryptedFileNamer();

    @Nested
    @DisplayName("forEncrypted")
    class ForEncrypted {

        @Test
        @DisplayName("appends ' [ENCRYPTED]' before .txt")
        void appendsEncryptedMarker() {
            Path result = namer.forEncrypted(Paths.get("/tmp/foo.txt"));
            assertEquals(Paths.get("/tmp/foo [ENCRYPTED].txt"), result);
        }

        @Test
        @DisplayName("preserves the parent directory")
        void preservesParent() {
            Path result = namer.forEncrypted(Paths.get("/a/b/c/file.txt"));
            assertEquals(Paths.get("/a/b/c/file [ENCRYPTED].txt"), result);
        }
    }

    @Nested
    @DisplayName("forDecrypted")
    class ForDecrypted {

        @Test
        @DisplayName("replaces [ENCRYPTED] with [DECRYPTED] when present")
        void replacesEncryptedMarker() {
            Path result = namer.forDecrypted(Paths.get("/tmp/foo [ENCRYPTED].txt"));
            assertEquals(Paths.get("/tmp/foo [DECRYPTED].txt"), result);
        }

        @Test
        @DisplayName("appends ' [DECRYPTED]' before .txt when no marker present")
        void appendsWhenNoMarker() {
            Path result = namer.forDecrypted(Paths.get("/tmp/foo.txt"));
            assertEquals(Paths.get("/tmp/foo [DECRYPTED].txt"), result);
        }

        @Test
        @DisplayName("does not double-suffix")
        void noDoubleSuffix() {
            Path result = namer.forDecrypted(Paths.get("/tmp/foo [ENCRYPTED].txt"));
            assertEquals(Paths.get("/tmp/foo [DECRYPTED].txt"), result);
            // Specifically: not "foo [ENCRYPTED] [DECRYPTED].txt"
        }
    }
}
```

- [ ] **Step 2: Run the test — expect compile failure (class doesn't exist yet)**

Run: `mvn -q -Dtest=EncryptedFileNamerTest test`
Expected: BUILD FAILURE with `cannot find symbol: class EncryptedFileNamer` (or similar)

- [ ] **Step 3: Create the implementation**

Create `src/main/java/ua/com/javarush/gnew/file/EncryptedFileNamer.java`:

```java
package ua.com.javarush.gnew.file;

import java.nio.file.Path;

/**
 * Produces output filenames for encrypted and decrypted variants of a .txt input.
 * Assumes the input filename ends in ".txt"; behavior for other extensions is undefined.
 */
public class EncryptedFileNamer {

    private static final String EXTENSION = ".txt";
    private static final String ENCRYPTED_MARKER = " [ENCRYPTED]";
    private static final String DECRYPTED_MARKER = " [DECRYPTED]";

    public Path forEncrypted(Path input) {
        return input.resolveSibling(stripExtension(input) + ENCRYPTED_MARKER + EXTENSION);
    }

    public Path forDecrypted(Path input) {
        String base = stripExtension(input);
        if (base.endsWith(ENCRYPTED_MARKER)) {
            base = base.substring(0, base.length() - ENCRYPTED_MARKER.length());
        }
        return input.resolveSibling(base + DECRYPTED_MARKER + EXTENSION);
    }

    private String stripExtension(Path input) {
        String fileName = input.getFileName().toString();
        return fileName.substring(0, fileName.length() - EXTENSION.length());
    }
}
```

- [ ] **Step 4: Run tests — expect pass**

Run: `mvn -q -Dtest=EncryptedFileNamerTest test`
Expected: BUILD SUCCESS, 5 tests pass

- [ ] **Step 5: Commit**

```bash
git add src/main/java/ua/com/javarush/gnew/file/EncryptedFileNamer.java src/test/java/ua/com/javarush/gnew/file/EncryptedFileNamerTest.java
git commit -m "$(cat <<'EOF'
Add EncryptedFileNamer with [ENCRYPTED]/[DECRYPTED] suffix rules

Extracts the filename-marker logic that lived inline in Main with a
brittle .length() - 4. forDecrypted swaps an existing [ENCRYPTED]
marker rather than appending blindly, so decrypting foo [ENCRYPTED].txt
produces foo [DECRYPTED].txt instead of foo [ENCRYPTED] [DECRYPTED].txt.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 4: Refactor `Main` to switch + stubs, use new namer

**Files:**
- Modify: `src/main/java/ua/com/javarush/gnew/Main.java`

Replace the lone `if (ENCRYPT)` with a `switch` covering all three commands. ENCRYPT branch keeps working behavior but routes through `EncryptedFileNamer`. DECRYPT and BRUTEFORCE throw `UnsupportedOperationException` (caught by the existing try/catch, so `Main.main` still doesn't propagate).

- [ ] **Step 1: Replace `Main.java` contents**

```java
package ua.com.javarush.gnew;

import ua.com.javarush.gnew.crypto.Cypher;
import ua.com.javarush.gnew.file.EncryptedFileNamer;
import ua.com.javarush.gnew.file.FileManager;
import ua.com.javarush.gnew.runner.ArgumentsParser;
import ua.com.javarush.gnew.runner.RunOptions;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        Cypher cypher = new Cypher();
        FileManager fileManager = new FileManager();
        EncryptedFileNamer namer = new EncryptedFileNamer();
        ArgumentsParser argumentsParser = new ArgumentsParser();

        try {
            RunOptions runOptions = argumentsParser.parse(args);

            switch (runOptions.getCommand()) {
                case ENCRYPT -> {
                    String content = fileManager.read(runOptions.getFilePath());
                    String encrypted = cypher.encrypt(content, runOptions.getKey());
                    Path output = namer.forEncrypted(runOptions.getFilePath());
                    fileManager.write(output, encrypted);
                }
                case DECRYPT -> throw new UnsupportedOperationException(
                        "TODO: implement DECRYPT — see Cypher.decrypt and CHECKLIST.md");
                case BRUTEFORCE -> throw new UnsupportedOperationException(
                        "TODO: implement BRUTEFORCE — see BruteForce.bruteForce and CHECKLIST.md");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
```

Note: `argumentsParser.parse(args)` moved INSIDE the try/catch so parser errors are also swallowed (matches the existing `ValidationTests.fileNotExists` expectation that `Main.main` never throws).

- [ ] **Step 2: Run encrypt tests — must still pass**

Run: `mvn -q -Dtest='MainTest$EnglishTests#encrypt,MainTest$FileTests$EncryptFileTests' test`
Expected: BUILD SUCCESS

- [ ] **Step 3: Run round-trip test — must still fail with same mode as before**

Run: `mvn -q -Dtest='MainTest$EnglishTests#decryptedFileTextValidate' test`
Expected: BUILD FAILURE with "No new file was created" (because DECRYPT now throws UnsupportedOperationException, caught by Main, no file produced — same observable outcome as the old behavior of "ignored unrecognized command").

- [ ] **Step 4: Commit**

```bash
git add src/main/java/ua/com/javarush/gnew/Main.java
git commit -m "$(cat <<'EOF'
Refactor Main: switch with DECRYPT/BRUTEFORCE stubs, use namer

ENCRYPT branch now routes the output filename through EncryptedFileNamer
(removes the inline .length() - 4 logic). DECRYPT and BRUTEFORCE throw
UnsupportedOperationException — caught by the existing try/catch so
Main.main still never propagates. Students see all three branches and
where to plug in.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 5: Add `decrypt` stub to `Cypher`; remove hint comments

**Files:**
- Modify: `src/main/java/ua/com/javarush/gnew/crypto/Cypher.java`

The committed `Cypher.java` already has clean (uncommented) `encrypt`. Task 1 stashed the experimental hints. Now add the `decrypt` stub.

- [ ] **Step 1: Verify Cypher.java is in committed state**

Run: `git diff src/main/java/ua/com/javarush/gnew/crypto/Cypher.java`
Expected: empty (no diff vs HEAD)

- [ ] **Step 2: Replace `Cypher.java` contents**

```java
package ua.com.javarush.gnew.crypto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Cypher {

    private final ArrayList<Character> originalAlphabet = new ArrayList<>(Arrays.asList(
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'));

    public String encrypt(String input, int key) {
        key = Math.negateExact(key);
        ArrayList<Character> rotatedAlphabet = new ArrayList<>(originalAlphabet);
        Collections.rotate(rotatedAlphabet, key);

        StringBuilder builder = new StringBuilder();
        for (char symbol : input.toCharArray()) {
            builder.append(processSymbol(symbol, rotatedAlphabet));
        }
        return builder.toString();
    }

    /**
     * Reverse of {@link #encrypt}. The relationship to encrypt is the key
     * to keeping this short.
     */
    public String decrypt(String input, int key) {
        throw new UnsupportedOperationException("TODO: implement decrypt");
    }

    private Character processSymbol(char symbol, ArrayList<Character> rotatedAlphabet) {
        if (!originalAlphabet.contains(symbol)) {
            return symbol;
        }
        int index = originalAlphabet.indexOf(symbol);
        return rotatedAlphabet.get(index);
    }
}
```

- [ ] **Step 3: Run encrypt tests — must still pass**

Run: `mvn -q -Dtest='MainTest$EnglishTests#encrypt' test`
Expected: BUILD SUCCESS (4 parameterized cases pass: A→B at key=1, etc.)

- [ ] **Step 4: Commit**

```bash
git add src/main/java/ua/com/javarush/gnew/crypto/Cypher.java
git commit -m "$(cat <<'EOF'
Add decrypt stub to Cypher with hint Javadoc

Stub throws UnsupportedOperationException; Javadoc points at the
encrypt/decrypt relationship without giving away the implementation.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 6: Create `BruteForce` stub class

**Files:**
- Create: `src/main/java/ua/com/javarush/gnew/crypto/BruteForce.java`

- [ ] **Step 1: Create the class**

```java
package ua.com.javarush.gnew.crypto;

public class BruteForce {

    /**
     * Try plausible keys and score each candidate to find the correct decryption.
     * Two textbook approaches: dictionary match against common words, or letter-frequency
     * comparison against expected language statistics.
     */
    public String bruteForce(String cipherText) {
        throw new UnsupportedOperationException("TODO: implement brute force");
    }
}
```

- [ ] **Step 2: Verify project compiles**

Run: `mvn -q compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/ua/com/javarush/gnew/crypto/BruteForce.java
git commit -m "$(cat <<'EOF'
Add BruteForce stub class

Single-method class students implement. Javadoc names two textbook
approaches (dictionary match, letter-frequency) without picking one.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 7: Create `CypherTest` with encrypt edge cases

**Files:**
- Create: `src/test/java/ua/com/javarush/gnew/crypto/CypherTest.java`

Unit-level tests for `encrypt` — same edge cases that `MainTest.EncryptEdgeCases` (Task 10) will run end-to-end, but here without file I/O so they fail faster and isolate bugs.

- [ ] **Step 1: Create the test file**

```java
package ua.com.javarush.gnew.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CypherTest {

    private final Cypher cypher = new Cypher();

    @DisplayName("[ENCRYPT] simple shifts")
    @ParameterizedTest(name = "encrypt({0}, {1}) = {2}")
    @CsvSource({
            "A, 1, B",
            "a, 1, b",
            "A, 25, Z",
            "a, 25, z",
            "Z, 1, a",   // wraps across case boundary in this 52-char alphabet
            "z, 1, A"    // wraps from end back to start
    })
    void simpleShift(String input, int key, String expected) {
        assertEquals(expected, cypher.encrypt(input, key));
    }

    @DisplayName("[ENCRYPT] key=0 leaves text unchanged")
    @Test
    void keyZeroIsIdentity() {
        assertEquals("Hello, World!", cypher.encrypt("Hello, World!", 0));
    }

    @DisplayName("[ENCRYPT] key=52 (full alphabet cycle) leaves text unchanged")
    @Test
    void fullCycleIsIdentity() {
        assertEquals("Hello", cypher.encrypt("Hello", 52));
    }

    @DisplayName("[ENCRYPT] key=53 equals key=1")
    @Test
    void cycleWrapsAround() {
        assertEquals(cypher.encrypt("Hello", 1), cypher.encrypt("Hello", 53));
    }

    @DisplayName("[ENCRYPT] negative key shifts in the opposite direction")
    @ParameterizedTest(name = "encrypt({0}, {1}) = {2}")
    @CsvSource({
            "A, -1, z",
            "a, -1, Z",
            "Z, -25, A",
            "z, -25, a"
    })
    void negativeKey(String input, int key, String expected) {
        assertEquals(expected, cypher.encrypt(input, key));
    }

    @DisplayName("[ENCRYPT] non-alphabet characters pass through unchanged")
    @ParameterizedTest(name = "encrypt({0}, 5) = {0}")
    @CsvSource({"'.'", "','", "' '", "'!'", "'?'", "'0'", "'9'", "'\t'"})
    void passThrough(String input) {
        assertEquals(input, cypher.encrypt(input, 5));
    }

    @DisplayName("[ENCRYPT] empty string returns empty string")
    @Test
    void emptyString() {
        assertEquals("", cypher.encrypt("", 5));
    }

    @DisplayName("[ENCRYPT] preserves digit and whitespace alongside letters")
    @Test
    void mixedContent() {
        // "abc" + 1 = "bcd"; digits and space unchanged
        assertEquals("bcd 123", cypher.encrypt("abc 123", 1));
    }
}
```

- [ ] **Step 2: Run the test class**

Run: `mvn -q -Dtest=CypherTest test`
Expected: BUILD SUCCESS, all tests pass

- [ ] **Step 3: Commit**

```bash
git add src/test/java/ua/com/javarush/gnew/crypto/CypherTest.java
git commit -m "$(cat <<'EOF'
Add CypherTest with encrypt edge cases at unit level

Covers key=0, key=52 (full cycle), key=53 (= key=1), negative keys,
case-boundary wrap (Z+1=a, z+1=A), pass-through for non-letters,
empty string, and mixed content. Gives students a place to add their
decrypt unit tests when they implement it.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 8: Create `ArgumentsParserTest`

**Files:**
- Create: `src/test/java/ua/com/javarush/gnew/runner/ArgumentsParserTest.java`

Direct unit tests for the parser. Covers happy paths in arbitrary order plus every error condition.

- [ ] **Step 1: Create the test file**

```java
package ua.com.javarush.gnew.runner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentsParserTest {

    private final ArgumentsParser parser = new ArgumentsParser();

    @Nested
    @DisplayName("happy paths")
    class HappyPaths {

        @Test
        @DisplayName("parses -e -k -f in given order")
        void canonicalOrder() {
            RunOptions options = parser.parse(new String[]{"-e", "-k", "5", "-f", "/tmp/foo.txt"});
            assertEquals(Command.ENCRYPT, options.getCommand());
            assertEquals(5, options.getKey());
            assertEquals(Paths.get("/tmp/foo.txt"), options.getFilePath());
        }

        @Test
        @DisplayName("parses arguments in arbitrary order")
        void reorderedArgs() {
            RunOptions options = parser.parse(new String[]{"-f", "/tmp/foo.txt", "-k", "5", "-e"});
            assertEquals(Command.ENCRYPT, options.getCommand());
            assertEquals(5, options.getKey());
            assertEquals(Paths.get("/tmp/foo.txt"), options.getFilePath());
        }

        @Test
        @DisplayName("recognizes -d as DECRYPT")
        void decryptCommand() {
            RunOptions options = parser.parse(new String[]{"-d", "-k", "5", "-f", "/tmp/foo.txt"});
            assertEquals(Command.DECRYPT, options.getCommand());
        }

        @Test
        @DisplayName("recognizes -bf as BRUTEFORCE")
        void bruteforceCommand() {
            RunOptions options = parser.parse(new String[]{"-bf", "-k", "5", "-f", "/tmp/foo.txt"});
            assertEquals(Command.BRUTEFORCE, options.getCommand());
        }

        @Test
        @DisplayName("accepts negative key")
        void negativeKey() {
            RunOptions options = parser.parse(new String[]{"-e", "-k", "-5", "-f", "/tmp/foo.txt"});
            assertEquals(-5, options.getKey());
        }
    }

    @Nested
    @DisplayName("error cases")
    class Errors {

        @Test
        @DisplayName("missing command throws")
        void missingCommand() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> parser.parse(new String[]{"-k", "5", "-f", "/tmp/foo.txt"}));
            assertTrue(e.getMessage().toLowerCase().contains("command"),
                    "Expected 'command' in error message, got: " + e.getMessage());
        }

        @Test
        @DisplayName("missing -k throws")
        void missingKey() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> parser.parse(new String[]{"-e", "-f", "/tmp/foo.txt"}));
            assertTrue(e.getMessage().toLowerCase().contains("key"),
                    "Expected 'key' in error message, got: " + e.getMessage());
        }

        @Test
        @DisplayName("missing -f throws")
        void missingFile() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> parser.parse(new String[]{"-e", "-k", "5"}));
            assertTrue(e.getMessage().toLowerCase().contains("file"),
                    "Expected 'file' in error message, got: " + e.getMessage());
        }

        @Test
        @DisplayName("-k with no value throws")
        void keyFlagWithoutValue() {
            assertThrows(IllegalArgumentException.class,
                    () -> parser.parse(new String[]{"-e", "-f", "/tmp/foo.txt", "-k"}));
        }

        @Test
        @DisplayName("-f with no value throws")
        void fileFlagWithoutValue() {
            assertThrows(IllegalArgumentException.class,
                    () -> parser.parse(new String[]{"-e", "-k", "5", "-f"}));
        }

        @Test
        @DisplayName("non-numeric key throws NumberFormatException")
        void nonNumericKey() {
            assertThrows(NumberFormatException.class,
                    () -> parser.parse(new String[]{"-e", "-k", "abc", "-f", "/tmp/foo.txt"}));
        }

        @Test
        @DisplayName("unknown flag throws")
        void unknownFlag() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> parser.parse(new String[]{"-e", "-x", "-k", "5", "-f", "/tmp/foo.txt"}));
            assertTrue(e.getMessage().toLowerCase().contains("unknown"),
                    "Expected 'unknown' in error message, got: " + e.getMessage());
        }
    }
}
```

- [ ] **Step 2: Run the test class**

Run: `mvn -q -Dtest=ArgumentsParserTest test`
Expected: BUILD SUCCESS, all tests pass

- [ ] **Step 3: Commit**

```bash
git add src/test/java/ua/com/javarush/gnew/runner/ArgumentsParserTest.java
git commit -m "$(cat <<'EOF'
Add ArgumentsParserTest with happy paths and error cases

Direct unit tests against the parser, covering arbitrary argument
order, all three commands, negative keys, missing/unknown flags,
flags without values, and non-numeric keys.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 9: Expand `Language` to minimal real base + add `EnglishLanguage`

**Files:**
- Modify: `src/main/java/ua/com/javarush/gnew/language/Language.java`
- Create: `src/main/java/ua/com/javarush/gnew/language/EnglishLanguage.java`

Make `Language` useful as an extension point for the Ukrainian bonus without coupling `Cypher` to it (per Q5 in the design — `Cypher` keeps its hard-coded alphabet).

- [ ] **Step 1: Replace `Language.java`**

```java
package ua.com.javarush.gnew.language;

import java.util.List;

/**
 * Extension point for the optional Ukrainian-alphabet task
 * (see src/main/resources/project-description.pdf, "Додаткові завдання").
 *
 * The core implementation does not use this class — {@link ua.com.javarush.gnew.crypto.Cypher}
 * ships with a hard-coded English alphabet. To add Ukrainian support, subclass
 * Language with a Ukrainian alphabet and refactor Cypher to accept a Language
 * instead of using its hard-coded list.
 */
public abstract class Language {

    private final List<Character> alphabet;

    protected Language(List<Character> alphabet) {
        this.alphabet = alphabet;
    }

    public List<Character> getAlphabet() {
        return alphabet;
    }
}
```

- [ ] **Step 2: Create `EnglishLanguage.java`**

```java
package ua.com.javarush.gnew.language;

import java.util.List;

public class EnglishLanguage extends Language {

    public EnglishLanguage() {
        super(List.of(
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'));
    }
}
```

- [ ] **Step 3: Verify compile**

Run: `mvn -q compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/ua/com/javarush/gnew/language/Language.java src/main/java/ua/com/javarush/gnew/language/EnglishLanguage.java
git commit -m "$(cat <<'EOF'
Expand Language abstract class; add EnglishLanguage subclass

Language is now a real (minimal) base class with an alphabet accessor.
EnglishLanguage holds the 52-char A-Z+a-z alphabet. Class-level Javadoc
documents this as the extension point for the optional Ukrainian
bonus — Cypher itself stays decoupled and keeps its hard-coded alphabet.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 10: Add `EncryptEdgeCases` + `OriginalFileSafety` groups to `MainTest`

**Files:**
- Modify: `src/test/java/ua/com/javarush/gnew/MainTest.java`

Both groups exercise `Main.main` end-to-end via `-e` and are expected to **pass** against the starter (encrypt works).

- [ ] **Step 1: Add `EncryptEdgeCases` nested class**

Append inside `MainTest`, after the existing `EnglishTests` class (around line 270, before `UkrainianLanguageTest`):

```java
@Nested
@DisplayName("Encrypt edge cases")
class EncryptEdgeCases {

    @Test
    @DisplayName("empty file produces empty encrypted file")
    void emptyFile() throws IOException {
        Path testFile = createTestFile("empty.txt", "");
        Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, 5);
        assertEquals("", readFile(encryptedFile));
    }

    @Test
    @DisplayName("single letter file")
    void singleLetter() throws IOException {
        Path testFile = createTestFile("single.txt", "A");
        Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, 1);
        assertEquals("B", readFile(encryptedFile));
    }

    @Test
    @DisplayName("digits-only file is unchanged")
    void digitsOnly() throws IOException {
        Path testFile = createTestFile("digits.txt", "0123456789");
        Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, 5);
        assertEquals("0123456789", readFile(encryptedFile));
    }

    @Test
    @DisplayName("key=0 produces identical content")
    void keyZero() throws IOException {
        Path testFile = createTestFile("k0.txt", "Hello, World!");
        Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, 0);
        assertEquals("Hello, World!", readFile(encryptedFile));
    }

    @Test
    @DisplayName("key=52 (full alphabet cycle) produces identical content")
    void keyFullCycle() throws IOException {
        Path testFile = createTestFile("k52.txt", "Hello");
        Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, 52);
        assertEquals("Hello", readFile(encryptedFile));
    }

    @Test
    @DisplayName("key=53 produces same output as key=1")
    void keyOverCycle() throws IOException {
        Path withK1 = execute(ENCRYPT_COMMAND, createTestFile("k1.txt", "Hello"), 1);
        Path withK53 = execute(ENCRYPT_COMMAND, createTestFile("k53.txt", "Hello"), 53);
        assertEquals(readFile(withK1), readFile(withK53));
    }

    @Test
    @DisplayName("key=-52 produces identical content (full cycle backwards)")
    void keyNegativeFullCycle() throws IOException {
        Path testFile = createTestFile("kneg52.txt", "Hello");
        Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, -52);
        assertEquals("Hello", readFile(encryptedFile));
    }

    @Test
    @DisplayName("special characters pass through unchanged")
    void specialCharsPassThrough() throws IOException {
        Path testFile = createTestFile("special.txt", ".,!? \t");
        Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, 5);
        assertEquals(".,!? \t", readFile(encryptedFile));
    }

    @Test
    @DisplayName("multiline content preserves newlines")
    void multilineContent() throws IOException {
        Path testFile = createTestFile("multi.txt", "abc\ndef\n");
        Path encryptedFile = execute(ENCRYPT_COMMAND, testFile, 1);
        assertEquals("bcd\nefg\n", readFile(encryptedFile));
    }
}

@Nested
@DisplayName("Original file safety")
class OriginalFileSafety {

    @Test
    @DisplayName("encrypt does not modify the input file")
    void encryptDoesNotModifyInput() throws IOException {
        String original = "Hello, World!";
        Path testFile = createTestFile("safety.txt", original);
        execute(ENCRYPT_COMMAND, testFile, 5);
        assertEquals(original, Files.readString(testFile));
    }
}
```

- [ ] **Step 2: Run the new groups**

Run: `mvn -q -Dtest='MainTest$EncryptEdgeCases,MainTest$OriginalFileSafety' test`
Expected: BUILD SUCCESS, all new tests pass

- [ ] **Step 3: Run full MainTest to confirm no regressions in untouched groups**

Run: `mvn -q -Dtest=MainTest test`
Expected: same pass/fail mix as the baseline — encrypt-side tests pass, decrypt/BF tests still fail with "No new file was created" (intentional, students implement them).

- [ ] **Step 4: Commit**

```bash
git add src/test/java/ua/com/javarush/gnew/MainTest.java
git commit -m "$(cat <<'EOF'
Add EncryptEdgeCases and OriginalFileSafety groups to MainTest

EncryptEdgeCases covers empty file, single char, digits-only, key=0,
key=52 (full cycle = identity), key=53 (= key=1), key=-52, special
char pass-through, and multiline content. OriginalFileSafety asserts
that the input file on disk is unchanged after an encrypt run.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 11: Add `DecryptFilenameTransformation` group + expand `ValidationTests`

**Files:**
- Modify: `src/test/java/ua/com/javarush/gnew/MainTest.java`

`DecryptFilenameTransformation` is intentionally failing — depends on students implementing DECRYPT. `ValidationTests` expansion all pass (parser errors are swallowed, no new file).

- [ ] **Step 1: Add `DecryptFilenameTransformation` to the existing `FileTests` nested class**

Inside `FileTests`, after `BruteForceFileTests` (around line 213 in pre-Task-2 numbering, ~120 in post-Task-2):

```java
@Nested
@DisplayName("DECRYPT filename transformation")
class DecryptFilenameTransformation {

    @Test
    @DisplayName("decrypting foo [ENCRYPTED].txt produces foo [DECRYPTED].txt (not double-suffixed)")
    void replacesEncryptedMarker() {
        Path encrypted = execute(ENCRYPT_COMMAND, inputFilePathEN, 5);
        Path decrypted = execute(DECRYPT_COMMAND, encrypted, 5);
        String decryptedName = decrypted.getFileName().toString();
        assertTrue(decryptedName.contains("[DECRYPTED]"),
                "Expected '[DECRYPTED]' in name: " + decryptedName);
        assertFalse(decryptedName.contains("[ENCRYPTED]"),
                "Decrypted file should not still carry '[ENCRYPTED]' marker: " + decryptedName);
    }
}
```

- [ ] **Step 2: Expand `ValidationTests`** — add inside the existing `ValidationTests` class:

```java
@Test
@DisplayName("Missing -k is handled (no new file created, no exception)")
void missingKey() {
    List<Path> before = listFiles(tempDir);
    String[] params = {ENCRYPT_COMMAND, "-f", inputFilePathEN.toString()};
    assertDoesNotThrow(() -> Main.main(params));
    assertEquals(before, listFiles(tempDir),
            "No new file should be created when -k is missing");
}

@Test
@DisplayName("Missing -f is handled")
void missingFile() {
    List<Path> before = listFiles(tempDir);
    String[] params = {ENCRYPT_COMMAND, "-k", "5"};
    assertDoesNotThrow(() -> Main.main(params));
    assertEquals(before, listFiles(tempDir));
}

@Test
@DisplayName("Missing command is handled")
void missingCommand() {
    List<Path> before = listFiles(tempDir);
    String[] params = {"-k", "5", "-f", inputFilePathEN.toString()};
    assertDoesNotThrow(() -> Main.main(params));
    assertEquals(before, listFiles(tempDir));
}

@Test
@DisplayName("Unknown flag is handled")
void unknownFlag() {
    List<Path> before = listFiles(tempDir);
    String[] params = {ENCRYPT_COMMAND, "-x", "-k", "5", "-f", inputFilePathEN.toString()};
    assertDoesNotThrow(() -> Main.main(params));
    assertEquals(before, listFiles(tempDir));
}

@Test
@DisplayName("Non-numeric key is handled")
void nonNumericKey() {
    List<Path> before = listFiles(tempDir);
    String[] params = {ENCRYPT_COMMAND, "-k", "abc", "-f", inputFilePathEN.toString()};
    assertDoesNotThrow(() -> Main.main(params));
    assertEquals(before, listFiles(tempDir));
}
```

- [ ] **Step 3: Run ValidationTests — expect pass**

Run: `mvn -q -Dtest='MainTest$ValidationTests' test`
Expected: BUILD SUCCESS, all tests pass

- [ ] **Step 4: Run DecryptFilenameTransformation — expect failure (intentional)**

Run: `mvn -q -Dtest='MainTest$FileTests$DecryptFilenameTransformation' test`
Expected: BUILD FAILURE with "No new file was created" (because DECRYPT is stubbed — the failure IS the assignment hook).

- [ ] **Step 5: Commit**

```bash
git add src/test/java/ua/com/javarush/gnew/MainTest.java
git commit -m "$(cat <<'EOF'
Add DecryptFilenameTransformation group + expanded ValidationTests

DecryptFilenameTransformation is intentionally failing — it asserts
that decrypting foo [ENCRYPTED].txt produces foo [DECRYPTED].txt
(not double-suffixed), which depends on students implementing the
DECRYPT command. ValidationTests expansion covers missing -k/-f/cmd,
unknown flags, and non-numeric keys — all swallowed by Main, asserted
via "no new file in @TempDir".

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 12: Migrate Ukrainian-test gating to system property

**Files:**
- Modify: `src/test/java/ua/com/javarush/gnew/MainTest.java`

- [ ] **Step 1: Delete the `UKRAINIAN_LANGUAGE_TEST` constant** (line 18 in the original, post-Task-2 location varies):

```java
private static final boolean UKRAINIAN_LANGUAGE_TEST = false;
```

- [ ] **Step 2: Change the `isUkrainianLanguageTestEnabled` method body**

The method currently looks like:

```java
private static boolean isUkrainianLanguageTestEnabled() {
    return UKRAINIAN_LANGUAGE_TEST;
}
```

Replace with:

```java
private static boolean isUkrainianLanguageTestEnabled() {
    // Enable via: mvn -DukrainianLanguageTest=true test
    return Boolean.getBoolean("ukrainianLanguageTest");
}
```

- [ ] **Step 3: Verify default (skipped)**

Run: `mvn -q -Dtest='MainTest$UkrainianLanguageTest' test`
Expected: BUILD SUCCESS with tests skipped (JUnit prints "tests skipped" or similar — no failures).

- [ ] **Step 4: Verify enabled (still fails, because Ukrainian alphabet isn't implemented)**

Run: `mvn -q -DukrainianLanguageTest=true -Dtest='MainTest$UkrainianLanguageTest' test`
Expected: BUILD FAILURE — the Ukrainian tests now actually run, and they fail because the Cypher's alphabet doesn't contain Ukrainian letters. This is the intended starter state.

- [ ] **Step 5: Commit**

```bash
git add src/test/java/ua/com/javarush/gnew/MainTest.java
git commit -m "$(cat <<'EOF'
Gate Ukrainian tests on system property instead of hard-coded constant

Was: edit MainTest to flip a boolean. Now: mvn -DukrainianLanguageTest=true
Easier for students to enable when starting the bonus task; easier for
CI matrix runs if anyone wants one.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 13: Add `maven-jar-plugin` to `pom.xml`

**Files:**
- Modify: `pom.xml`

Produces a runnable jar (`java -jar target/...jar`) as the PDF requires for the GitHub Release.

- [ ] **Step 1: Add the plugin block**

Inside `<plugins>` in `pom.xml`, after the existing `maven-surefire-report-plugin` block (line 37):

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <version>3.4.1</version>
    <configuration>
        <archive>
            <manifest>
                <mainClass>ua.com.javarush.gnew.Main</mainClass>
            </manifest>
        </archive>
    </configuration>
</plugin>
```

- [ ] **Step 2: Build the jar**

Run: `mvn -q -DskipTests package`
Expected: BUILD SUCCESS, `target/GNEW-M1-FP-1.0-SNAPSHOT.jar` exists.

- [ ] **Step 3: Verify the jar is runnable and produces output**

Run: `java -jar target/GNEW-M1-FP-1.0-SNAPSHOT.jar -e -k 5 -f src/main/resources/input.txt && ls -la "src/main/resources/input [ENCRYPTED].txt"`
Expected: file `src/main/resources/input [ENCRYPTED].txt` exists, command exits 0.

Note: `input.txt` will be replaced in Task 16, but for now the existing file is enough to smoke-test the jar.

- [ ] **Step 4: Check git status of the resources directory**

The smoke-test `java -jar` step above overwrote the pre-existing `input [ENCRYPTED].txt`. Leave it alone for now — Task 16 deletes it outright.

Run: `git status src/main/resources/`
Expected: ` M src/main/resources/input [ENCRYPTED].txt` (modified). Don't stage it.

- [ ] **Step 5: Commit**

```bash
git add pom.xml
git commit -m "$(cat <<'EOF'
Add maven-jar-plugin to produce a runnable jar

The PDF spec requires students to ship a runnable jar in GitHub
Releases. The starter's pom.xml previously had no manifest config,
so the produced jar wouldn't run. Sets Main-Class on the manifest.
No shade plugin needed — project has zero runtime dependencies.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 14: Update CI workflow to `mvn package`

**Files:**
- Modify: `.github/workflows/run_tests.yaml`

Catches "tests pass but jar broken" failures.

- [ ] **Step 1: Edit line 23**

Change:

```yaml
      - name: Run tests with Maven
        run: mvn --batch-mode --update-snapshots test
```

To:

```yaml
      - name: Run tests and package jar
        run: mvn --batch-mode --update-snapshots package
```

- [ ] **Step 2: Verify locally**

Run: `mvn -q package`
Expected: BUILD FAILURE in the `test` phase — surefire reports the decrypt/BF tests as failing (intentional starter state, same as `mvn test` would). CI will stay red on student forks until they implement enough to make all tests pass.

- [ ] **Step 3: Commit**

```bash
git add .github/workflows/run_tests.yaml
git commit -m "$(cat <<'EOF'
CI: mvn test -> mvn package

package runs tests AND builds the jar — catches "tests pass but
jar broken" regressions. CI continues to be red on student forks
until they implement decrypt and brute force, which is exactly
the intent.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 15: Add Maven wrapper

**Files:**
- Create: `mvnw`, `mvnw.cmd`
- Create: `.mvn/wrapper/maven-wrapper.properties`

Lets students run `./mvnw test` without needing a local Maven install.

- [ ] **Step 1: Generate wrapper files**

Run: `mvn -N wrapper:wrapper -Dmaven=3.9.6`
Expected: creates `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`.

If the local Maven is too old (3.6.x) and the command fails with "No plugin found for prefix 'wrapper'", use the explicit coordinates: `mvn -N org.apache.maven.plugins:maven-wrapper-plugin:3.2.0:wrapper -Dmaven=3.9.6`.

- [ ] **Step 2: Verify the wrapper works**

Run: `./mvnw -q -DskipTests package`
Expected: BUILD SUCCESS. First run downloads Maven 3.9.6 into `~/.m2/wrapper/dists/`; subsequent runs use the cache.

- [ ] **Step 3: Verify gitignore handles wrapper files correctly**

Run: `git status mvnw mvnw.cmd .mvn/`
Expected: all three are listed as new (untracked) files. The existing `.gitignore` has `!.mvn/wrapper/maven-wrapper.jar` exception — but newer wrapper versions don't ship a jar in `.mvn/wrapper/`, they download it on demand via the properties file. So the gitignore exception is harmless.

- [ ] **Step 4: Commit**

```bash
git add mvnw mvnw.cmd .mvn/
git commit -m "$(cat <<'EOF'
Add Maven wrapper (./mvnw)

Students can now run ./mvnw test without installing Maven locally —
removes a setup hurdle for a beginner-course starter. README will
point at ./mvnw as the primary entry point.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 16: Replace `input.txt` sample; delete stale `input [ENCRYPTED].txt`

**Files:**
- Modify: `src/main/resources/input.txt`
- Delete: `src/main/resources/input [ENCRYPTED].txt`

- [ ] **Step 1: Overwrite `input.txt`** with a short readable paragraph for manual jar testing:

```
The quick brown fox jumps over the lazy dog.
This sentence contains every letter of the English alphabet,
which makes it a useful sample for testing a Caesar cipher.
Special characters like commas, periods, and spaces should
pass through encryption unchanged.
```

- [ ] **Step 2: Delete `input [ENCRYPTED].txt`**

Run: `git rm "src/main/resources/input [ENCRYPTED].txt"`
Expected: removed from the index.

- [ ] **Step 3: Verify**

Run: `git status src/main/resources/`
Expected:
- ` M src/main/resources/input.txt` (modified)
- ` D src/main/resources/input [ENCRYPTED].txt` (deleted)

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/input.txt
git commit -m "$(cat <<'EOF'
Replace input.txt sample; delete stale [ENCRYPTED] artifact

input.txt now contains a clean pangram-style paragraph suitable for
manual jar testing. The previous "input [ENCRYPTED].txt" was stale
output from a manual run (not actual input data) — deleted.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 17: Create `CHECKLIST.md`

**Files:**
- Create: `CHECKLIST.md`

The test-to-task map. The artifact that turns the failing tests into a navigable assignment.

- [ ] **Step 1: Create the file**

```markdown
# Implementation checklist

The starter ships stubs for everything that's still your work. Each section below names the file to edit, the test that proves it works, and a one-line hint. Work top-to-bottom — items build on each other.

The canonical spec is `src/main/resources/project-description.pdf`. This checklist is just the test-driven view of it.

## 1. Implement `Cypher.decrypt`

- **File:** `src/main/java/ua/com/javarush/gnew/crypto/Cypher.java`
- **Watch:** `CypherTest` (add your own decrypt tests here), `MainTest$EnglishTests#decrypt`, `MainTest$EnglishTests#decryptedFileTextValidate`
- **Hint:** `encrypt(text, key)` and `decrypt(text, key)` are related by something simple. Look at the existing `encrypt` for the pattern.

Verify: `./mvnw -Dtest='CypherTest,MainTest$EnglishTests#decrypt' test`

## 2. Wire `DECRYPT` into `Main`

- **File:** `src/main/java/ua/com/javarush/gnew/Main.java`
- **Watch:** `MainTest$FileTests$DecryptFileTests`, `MainTest$FileTests$DecryptFilenameTransformation`
- **Hint:** Mirror the `ENCRYPT` branch. Use `EncryptedFileNamer.forDecrypted` for the output filename — it handles the `[ENCRYPTED]` → `[DECRYPTED]` swap.

Verify: `./mvnw -Dtest='MainTest$FileTests$DecryptFileTests,MainTest$FileTests$DecryptFilenameTransformation' test`

## 3. Implement `BruteForce.bruteForce`

- **File:** `src/main/java/ua/com/javarush/gnew/crypto/BruteForce.java` (and wire it into `Main`'s `BRUTEFORCE` branch)
- **Watch:** `MainTest$EnglishTests#bruteForceEN`, `MainTest$FileTests$BruteForceFileTests`
- **Hint:** Try every plausible key, score each candidate output, pick the best. Two textbook ways to score: (a) count occurrences of common words like "the", "and", "of"; (b) compare letter-frequency distribution against expected English.

Verify: `./mvnw -Dtest='MainTest$EnglishTests#bruteForceEN' test`

## 4. Cover edge cases

Most of these are already passing thanks to the existing `encrypt` implementation. Re-verify after you add decrypt:

- `MainTest$EncryptEdgeCases` — empty file, key=0, key=52, special chars, multiline.
- `MainTest$OriginalFileSafety` — encrypt doesn't modify the input file.
- `MainTest$ValidationTests` — missing flags, unknown flags, non-numeric key.

Verify everything: `./mvnw test`

## 5. (Bonus) Ukrainian alphabet

The PDF lists Ukrainian-language support as the easiest bonus. Tests for it exist already, gated by a system property:

- **Files:** `src/main/java/ua/com/javarush/gnew/language/Language.java` (subclass it for Ukrainian), then refactor `Cypher` to accept a `Language`.
- **Watch:** `MainTest$UkrainianLanguageTest`
- **Hint:** The 33-letter Ukrainian alphabet is А Б В Г Ґ Д Е Є Ж З И І Ї Й К Л М Н О П Р С Т У Ф Х Ц Ч Ш Щ Ь Ю Я (plus lowercase).

Verify: `./mvnw -DukrainianLanguageTest=true -Dtest='MainTest$UkrainianLanguageTest' test`

## 6. Build the jar for your Release

`./mvnw package` produces `target/GNEW-M1-FP-1.0-SNAPSHOT.jar`. Test it manually:

```
java -jar target/GNEW-M1-FP-1.0-SNAPSHOT.jar -e -k 5 -f src/main/resources/input.txt
java -jar target/GNEW-M1-FP-1.0-SNAPSHOT.jar -d -k 5 -f "src/main/resources/input [ENCRYPTED].txt"
java -jar target/GNEW-M1-FP-1.0-SNAPSHOT.jar -bf -f "src/main/resources/input [ENCRYPTED].txt"
```

Upload the jar to GitHub Releases on your fork.
```

- [ ] **Step 2: Commit**

```bash
git add CHECKLIST.md
git commit -m "$(cat <<'EOF'
Add CHECKLIST.md mapping student tasks to tests

Single-page view: what to implement, which test proves it works,
and a one-line hint per task. Includes the Ukrainian bonus path.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 18: Create PR template

**Files:**
- Create: `.github/PULL_REQUEST_TEMPLATE.md`

- [ ] **Step 1: Create the file**

```markdown
## What I implemented

<!-- Core features and any bonus tasks you tackled. -->

## What I couldn't get to work

<!-- Anything from the core requirements you left unfinished, and why.
     This is the most useful section for the mentor — be honest. -->

## Notable decisions / interesting choices

<!-- Design choices worth pointing out: a non-obvious algorithm, a
     refactor you made, a class structure you're proud of. -->

## What to focus on during review

<!-- Where you'd most like mentor attention. -->
```

- [ ] **Step 2: Commit**

```bash
git add .github/PULL_REQUEST_TEMPLATE.md
git commit -m "$(cat <<'EOF'
Add PR template with the four mentor-review questions

Surfaces the "what worked / what didn't / interesting choices /
focus areas" questions from the PDF spec at PR time, where the
mentor actually reads them.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 19: Rewrite `readme.md`

**Files:**
- Modify: `readme.md`

- [ ] **Step 1: Replace contents**

```markdown
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
./mvnw test                          # run the whole test suite
./mvnw -Dtest=CypherTest test        # run one test class
./mvnw -DukrainianLanguageTest=true test   # also run the Ukrainian-bonus tests
./mvnw package                       # build target/GNEW-M1-FP-1.0-SNAPSHOT.jar
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
```

- [ ] **Step 2: Commit**

```bash
git add readme.md
git commit -m "$(cat <<'EOF'
Rewrite readme.md: context, fork->PR flow, what's done vs to-do

Previous readme was just CLI examples with no context. New version
explains the fork->implement->PR workflow, names what the starter
already ships, points at CHECKLIST.md for the test-to-task map,
and flags the option-style CLI divergence from the PDF.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Task 20: Update `CLAUDE.md` to reflect the new shape

**Files:**
- Modify: `CLAUDE.md`

- [ ] **Step 1: Update the architecture section**

Open `CLAUDE.md` and find the "## Architecture" section. Replace the bullet list (the lines starting with `- \`Main\` — entry point...` through the `\`language.Language\`` bullet) with:

```markdown
- `Main` — entry point. `switch (command)` dispatches to ENCRYPT (working), DECRYPT and BRUTEFORCE (throw `UnsupportedOperationException` — student work). Exceptions are caught and printed (`Main.main` must not propagate — pinned by `ValidationTests`).
- `crypto.Cypher` — Caesar cipher. `ArrayList<Character>` alphabet of A–Z + a–z, `Math.negateExact(key)` + `Collections.rotate`. `encrypt` works; `decrypt` is a stub.
- `crypto.BruteForce` — single-method stub class. Students implement.
- `file.EncryptedFileNamer` — owns the `[ENCRYPTED]` / `[DECRYPTED]` filename suffix rules. `forDecrypted` swaps an existing `[ENCRYPTED]` marker rather than appending, so decrypting `foo [ENCRYPTED].txt` produces `foo [DECRYPTED].txt`.
- `file.FileManager` — thin wrapper around `Files.readString` / `Files.writeString`.
- `runner.ArgumentsParser` → `runner.RunOptions` → consumed by `Main`. `Command` is an enum (`ENCRYPT`, `DECRYPT`, `BRUTEFORCE`).
- `language.Language` + `language.EnglishLanguage` — minimal extension point for the optional Ukrainian-alphabet bonus. The core `Cypher` does NOT consume `Language` — kept hard-coded by design.
```

- [ ] **Step 2: Update the "Test suite shape" section**

Find the section starting `## Test suite shape`. Replace its body with:

```markdown
End-to-end tests live in `src/test/java/.../MainTest.java`. They drive `Main.main(...)` with a `@TempDir` and assert on the file the run creates (diffing directory listings before/after). Test fixtures (Hamlet, Orwell) live in `src/test/resources/hamlet.txt` and `orwell.txt`, loaded via `MainTest.loadResource`.

Nested groups:
- `FileTests` — file creation, markers, `DecryptFilenameTransformation` (intentionally failing — depends on student-implemented DECRYPT).
- `EnglishTests` — Hamlet round-trip + brute force.
- `EncryptEdgeCases` — empty file, key=0/52/53/-52, special chars, multiline.
- `OriginalFileSafety` — input file is unchanged after encrypt.
- `UkrainianLanguageTest` — gated on `-DukrainianLanguageTest=true` system property.
- `ValidationTests` — missing/unknown flags, non-numeric keys, non-existent file. All assert via "no new file appears in `@TempDir`".

Focused unit-test classes (don't go through `Main.main`):
- `CypherTest` — encrypt edge cases at unit level. Place students put their `decrypt` unit tests when they implement it.
- `ArgumentsParserTest` — happy paths in arbitrary order + every error condition.
- `EncryptedFileNamerTest` — filename transformation rules.

Brute-force tests assume the result matches the original text exactly (case-sensitive). Students must recover the correct key, not just *some* readable shift.
```

- [ ] **Step 3: Update the "Working with this repo" section**

Find the section starting `## Working with this repo`. Replace its body with:

```markdown
- Maven wrapper is included — use `./mvnw test` rather than `mvn test`. First run downloads Maven into `~/.m2/wrapper/dists/`.
- `pom.xml` configures `maven-jar-plugin` with `Main-Class: ua.com.javarush.gnew.Main`. `./mvnw package` produces a runnable jar in `target/`.
- CI runs `mvn package` — fails (intentionally) until students implement enough to make all tests pass. CI being red on a student fork IS the signal.
- `CHECKLIST.md` is the test-to-task map students follow. Keep it in sync if you add/remove tests.
- `src/main/resources/input.txt` is a sample paragraph for manual `java -jar` testing — not used by automated tests.
- Don't reformat the starter classes for cosmetic reasons. Students are graded against this baseline; noisy diffs hurt review.
```

- [ ] **Step 4: Commit**

```bash
git add CLAUDE.md
git commit -m "$(cat <<'EOF'
Update CLAUDE.md to reflect new starter shape

Architecture section lists new classes (BruteForce, EncryptedFileNamer,
EnglishLanguage). Test section names the new nested groups and focused
unit-test classes. Working-with-the-repo section mentions ./mvnw,
mvn package, and CHECKLIST.md as the test-to-task map.

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## Post-implementation verification

After all tasks are committed:

- [ ] **Run the full test suite** — `./mvnw test`. Expected: encrypt-side tests pass; decrypt/BF tests fail with "No new file was created" (intentional starter state).
- [ ] **Build the jar** — `./mvnw package` builds `target/GNEW-M1-FP-1.0-SNAPSHOT.jar` (after surefire reports failures, but the build does fail at test phase — that's correct).
- [ ] **Smoke-test the jar** — `java -jar target/GNEW-M1-FP-1.0-SNAPSHOT.jar -e -k 5 -f src/main/resources/input.txt` produces `src/main/resources/input [ENCRYPTED].txt`.
- [ ] **Ukrainian flag** — `./mvnw -DukrainianLanguageTest=true -Dtest='MainTest$UkrainianLanguageTest' test` actually runs the Ukrainian group (and fails, intentionally, because the alphabet doesn't include Ukrainian letters).
- [ ] **Git status clean** — only the stash from Task 1 remains.
