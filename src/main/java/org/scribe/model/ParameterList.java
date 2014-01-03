package org.scribe.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.scribe.IParameter;
import org.scribe.utils.OAuthEncoder;
import org.scribe.utils.Preconditions;

/**
 * @author: Pablo Fernandez
 */
public class ParameterList {
	private static final char QUERY_STRING_SEPARATOR = '?';
	private static final String PARAM_SEPARATOR = "&";
	private static final String PAIR_SEPARATOR = "=";
	private static final String EMPTY_STRING = "";

	private static String boundary;

	private final List<IParameter> params;
	private boolean chunkingRecommended;

	public ParameterList() {
		this(new ArrayList<IParameter>());
	}

	ParameterList(List<IParameter> params) {
		this.params = new ArrayList<IParameter>(params);
		this.chunkingRecommended = false;
	}

	public ParameterList(Map<String, String> map) {
		this();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			this.add(entry.getKey(), entry.getValue());
		}
	}

	public void add(String key, String value) {
		this.add(new Parameter(key, value));
	}

	/**
	 * Determines whether or not a file parameter has been added and chunking is
	 * recommended.
	 * 
	 * @return Whether or not chunking is recomended.
	 */
	public boolean isChunkingRecommended() {
		return chunkingRecommended;
	}

	/**
	 * Adds a new parameter to the content of this list.
	 * 
	 * @param newParam
	 *            The new parameter to be added.
	 */
	public void add(IParameter newParam) {
		// If the new parameter is valid
		if (newParam != null) {
			// Add it to the list.
			params.add(newParam);
			if (newParam instanceof FileParameter) {
				this.chunkingRecommended = true;
			}
		}
	}

	public String appendTo(String url) {
		Preconditions.checkNotNull(url, "Cannot append to null URL");
		// String queryString = asFormUrlEncodedString();
		final ByteArrayOutputStream boutStr = new ByteArrayOutputStream();
		writeTo(IParameter.ENCODING_URL, boutStr);
		String queryString = boutStr.toString();
		try {
			boutStr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (queryString.equals(EMPTY_STRING)) {
			return url;
		} else {
			url += url.indexOf(QUERY_STRING_SEPARATOR) != -1 ? PARAM_SEPARATOR
					: QUERY_STRING_SEPARATOR;
			url += queryString;
			return url;
		}
	}

	public long writeTo(final int encoding, final OutputStream writeTo) {
		long byteCount = 0;
		if (params.size() > 0) {
			try {
				switch (encoding) {
				case IParameter.ENCODING_MULTIPART:
					final byte[] doubleDashBytes = "--".getBytes();
					final byte[] newLineBytes = IParameter.SEQUENCE_NEW_LINE
							.getBytes();
					final byte[] boundaryBytes = getBoundary().getBytes();
					final byte[] paramSep = new StringBuilder(new String(
							doubleDashBytes)).append(new String(boundaryBytes))
							.append(new String(newLineBytes)).toString()
							.getBytes();
					writeTo.write(paramSep);
					byteCount += paramSep.length;
					// Add the parameters
					for (final IParameter p : params) {
						// Add the encoded string.
						byteCount += p.writeTo(IParameter.ENCODING_MULTIPART,
								writeTo);
						// Add the boundrary.
						writeTo.write(paramSep);
						byteCount += paramSep.length;
					}
					writeTo.write(newLineBytes);
					byteCount += newLineBytes.length;
					writeTo.write(doubleDashBytes);
					byteCount += doubleDashBytes.length;
					writeTo.write(boundaryBytes);
					byteCount += boundaryBytes.length;
					writeTo.write(doubleDashBytes);
					byteCount += doubleDashBytes.length;
					writeTo.write(newLineBytes);
					byteCount += newLineBytes.length;
					break;
				case IParameter.ENCODING_URL:
					final byte[] strBytes = this.buildURLParamStr(false)
							.getBytes();
					writeTo.write(strBytes);
					byteCount += strBytes.length;
					break;
				case IParameter.ENCODING_OAUTHBASE:
					final byte[] oAuthBaseStrBytes = OAuthEncoder.encode(
							this.buildURLParamStr(true)).getBytes();
					writeTo.write(oAuthBaseStrBytes);
					byteCount += oAuthBaseStrBytes.length;
					break;
				}
			} catch (Throwable error) {
				error.printStackTrace();
			}
		}
		return byteCount;
	}

	/**
	 * @param forBase Is this being built as a base string
	 * @return The string which was created.
	 * @throws IOException
	 */
	private String buildURLParamStr(final boolean forBase) throws IOException {
		StringBuilder oauthBaseStrBuilder = new StringBuilder();
		for (IParameter p : params) {
			// Ensure the parmter is to be added.
			if ((forBase && p.isUsedInBaseString()) || !forBase) {
				final ByteArrayOutputStream boutStr = new ByteArrayOutputStream();
				p.writeTo(IParameter.ENCODING_URL, boutStr);
				boutStr.close();
				oauthBaseStrBuilder.append('&').append(boutStr.toString());
			}
		}
		// Remove the first &
		oauthBaseStrBuilder.delete(0, 1);
		return oauthBaseStrBuilder.toString();
	}

	/**
	 * Gets the boundary to be used when building the body of the request.
	 * 
	 * @return The boundary to be used in the body of multi-part requests.
	 */
	public static String getBoundary() {
		if (boundary == null) {
			final int radix = 36;
			final Random randGen = new Random();
			final StringBuilder strBldr = new StringBuilder();
			strBldr.append(Long.toString(randGen.nextLong(), radix));
			boundary = strBldr.toString();
		}
		return boundary;
	}

	public void addAll(ParameterList other) {
		params.addAll(other.params);
	}

	public void addQuerystring(String queryString) {
		if (queryString != null && queryString.length() > 0) {
			for (String param : queryString.split(PARAM_SEPARATOR)) {
				String pair[] = param.split(PAIR_SEPARATOR);
				String key = OAuthEncoder.decode(pair[0]);
				String value = pair.length > 1 ? OAuthEncoder.decode(pair[1])
						: EMPTY_STRING;
				params.add(new Parameter(key, value));
			}
		}
	}

	public boolean contains(IParameter param) {
		return params.contains(param);
	}

	public int size() {
		return params.size();
	}

	public ParameterList sort() {
		ParameterList sorted = new ParameterList(params);
		Collections.sort(sorted.params);
		return sorted;
	}
}
