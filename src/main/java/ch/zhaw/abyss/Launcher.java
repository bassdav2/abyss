package ch.zhaw.abyss;

import ch.zhaw.abyss.ui.AbyssApplication;

import javafx.application.Application;

/** Separater Bootstrap erlaubt den Start auch aus einem paketierten Klassenpfad. */
public final class Launcher {
    private Launcher() {}

    public static void main(String[] args) {
        Application.launch(AbyssApplication.class, args);
    }
}
