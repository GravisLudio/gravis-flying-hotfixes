package com.gravisludio.flyinghotfixes.gametest;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.schematics.cannon.SchematicannonBlockEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Printing one checklist must consume one book, not the stack. The printer needs a loaded schematic
 * to run, so the test marks it loaded by reflection and starts the print at 100 %; the checklist
 * update itself is skipped ({@code dontUpdateChecklist}) because it would need real schematic data.
 * When Create: Connected (which carries the same fix) is loaded as well, the result must still be
 * one book, not two.
 */
public class SchematicannonChecklistTests {
    private static final int BOOK_INPUT = 2;
    private static final int BOOK_OUTPUT = 3;
    private static final BlockPos CANNON = new BlockPos(1, 1, 1);

    @GameTest
    public void checklistConsumesOneBookNotTheStack(GameTestHelper helper) throws ReflectiveOperationException {
        // Not placed in the world: a cannon with a fake "loaded" printer would crash the server's own
        // tick (no anchor), and this test only needs tickPaperPrinter.
        SchematicannonBlockEntity cannon = new SchematicannonBlockEntity(helper.absolutePos(CANNON), AllBlocks.SCHEMATICANNON.defaultBlockState());

        Field loaded = cannon.printer.getClass().getDeclaredField("schematicLoaded");
        loaded.setAccessible(true);
        loaded.setBoolean(cannon.printer, true);
        cannon.dontUpdateChecklist = true;
        cannon.bookPrintingProgress = 1f;
        cannon.inventory.setItem(BOOK_INPUT, new ItemStack(AllItems.CLIPBOARD, 5));

        Method tick = SchematicannonBlockEntity.class.getDeclaredMethod("tickPaperPrinter");
        tick.setAccessible(true);
        tick.invoke(cannon);

        int left = cannon.inventory.getItem(BOOK_INPUT).getCount();
        int printed = cannon.inventory.getItem(BOOK_OUTPUT).getCount();
        if (left != 4 || printed != 1) {
            throw helper.assertionException(CANNON, Component.literal(
                    "5 clipboards in, expected 4 left and 1 printed, got %d left and %d printed".formatted(left, printed)));
        }
        helper.succeed();
    }
}
