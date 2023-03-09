package com.cleo.labs.sclient;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.regex.Pattern;

public class CertUtils {

    private static final String BEGIN = "-----BEGIN ";
    private static final String DASH = "-----";
    private static final String END = "-----END ";

    /**
     * Folds a string into lines of no more than 64 characters
     * by inserting newlines. Note that openssl's base64
     * decoder seems to work only with folded output.
     * @param s the string to fold
     * @return the folded string
     */
    public static String fold(String s) {
        return unfold(s).replaceAll(".{64}", "$0\n");
    }

    /**
     * Unfolds a possibly folded string by removing all
     * whitespace, including newlines, carriage returns,
     * and other whitespace as defined by {@link Pattern}.
     * @param s the string to unfold
     * @return the unfolded string
     */
    public static String unfold(String s) {
        return s.replaceAll("\\s", "");
    }

    /**
     * Returns the base 64 encoding of a certificate, or
     * {@code null} if there is an encoding error.
     * @param cert the certificate to encode
     * @return a base 64 string, or {@code null}
     */
    public static String base64(X509Certificate cert) {
        if (cert == null) {
            return null;
        }
        try {
            return Base64.getEncoder().encodeToString(cert.getEncoded());
        } catch (CertificateEncodingException e) {
            return null;
        }
    }

    /**
     * Converts a certificate into a multi-line base 64 encoded format
     * wrapped in BEGIN and END delimiters, such as might be returned
     * by {@code openssl}.
     * @param cert a (possibly {@code null}) X509Certificate
     * @return a multi-line String (or {@code null})
     */
    public static String export(X509Certificate cert) {
        return export(base64(cert));
    }

    /**
     * Wraps a base 64 encoded certificate in BEGIN and END delimiters,
     * such as might be returned by {@code openssl}.
     * @param cert a (possibly {@code null}) base 64 encoded string
     * @return a multi-line String (or {@code null})
     */
    public static String export(String cert) {
        if (cert==null || cert.isEmpty()) {
            return null;
        }
        return BEGIN+"CERTIFICATE"+DASH+"\n"+
            fold(cert)+"\n"+
            END+"CERTIFICATE"+DASH;
    }

}
