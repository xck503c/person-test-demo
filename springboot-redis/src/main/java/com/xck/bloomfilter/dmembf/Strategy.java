package com.xck.bloomfilter.dmembf;

import com.google.common.hash.Funnel;

public interface Strategy {

    /**
     * Sets {@code numHashFunctions} bits of the given bit array, by hashing a user element.
     *
     * <p>Returns whether any bits changed as a result of this operation.
     */
    <T> boolean put(T object, Funnel<? super T> funnel, int numHashFunctions, LongBitMap bits);

    /**
     * Queries {@code numHashFunctions} bits of the given bit array, by hashing a user element;
     * returns {@code true} if and only if all selected bits are set.
     */
    <T> boolean mightContain(T object, Funnel<? super T> funnel, int numHashFunctions, LongBitMap bits);
}
