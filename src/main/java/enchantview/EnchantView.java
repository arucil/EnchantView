package enchantview;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import enchantview.network.MessageEnchant;
import enchantview.network.PacketHandler;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * The main EnchantView mod
 * @author thebombzen
 */
@Mod(modid = EnchantView.ID, name = EnchantView.NAME, version = EnchantView.VERSION,
      acceptedMinecraftVersions = "[1.10.2]")
public class EnchantView {
   public static final String ID = "enchantview";
   public static final String NAME = "EnchantView";
   public static final String VERSION = "6.0.0";

   public static final Random rand = new Random();

   public static final Method methodGetEnchList = ReflectionHelper.getNameMatchedMethod(
         ContainerEnchantment.class,
         new String[] { "getEnchantmentList", "a", "func_178148_a" },
         new Class<?>[] { ItemStack.class, int.class, int.class });

   // client only
   public static List<String>[] enchantments;
   public static GuiEnchantment guiEnchant;

   // server-side event
	@SubscribeEvent
	public void onContainerOpen(PlayerContainerEvent.Open e){
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
      if (guiEnchant != null && list.get(0).endsWith(I18n.translateToLocal("text.enchantClue.suffix"))) {
         int lv = getHoveringLevel(e.getY());
         if (enchantments[lv] != null) {
            // original list is wrapped in unmodifiable list
            Class[] classes = Collections.class.getDeclaredClasses();
            for (Class cl : classes) {
               if("java.util.Collections$UnmodifiableList".equals(cl.getName())) {
                  Field field = cl.getDeclaredField("list");
                  field.setAccessible(true);
                  List<String> org = (List<String>) field.get(list);
                  org.remove(0);
                  org.addAll(0, enchantments[lv]);
               }
            }
         }
      }
   }

   public static int getHoveringLevel(int y) {
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
      public void updateCraftingInventory(Container containerToSend, List<ItemStack> itemsList) { }

      @Override
      public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
         if (0 == slotInd && stack != null && containerToSend instanceof ContainerEnchantment) {
            ContainerEnchantment container = (ContainerEnchantment) containerToSend;

            if (Config.transientEnchantment) {
               container.xpSeed = rand.nextInt();
               container.detectAndSendChanges();
            }

            List<String>[] ench = new List[3];
            for (int j = 0; j < 3; ++j) {
               List<String> ench1 = ench[j] = new ArrayList<>();
               List<EnchantmentData> list = ReflectionHelper.invokeMethod(methodGetEnchList,
                     container,
                     container.tableInventory.getStackInSlot(0), j, container.enchantLevels[j]);

               for (int i = 0; i < list.size(); ++i){
                  EnchantmentData data = list.get(i);
                  ench1.add(data.enchantmentobj.getTranslatedName(data.enchantmentLevel));
               }
            }

            PacketHandler.instance.sendTo(new MessageEnchant(ench), player);
         }
      }

      @Override
      public void sendProgressBarUpdate(Container containerIn, int varToUpdate, int newValue) { }

      @Override
      public void sendAllWindowProperties(Container containerIn, IInventory inventory) { }
   }
}
