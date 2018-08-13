/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is the Netscape Security Services for Java.
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 1998-2000
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.mozilla.jss.pkix.cmc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.mozilla.jss.asn1.ASN1Template;
import org.mozilla.jss.asn1.ASN1Value;
import org.mozilla.jss.asn1.INTEGER;
import org.mozilla.jss.asn1.InvalidBERException;
import org.mozilla.jss.asn1.OCTET_STRING;
import org.mozilla.jss.asn1.SEQUENCE;
import org.mozilla.jss.asn1.Tag;
import org.mozilla.jss.pkix.primitive.AlgorithmIdentifier;

/**
 * CMC <i>DecryptedPOP</i>:
 * <pre>
 *     DecryptedPOP ::= SEQUENCE {
 *         bodyPartID      BodyPartID,
 *         thePOPAlgID     AlgorithmIdentifier,
 *         thePOP         OCTET STRING
 *     }
 * </pre>
 *
 * @author Christina Fu (cfu)
 */
public class DecryptedPOP implements ASN1Value {

    ///////////////////////////////////////////////////////////////////////
    // members and member access
    ///////////////////////////////////////////////////////////////////////
    private INTEGER bodyPartID;
    private AlgorithmIdentifier thePOPAlgID;
    private OCTET_STRING thePOP;
    private SEQUENCE sequence;  // for DER encoding

    /**
     * Returns the <code>bodyPartID</code> field.
     */
    public INTEGER getBodyPartID() {
        return bodyPartID;
    }

    public AlgorithmIdentifier getThePOPAlgID() {
        return thePOPAlgID;
    }

    public OCTET_STRING getWitness() {
        return thePOP;
    }

    ///////////////////////////////////////////////////////////////////////
    // constructors
    ///////////////////////////////////////////////////////////////////////

    public DecryptedPOP(
            INTEGER bodyPartID,
            AlgorithmIdentifier thePOPAlgID,
            OCTET_STRING thePOP)
    {
        if( bodyPartID==null || thePOPAlgID==null ||
                thePOP==null ) {
            throw new IllegalArgumentException("DecryptedPOP constructor"
                +" parameter is null");
        }

        this.bodyPartID = bodyPartID;
        this.thePOPAlgID = thePOPAlgID;
        this.thePOP = thePOP;

        sequence = new SEQUENCE();
        sequence.addElement(bodyPartID);
        sequence.addElement(thePOPAlgID);
        sequence.addElement(thePOP);
    }

    ///////////////////////////////////////////////////////////////////////
    // DER encoding
    ///////////////////////////////////////////////////////////////////////

    private static final Tag TAG = SEQUENCE.TAG;

    public Tag getTag() {
        return TAG;
    }

    public void encode(OutputStream ostream) throws IOException {
        sequence.encode(ostream);
    }

    public void encode(Tag implicitTag, OutputStream ostream)
            throws IOException {
        sequence.encode(implicitTag, ostream);
    }

    private static final Template templateInstance = new Template();
    public static Template getTemplate() {
        return templateInstance;
    }

    /**
     * A Template for decoding BER-encoded DecryptedPOP items.
     */
    public static class Template implements ASN1Template {

        private SEQUENCE.Template seqt;

        public Template() {
            seqt = new SEQUENCE.Template();

            seqt.addElement( INTEGER.getTemplate() );
            seqt.addElement( AlgorithmIdentifier.getTemplate() );
            seqt.addElement( OCTET_STRING.getTemplate() );
        }

        public boolean tagMatch(Tag tag) {
            return TAG.equals(tag);
        }

        public ASN1Value decode(InputStream istream)
                throws InvalidBERException, IOException {
            return decode(TAG, istream);
        }

        public ASN1Value decode(Tag implicitTag, InputStream istream)
                throws InvalidBERException, IOException {

            SEQUENCE seq = (SEQUENCE) seqt.decode(implicitTag, istream);

            return new DecryptedPOP(
                            (INTEGER) seq.elementAt(0),
                            (AlgorithmIdentifier) seq.elementAt(1),
                            (OCTET_STRING) seq.elementAt(2) );
        }
    }
}
