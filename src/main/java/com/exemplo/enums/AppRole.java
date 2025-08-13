package com.exemplo.enums;

public enum AppRole {
	API_CLIENT("api-client");

	private final String role;

	AppRole(String role) {
		this.role = role;
	}

	public String role() {
		return role;
	}
}
