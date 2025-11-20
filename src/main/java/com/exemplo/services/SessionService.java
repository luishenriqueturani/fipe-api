package com.exemplo.services;

import com.exemplo.entities.AdminUser;
import com.exemplo.entities.ApiClient;
import com.exemplo.entities.Session;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.HttpHeaders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SessionService {

	@Transactional
	public Session createSessionForClient(ApiClient client, String ipAddress, String userAgent, LocalDateTime expiresAt) {
		Session session = new Session();
		session.apiClient = client;
		session.tokenJti = UUID.randomUUID().toString();
		session.ipAddress = ipAddress;
		session.userAgent = userAgent;
		session.loginAt = LocalDateTime.now();
		session.lastActivityAt = LocalDateTime.now();
		session.expiresAt = expiresAt;
		session.isActive = true;
		session.persist();

		// Atualizar lastAccessAt do cliente
		client.lastAccessAt = LocalDateTime.now();
		client.persist();

		return session;
	}

	@Transactional
	public Session createSessionForAdmin(AdminUser admin, String ipAddress, String userAgent, LocalDateTime expiresAt) {
		Session session = new Session();
		session.adminUser = admin;
		session.tokenJti = UUID.randomUUID().toString();
		session.ipAddress = ipAddress;
		session.userAgent = userAgent;
		session.loginAt = LocalDateTime.now();
		session.lastActivityAt = LocalDateTime.now();
		session.expiresAt = expiresAt;
		session.isActive = true;
		session.persist();

		return session;
	}

	@Transactional
	public void updateLastActivity(String tokenJti) {
		Session session = Session.find("tokenJti = ?1 and isActive = true", tokenJti).firstResult();
		if (session != null && !session.isExpired()) {
			session.lastActivityAt = LocalDateTime.now();
			session.persist();
		}
	}

	@Transactional
	public void logoutSession(String tokenJti) {
		Session session = Session.find("tokenJti = ?1", tokenJti).firstResult();
		if (session != null) {
			session.isActive = false;
			session.logoutAt = LocalDateTime.now();
			session.persist();
		}
	}

	public List<Session> getActiveSessions() {
		return Session.find("isActive = true and expiresAt > ?1", LocalDateTime.now())
				.list();
	}

	public List<Session> getActiveSessionsForClient(Long clientId) {
		return Session.find("apiClient.id = ?1 and isActive = true and expiresAt > ?2", clientId, LocalDateTime.now())
				.list();
	}

	public List<Session> getActiveSessionsForAdmin(Long adminId) {
		return Session.find("adminUser.id = ?1 and isActive = true and expiresAt > ?2", adminId, LocalDateTime.now())
				.list();
	}

	public Session findByTokenJti(String tokenJti) {
		return Session.find("tokenJti = ?1", tokenJti).firstResult();
	}
}

