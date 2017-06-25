package org.marid.misc;

import javax.annotation.Nonnull;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface Digests {

    @Nonnull
    static MessageDigest digest(@Nonnull String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException x) {
            throw new IllegalArgumentException(algorithm, x);
        }
    }
}
