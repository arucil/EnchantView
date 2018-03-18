package enchantview.network;

import enchantview.EnchantView;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


public class MessageEnchant implements IMessage {
    public int[][] enchantments;

    public MessageEnchant(int[][] ench) {
        enchantments = ench;
    }

    public MessageEnchant() {
        enchantments = new int[3][];
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        for (int i = 0; i < 3; ++i) {
            int j = buf.readInt();
            if (0 == j) {
                enchantments[i] = null;
            } else {
                int[] arr = new int[j];
                for (int k = 0; k < j; ++k) {
                    arr[k] = buf.readInt();
                }
                enchantments[i] = arr;
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        for (int i = 0; i < 3; ++i) {
            if (null == enchantments[i]) {
                buf.writeInt(0);
            } else {
                int j = enchantments[i].length;
                buf.writeInt(j);
                for (int t : enchantments[i]) {
                    buf.writeInt(t);
                }
            }
        }
    }

    public static class Handler implements IMessageHandler<MessageEnchant, IMessage> {
        @Override
        public IMessage onMessage(MessageEnchant message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler)
                    .addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private static void handle(MessageEnchant msg, MessageContext ctx) {
            EnchantView.enchantments = msg.enchantments;
        }
    }
}
