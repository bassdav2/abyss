package ch.zhaw.abyss.ui;

import ch.zhaw.abyss.application.GameService;
import ch.zhaw.abyss.infrastructure.FileGameRepository;

import javafx.application.Application;
import javafx.stage.Stage;

public final class AbyssApplication extends Application {
    private GameWindow window;

    @Override
    public void start(Stage stage) {
        var repository = new FileGameRepository(FileGameRepository.defaultDirectory());
        window = new GameWindow(stage, new GameService(repository), getParameters().getNamed());
        window.show();
    }

    @Override
    public void stop() {
        if (window != null) window.close();
    }
}
