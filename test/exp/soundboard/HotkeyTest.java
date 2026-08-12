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
 * Comprueba que el hook global de teclado (JNativeHook, libreria nativa de 2013) sigue
 * recibiendo pulsaciones en el sistema actual. Sin esto las hotkeys no funcionan.
 * Pulsa y suelta SHIFT, que por si solo no tiene ningun efecto.
 */
public class HotkeyTest {

    public static void main(String[] args) throws Exception {
        Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);

        final CountDownLatch pressed = new CountDownLatch(1);
        final CountDownLatch released = new CountDownLatch(1);

        Utils.initGlobalKeyLibrary();
        GlobalScreen.getInstance().addNativeKeyListener(new NativeKeyListener() {
            public void nativeKeyPressed(NativeKeyEvent e) {
                System.out.println("  hook: tecla pulsada -> " + NativeKeyEvent.getKeyText(e.getKeyCode()));
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

        System.out.println((okPress ? "  OK   " : "  FALLA") + "  el hook global recibe pulsaciones");
        System.out.println((okRelease ? "  OK   " : "  FALLA") + "  el hook global recibe sueltas");
        System.out.println(okPress && okRelease ? "\nHOTKEYS OK" : "\nHOTKEYS KO");
        System.exit(okPress && okRelease ? 0 : 1);
    }
}
