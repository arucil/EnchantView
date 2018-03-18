package enchantview.network;

import enchantview.EnchantView;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
    public static SimpleNetworkWrapper instance;

    public static void init() {
        instance = NetworkRegistry.INSTANCE.newSimpleChannel(EnchantView.ID);

        instance.registerMessage(MessageEnchant.Handler.class, MessageEnchant.class, 0, Side.CLIENT);
    }
}
