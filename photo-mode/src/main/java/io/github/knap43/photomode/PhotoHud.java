package io.github.knap43.photomode;

import java.util.List;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.Util;

/**
 * Brief, fading text drawn above the (hidden) HUD. It would show in a screenshot taken
 * in the same couple of seconds, so it stays small and clears quickly.
 */
final class PhotoHud {
    private static final long MESSAGE_MS = 1600;
    private static final long HINT_MS = 6000;
    private static final long FADE_MS = 400;

    private static List<Component> lines = List.of();
    private static long shownAt;
    private static long duration;

    private PhotoHud() {}

    static void message(Component text) {
        show(List.of(text), MESSAGE_MS);
    }

    static void toggled(String key, boolean on) {
        message(Component.translatable(key, Component.translatable(on ? "options.on" : "options.off")));
    }

    static void hint() {
        show(List.of(
                Component.translatable("photomode.hint.title"),
                Component.translatable("photomode.hint.move"),
                Component.translatable("photomode.hint.wheel"),
                Component.translatable("photomode.hint.keys",
                        PhotoKeys.PLAYER.getTranslatedKeyMessage(),
                        PhotoKeys.DOF.getTranslatedKeyMessage(),
                        PhotoKeys.ISOMETRIC.getTranslatedKeyMessage(),
                        PhotoKeys.FREEZE.getTranslatedKeyMessage()),
                Component.translatable("photomode.hint.exit", PhotoKeys.TOGGLE.getTranslatedKeyMessage())),
                HINT_MS);
    }

    private static void show(List<Component> text, long ms) {
        lines = text;
        duration = ms;
        shownAt = Util.getMillis();
    }

    static void render(GuiGraphics graphics, DeltaTracker delta) {
        long age = Util.getMillis() - shownAt;
        if (lines.isEmpty() || age > duration) {
            return;
        }
        float alpha = Mth.clamp((duration - age) / (float) FADE_MS, 0.0F, 1.0F);
        int a = (int) (alpha * 255.0F);
        if (a < 8) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        int width = graphics.guiWidth();
        int y = 10;
        for (Component line : lines) {
            int x = (width - font.width(line)) / 2;
            graphics.drawString(font, line, x, y, (a << 24) | 0xFFFFFF, true);
            y += font.lineHeight + 2;
        }
    }
}
