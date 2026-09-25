package ch.zhaw.abyss.domain;

/**
 * Strategie für das Verhalten einer Gegnerart. Implementierungen sind zustandslos und werden von
 * allen Gegnern derselben Art geteilt.
 */
interface EnemyBehavior {
    /**
     * Entscheidet Bewegung, Zustandswechsel und Angriffe für einen Simulationsschritt.
     *
     * @param enemy gesteuerter Gegner
     * @param run laufender Tauchgang
     * @param dt bereits um Kälte verlangsamte Schrittweite in Sekunden
     */
    void update(Enemy enemy, GameRun run, double dt);
}
