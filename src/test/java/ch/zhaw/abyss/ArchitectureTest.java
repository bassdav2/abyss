package ch.zhaw.abyss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Prüft die Schichtenarchitektur automatisch anhand der Importe im Quellcode, ohne
 * Zusatzbibliothek. Wer eine verbotene Abhängigkeit einbaut, bekommt einen roten Test statt einer
 * stillen Erosion der Architektur.
 */
class ArchitectureTest {
    private static final Path SOURCES = Path.of("src/main/java/ch/zhaw/abyss");
    private static final Pattern IMPORT =
            Pattern.compile("^import\\s+(?:static\\s+)?([\\w.]+?)(?:\\.\\*)?;", Pattern.MULTILINE);

    /** Quelldatei relativ zu {@code ch/zhaw/abyss} → importierte Typen. */
    private static final Map<String, List<String>> IMPORTS = new TreeMap<>();

    @BeforeAll
    static void readSources() throws IOException {
        try (Stream<Path> files = Files.walk(SOURCES)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                var matcher = IMPORT.matcher(Files.readString(file));
                var imports = new ArrayList<String>();
                while (matcher.find()) imports.add(matcher.group(1));
                IMPORTS.put(SOURCES.relativize(file).toString().replace('\\', '/'), imports);
            }
        }
        assertTrue(IMPORTS.size() > 100, "Quellcode nicht gefunden: " + SOURCES.toAbsolutePath());
    }

    @Test
    void domainImportsOnlyItselfAndJavaUtil() {
        var violations = new TreeSet<String>();
        IMPORTS.forEach(
                (file, imports) -> {
                    if (!file.startsWith("domain/")) return;
                    for (var imported : imports)
                        if (!imported.startsWith("java.util.")
                                && !imported.startsWith("ch.zhaw.abyss.domain."))
                            violations.add(file + " → " + imported);
                });
        assertEquals(Set.of(), violations, "Die Domäne darf nur java.util und sich selbst kennen");
    }

    @Test
    void applicationKnowsNeitherUiNorInfrastructureNorJavaFx() {
        assertEquals(
                Set.of(),
                violations(
                        "application/",
                        "ch.zhaw.abyss.ui.",
                        "ch.zhaw.abyss.infrastructure.",
                        "javafx."));
    }

    @Test
    void infrastructureKnowsNeitherUiNorJavaFx() {
        assertEquals(Set.of(), violations("infrastructure/", "ch.zhaw.abyss.ui.", "javafx."));
    }

    @Test
    void renderingWorksWithoutJavaFx() {
        for (var part : List.of("ui/pixel/", "ui/art/", "ui/render/", "ui/gui/"))
            assertEquals(Set.of(), violations(part, "javafx."), part + " soll ohne Fenster laufen");
    }

    /**
     * Bekannte Abweichung: Die Präsentation erzeugt die Adapter beim Start selbst. Der Test hält
     * genau diese Stellen fest, damit keine weiteren dazukommen, bis ein eigener Startpunkt sie
     * übernimmt.
     */
    @Test
    void onlyTheStartupWiresInfrastructure() {
        var files = new TreeSet<String>();
        IMPORTS.forEach(
                (file, imports) -> {
                    if (file.startsWith("ui/")
                            && imports.stream()
                                    .anyMatch(i -> i.startsWith("ch.zhaw.abyss.infrastructure.")))
                        files.add(file);
                });
        assertEquals(Set.of("ui/AbyssApplication.java", "ui/GameWindow.java"), files);
    }

    /**
     * Gilt für alle Pakete unter ch.zhaw.abyss, also auch für neue wie das frühere {@code ports}.
     */
    @Test
    void packagesHaveNoCycles() {
        Map<String, Set<String>> uses = new TreeMap<>();
        IMPORTS.forEach(
                (file, imports) -> {
                    var from = packageOf(file);
                    if (from == null) return;
                    uses.computeIfAbsent(from, k -> new TreeSet<>());
                    for (var imported : imports) {
                        if (!imported.startsWith("ch.zhaw.abyss.")) continue;
                        var parts = imported.substring("ch.zhaw.abyss.".length()).split("\\.");
                        if (parts.length > 1 && !parts[0].equals(from))
                            uses.get(from).add(parts[0]);
                    }
                });
        for (var start : uses.keySet())
            assertTrue(
                    !reaches(uses, start, start, new TreeSet<>()),
                    "Zyklus über " + start + ": " + uses);
    }

    private static Set<String> violations(String prefix, String... forbidden) {
        var result = new TreeSet<String>();
        IMPORTS.forEach(
                (file, imports) -> {
                    if (!file.startsWith(prefix)) return;
                    for (var imported : imports)
                        for (var bad : forbidden)
                            if (imported.startsWith(bad)) result.add(file + " → " + imported);
                });
        return result;
    }

    private static String packageOf(String file) {
        var parts = file.split("/");
        return parts.length > 1 ? parts[0] : null;
    }

    private static boolean reaches(
            Map<String, Set<String>> uses, String from, String target, Set<String> seen) {
        for (var next : uses.getOrDefault(from, Set.of())) {
            if (next.equals(target)) return true;
            if (seen.add(next) && reaches(uses, next, target, seen)) return true;
        }
        return false;
    }
}
