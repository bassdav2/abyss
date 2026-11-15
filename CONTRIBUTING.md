# Mitarbeiten an ABYSS

Diese Seite fasst die Regeln zusammen, nach denen wir Code schreiben und prüfen. Die meisten Regeln
prüft der Build automatisch. Was der Build nicht prüfen kann, prüft das Review.

## Codier-Richtlinien

| Regel | Geprüft durch |
|---|---|
| Format nach google-java-format im AOSP-Stil (4 Leerzeichen, 100 Zeichen) | `./gradlew checkJavaFormat`; `./gradlew formatJava` formatiert |
| Keine Compiler-Warnungen (`-Xlint:all`) | `./gradlew build` |
| Javadoc für alles Öffentliche, auf Deutsch, vollständig mit `@param` und `@return` | `./gradlew javadoc` (`-Xdoclint`) |
| Schichten: `domain` kennt nur sich selbst und `java.util`; `application` kennt weder `ui` noch `infrastructure`; keine Paketzyklen | `ArchitectureTest` |
| Jede Änderung an Spielregeln hat einen Test in derselben Änderung | Review |
| Bezeichner auf Englisch, Kommentare und Spieltexte auf Deutsch | Review |
| Namen sagen, was etwas bedeutet. Einbuchstabige Namen nur für Schleifenzähler und in kurzen Lambdas | Review |
| Zahlen mit Bedeutung sind benannte Konstanten mit Einheit im Kommentar | Review |
| Neue Abhängigkeiten nur nach Teamentscheid; JavaFX bleibt die einzige Produktionsbibliothek | Review |

Vor jedem Pull Request lokal ausführen:

```sh
./gradlew test checkJavaFormat javadoc
```

Die Testabdeckung steht danach in `build/reports/jacoco/test/html/index.html`.

## Pull-Request-Prozess

1. **Branch** von `main` anlegen: `feature/…`, `fix/…` oder `refactor/…`.
2. **Klein bleiben:** ein Thema pro Pull Request, möglichst unter 400 geänderten Zeilen.
3. **Lokal prüfen** mit dem Befehl oben.
4. **Pull Request öffnen** und die Vorlage ausfüllen: Was ändert sich, warum, wie wurde getestet.
5. **Review:** Mindestens eine zweite Person liest die Änderung. Bei Änderungen in einem fremden
   Paket ist es die Person, die das Paket verantwortet (`docs/planung/Paketverantwortung.md`).
6. **Kommentare** werden beantwortet oder umgesetzt. Offene Punkte blockieren das Mergen.
7. **Mergen** per Squash, sobald das Review zustimmt und alle Prüfungen grün sind. Direkte Pushes
   auf `main` sind nicht vorgesehen.

### Worauf das Review achtet

- Stimmt die Schicht? Gehört die Regel in die Domäne statt in die Oberfläche?
- Gibt es einen Test, der ohne die Änderung fehlschlägt?
- Versteht man Namen und Ablauf ohne Rückfrage?
- Ist KI-Unterstützung in `docs/KI_EINSATZ.md` nachgetragen?
