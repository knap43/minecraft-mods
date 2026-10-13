package io.github.knap43.photomode;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/** All rebindable under Options → Controls → Key Binds → Photo Mode. */
final class PhotoKeys {
    static final String CATEGORY = "key.categories.photomode";

    static final KeyMapping TOGGLE = key("toggle", GLFW.GLFW_KEY_P);
    static final KeyMapping PLAYER = key("player", GLFW.GLFW_KEY_B);
    static final KeyMapping DOF = key("dof", GLFW.GLFW_KEY_G);
    static final KeyMapping ISOMETRIC = key("isometric", GLFW.GLFW_KEY_I);
    static final KeyMapping FREEZE = key("freeze", GLFW.GLFW_KEY_Z);
    static final KeyMapping ROTATE_LEFT = key("rotate_left", GLFW.GLFW_KEY_LEFT);
    static final KeyMapping ROTATE_RIGHT = key("rotate_right", GLFW.GLFW_KEY_RIGHT);

    static final KeyMapping[] ALL = {TOGGLE, PLAYER, DOF, ISOMETRIC, FREEZE, ROTATE_LEFT, ROTATE_RIGHT};

    private PhotoKeys() {}

    private static KeyMapping key(String name, int glfwKey) {
        return new KeyMapping("key.photomode." + name, KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM, glfwKey, CATEGORY);
    }
}
