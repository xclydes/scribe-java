/**
 * 
 */
package org.scribe;

import java.io.OutputStream;

/**
 * @author Jermaine
 * 
 */
public interface IParameter extends Comparable<IParameter> {

	int ENCODING_RAW = 1;
	int ENCODING_URL = ENCODING_RAW << 1;
	int ENCODING_MULTIPART = ENCODING_RAW << 2;
	int ENCODING_OAUTHBASE = ENCODING_RAW << 3;

	String DISPOSITION_FORM_DATA = "form-data";
	public static final String DISPOSITION_ATTACHMENT = "attachment";
	public static final String DISPOSITION_INLINE = "inline";

	public static final String SEQUENCE_NEW_LINE = "\r\n";

	/**
	 * Indicates whether or not this parameter is to be included in the base
	 * string which is generated.
	 * 
	 * @return Whether or not to include this parameter in the base string.
	 */
	boolean isUsedInBaseString();

	/**
	 * Gets the name of this paramter.
	 * 
	 * @return The name of this parameter.
	 */
	String getKey();

	/**
	 * Gets the text representation of this parameter.
	 * 
	 * @return The value of this pararmeter as text.
	 */
	String getValue();

	/**
	 * Gets the disposition to be used by this
	 * 
	 * @return
	 */
	String getDisposition();

	/**
	 * Appends the content of this parameter to this stream supplied.
	 * 
	 * @param encoding
	 *            The method of encoding to be used for writing this parameter.
	 * @param writeTo
	 *            The {@link OutputStream} to which the parameter should be
	 *            written.
	 */
	long writeTo(final int encoding, final OutputStream writeTo);
}
