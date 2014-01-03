/**
 * 
 */
package org.scribe.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Jermaine
 * 
 */
public class FileParameter extends Parameter {

	public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
	public static final int BUFFER_SIZE = 1024 * 1024 * 8;
	
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
		String fileURL = null;
		try {
			if( this.srcFile != null 
				&& this.srcFile.exists() ) {
				fileURL = this.srcFile.toURI().toString();
			}
		} catch (Throwable error) {}
		return fileURL;
	}
	
	/* (non-Javadoc)
	 * @see org.scribe.model.Parameter#writeAdditionAttributes(java.io.OutputStream)
	 */
	@Override
	protected long writeAdditionalProperties(OutputStream writeTo) throws IOException {
		long byteCount = super.writeAdditionalProperties(writeTo);
		final byte[] newLineBytes = SEQUENCE_NEW_LINE.getBytes();
		final byte[] fileNameTag = String.format("filename=\"%1$s\"", this.getFileName()).getBytes();
		final byte[] contentTypeTag = String.format("Content-Type: %1$s",this.getMimeType()).getBytes();
		writeTo.write( fileNameTag );//Filename
		byteCount += fileNameTag.length;
		writeTo.write( newLineBytes );//New Line
		byteCount += newLineBytes.length;
		writeTo.write( contentTypeTag );//Mime/Content type
		byteCount += contentTypeTag.length;
		return byteCount;
	}
	
	/* (non-Javadoc)
	 * @see org.scribe.model.Parameter#writeValue(java.io.OutputStream)
	 */
	@Override
	protected long writeValue(final OutputStream writeTo) throws IOException {
		long byteCount = 0;
		try {
			//Ensure a valid file is specified.
			if( this.srcFile != null 
				&& this.srcFile.exists() ) {			
				FileInputStream fInStr = null;
				try {
					// Load the file to a stream
					fInStr = new FileInputStream(srcFile);
					//Create a byte buffer the size of the file
					byte[] readBuf = new byte[BUFFER_SIZE];
					int readCnt = fInStr.read(readBuf);
					while (0 < readCnt) {
						//Write to the output stream
						writeTo.write(readBuf, 0, readCnt);
						byteCount += readCnt;
						System.out.println("Wrote file bytes: " + readCnt + ", Total: " + byteCount);
						//Reset the buffer
						readBuf = new byte[BUFFER_SIZE];
						//Read from the file to the buffer
						readCnt = fInStr.read(readBuf);
					}
					fInStr.close();
					fInStr = null;
				} catch (Throwable error) {
					error.printStackTrace();
				} finally {
					if( fInStr != null ) {
						try {
							fInStr.close();
						} catch (Throwable e) {	}
					}
				}
			}
		} catch (Throwable e) { }
		return byteCount;
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
}
