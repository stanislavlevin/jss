/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.mozilla.jss.tests;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.ECPrivateKey;
import java.util.Calendar;
import java.util.Date;

import org.mozilla.jss.CryptoManager;
import org.mozilla.jss.crypto.CryptoToken;
import org.mozilla.jss.crypto.KeyPairAlgorithm;
import org.mozilla.jss.crypto.KeyPairGenerator;
import org.mozilla.jss.netscape.security.util.BigInt;
import org.mozilla.jss.netscape.security.util.DerValue;
import org.mozilla.jss.netscape.security.x509.AlgorithmId;
import org.mozilla.jss.netscape.security.x509.CertificateAlgorithmId;
import org.mozilla.jss.netscape.security.x509.CertificateExtensions;
import org.mozilla.jss.netscape.security.x509.CertificateIssuerName;
import org.mozilla.jss.netscape.security.x509.CertificateSerialNumber;
import org.mozilla.jss.netscape.security.x509.CertificateSubjectName;
import org.mozilla.jss.netscape.security.x509.CertificateValidity;
import org.mozilla.jss.netscape.security.x509.CertificateVersion;
import org.mozilla.jss.netscape.security.x509.CertificateX509Key;
import org.mozilla.jss.netscape.security.x509.X500Name;
import org.mozilla.jss.netscape.security.x509.X509CertImpl;
import org.mozilla.jss.netscape.security.x509.X509CertInfo;
import org.mozilla.jss.netscape.security.x509.X509Key;
import org.mozilla.jss.pkcs11.PK11ECPublicKey;
import org.mozilla.jss.util.PasswordCallback;

public class X509CertTest {

    public static String subjectDN = "CN = 8a99f98342b97d130142ba2cc30f07d3";
    public static String issuerDN = "C = US, ST = North Carolina, O = Red Hat Inc., OU = Red Hat Network, CN = Red Hat Candlepin Authority, E = ca-support@redhat.com ";

    /* Just some sample code to exercise the new classes */
    public static void main(String []args) throws Exception {
        if( args.length != 2 ) {
            System.out.println("Usage: X509CertTest  <dbdir> <passwordfile>");
            return;
        }

        String dbdir = args[0];
        String passwordfile = args[1];


        Date notBefore = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(notBefore);
        cal.set(Calendar.YEAR, 2037);

        Date notAfter = cal.getTime();

        //Generate ca keyPair
        CryptoManager.initialize(dbdir);
        CryptoManager cryptoManager = CryptoManager.getInstance();
        CryptoToken token = cryptoManager.getInternalKeyStorageToken();
        PasswordCallback cb = new FilePasswordCallback(passwordfile);
        token.login(cb);

        testEC(token, notBefore, notAfter);
        testRSA(token, notBefore, notAfter);
    }

    public static void testEC(CryptoToken token, Date notBefore, Date notAfter) throws Exception {
        X509CertImpl certImpl = null;
        X509CertInfo certInfo = null;

        KeyPairGenerator gen = token.getKeyPairGenerator(KeyPairAlgorithm.EC);
        gen.initialize(gen.getCurveCodeByName("secp521r1"));
        KeyPair keypairCA = gen.genKeyPair();
        testKeys(keypairCA);
        PublicKey pubCA = keypairCA.getPublic();


        gen.initialize(gen.getCurveCodeByName("secp521r1"));
        KeyPair keypairUser = gen.genKeyPair();
        testKeys(keypairUser);
        PublicKey pubUser = keypairUser.getPublic();

        CertificateIssuerName issuernameObj = new CertificateIssuerName(new X500Name(issuerDN));

        certInfo = createX509CertInfo(convertPublicKeyToX509Key(pubUser),
                                      BigInteger.valueOf(1),issuernameObj,subjectDN,
                                      notBefore, notAfter, "SHA256withEC");

        certImpl = new X509CertImpl(certInfo);
        certImpl.sign(keypairCA.getPrivate(),"SHA256withEC");

        String certOutput = certImpl.toString();

        System.out.println("Test certificate output: \n" + certOutput);
    }

