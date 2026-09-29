package ch.zhaw.abyss.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Räumliches Raster über die lebenden Gegner eines Raums. Treffer-, Explosions- und Suchabfragen
 * prüfen nur die Zellen in Reichweite statt aller Gegner; so bleiben auch Schwärme mit mehreren
 * hundert Gegnern pro Simulationsschritt günstig. Abfragen liefern Gegner in Spawn-Reihenfolge,
 * damit Zufallszüge und damit ganze Tauchgänge deterministisch bleiben.
 */
final class EnemyGrid {
    /** Kantenlänge einer Zelle in Welteinheiten. */
    static final double CELL = 160;

    private static final Comparator<Enemy> SPAWN_ORDER = Comparator.comparingLong(e -> e.id);
    private List<List<Enemy>> cells = newCells(1);
    private int columns = 1, rows = 1;

    /** Anzahl Gegner in der Liste beim letzten Aufbau. */
    int built = -1;

    private final List<Enemy> scratch = new ArrayList<>();

    private static List<List<Enemy>> newCells(int count) {
        var result = new ArrayList<List<Enemy>>(count);
        for (int i = 0; i < count; i++) result.add(new ArrayList<>());
        return result;
    }

    /**
     * Baut das Raster neu auf. Unverwundbare und besiegte Gegner werden nicht eingetragen.
     *
     * @param enemies alle Gegner des Raums
     * @param width Raumbreite
     */
    void rebuild(List<Enemy> enemies, double width) {
        built = enemies.size();
        int needColumns = Math.max(1, (int) Math.ceil((width + 400) / CELL));
        int needRows = (int) Math.ceil((GameRun.HEIGHT + 400) / CELL);
        if (needColumns != columns || needRows != rows) {
            columns = needColumns;
            rows = needRows;
            cells = newCells(columns * rows);
        } else for (var cell : cells) cell.clear();
        for (var e : enemies)
            if (e.alive() && !e.untargetable()) cells.get(index(e.x, e.centerY())).add(e);
    }

    private int column(double x) {
        return Math.max(0, Math.min(columns - 1, (int) Math.floor((x + 200) / CELL)));
    }

    private int row(double y) {
        return Math.max(0, Math.min(rows - 1, (int) Math.floor((y + 200) / CELL)));
    }

    private int index(double x, double y) {
        return row(y) * columns + column(x);
    }

    /**
     * Sammelt alle eingetragenen Gegner, deren Mittelpunkt in der Zellumgebung des Rechtecks liegt.
     * Die genaue Trefferprüfung bleibt beim Aufrufer.
     *
     * @param left linke Kante
     * @param top obere Kante
     * @param right rechte Kante
     * @param bottom untere Kante
     * @return neue Liste in Spawn-Reihenfolge
     */
    List<Enemy> query(double left, double top, double right, double bottom) {
        // Gegner sind höchstens ~200 Einheiten breit: eine Zelle Rand reicht für Überlappung.
        int c0 = column(left - CELL), c1 = column(right + CELL);
        int r0 = row(top - CELL), r1 = row(bottom + CELL);
        var result = new ArrayList<Enemy>();
        for (int r = r0; r <= r1; r++)
            for (int c = c0; c <= c1; c++) result.addAll(cells.get(r * columns + c));
        if (result.size() > 1) result.sort(SPAWN_ORDER);
        return result;
    }

    /**
     * @param x Mittelpunkt
     * @param y Mittelpunkt
     * @param radius Suchradius
     * @return Kandidaten im Quadrat um den Kreis
     */
    List<Enemy> around(double x, double y, double radius) {
        return query(x - radius, y - radius, x + radius, y + radius);
    }

    /**
     * Nächster Gegner zum Punkt innerhalb der Reichweite.
     *
     * @param x Punkt
     * @param y Punkt
     * @param range Reichweite
     * @param exclude auszulassender Gegner oder {@code null}
     * @return nächster Gegner oder {@code null}
     */
    Enemy nearest(double x, double y, double range, Enemy exclude) {
        Enemy best = null;
        double bestDistance = range;
        for (var e : around(x, y, range)) {
            if (e == exclude || !e.alive()) continue;
            double d = Math.hypot(e.x - x, e.centerY() - y);
            if (d < bestDistance) {
                bestDistance = d;
                best = e;
            }
        }
        return best;
    }

    /**
     * Die {@code count} nächsten Gegner innerhalb der Reichweite, nach Abstand sortiert.
     *
     * @param x Punkt
     * @param y Punkt
     * @param range Reichweite
     * @param count Höchstzahl
     * @return nächste Gegner
     */
    List<Enemy> nearest(double x, double y, double range, int count) {
        scratch.clear();
        for (var e : around(x, y, range))
            if (e.alive() && Math.hypot(e.x - x, e.centerY() - y) <= range) scratch.add(e);
        var sorted = scratch.toArray(new Enemy[0]);
        Arrays.sort(
                sorted,
                Comparator.comparingDouble((Enemy e) -> Math.hypot(e.x - x, e.centerY() - y))
                        .thenComparingLong(e -> e.id));
        return List.of(sorted).subList(0, Math.min(count, sorted.length));
    }

    /**
     * Stösst eng stehende Schwarmgegner auseinander, damit sich Horden auffächern statt zu einem
     * Punkt zu verschmelzen. Bosse, Geschütze und versteckte Gegner bleiben unberührt.
     *
     * @param dt Schrittweite
     */
    void separate(double dt) {
        for (var cell : cells) {
            int n = cell.size();
            if (n < 2) continue;
            for (int i = 0; i < n; i++) {
                var a = cell.get(i);
                if (!movable(a)) continue;
                for (int j = i + 1; j < n; j++) {
                    var b = cell.get(j);
                    if (!movable(b) || a.flying() != b.flying()) continue;
                    double dx = b.x - a.x, dy = b.centerY() - a.centerY();
                    double reach = (a.width + b.width) * .38;
                    if (Math.abs(dx) >= reach || Math.abs(dy) >= reach) continue;
                    double overlap = reach - Math.abs(dx);
                    double push = Math.min(overlap, 220 * dt) * .5;
                    double side = dx == 0 ? (a.id < b.id ? -1 : 1) : Math.signum(dx);
                    a.x -= side * push;
                    b.x += side * push;
                    if (a.flying()) {
                        double vertical = Math.min(reach - Math.abs(dy), 160 * dt) * .5;
                        double up = dy == 0 ? (a.id < b.id ? -1 : 1) : Math.signum(dy);
                        a.y -= up * vertical;
                        b.y += up * vertical;
                    }
                }
            }
        }
    }

    private static boolean movable(Enemy e) {
        return !e.kind.boss()
                && e.kind != EnemyKind.TURRET
                && e.kind != EnemyKind.EEL
                && e.state != Enemy.State.HIDDEN;
    }
}
