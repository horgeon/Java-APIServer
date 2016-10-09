package com.amazon.webservices.common;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SignatureException;

/**
 * This class defines common routines for generating
 * authentication signatures.
 */
public class HMAC256 {
	private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

	/**
	 * Computes RFC 2104-compliant HMAC256 signature.
	 * * @param data
	 * The data to be signed.
	 *
	 * @param key The signing key.
	 * @return The Base64-encoded RFC 2104-compliant HMAC256 signature.
	 * @throws java.security.SignatureException when signature generation fails
	 */
	public static String calculate( String data, String key ) throws java.security.SignatureException {
		String result;
		try {

			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec( key.getBytes(), HMAC_SHA256_ALGORITHM );

			// get an hmac_sha256 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance( HMAC_SHA256_ALGORITHM );
			mac.init( signingKey );

			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal( data.getBytes() );
			//byte[] rawHmac = mac.doFinal(data.getBytes("UTF-8"));

			// base64-encode the hmac
			result = HMACEncoding.EncodeBase64( rawHmac );

		} catch( Exception e ) {
			throw new SignatureException( "Failed to generate HMAC (SHA256): " + e.getMessage() );
		}
		return result;
	}
}