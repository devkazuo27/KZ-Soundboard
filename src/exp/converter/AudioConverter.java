/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.sauronsoftware.jave.AudioAttributes
 *  it.sauronsoftware.jave.Encoder
 *  it.sauronsoftware.jave.EncoderException
 *  it.sauronsoftware.jave.EncoderProgressListener
 *  it.sauronsoftware.jave.EncodingAttributes
 */
package exp.converter;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncoderProgressListener;
import it.sauronsoftware.jave.EncodingAttributes;
import java.io.File;
import javax.swing.JOptionPane;

public class AudioConverter {
    private static final String mp3 = "libmp3lame";
    private static final String wav = "pcm_s16le";
    private static final Integer mp3bitrate = new Integer(256000);
    private static final Integer channels = new Integer(2);
    private static final Integer samplerate = new Integer(44100);

    public static void batchConvertToMP3(final File[] inputfiles, final File outputfolder, final EncoderProgressListener listener) {
        new Thread(new Runnable(){

            @Override
            public void run() {
                File[] fileArray = inputfiles;
                int n = inputfiles.length;
                int n2 = 0;
                while (n2 < n) {
                    File input = fileArray[n2];
                    File output = AudioConverter.getAbsoluteForOutputExtensionAndFolder(input, outputfolder, ".mp3");
                    System.out.println("processing: " + output.getAbsolutePath());
                    AudioConverter.mp3(input, output, listener);
                    ++n2;
                }
            }
        }).start();
    }

    public static void batchConvertToWAV(final File[] inputfiles, final File outputfolder, final EncoderProgressListener listener) {
        new Thread(new Runnable(){

            @Override
            public void run() {
                File[] fileArray = inputfiles;
                int n = inputfiles.length;
                int n2 = 0;
                while (n2 < n) {
                    File input = fileArray[n2];
                    File output = AudioConverter.getAbsoluteForOutputExtensionAndFolder(input, outputfolder, ".wav");
                    System.out.println("processing: " + output.getAbsolutePath());
                    AudioConverter.wav(input, output, listener);
                    ++n2;
                }
            }
        }).start();
    }

    public static void convertToMP3(final File inputfile, final File outputfile, final EncoderProgressListener listener) {
        new Thread(new Runnable(){

            @Override
            public void run() {
                AudioConverter.mp3(inputfile, outputfile, listener);
            }
        }).start();
    }

    public static void convertToWAV(final File inputfile, final File outputfile, final EncoderProgressListener listener) {
        new Thread(new Runnable(){

            @Override
            public void run() {
                AudioConverter.wav(inputfile, outputfile, listener);
            }
        }).start();
    }

    private static void mp3(File inputfile, File outputfile, EncoderProgressListener listener) {
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec(mp3);
        audio.setBitRate(mp3bitrate);
        audio.setChannels(channels);
        audio.setSamplingRate(samplerate);
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("mp3");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        try {
            if (listener != null) {
                encoder.encode(inputfile, outputfile, attrs, listener);
            } else {
                encoder.encode(inputfile, outputfile, attrs);
            }
        }
        catch (EncoderException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Input file formatting/encoding is incompatible\n" + inputfile.getName(), "Input File incompatible", 0);
            listener.progress(1001);
            e.printStackTrace();
        }
    }

    private static void wav(File inputfile, File outputfile, EncoderProgressListener listener) {
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec(wav);
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("wav");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        try {
            if (listener != null) {
                encoder.encode(inputfile, outputfile, attrs, listener);
            } else {
                encoder.encode(inputfile, outputfile, attrs);
            }
        }
        catch (EncoderException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Input file formatting/encoding is incompatible\n" + inputfile.getName(), "Input File incompatible", 0);
            listener.progress(1001);
            e.printStackTrace();
        }
    }

    private static File getAbsoluteForOutputExtensionAndFolder(File inputfile, File outputfolder, String dotext) {
        String filename = inputfile.getName();
        int period = filename.lastIndexOf(46);
        if (period > 0) {
            filename = String.valueOf(filename.substring(0, period)) + dotext;
        }
        return new File(outputfolder + File.separator + filename);
    }
}

