package org.scribe.model;

import java.io.ByteArrayOutputStream;

import org.scribe.utils.OAuthEncoder;

/**
 * @author: Pablo Fernandez
 */
public class Parameter implements Comparable<Parameter>
{
  public static final String DISPOSITION_FORM_DATA = "form-data";
  public static final String DISPOSITION_ATTACHMENT = "attachment";
  public static final String DISPOSITION_INLINE = "inline";
  
  public static final String SEQUENCE_NEW_LINE = "\r\n";
	
  private final String key;
  private final String value;
  private String disposition;

  public Parameter(String key, String value)
  {
    this.key = key;
    this.value = value;
  }

  public String getValue() {
	return value;
  }

  public byte[] getValueBytes() {
	 byte[] bytes;
	 if( this.getValue() != null ) {
		 bytes = this.getValue().getBytes();
	 } else {
		 bytes = new byte[0];
	 }
	 return bytes;
  }
  
  public String getKey() {
	return key;
  }

public String asUrlEncodedPair()
  {
    return OAuthEncoder.encode(getKey()).concat("=").concat(OAuthEncoder.encode(getValue()));
  }
  
  /**
   * Gets the content disposition to be used for this parameter.
   * 
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
   * Sets the content disposition to be used for this parameter.
   * 
   * @param newDisposition
   *            The new content disposition to be set.
   */
  public void setDisposition(final String newDisposition) {
	this.disposition = newDisposition;
  }
	
  /**
   * Encodes this parameter in the multi part format.
   * 
   * @return The string encoded using the multi part format.
   */
  public String asMultiPartEncodedString() {
	return new String(this.asMultiPartEncodedBytes());
  }

  public byte[] asMultiPartEncodedBytes() {
	  final ByteArrayOutputStream strBldr = new ByteArrayOutputStream();
	  try {
		  strBldr.write(String.format("Content-Disposition: %1$s; ",
				  this.getDisposition()).getBytes());
		  strBldr.write(String.format("name=\"%1$s\"; ", this.getKey()).getBytes());
		  strBldr.write(SEQUENCE_NEW_LINE.getBytes());
		  strBldr.write(SEQUENCE_NEW_LINE.getBytes());
		  strBldr.write(this.getValueBytes());
		  strBldr.write(SEQUENCE_NEW_LINE.getBytes());
		
	  } catch (Throwable error) { }
	  return strBldr.toByteArray();
  }
  
  /**
   * Indicates whether or not this parameter is to be included in
   * the base string which is generated.
   * @return Whether or not to include this parameter in the base string.
   */
  public boolean isUsedInBaseString() {
	return true;
  }
	
  public boolean equals(Object other)
  {
    if(other == null) return false;
    if(other == this) return true;
    if(!(other instanceof Parameter)) return false;
    
    Parameter otherParam = (Parameter) other;
    return otherParam.getKey().equals(getKey()) && otherParam.getValue().equals(getValue());
  }

  public int hashCode()
  {
    return getKey().hashCode() + getValue().hashCode();
  }

  public int compareTo(Parameter parameter)
  {
    int keyDiff = getKey().compareTo(parameter.getKey());

    return keyDiff != 0 ? keyDiff : getValue().compareTo(parameter.getValue());
  }
}
