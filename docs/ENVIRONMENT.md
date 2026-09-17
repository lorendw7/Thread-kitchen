# Environment Setup and Verification

Use this checklist before writing queue code. A healthy environment can compile the
project, run tests, and use the same Java version from both the IDE and terminal.

## Required tools

- JDK 21 (not only a JRE)
- IntelliJ IDEA Community or Ultimate
- Maven 3.9+ through either:
  - IntelliJ's bundled Maven for IDE-only work, or
  - a separate Maven installation for terminal commands
- Git

## 1. Verify Java in PowerShell

Run:

```powershell
java -version
javac -version
```

Both commands must report major version `21`. If `java` works but `javac` does not,
a JRE or an incomplete `PATH` is being used instead of a JDK.

Optional path checks:

```powershell
$env:JAVA_HOME
Get-Command java
Get-Command javac
```

`JAVA_HOME` should point to the JDK directory, and `java` / `javac` should resolve
to that installation's `bin` directory.

## 2. Choose and verify Maven

### Option A — terminal Maven (recommended)

Install Maven 3.9+, add its `bin` directory to `PATH`, open a new PowerShell window,
then run:

```powershell
mvn -version
```

Check both facts in the output:

1. Maven reports version 3.9 or newer.
2. Its `Java version` is 21.

### Option B — IntelliJ bundled Maven

In IntelliJ open:

`File > Settings > Build, Execution, Deployment > Build Tools > Maven`

Set **Maven home path** to **Bundled (Maven 3)**. This is enough to run Maven from
IntelliJ's Maven tool window, but it does **not** make `mvn` available in an ordinary
PowerShell terminal.

## 3. Verify the IntelliJ project

1. Open the repository root, not the `src` directory.
2. Set **Project SDK** to JDK 21.
3. Set **language level** to `21` or `SDK default`.
4. Reload the Maven project from the Maven tool window.
5. Confirm `src/main/java` is marked as Sources and `src/test/java` as Test Sources.

## 4. Run the acceptance checks

From a terminal with Maven configured:

```powershell
mvn clean test
```

Or, in IntelliJ, run Maven lifecycle goals `clean` and `test`. The setup is ready
when all of the following are true:

- dependency import completes without red errors;
- compilation succeeds with Java 21;
- all tests pass (the repository may initially report zero tests);
- no generated `target/` files appear in Git changes;
- `git status --short` shows only changes you intentionally made.

## Current machine result (2026-09-17)

| Check | Result |
|-------|--------|
| `java -version` | Pass — Java 21.0.10 LTS |
| `javac -version` | Pass — javac 21.0.10 |
| IntelliJ bundled Maven | Pass — Maven 3.9.11 with Java 21.0.10 |
| IntelliJ bundled Maven `test` | Pass — project compiles; no test classes exist yet |
| Terminal `mvn -version` | Not configured — expected for the IDE-only workflow |

The selected IDE-only workflow is ready. Run future builds from IntelliJ's Maven
tool window. Terminal `mvn` remains optional and is not required for this project.

## Common failures

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| `mvn` is not recognized | Maven `bin` is not on `PATH` | Use IntelliJ bundled Maven or install Maven and reopen the terminal |
| `release version 21 not supported` | Maven is running on an older JDK | Correct `JAVA_HOME` and `PATH`, then verify with `mvn -version` |
| Dependencies stay red in IntelliJ | Maven import is stale/failed | Reload the Maven project and check Maven/JDK settings |
| Tests hang | A blocking test has no timeout or cleanup | Add timeouts and always stop/join worker threads |
| Test passes alone but fails in suite | Leaked thread or timing assumption | Use latches/barriers and deterministic cleanup, not arbitrary sleeps |
