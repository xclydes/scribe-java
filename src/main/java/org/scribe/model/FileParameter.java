/**
 * 
 */
package org.scribe.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.scribe.utils.OAuthEncoder;

/**
 * @author Jermaine
 * 
 */
public class FileParameter extends Parameter {

	public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
	
	private String mimeType;
	private final File srcFile;

	public FileParameter(final String key, final File file) {
		super(key, (String) null);
		this.srcFile = file;
	}

	/* (non-Javadoc)
	 * @see org.scribe.model.Parameter#getValue()
	 */
	@Override
	public String getValue() {
		return new String( this.getValueBytes() );
	}

	/* (non-Javadoc)
	 * @see org.scribe.model.Parameter#getValueBytes()
	 */
	@Override
	public byte[] getValueBytes() {
		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			//Ensure a valid file is specified.
			if( this.srcFile != null 
				&& this.srcFile.exists() ) {			
				FileInputStream fInStr = null;
				try {
					// Load the file to a stream
					fInStr = new FileInputStream(srcFile);
					//Creat a byte buffer the size of the file
					byte readBuf[] = new byte[(int) fInStr.getChannel().size()];
					int readCnt = fInStr.read(readBuf);
					while (0 < readCnt) {
						bout.write(readBuf, 0, readCnt);
						readCnt = fInStr.read(readBuf);
					}
					fInStr.close();
					bout.close();
				} catch (Throwable error) {
				} finally {
					if( fInStr != null ) {
						try {
							fInStr.close();
						} catch (Throwable e) {	}
					}
				}
			}
		} catch (Exception e) { }
		final byte[] byteArray = bout.toByteArray();
		//Clean up
		try {
			bout.close();
		} catch (Throwable error) {	}
		return byteArray;
	}
	
	/**
	 * Gets the name which should be sent as the filenam
	 * in the request.
	 * @return The filename to be sent.
	 */
	public String getFileName() {
		return this.srcFile.getName();
	}
	
	/**
	 * Gets the MIME-Type for this parameter.
	 * @return The mime type specified for this parameter.
	 */
	public String getMimeType() {
		if( this.mimeType == null ) {
			//TODO Attempt to get the MIME-Type based on the file
			this.mimeType = DEFAULT_MIME_TYPE;
		}
		return mimeType;
	}

	/**
	 * Sets the mime type to be used for this parameter.
	 * @param newMimeType The new mime type to be used.
	 * 
	 */
	public void setMimeType(final String newMimeType) {
		this.mimeType = newMimeType;
	}

	/* (non-Javadoc)
	 * @see org.scribe.model.Parameter#usedInBaseString()
	 */
	@Override
	public boolean isUsedInBaseString() {
		return false;
	}
	
	/**
	 * Encodes this parameter in the multi part format.
	 * @return The string encoded using the multi part
	 * format.
	 */
	public byte[] asMultiPartEncodedBytes() {
		final ByteArrayOutputStream strBldr = new ByteArrayOutputStream();
		try {
			strBldr.write(String.format("Content-Disposition: %1$s; ",
					this.getDisposition()).getBytes());
			strBldr.write(String.format("name=\"%1$s\"; ",
					OAuthEncoder.encode(this.getKey())).getBytes());
			strBldr.write(String.format("filename=\"%1$s\"",
					this.getFileName()).getBytes());
			strBldr.write(SEQUENCE_NEW_LINE.getBytes());
			strBldr.write(String.format("Content-Type: %1$s",
					this.getMimeType()).getBytes());
			strBldr.write(SEQUENCE_NEW_LINE.getBytes());
			strBldr.write(SEQUENCE_NEW_LINE.getBytes());
			strBldr.write(this.getValueBytes());
			strBldr.write(SEQUENCE_NEW_LINE.getBytes());
		} catch (Throwable error) {
		}
		return strBldr.toByteArray();
	}
}
