package org.scribe.model;

import java.io.IOException;
import java.io.OutputStream;

import org.scribe.IParameter;
import org.scribe.utils.OAuthEncoder;

/**
 * @author: Pablo Fernandez
 */
public class Parameter implements IParameter {
	
	private final String key;
	private final String value;
	private String disposition;

	/**
	 * Creates a new parameter with the key and
	 * value. 
	 * @param key The name of this parameter.
	 * @param value The value to be 
	 */
	public Parameter(final String key, final String value) {
		this(key, value, DISPOSITION_FORM_DATA);
	}
	
	/**
	 * @param key
	 * @param value
	 * @param disp
	 */
	public Parameter(final String key, final String value, final String disp) {
		this.key = key;
		this.value = value;
		this.disposition = disp;
	}

	/* (non-Javadoc)
	 * @see org.scribe.IParameter#getValue()
	 */
	@Override
	public String getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see org.scribe.IParameter#getKey()
	 */
	@Override
	public String getKey() {
		return key;
	}

	/* (non-Javadoc)
	 * @see org.scribe.IParameter#writeTo(int, java.io.OutputStream)
	 */
	@Override
	public long writeTo(final int encoding, final OutputStream writeTo) {
		long byteCount = 0;
		try {
			switch( encoding ) {
				case ENCODING_MULTIPART:
					final byte[] newLineBytes = SEQUENCE_NEW_LINE.getBytes();
					final byte[] dispTag = String.format("Content-Disposition: %1$s; ",	this.getDisposition()).getBytes();
					final byte[] nameTag = String.format("name=\"%1$s\"; ", OAuthEncoder.encode( this.getKey() ) ).getBytes();
					writeTo.write( dispTag );
					byteCount += dispTag.length;
					writeTo.write( nameTag );
					byteCount += nameTag.length;
					//Write any special fields/attributes
					byteCount += this.writeAdditionalProperties(writeTo);
					writeTo.write( newLineBytes );
					byteCount += newLineBytes.length;
					writeTo.write( newLineBytes );
					byteCount += newLineBytes.length;
					//Write the content to stream
					byteCount += this.writeValue( writeTo );
					writeTo.write( newLineBytes );
					byteCount += newLineBytes.length;
					break;
				case ENCODING_OAUTHBASE:
				case ENCODING_RAW:
				case ENCODING_URL:
					final byte[] encKey = OAuthEncoder.encode( this.getKey() ).getBytes();
					final byte[] encValue = OAuthEncoder.encode( this.getValue() ).getBytes();
					writeTo.write( encKey );
					writeTo.write( '=' );
					writeTo.write( encValue );
					byteCount += encKey.length + encValue.length + 1;
					break;
			}		
		} catch (Throwable error) {
			error.printStackTrace();
		}
		return byteCount;
	}	
	
	/**
	 * Adds any addition Multipart properties to this
	 * parameter.
	 * @param writeTo The content stream to which the
	 * fields should be writted.
	 * @return
	 */
	protected long writeAdditionalProperties(final OutputStream writeTo) throws IOException {
		return 0;
	}
	
	/**
	 * Writes the content of this parameter to the stream specified.
	 * @param writeTo The stream to which the content should be written.
	 * @throws IOException 
	 */
	protected long writeValue(final OutputStream writeTo) throws IOException {
		final byte[] valueBytes = this.getValue().getBytes();
		writeTo.write( valueBytes );
		return valueBytes.length;
	}
	
	/**
	 * Gets the content disposition to be used for this parameter.
	 * @return The content dispoition to be specified when for this parameter.
	 */
	public String getDisposition() {
		// If no disposition is set
		if (this.disposition == null) {
			// Assume form data
			this.disposition = DISPOSITION_FORM_DATA;
		}
		return disposition;
	}

	/**
	 * Indicates whether or not this parameter is to be included in the base
	 * string which is generated.
	 * @return Whether or not to include this parameter in the base string.
	 */
	public boolean isUsedInBaseString() {
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof IParameter))
			return false;

		IParameter otherParam = (IParameter) other;
		return otherParam.getKey().equals(getKey())
				&& otherParam.getValue().equals(getValue());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getKey().hashCode() + getValue().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IParameter parameter) {
		int keyDiff = getKey().compareTo(parameter.getKey());

		return keyDiff != 0 ? keyDiff : getValue().compareTo(
				parameter.getValue());
	}
}
