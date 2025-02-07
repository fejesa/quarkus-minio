package io.crunch.media.resource;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

@ApplicationScoped
public class MediaFileNameGenerator {

    private static final BigInteger preA = BigInteger.valueOf(3781927463263421L);
    private static final BigInteger preC = BigInteger.valueOf(2113248654051873L);
    private static final BigInteger preM = BigInteger.valueOf(10000000000000000L);

    private final Random rnd = new SecureRandom();

    public String generate() {
        var key = rnd.nextLong();
        var x = new BigInteger(String.valueOf(key));
        var res = x.multiply(preA).add(preC).mod(preM).toString();
        return StringUtils.leftPad(res + Long.toHexString(rnd.nextLong()), 32, '0');
    }
}
