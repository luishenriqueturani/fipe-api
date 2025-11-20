package com.exemplo.services;

import com.exemplo.entities.ApiAccessLog;
import com.exemplo.entities.ApiClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@ApplicationScoped
public class AccessLogService {

	@Inject
	SessionService sessionService;

	@Transactional
	public void logAccess(String tokenJti, String clientId, String method, String path, 
			String query, int statusCode, String ip, String userAgent, long durationMs) {
		// Atualizar última atividade da sessão se houver token JTI
		if (tokenJti != null) {
			sessionService.updateLastActivity(tokenJti);
		}

		// Registrar log de acesso
		ApiAccessLog log = new ApiAccessLog();
		ApiClient client = null;
		if (clientId != null) {
			client = ApiClient.find("clientId", clientId).firstResult();
		}
		log.apiClient = client;
		log.method = method;
		log.path = path;
		log.query = query;
		log.statusCode = statusCode;
		log.ip = ip;
		log.userAgent = userAgent;
		log.durationMs = durationMs;
		log.createdAt = LocalDateTime.now();
		log.persist();
	}
}

