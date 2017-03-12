package enchantview;

import java.lang.reflect.*;

public class ReflectionHelper {

   public static Method getNameMatchedMethod(Class<?> clazz, String[] names, Class<?>[] paramTypes) {
      for (String name : names) {
         try {
            Method m = clazz.getDeclaredMethod(name, paramTypes);
            m.setAccessible(true);
            return m;
         } catch (NoSuchMethodException e) {
            continue;
         }
      }
      return null;
   }

   public static <T> T invokeMethod(Method method, Object obj, Object... args) {
      try {
         return (T) method.invoke(obj, args);
      } catch (ReflectiveOperationException e) {
         throw new RuntimeException(e);
      }
   }
}
