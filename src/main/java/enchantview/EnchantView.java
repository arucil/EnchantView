package enchantview;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import enchantview.network.MessageEnchant;
import enchantview.network.PacketHandler;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

/**
 * The main EnchantView mod
 *
 * @author thebombzen
 */
@Mod(modid = EnchantView.ID, name = EnchantView.NAME, version = EnchantView.VERSION,
        acceptedMinecraftVersions = "[1.12.2]")
public class EnchantView {
    public static final String ID = "enchantview";
    public static final String NAME = "EnchantView";
    public static final String VERSION = "6.0.0";

    public static final Random rand = new Random();

    public static final Method methodGetEnchList = ReflectionHelper.getNameMatchedMethod(
            ContainerEnchantment.class,
            new String[]{"getEnchantmentList", "a", "func_178148_a"},
            new Class<?>[]{ItemStack.class, int.class, int.class});

    // client only
    public static int[][] enchantments;
    private static GuiEnchantment guiEnchant;

    // server-side event
    @SubscribeEvent
    public void onContainerOpen(PlayerContainerEvent.Open e) {
        e.getContainer().addListener(new ContainerListener((EntityPlayerMP) e.getEntityPlayer()));
    }

    // client-side event
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent e) {
        guiEnchant = e.getGui() instanceof GuiEnchantment ? (GuiEnchantment) e.getGui() : null;
    }

    // client-side event
    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onRenderTooltip(RenderTooltipEvent.Pre e) throws Exception {
        List<String> list = e.getLines();
        if (guiEnchant != null && list.get(0).endsWith(I18n.format("text.enchantClue.suffix"))) {
            int lv = getHoveringLevel(e.getY());
            if (enchantments[lv] != null) {
                // original list is wrapped in unmodifiable list
                Class[] classes = Collections.class.getDeclaredClasses();
                for (Class cl : classes) {
                    if ("java.util.Collections$UnmodifiableList".equals(cl.getName())) {
                        Field field = cl.getDeclaredField("list");
                        field.setAccessible(true);
                        List<String> orgList = (List<String>) field.get(list);
                        orgList.remove(0);

                        List<String> newList = new ArrayList<>();
                        for (int k : enchantments[lv]) {
                            newList.add(Enchantment.getEnchantmentByID(k >>> 16).getTranslatedName(k & 65535));
                        }
                        orgList.addAll(0, newList);
                    }
                }
            }
        }
    }

    private static int getHoveringLevel(int y) {
        // magic numbers are from GuiEnchantment.drawScreen()
        int H = 166;
        int i = (y - (guiEnchant.height - H) / 2 - 14) / 19;

        if (i < 0)
            i = 0;
        else if (i > 2)
            i = 2;
        return i;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(this);
        Config.init(e);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        PacketHandler.init();
    }

    private static class ContainerListener implements IContainerListener {
        private EntityPlayerMP player;

        public ContainerListener(EntityPlayerMP player) {
            this.player = player;
        }

        @Override
        public void sendAllContents(@Nonnull Container containerToSend, @Nonnull NonNullList<ItemStack> itemsList) {

        }

        @Override
        public void sendSlotContents(@Nonnull Container containerToSend, int slotInd, @Nonnull ItemStack stack) {
            if (0 == slotInd && containerToSend instanceof ContainerEnchantment) {
                ContainerEnchantment container = (ContainerEnchantment) containerToSend;

                if (Config.transientEnchantment) {
                    // player.removeExperienceLevel(0); // update xpSeed
                    player.onEnchant(null, 0); // update xpSeed
                    container.xpSeed = player.getXPSeed();
                    container.detectAndSendChanges();
                }

                int[][] ench = new int[3][];
                for (int j = 0; j < 3; ++j) {
                    List<EnchantmentData> list = ReflectionHelper.invokeMethod(methodGetEnchList,
                            container,
                            container.tableInventory.getStackInSlot(0), j, container.enchantLevels[j]);

                    int[] ench1 = new int[list.size()];

                    for (int i = 0; i < list.size(); ++i) {
                        EnchantmentData data = list.get(i);
                        ench1[i] = (Enchantment.getEnchantmentID(data.enchantment) << 16) + data.enchantmentLevel;
                    }
                    ench[j] = ench1;
                }

                PacketHandler.instance.sendTo(new MessageEnchant(ench), player);
            }
        }

        @Override
        public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {

        }

        @Override
        public void sendAllWindowProperties(Container containerIn, IInventory inventory) {
        }
    }
}
