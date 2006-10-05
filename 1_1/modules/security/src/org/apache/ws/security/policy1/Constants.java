/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ws.security.policy1;

public class Constants {

    public final static String SP_NS = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy";
    
    public final static String ATTR_INCLUDE_TOKEN = "IncludeToken";

    public final static String INCLUDE_NEVER = Constants.SP_NS
            + "/IncludeToken/Never";

    public final static String INCLUDE_ONCE = Constants.SP_NS
            + "/IncludeToken/Once";

    public final static String INCLUDE_ALWAYS_TO_RECIPIENT = Constants.SP_NS
            + "/IncludeToken/AlwaysToRecipient";

    public final static String INCLUDE_ALWAYS = Constants.SP_NS
            + "/IncludeToken/Always";

    
    public final static int SUPPORTING_TOKEN_SUPPORTING = 1;
    public final static int SUPPORTING_TOKEN_ENDORSING = 2;
    public final static int SUPPORTING_TOKEN_SIGNED = 3;
    public final static int SUPPORTING_TOKEN_SIGNED_ENDORSING = 4;
    
    /**
     * Security Header Layout : Strict
     */
    public final static String LAYOUT_STRICT = "Strict";

    /**
     * Security Header Layout : Lax
     */
    public final static String LAYOUT_LAX = "Lax";

    /**
     * Security Header Layout : LaxTimestampFirst
     */
    public final static String LAYOUT_LAX_TIMESTAMP_FIRST = "LaxTimestampFirst";

    /**
     * Security Header Layout : LaxTimestampLast
     */
    public final static String LAYOUT_LAX_TIMESTAMP_LAST = "LaxTimestampLast";
    
    /**
     * Protection Order : EncryptBeforeSigning
     */
    public final static String ENCRYPT_BEFORE_SIGNING = "EncryptBeforeSigning";

    /**
     * Protection Order : SignBeforeEncrypting
     */
    public final static String SIGN_BEFORE_ENCRYPTING = "SignBeforeEncrypting";
    
    public final static String WSS_X509_V1_TOKEN10 = "WssX509V1Token10";
    
    public final static String WSS_X509_V3_TOKEN10 = "WssX509V3Token10";
    
    public final static String WSS_X509_PKCS7_TOKEN10 = "WssX509Pkcs7Token10";
    
    public final static String WSS_X509_PKI_PATH_V1_TOKEN10 = 
                                                    "WssX509PkiPathV1Token10";
    
    public final static String WSS_X509_V1_TOKEN11 = "WssX509V1Token11";
    
    public final static String WSS_X509_V3_TOKEN11 = "WssX509V3Token11";
    
    public final static String WSS_X509_PKCS7_TOKEN11 = "WssX509Pkcs7Token11";
    
    public final static String WSS_X509_PKI_PATH_V1_TOKEN11 = 
                                                    "WssX509PkiPathV1Token11";
    
    ///
    ///Algorithm Suites
    ///
    public final static String ALGO_SUITE_BASIC256 = "Basic256";
    public final static String ALGO_SUITE_BASIC192 = "Basic192";
    public final static String ALGO_SUITE_BASIC128 = "Basic128";
    public final static String ALGO_SUITE_TRIPLE_DES = "TripleDes";
    public final static String ALGO_SUITE_BASIC256_RSA15 = "Basic256Rsa15";
    public final static String ALGO_SUITE_BASIC192_RSA15 = "Basic192Rsa15";
    public final static String ALGO_SUITE_BASIC128_RSA15 = "Basic128Rsa15";
    public final static String ALGO_SUITE_TRIPLE_DES_RSA15 = "TripleDesRsa15";
    public final static String ALGO_SUITE_BASIC256_SHA256 = "Basic256Sha256";
    public final static String ALGO_SUITE_BASIC192_SHA256 = "Basic192Sha256";
    public final static String ALGO_SUITE_BASIC128_SHA256 = "Basic128Sha256";
    public final static String ALGO_SUITE_TRIPLE_DES_SHA256 = "TripleDesSha256";
    public final static String ALGO_SUITE_BASIC256_SHA256_RSA15 = 
                                                        "Basic256Sha256Rsa15";
    public final static String ALGO_SUITE_BASIC192_SHA256_RSA15 = 
                                                        "Basic192Sha256Rsa15";
    public final static String ALGO_SUITE_BASIC128_SHA256_RSA15 = 
                                                        "Basic128Sha256Rsa15";
    public final static String ALGO_SUITE_TRIPLE_DES_SHA256_RSA15 = 
                                                        "TripleDesSha256Rsa15";
    
    ///
    ///Algorithms
    ///
    public final static String HMAC_SHA1 = "http://www.w3.org/2000/09/xmldsig#hmac-sha1";

    public final static String RSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";

    public final static String SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";

    public final static String SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";

    public final static String SHA512 = "http://www.w3.org/2001/04/xmlenc#sha512";

    public final static String AES128 = "http://www.w3.org/2001/04/xmlenc#aes128-cbc";

    public final static String AES192 = "http://www.w3.org/2001/04/xmlenc#aes192-cbc";

    public final static String AES256 = "http://www.w3.org/2001/04/xmlenc#aes256-cbc";

    public final static String TRIPLE_DES = "http://www.w3.org/2001/04/xmlenc#tripledes-cbc";

    public final static String KW_AES128 = "http://www.w3.org/2001/04/xmlenc#kw-aes256";

    public final static String KW_AES192 = "http://www.w3.org/2001/04/xmlenc#kw-aes192";

    public final static String KW_AES256 = "http://www.w3.org/2001/04/xmlenc#kw-aes128";

    public final static String KW_TRIPLE_DES = "http://www.w3.org/2001/04/xmlenc#kw-tripledes";

    public final static String KW_RSA_OAEP = "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p";

    public final static String KW_RSA15 = "http://www.w3.org/2001/04/xmlenc#rsa-1_5";

    public final static String P_SHA1 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";

    public final static String P_SHA1_L128 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";

    public final static String P_SHA1_L192 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";

    public final static String P_SHA1_L256 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";

    public final static String XPATH = "http://www.w3.org/TR/1999/REC-xpath-19991116";

    public final static String XPATH20 = "http://www.w3.org/2002/06/xmldsig-filter2";

    public final static String C14N = "http://www.w3.org/2001/10/xml-c14n#";

    public final static String EX_C14N = "http://www.w3.org/2001/10/xml-exc-c14n#";

    public final static String SNT = "http://www.w3.org/TR/soap12-n11n";

    public final static String STRT10 = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#STR-Transform";
}
