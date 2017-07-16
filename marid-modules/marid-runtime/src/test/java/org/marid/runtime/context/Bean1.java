package org.marid.runtime.context;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class Bean1 {

    private final int x;
    public final String y;
    private final BigDecimal z;
    private boolean a;

    public Bean1(int x, String y, BigDecimal z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public String getY() {
        return y;
    }

    public BigDecimal getZ() {
        return z;
    }

    public boolean isA() {
        return a;
    }

    public Bean1 setA(boolean a) {
        this.a = a;
        return this;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public static List<Integer> list() {
        return new ArrayList<>();
    }
}
