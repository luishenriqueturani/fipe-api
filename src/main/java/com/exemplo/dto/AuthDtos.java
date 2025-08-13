package com.exemplo.dto;

public final class AuthDtos {
	private AuthDtos() {}

	public static class TokenRequest {
		public String clientId;
		public String clientSecret;
	}

	public static class TokenResponse {
		public String access_token;
		public String token_type;
		public long expires_in;

		public TokenResponse() {}

		public TokenResponse(String access_token, String token_type, long expires_in) {
			this.access_token = access_token;
			this.token_type = token_type;
			this.expires_in = expires_in;
		}
	}
}
