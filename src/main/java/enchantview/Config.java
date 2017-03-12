package enchantview;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config {
   public static boolean transientEnchantment;

   public static void init(FMLPreInitializationEvent e) {
      Configuration config = new Configuration(e.getSuggestedConfigurationFile());
      config.load();

      transientEnchantment = config.getBoolean("transientEnchantment", Configuration.CATEGORY_GENERAL,
            true, "the enchantments may change without enchanting anything, like in 1.7.x");

      if (config.hasChanged())
         config.save();
   }
}
