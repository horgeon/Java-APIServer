package com.amazon.webservices.common;

import java.util.Base64;

/**
 * This class defines common routines for encoding data.
 */
public class HMACEncoding {
	/**
	 * Performs base64-encoding of input bytes.
	 *
	 * @param rawData * Array of bytes to be encoded.
	 * @return * The base64 encoded string representation of rawData.
	 */
	public static String EncodeBase64( byte[] rawData ) {
		return Base64.getEncoder().encodeToString( rawData );
	}
}