    public static void testRSA(CryptoToken token, Date notBefore, Date notAfter) throws Exception {
        X509CertImpl certImpl = null;
        X509CertInfo certInfo = null;

        KeyPairGenerator gen = token.getKeyPairGenerator(KeyPairAlgorithm.RSA);
        gen.initialize(4096);
        KeyPair keypairCA = gen.genKeyPair();
        testKeys(keypairCA);
        PublicKey pubCA = keypairCA.getPublic();


        gen.initialize(4096);
        KeyPair keypairUser = gen.genKeyPair();
        testKeys(keypairUser);
        PublicKey pubUser = keypairUser.getPublic();

        CertificateIssuerName issuernameObj = new CertificateIssuerName(new X500Name(issuerDN));

        certInfo = createX509CertInfo(convertPublicKeyToX509Key(pubUser),
                                      BigInteger.valueOf(1),issuernameObj,subjectDN,
                                      notBefore, notAfter, "SHA256withRSA");

        certImpl = new X509CertImpl(certInfo);
        certImpl.sign(keypairCA.getPrivate(),"SHA256withRSA");

        String certOutput = certImpl.toString();

        System.out.println("Test certificate output: \n" + certOutput);
    }

    public static void testKeys(KeyPair keypair) throws Exception {
        PublicKey pubKey = keypair.getPublic();
        PrivateKey privKey = keypair.getPrivate();

        if (pubKey instanceof ECPublicKey) {
            assert(privKey instanceof ECPrivateKey);

            ECPublicKey ecPub = (ECPublicKey)pubKey;
            ECPrivateKey ecPriv = (ECPrivateKey)privKey;
            // Two public functions we want to test on ECPublicKey:
            // 1. getW() -> ECPoint
            // 2. getParams() -> ECParameterSpec

            assert(ecPub.getW() != null);
            assert(ecPub.getW().getAffineX() != null);
            assert(ecPub.getW().getAffineY() != null);

            assert(ecPub.getParams() != null);
            assert(ecPriv.getParams() != null);

            // Since ECParameterSpec doesn't define a comparator, we have to
            // check each member individually.
            assert(ecPub.getParams().getCofactor() == ecPriv.getParams().getCofactor());
            assert(ecPub.getParams().getCurve().equals(ecPriv.getParams().getCurve()));
            assert(ecPub.getParams().getGenerator().getAffineX().equals(ecPriv.getParams().getGenerator().getAffineX()));
            assert(ecPub.getParams().getGenerator().getAffineY().equals(ecPriv.getParams().getGenerator().getAffineY()));
            assert(ecPub.getParams().getOrder().equals(ecPriv.getParams().getOrder()));
        }
    }

    public static X509CertInfo createX509CertInfo(X509Key x509key, BigInteger serialno,
                                                  CertificateIssuerName issuernameObj,
                                                  String subjname, Date notBefore,
                                                  Date notAfter, String alg)
            throws Exception
    {
        X509CertInfo info = new X509CertInfo();

        info.set(X509CertInfo.VERSION, new
                CertificateVersion(CertificateVersion.V3));
        info.set(X509CertInfo.SERIAL_NUMBER, new
                CertificateSerialNumber(serialno));
        if (issuernameObj != null) {
            info.set(X509CertInfo.ISSUER,
                    issuernameObj);
        }
        info.set(X509CertInfo.SUBJECT, new
                CertificateSubjectName(new X500Name(subjname)));
        info.set(X509CertInfo.VALIDITY, new
                CertificateValidity(notBefore, notAfter));
        info.set(X509CertInfo.ALGORITHM_ID, new
                CertificateAlgorithmId(AlgorithmId.get(alg)));
        info.set(X509CertInfo.KEY, new CertificateX509Key(x509key));
        info.set(X509CertInfo.EXTENSIONS, new CertificateExtensions());
        return info;
    }

    public static X509Key convertPublicKeyToX509Key(PublicKey pubk) throws Exception {
        X509Key xKey = null;

        if (pubk instanceof RSAPublicKey) {
            RSAPublicKey rsaKey = (RSAPublicKey) pubk;

            xKey = new org.mozilla.jss.netscape.security.provider.RSAPublicKey(
                    new BigInt(rsaKey.getModulus()),
                    new BigInt(rsaKey.getPublicExponent()));
        } else if (pubk instanceof PK11ECPublicKey) {
            byte encoded[] = pubk.getEncoded();

            xKey = X509Key.parse(new DerValue(encoded));
        }
        return xKey;
    }
}
