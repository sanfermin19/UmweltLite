package com.fermine.umweltlite.impl.engine.emotion.map;

import com.fermine.umweltlite.utils.UmweltNBTUtils;

/**
 * Immutable data container tracking localized relationships between distinct mobs.
 *
 * @param valence   How positive or negative this specific individual is viewed (-1.0 to 1.0).
 * @param arousal The mental weight/volatility given to interactions with them (0.0 to 1.0).
 * @param bond      The permanent baseline closeness score of the connection (0.0 to 1.0).
 */
public record AttachmentMap(float valence, float arousal, float bond) {

    /**
     * Secure canonical constructor ensuring zero toxic infinity/NaN states creep inside your records.
     */
    public AttachmentMap {
        valence = UmweltNBTUtils.safeFloat(valence, 0.0f);
        arousal = UmweltNBTUtils.safeFloat(arousal, 0.0f);
        bond = UmweltNBTUtils.safeFloat(bond, 0.0f);
    }

    /**
     * Determines if entities meet thresholds to bypass generic AI limits and act as deep partners.
     */
    public boolean isMate() {
        return this.bond >= 0.8f;
    }
}