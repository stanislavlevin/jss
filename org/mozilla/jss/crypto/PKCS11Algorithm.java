package org.mozilla.jss.crypto;

import org.mozilla.jss.pkcs11.PKCS11Constants;
import org.mozilla.jss.crypto.Algorithm;

public enum PKCS11Algorithm {
    CKM_AES_CBC (Algorithm.CKM_AES_CBC, PKCS11Constants.CKM_AES_CBC),
    CKM_AES_CBC_PAD (Algorithm.CKM_AES_CBC_PAD, PKCS11Constants.CKM_AES_CBC_PAD),
    CKM_AES_ECB (Algorithm.CKM_AES_ECB, PKCS11Constants.CKM_AES_ECB),
    CKM_AES_KEY_GEN (Algorithm.CKM_AES_KEY_GEN, PKCS11Constants.CKM_AES_KEY_GEN),
    CKM_DES3_CBC_PAD (Algorithm.CKM_DES3_CBC_PAD, PKCS11Constants.CKM_DES3_CBC_PAD),
    CKM_DES3_ECB (Algorithm.CKM_DES3_ECB, PKCS11Constants.CKM_DES3_ECB),
    CKM_DES3_KEY_GEN (Algorithm.CKM_DES3_KEY_GEN, PKCS11Constants.CKM_DES3_KEY_GEN),
    CKM_DES_CBC_PAD (Algorithm.CKM_DES_CBC_PAD, PKCS11Constants.CKM_DES_CBC_PAD),
    CKM_DES_KEY_GEN (Algorithm.CKM_DES_KEY_GEN, PKCS11Constants.CKM_DES_KEY_GEN),
    CKM_DSA_KEY_PAIR_GEN (Algorithm.CKM_DSA_KEY_PAIR_GEN, PKCS11Constants.CKM_DSA_KEY_PAIR_GEN),
    CKM_EC_KEY_PAIR_GEN (Algorithm.CKM_EC_KEY_PAIR_GEN, PKCS11Constants.CKM_EC_KEY_PAIR_GEN),
    CKM_NSS_AES_KEY_WRAP (Algorithm.CKM_NSS_AES_KEY_WRAP, PKCS11Constants.CKM_NSS_AES_KEY_WRAP),
    CKM_NSS_AES_KEY_WRAP_PAD (Algorithm.CKM_NSS_AES_KEY_WRAP_PAD, PKCS11Constants.CKM_NSS_AES_KEY_WRAP_PAD),
    CKM_PBA_SHA1_WITH_SHA1_HMAC (Algorithm.CKM_PBA_SHA1_WITH_SHA1_HMAC, PKCS11Constants.CKM_PBA_SHA1_WITH_SHA1_HMAC),
    CKM_RC2_CBC_PAD (Algorithm.CKM_RC2_CBC_PAD, PKCS11Constants.CKM_RC2_CBC_PAD),
    CKM_RC2_KEY_GEN (Algorithm.CKM_RC2_KEY_GEN, PKCS11Constants.CKM_RC2_KEY_GEN),
    CKM_RC4_KEY_GEN (Algorithm.CKM_RC4_KEY_GEN, PKCS11Constants.CKM_RC4_KEY_GEN),
    CKM_RSA_PKCS_KEY_PAIR_GEN (Algorithm.CKM_RSA_PKCS_KEY_PAIR_GEN, PKCS11Constants.CKM_RSA_PKCS_KEY_PAIR_GEN),
    CKM_SHA_1_HMAC (Algorithm.CKM_SHA_1_HMAC, PKCS11Constants.CKM_SHA_1_HMAC);

    // Value from Algorithm's constant -- this is an index into Algorithm's
    // table.
    private int alg_index;

    // Value from PKCS11Constants -- this is a constant defined in PKCS #11.
    private long pk11_value;

    private PKCS11Algorithm(int alg_index, long pk11_value) {
        this.alg_index = alg_index;
        this.pk11_value = pk11_value;
    }

    public int getIndex() {
        return alg_index;
    }

    public long getValue() {
        return pk11_value;
    }

    public static PKCS11Algorithm valueOfIndex(int index) {
        for (PKCS11Algorithm alg : PKCS11Algorithm.values()) {
            if (alg.alg_index == index) {
                return alg;
            }
        }
        return null;
    }

    public static PKCS11Algorithm valueOfConstant(long constant) {
        for (PKCS11Algorithm alg : PKCS11Algorithm.values()) {
            if (alg.pk11_value == constant) {
                return alg;
            }
        }
        return null;
    }
}
