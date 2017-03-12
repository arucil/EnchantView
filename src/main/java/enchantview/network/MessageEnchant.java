package enchantview.network;

import enchantview.EnchantView;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MessageEnchant implements IMessage {
   public static final Charset UTF8 = Charset.forName("UTF-8");

   public List<String>[] enchantments;

   public MessageEnchant(List<String>[] ench) {
      enchantments = ench;
   }

   public MessageEnchant() {
      enchantments = new List[3];
   }

   @Override
   public void fromBytes(ByteBuf buf) {
      for (int i = 0; i < 3; ++i) {
         int j = buf.readInt();
         if (0 == j) {
            enchantments[i] = null;
         } else {
            List<String> list = enchantments[i] = new ArrayList<>(j);
            for (int k = 0; k < j; ++k) {
               byte[] b = new byte[buf.readInt()];
               buf.readBytes(b);
               list.add(new String(b, UTF8));
            }
         }
      }
   }

   @Override
   public void toBytes(ByteBuf buf) {
      for (int i = 0; i < 3; ++i) {
         if (null == enchantments[i]) {
            buf.writeInt(0);
         } else {
            int j = enchantments[i].size();
            buf.writeInt(j);
            for (String s : enchantments[i]) {
               byte[] b = s.getBytes(UTF8);
               buf.writeInt(b.length);
               buf.writeBytes(b);
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
