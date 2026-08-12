package exp.soundboard;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

/**
 * Checks that the global keyboard hook (JNativeHook, a 2013 native binary) still receives key
 * events on the current system. Without it, no hotkey works at all. It presses and releases
 * SHIFT, which on its own has no effect.
 */
public class HotkeyTest {

    public static void main(String[] args) throws Exception {
        Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);

        final CountDownLatch pressed = new CountDownLatch(1);
        final CountDownLatch released = new CountDownLatch(1);

        Utils.initGlobalKeyLibrary();
        GlobalScreen.getInstance().addNativeKeyListener(new NativeKeyListener() {
            public void nativeKeyPressed(NativeKeyEvent e) {
                System.out.println("  hook: key pressed -> " + NativeKeyEvent.getKeyText(e.getKeyCode()));
                pressed.countDown();
            }
            public void nativeKeyReleased(NativeKeyEvent e) {
                released.countDown();
            }
            public void nativeKeyTyped(NativeKeyEvent e) {}
        });

        Robot robot = new Robot();
        Thread.sleep(300);
        robot.keyPress(KeyEvent.VK_SHIFT);
        Thread.sleep(150);
        robot.keyRelease(KeyEvent.VK_SHIFT);

        boolean okPress = pressed.await(5, TimeUnit.SECONDS);
        boolean okRelease = released.await(5, TimeUnit.SECONDS);
        Utils.deregisterGlobalKeyLibrary();

        System.out.println((okPress ? "  PASS  " : "  FAIL  ") + "the global hook receives key presses");
        System.out.println((okRelease ? "  PASS  " : "  FAIL  ") + "the global hook receives key releases");
        System.out.println(okPress && okRelease ? "\nHOTKEYS OK" : "\nHOTKEYS BROKEN");
        System.exit(okPress && okRelease ? 0 : 1);
    }
}
