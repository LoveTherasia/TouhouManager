package com.thmanager.util;

import com.thmanager.model.Game;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 解析游戏封面图路径（thmanager-frontend/image/cover/thXX_title.*）
 */
public final class GameCoverResolver {

    private static final Path COVER_ROOT = Paths.get(System.getProperty("user.dir"), "thmanager-frontend", "image", "cover");
    private static final String DEFAULT_COVER = "/image/main/default.svg";

    private GameCoverResolver() {
    }

    public static String resolve(int gameNumber) {
        String stem = String.format("th%02d_title", gameNumber);
        for (String ext : new String[] { ".jpg", ".png", ".webp" }) {
            Path file = COVER_ROOT.resolve(stem + ext);
            if (Files.exists(file)) {
                return "/image/cover/" + stem + ext;
            }
        }
        return DEFAULT_COVER;
    }

    public static void applyIfMissing(Game game) {
        if (game == null) {
            return;
        }
        if (game.getCoverImage() == null || game.getCoverImage().isBlank()) {
            game.setCoverImage(resolve(game.getGameNumber()));
        }
    }
}
