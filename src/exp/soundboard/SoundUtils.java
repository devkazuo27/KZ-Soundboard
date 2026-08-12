/*
 * Decompiled with CFR 0.152.
 */
package exp.soundboard;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SoundUtils {
    public static short[] byteToShortArray(byte[] byteArray) {
        short[] shortArray = new short[byteArray.length / 2];
        int i = 0;
        while (i < shortArray.length) {
            int ub1 = byteArray[i * 2 + 0] & 0xFF;
            int ub2 = byteArray[i * 2 + 1] & 0xFF;
            shortArray[i] = (short)((ub2 << 8) + ub1);
            ++i;
        }
        return shortArray;
    }

    public static byte[] shortArrayToByteArray(short[] shortArray) {
        byte[] byteArray = new byte[shortArray.length * 2];
        ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shortArray);
        return byteArray;
    }
}

