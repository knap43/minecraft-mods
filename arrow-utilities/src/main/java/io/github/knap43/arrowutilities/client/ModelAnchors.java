package io.github.knap43.arrowutilities.client;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.knap43.arrowutilities.mixin.client.AgeableListModelAccessor;
import io.github.knap43.arrowutilities.mixin.client.ModelPartAccessor;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Every model part with geometry, together with the chain of parents that positions it,
 * so arrows can be planted on any entity model rather than only the player's.
 */
final class ModelAnchors {
    enum Group { NONE, BABY_HEAD, BABY_BODY }

    record Anchor(Group group, ModelPart[] chain) {
        ModelPart part() {
            return chain[chain.length - 1];
        }

        boolean visible() {
            for (ModelPart p : chain) {
                if (!p.visible) {
                    return false;
                }
            }
            return !part().skipDraw;
        }

        void apply(PoseStack pose, EntityModel<?> model) {
            if (group != Group.NONE && model.young && model instanceof AgeableListModelAccessor ageable) {
                if (group == Group.BABY_HEAD) {
                    if (ageable.arrowutilities$scaleHead()) {
                        float s = 1.5F / ageable.arrowutilities$babyHeadScale();
                        pose.scale(s, s, s);
                    }
                    pose.translate(0.0F, ageable.arrowutilities$babyYHeadOffset() / 16.0F,
                            ageable.arrowutilities$babyZHeadOffset() / 16.0F);
                } else {
                    float s = 1.0F / ageable.arrowutilities$babyBodyScale();
                    pose.scale(s, s, s);
                    pose.translate(0.0F, ageable.arrowutilities$bodyYOffset() / 16.0F, 0.0F);
                }
            }
            for (ModelPart p : chain) {
                p.translateAndRotate(pose);
            }
        }
    }

    private static final Map<EntityModel<?>, List<Anchor>> CACHE = new WeakHashMap<>();

    private ModelAnchors() {}

    static List<Anchor> visible(EntityModel<?> model) {
        List<Anchor> all = CACHE.computeIfAbsent(model, ModelAnchors::collect);
        List<Anchor> out = new ArrayList<>(all.size());
        for (Anchor a : all) {
            if (a.visible()) {
                out.add(a);
            }
        }
        return out;
    }

    private static List<Anchor> collect(EntityModel<?> model) {
        List<Anchor> anchors = new ArrayList<>();
        if (model instanceof AgeableListModel<?> && model instanceof AgeableListModelAccessor ageable) {
            for (ModelPart p : ageable.arrowutilities$headParts()) walk(p, Group.BABY_HEAD, new ArrayDeque<>(), anchors);
            for (ModelPart p : ageable.arrowutilities$bodyParts()) walk(p, Group.BABY_BODY, new ArrayDeque<>(), anchors);
        } else if (model instanceof HierarchicalModel<?> hierarchical) {
            walk(hierarchical.root(), Group.NONE, new ArrayDeque<>(), anchors);
        } else if (model instanceof ListModel<?> list) {
            for (ModelPart p : list.parts()) walk(p, Group.NONE, new ArrayDeque<>(), anchors);
        } else {
            for (ModelPart p : reflectRoots(model)) walk(p, Group.NONE, new ArrayDeque<>(), anchors);
        }
        return List.copyOf(anchors);
    }

    private static void walk(ModelPart part, Group group, Deque<ModelPart> chain, List<Anchor> out) {
        chain.addLast(part);
        if (!part.isEmpty()) {
            out.add(new Anchor(group, chain.toArray(ModelPart[]::new)));
        }
        for (ModelPart child : ((ModelPartAccessor) (Object) part).arrowutilities$children().values()) {
            walk(child, group, chain, out);
        }
        chain.removeLast();
    }

    /** Fallback for models with bespoke structure: every ModelPart field that is not nested in another. */
    private static List<ModelPart> reflectRoots(EntityModel<?> model) {
        // Ordered, so arrows land in the same spots every session.
        List<ModelPart> found = new ArrayList<>();
        Set<ModelPart> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Class<?> c = model.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || !ModelPart.class.isAssignableFrom(f.getType())) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    if (f.get(model) instanceof ModelPart p && seen.add(p)) {
                        found.add(p);
                    }
                } catch (ReflectiveOperationException | RuntimeException ignored) {
                    // Inaccessible module or similar; that part simply won't catch arrows.
                }
            }
        }
        Set<ModelPart> nested = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ModelPart p : found) {
            p.getAllParts().skip(1).forEach(nested::add);
        }
        List<ModelPart> roots = new ArrayList<>();
        for (ModelPart p : found) {
            if (!nested.contains(p)) {
                roots.add(p);
            }
        }
        return roots;
    }
}
