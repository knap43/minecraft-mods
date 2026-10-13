package io.github.knap43.arrowutilities;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

/**
 * Lenient on read: an arrow whose item no longer exists (a removed mod) is skipped
 * rather than failing the whole entity load.
 */
final class StuckArrowsSerializer implements IAttachmentSerializer<ListTag, List<ItemStack>> {
    @Override
    public List<ItemStack> read(IAttachmentHolder holder, ListTag tag, HolderLookup.Provider provider) {
        List<ItemStack> stacks = new ArrayList<>(tag.size());
        for (int i = 0; i < tag.size(); i++) {
            ItemStack.parse(provider, tag.getCompound(i))
                    .filter(stack -> !stack.isEmpty())
                    .ifPresent(stacks::add);
        }
        return List.copyOf(stacks);
    }

    @Override
    @Nullable
    public ListTag write(List<ItemStack> stacks, HolderLookup.Provider provider) {
        if (stacks.isEmpty()) {
            return null;
        }
        ListTag tag = new ListTag();
        for (ItemStack stack : stacks) {
            tag.add(stack.save(provider));
        }
        return tag;
    }
}
