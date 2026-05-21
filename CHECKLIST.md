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

## 3. Implement `BruteForce.bruteForce` and wire it into `Main`

- **Files:** `src/main/java/ua/com/javarush/gnew/crypto/BruteForce.java` and `Main.java`
- **Watch:** `MainTest$EnglishTests#bruteForceEN`, `MainTest$FileTests$BruteForceFileTests`
- **Hint:** Try every plausible key, score each candidate output, pick the best. Two textbook ways to score: (a) count occurrences of common words like "the", "and", "of"; (b) compare letter-frequency distribution against expected English.

Verify: `./mvnw -Dtest='MainTest$EnglishTests#bruteForceEN' test`

## 4. Cover edge cases

Most of these already pass thanks to the existing `encrypt` implementation. Re-verify after you add decrypt:

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
