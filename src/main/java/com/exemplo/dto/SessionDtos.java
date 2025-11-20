package com.exemplo.dto;

import java.time.LocalDateTime;

public final class SessionDtos {
	private SessionDtos() {}

	public static class SessionResponse {
		public Long id;
		public String tokenJti;
		public LocalDateTime loginAt;
		public LocalDateTime lastActivityAt;
		public LocalDateTime expiresAt;
		public boolean isActive;
		public LocalDateTime logoutAt;
		public String ipAddress;
		public String userAgent;
		public ClientInfo client;
		public AdminInfo admin;

		public SessionResponse() {}
	}

	public static class ClientInfo {
		public Long id;
		public String name;
		public String clientId;

		public ClientInfo() {}
	}

	public static class AdminInfo {
		public Long id;
		public String name;
		public String username;
		public String email;

		public AdminInfo() {}
	}
}

