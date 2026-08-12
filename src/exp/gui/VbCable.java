package exp.gui;

import exp.soundboard.Utils;
import java.awt.Component;
import java.awt.Desktop;
import java.net.URI;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;

/**
 * Support for VB-Audio Virtual Cable, the virtual audio device that lets other people hear
 * your clips: the soundboard plays into "CABLE Input" and your voice software listens on
 * "CABLE Output" as if it were a microphone. Without it the second output has nothing useful
 * to point at and the Mic Injector has nowhere to send your microphone.
 *
 * The installer is deliberately NOT bundled: VB-CABLE is donationware whose licence does not
 * allow redistributing it. What this class does instead is detect whether it is installed,
 * explain what it is for, open the download page, and preselect the device once it shows up.
 *
 * @see <a href="https://vb-audio.com/Cable/">https://vb-audio.com/Cable/</a>
 */
public final class VbCable {

    public static final String DOWNLOAD_URL = "https://vb-audio.com/Cable/";
    private static final String PREF_DISMISSED = "vbCablePromptDismissed";

    private VbCable() {
    }

    /**
     * Windows truncates mixer names at 31 characters, so "CABLE Input (VB-Audio Virtual
     * Cable)" arrives clipped. Matching is done on the parts that survive.
     */
    private static boolean looksLikeCable(String mixerName) {
        String name = mixerName.toLowerCase();
        return name.contains("vb-audio") || name.contains("cable input") || name.contains("cable output");
    }

    public static boolean isInstalled() {
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            if (looksLikeCable(info.getName())) {
                return true;
            }
        }
        return false;
    }

    /** The device the soundboard should play into, so that others hear the clips. */
    public static String findPlaybackDevice(String[] availableOutputs) {
        return firstMatch(availableOutputs, "cable input");
    }

    /** The device your voice software should listen on. Shown to the user as guidance. */
    public static String findRecordingDevice(String[] availableInputs) {
        return firstMatch(availableInputs, "cable output");
    }

    private static String firstMatch(String[] names, String needle) {
        if (names == null) {
            return null;
        }
        for (String name : names) {
            if (name.toLowerCase().contains(needle)) {
                return name;
            }
        }
        // Fall back to any VB-Audio device if the exact wording is not there.
        for (String name : names) {
            if (name.toLowerCase().contains("vb-audio")) {
                return name;
            }
        }
        return null;
    }

    /**
     * Whether the driver is on the machine at all, even if Windows is not currently exposing
     * the device to applications. Java only lists devices that are enabled and started, so
     * without this check an install that is merely disabled looks exactly like no install, and
     * the user gets told to download something they already have.
     */
    public static boolean isDriverInstalled() {
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            return false;
        }
        String[] keys = new String[]{
            "HKLM\\SOFTWARE\\VB-Audio",
            "HKLM\\SYSTEM\\CurrentControlSet\\Services\\VBAudioVACWDM",
            "HKLM\\SYSTEM\\CurrentControlSet\\Services\\VBAudioVACMME"
        };
        for (String key : keys) {
            Process process = null;
            try {
                process = new ProcessBuilder("reg", "query", key)
                        .redirectErrorStream(true).start();
                if (process.waitFor(2, TimeUnit.SECONDS) && process.exitValue() == 0) {
                    return true;
                }
            }
            catch (Exception e) {
                return false;
            }
            finally {
                if (process != null) {
                    process.destroy();
                }
            }
        }
        return false;
    }

    /**
     * Offers to install it if it is missing. Returns true when it is already usable.
     * Remembers a refusal, so this is asked once and not on every launch.
     *
     * @param force ignores the remembered refusal (used by the menu entry)
     */
    public static boolean promptIfMissing(Component parent, boolean force) {
        if (isInstalled()) {
            return true;
        }
        if (!force && Utils.prefs.getBoolean(PREF_DISMISSED, false)) {
            return false;
        }

        if (isDriverInstalled()) {
            // Installed, but Windows is not handing the device to applications.
            JOptionPane.showMessageDialog(parent,
                    "<html><body style='width: 380px'>"
                    + "<b>VB-Audio Virtual Cable is installed but Windows is not exposing it.</b>"
                    + "<br><br>"
                    + "It is usually disabled, or the machine has not been restarted since "
                    + "installing it.<br><br>"
                    + "Open <b>Sound settings → More sound settings</b>, right-click inside "
                    + "the device list, tick <b>Show disabled devices</b>, and enable "
                    + "<b>CABLE Input</b> and <b>CABLE Output</b>. Then restart KZ Soundboard."
                    + "</body></html>",
                    "Virtual audio cable", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String[] options = new String[]{"Open download page", "Not now", "Don't ask again"};
        int choice = JOptionPane.showOptionDialog(parent,
                missingMessage(),
                "Virtual audio cable", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

        if (choice == 0) {
            openDownloadPage(parent);
        } else if (choice == 2) {
            Utils.prefs.putBoolean(PREF_DISMISSED, true);
        }
        return false;
    }

    /** The explanation shown when the cable is missing. Package-visible so it can be previewed. */
    static String missingMessage() {
        return "<html><body style='width: 380px'>"
                + "<b>VB-Audio Virtual Cable is not installed.</b><br><br>"
                + "It is the free virtual audio device that lets other people hear your clips: "
                + "KZ Soundboard plays into <b>CABLE Input</b>, and your voice software "
                + "(Discord, TeamSpeak, OBS…) listens on <b>CABLE Output</b> as if it were a "
                + "microphone.<br><br>"
                + "Without it you can still hear the clips yourself, but the second output and "
                + "the Mic Injector have nothing to point at.<br><br>"
                + "It is made by VB-Audio, not by us, so it has to be downloaded from their "
                + "site. Installing it needs administrator rights and a restart."
                + "</body></html>";
    }

    public static void openDownloadPage(Component parent) {
        try {
            Desktop.getDesktop().browse(new URI(DOWNLOAD_URL));
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                    "Could not open the browser. The download page is:\n" + DOWNLOAD_URL,
                    "Virtual audio cable", JOptionPane.WARNING_MESSAGE);
        }
    }
}
