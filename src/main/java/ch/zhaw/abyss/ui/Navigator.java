package ch.zhaw.abyss.ui;

import ch.zhaw.abyss.application.GameService;
import ch.zhaw.abyss.application.Loadout;
import ch.zhaw.abyss.application.Settings;
import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.ui.render.SpriteBank;

/**
 * Schnittstelle der Bildschirmklassen zum Fenster: Zugriff auf Dienste und Wechsel zwischen
 * Bildschirmen. Die Bildschirme enthalten keine Spielregeln, sie rufen nur Anwendungsfälle auf.
 */
interface Navigator {
    GameService service();

    GameRun run();

    SpriteBank bank();

    void title();

    void loadout();

    void wardrobe();

    void archive(int tab);

    void settings(boolean inGame);

    void help(boolean inGame);

    void play();

    void pause();

    void route();

    void reward();

    void shrine();

    void inventory();

    void map();

    void outcome();

    void startRun(Loadout loadout, long seed);

    void resume();

    void nextCycle();

    void applySettings(Settings settings);

    void toast(String text);

    void quit();
}
