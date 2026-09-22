package ch.zhaw.abyss.ui;

import ch.zhaw.abyss.domain.RoomPlan;

import javafx.scene.image.Image;
import javafx.scene.text.Font;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class AssetCatalog {
    private final Map<String, Image> images = new HashMap<>();
    private String textFamily = "System", displayFamily = "System";

    public AssetCatalog() {
        for (String filename :
                new String[] {
                    "Barlow-Regular.ttf", "Barlow-Medium.ttf", "BarlowCondensed-SemiBold.ttf"
                }) {
            try (var stream = AssetCatalog.class.getResourceAsStream("/fonts/" + filename)) {
                if (stream == null) continue;
                var font = Font.loadFont(stream, 24);
                if (font != null && filename.startsWith("BarlowCondensed"))
                    displayFamily = font.getFamily();
                else if (font != null) textFamily = font.getFamily();
            } catch (IOException ignored) {
            }
        }
        image("art/title-submarine.png");
        for (String room :
                new String[] {
                    "aft",
                    "engine",
                    "command",
                    "workshop",
                    "cargo",
                    "reactor",
                    "mess",
                    "cooling",
                    "commandhall",
                    "observatory",
                    "security",
                    "medbay",
                    "hydroponics",
                    "torpedo",
                    "ballast"
                }) image("art/rooms/" + room + ".png");
        for (String actor :
                new String[] {
                    "player", "scuttler", "drone", "sentinel", "captain", "warden", "reactor"
                }) {
            for (String pose : new String[] {"idle", "walk", "attack", "hurt", "dash"}) {
                int count = frames(actor, pose);
                for (int frame = 0; frame < count; frame++) actor(actor, pose, frame);
            }
        }
    }

    public Image image(String path) {
        return images.computeIfAbsent(
                path,
                key -> {
                    var resource = AssetCatalog.class.getResource("/" + key);
                    if (resource == null) throw new IllegalStateException("Grafik fehlt: " + key);
                    var image = new Image(resource.toExternalForm(), false);
                    if (image.isError())
                        throw new IllegalStateException(
                                "Grafik kann nicht geladen werden: " + key, image.getException());
                    return image;
                });
    }

    public Image room(RoomPlan plan) {
        String name = roomKey(plan);
        var image = image("art/rooms/" + name + ".png");
        return image == null ? image("art/rooms/aft.png") : image;
    }

    public String roomKey(RoomPlan plan) {
        if (plan.kind() == RoomPlan.Kind.WORKSHOP) return "workshop";
        if (plan.kind() == RoomPlan.Kind.CACHE) return plan.depth() == 13 ? "hydroponics" : "cargo";
        if (plan.branch() == 1 && plan.kind() == RoomPlan.Kind.ELITE)
            return switch (plan.depth() % 4) {
                case 0 -> "medbay";
                case 1 -> "security";
                case 2 -> "ballast";
                default -> "torpedo";
            };
        return switch (plan.depth()) {
            case 0 -> "aft";
            case 1 -> "cargo";
            case 2 -> "mess";
            case 3 -> "torpedo";
            case 4 -> "security";
            case 6 -> "engine";
            case 7 -> "cooling";
            case 8 -> "ballast";
            case 9, 10 -> "reactor";
            case 12 -> "observatory";
            case 13 -> "hydroponics";
            case 14 -> "medbay";
            case 15 -> "commandhall";
            case 16 -> "security";
            case 17 -> "command";
            default -> "aft";
        };
    }

    public int frames(String actor, String pose) {
        return switch (pose) {
            case "idle" -> 4;
            case "walk" -> actor.equals("player") ? 8 : 6;
            case "attack" -> actor.equals("player") ? 6 : 4;
            case "hurt" -> 2;
            case "dash" -> actor.equals("player") ? 2 : 0;
            default -> 1;
        };
    }

    public Image actor(String actor, String pose, int frame) {
        return image(
                "art/actors/" + actor + "/" + pose + "-" + String.format("%02d", frame) + ".png");
    }

    public Font text(double size) {
        return Font.font(textFamily, size);
    }

    public Font display(double size) {
        return Font.font(displayFamily, size);
    }

    public String textFamily() {
        return textFamily;
    }

    public String displayFamily() {
        return displayFamily;
    }
}
