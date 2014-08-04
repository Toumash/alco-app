package pl.pcd.alcohol;


import android.util.Base64;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Encryption {
    @NotNull
    public static String encodeBase64(@NotNull String origin) {
        return Base64.encodeToString(origin.getBytes(), Base64.DEFAULT);
    }

    @NotNull
    public static String decodeBase64(@NotNull String origin) {
        return new String(Base64.decode(origin, Base64.DEFAULT));
    }

    /**
     * Generates MD5 Hash from input string.
     *
     * @param s string to be processes
     * @return md5Hash hash or "" (empty string) if an error occured
     */
    @NotNull
    public static String md5Hash(@NotNull final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes("UTF-8"));
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
}