package ru.ifmo.ctddev.Voronina.walk;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

/**
 * Created by vorona on 16.02.16.
 */
public class Hash {

    public static String getHash(String filePath) {
        try {
            Path path = Paths.get(filePath);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buf = new byte[1024];

            try (InputStream stream = Files.newInputStream(path)) {
                int n;
                while ((n = stream.read(buf)) > 0) {
                    md.update(buf, 0, n);
                }
            } catch (IOException e) {
                return toHex(buf);
            }
            return toHex(md.digest());
        } catch (Exception e) {
            return toHex(new byte[1]);
        }
    }

    private static String toHex(byte[] buf) {
        String result = new BigInteger(1, buf).toString(16).toUpperCase();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32 - result.length(); i++)
            sb.append("0");
        return sb.toString() + result;
    }

}
