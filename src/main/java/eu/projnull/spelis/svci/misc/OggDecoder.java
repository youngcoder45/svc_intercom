package eu.projnull.spelis.svci.misc;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayOutputStream;
import java.io.File;

public final class OggDecoder {
    private OggDecoder() {
    }

    public static short[] decode(File file) throws Exception {
        try (AudioInputStream raw = AudioSystem.getAudioInputStream(file)) {

            AudioFormat source = raw.getFormat();

            AudioFormat target = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, source.getSampleRate(), 16, source.getChannels(), source.getChannels() * 2, source.getSampleRate(), false);

            try (AudioInputStream pcm = AudioSystem.getAudioInputStream(target, raw); ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

                byte[] data = new byte[4096];
                int read;

                while ((read = pcm.read(data)) != -1) {
                    buffer.write(data, 0, read);
                }

                byte[] bytes = buffer.toByteArray();
                short[] samples = new short[bytes.length / 2];

                for (int i = 0; i < samples.length; i++) {
                    int low = bytes[i * 2] & 0xFF;
                    int high = bytes[i * 2 + 1] << 8;
                    samples[i] = (short) (high | low);
                }

                return samples;
            }
        }
    }
}
