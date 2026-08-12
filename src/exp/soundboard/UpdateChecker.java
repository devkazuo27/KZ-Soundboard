/*
 * Decompiled with CFR 0.152.
 */
package exp.soundboard;

import exp.gui.UpdateConfirmFrame;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

public class UpdateChecker
implements Runnable {
    private static final String updatelink = "http://sourceforge.net/projects/expsoundboard/files/";

    public static String getUpdateNotes() {
        boolean internetconnection = false;
        BufferedReader reader = null;
        try {
            // FIX: sin timeouts, un servidor que no responde dejaba el hilo colgado indefinidamente.
            URLConnection connection = new URL(updatelink).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            internetconnection = true;
        }
        catch (MalformedURLException ex) {
            Logger.getLogger(UpdateChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex) {
            Logger.getLogger(UpdateChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (internetconnection) {
            System.out.println("UpdateChecker: System has Internet Connection.");
            boolean versionfound = false;
            String patchlist = "";
            boolean changelogFound = false;
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!changelogFound) {
                        if (!line.startsWith("CHANGELOG")) continue;
                        changelogFound = true;
                        continue;
                    }
                    if (!changelogFound) continue;
                    if (line.startsWith("vers.") && !versionfound) {
                        versionfound = true;
                        patchlist = String.valueOf(patchlist) + '\n' + line;
                        continue;
                    }
                    if (versionfound && line.startsWith("vers.")) {
                        reader.close();
                        return patchlist;
                    }
                    patchlist = String.valueOf(patchlist) + '\n' + line;
                }
                reader.close();
            }
            catch (IOException ex) {
                Logger.getLogger(UpdateChecker.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("UpdateChecker: System does not have Internet Connection.");
        }
        return "Update notes could not be found";
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static boolean isUpdateAvailable() {
        boolean internetconnection = false;
        BufferedReader reader = null;
        try {
            // FIX: sin timeouts, un servidor que no responde dejaba el hilo colgado indefinidamente.
            URLConnection connection = new URL(updatelink).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            internetconnection = true;
        }
        catch (MalformedURLException ex) {
            Logger.getLogger(UpdateChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex) {
            Logger.getLogger(UpdateChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!internetconnection) {
            System.out.println("UpdateChecker: System does not have Internet Connection.");
            return false;
        }
        System.out.println("UpdateChecker: System has Internet Connection.");
        boolean changelogFound = false;
        try {
            String line;
            while (true) {
                if ((line = reader.readLine()) == null) {
                    reader.close();
                    return false;
                }
                if (!changelogFound) {
                    if (!line.startsWith("CHANGELOG")) continue;
                    changelogFound = true;
                    continue;
                }
                if (changelogFound && line.startsWith("vers.")) break;
            }
            String version = line.substring(line.indexOf(46) + 1, line.lastIndexOf(58)).trim();
            float versionNo = Float.parseFloat(version);
            if (versionNo > 0.5f) {
                System.out.println("UpdateChecker: New version available!");
                reader.close();
                return true;
            }
            System.out.println("UpdateChecker: Currently up to date!");
            reader.close();
            return false;
        }
        catch (IOException ex) {
            Logger.getLogger(UpdateChecker.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public void run() {
        if (UpdateChecker.isUpdateAvailable()) {
            SwingUtilities.invokeLater(new Runnable(){

                @Override
                public void run() {
                    new UpdateConfirmFrame(UpdateChecker.getUpdateNotes());
                }
            });
        }
    }
}

