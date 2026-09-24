# Testing training (Java)

Hands-on exercises on software engineering, for tailored for scientists. This is the Java sibling of
[training-testing-python](https://github.com/sefop/training-testing-python). The theory is in the [Book](https://github.com/sefop/sefop-training-hub/tree/main/book/).

## Installation

### 1. Install a JDK 21

You need a Java Development Kit (JDK), version 21. Any distribution works, e.g. 
[Eclipse Temurin](https://adoptium.net/) or [Amazon Corretto](https://aws.amazon.com/corretto/). Check it:

```bash
java -version
```

The output should mention version `21`. You do **not** need to install Maven: the repository ships the
Maven Wrapper (`mvnw`), which downloads the right Maven version on first use.

<details>
<summary>Optional: install Maven yourself</summary>

[Maven](https://maven.apache.org/) is the build tool that compiles the code and runs the tests. A system-wide
install is handy if you work on other Java projects. Use your package manager:

```bash
brew install maven          # macOS (Homebrew)
sudo apt install maven      # Ubuntu / Debian
sdk install maven           # macOS / Linux with SDKMAN! (https://sdkman.io)
scoop install maven         # Windows with Scoop (https://scoop.sh)
choco install maven         # Windows with Chocolatey (https://chocolatey.org), from an admin terminal
```

Or install it by hand:

1. Download the binary zip (`apache-maven-3.9.x-bin.zip`) from
   [maven.apache.org/download.cgi](https://maven.apache.org/download.cgi).
2. Unzip it somewhere stable, e.g. `C:\tools\apache-maven-3.9.9` or `~/tools/apache-maven-3.9.9`.
3. Add its `bin` folder to your `PATH`:
   - **Windows:** Start menu → *Edit the system environment variables* → *Environment Variables…* →
     select `Path` under *User variables* → *Edit* → *New* → paste `C:\tools\apache-maven-3.9.9\bin`.
   - **macOS / Linux:** add `export PATH="$HOME/tools/apache-maven-3.9.9/bin:$PATH"` to `~/.zshrc` or `~/.bashrc`.
4. Open a **new** terminal so it picks up the new `PATH`.

Check it (Maven also needs `JAVA_HOME` or `java` on the `PATH`, which step 1 already covers):

```bash
mvn -v
```

From then on, you can type `mvn` wherever this repository says `./mvnw` or `mvnw.cmd`, e.g. `mvn test`.
The wrapper stays the safer choice, because it always uses the Maven version the project was tested with.

</details>

### 2. Clone the repository

```bash
git clone https://github.com/sefop/sefop-training-java.git
cd sefop-training-java
```

### 3. Run all tests to check the project works

```bash
./mvnw test          # macOS / Linux / Git Bash
.\mvnw.cmd test      # Windows PowerShell (or mvnw.cmd test in cmd)
```

The first run takes a minute or two while Maven and the libraries download. It should end with something like:

```
[WARNING] Tests run: 18, Failures: 0, Errors: 0, Skipped: 6
[INFO] BUILD SUCCESS
```

The 6 skipped tests are the ones you'll write in exercise 1. Maven prints `[WARNING]` only because some
tests are skipped. `BUILD SUCCESS` is what matters. The run also writes a code coverage report to
`target/site/jacoco/index.html`.

On macOS / Linux, if you get `permission denied`, make the wrapper executable once: `chmod +x mvnw`.

### 4. (Optional) Open the project in IntelliJ IDEA

*File → Open* and select the repository folder. IntelliJ detects `pom.xml` and imports the project.
Make sure *File → Project Structure → SDK* points to your JDK 21.

## Exercises

Each exercise lives in its own folder under `src/main/java/sefop/`, with its tests in the matching folder
under `src/test/java/sefop/`, and has a README with the Java-specific instructions.

1. [Unit tests and coverage](src/main/java/sefop/unit_tests/README.md)
