package com.mrc.infusionsoft;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Token {

	private static final String	ACCESS_TOKEN = "access_token";
	private static final String	END_OF_LIFE = "end_of_life";
	private static final String	EXPIRES_IN = "expires_in";
	private static final String	REFRESH_TOKEN = "refresh_token";

	@SerializedName("accessToken")
	@Expose
	private String	accessToken;
	@SerializedName("refreshToken")
	@Expose
	private String	refreshToken;
	@SerializedName("expiresIn")
	@Expose
	private long	expiresIn;
	@SerializedName("endOfLife")
	@Expose
	private long	endOfLife;
	@SerializedName("extraInfo")
	@Expose
	private Map<String, Object>	extraInfo;

	/**
	 * No args constructor for use in serialization
	 * 
	 */
	public Token() {}

	public Token(Map<String, Object> data) {
		super();

		if (data.containsKey(ACCESS_TOKEN)) {
			setAccessToken((String) data.get(ACCESS_TOKEN));

			data.remove(ACCESS_TOKEN);
		}

		if (data.containsKey(REFRESH_TOKEN)) {
			setRefreshToken((String) data.get(REFRESH_TOKEN));

			data.remove(REFRESH_TOKEN);
		}

		if (data.containsKey(EXPIRES_IN)) {
			setExpiresIn((long) data.get(EXPIRES_IN));
			setEndOfLife(calculateEndOfLife((long) data.get(EXPIRES_IN)));

			data.remove(EXPIRES_IN);
		} else if (data.containsKey(END_OF_LIFE)) {
			setEndOfLife((long) data.get(END_OF_LIFE));
			setExpiresIn(calculateExpiresIn((long) data.get(END_OF_LIFE)));

			data.remove(END_OF_LIFE);
		}

		if (!data.isEmpty()) setExtraInfo(data);
	}

	public Token(String accessToken, String refreshToken, long expiresIn, long endOfLife, Map<String, Object> extraInfo) {
		super();

		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.expiresIn = expiresIn;
		this.endOfLife = endOfLife;
		this.extraInfo = extraInfo;
	}

	private static long calculateEndOfLife(long expiresIn) {
		long	currentTimeMS = new Date().getTime();
		long	currentTimeSec = currentTimeMS / 1000;

		return currentTimeSec + expiresIn;
	}

	private static long calculateExpiresIn(long endOfLife) {
		long	currentTimeMS = new Date().getTime();
		long	currentTimeSec = currentTimeMS / 1000;

		return endOfLife - currentTimeSec;
	}

	/**
	 * Checks if the token is expired
	 *
	 * @return boolean
	 */
	public boolean isExpired() {
		long	now = new Date().getTime() / 1000;

		return (endOfLife < now);
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public long getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(long expiresIn) {
		this.expiresIn = expiresIn;
	}

	public long getEndOfLife() {
		return endOfLife;
	}

	public void setEndOfLife(long endOfLife) {
		this.endOfLife = endOfLife;
	}

	public Map<String, Object> getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(Map<String, Object> extraInfo) {
		this.extraInfo = extraInfo;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("accessToken", accessToken).append("refreshToken", refreshToken).append("expiresIn", expiresIn).append("endOfLife", endOfLife).append("extraInfo", extraInfo).toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(accessToken).append(refreshToken).append(expiresIn).append(endOfLife).append(extraInfo).toHashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) return true;

		if (!(other instanceof Token)) return false;

		Token	rhs = ((Token) other);

		return new EqualsBuilder().append(accessToken, rhs.accessToken).append(refreshToken, rhs.refreshToken).append(expiresIn, rhs.expiresIn).append(endOfLife, rhs.endOfLife).append(extraInfo, rhs.extraInfo).isEquals();
	}
}