package com.exemplo.services;

import com.exemplo.entities.Session;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.Timed;

/**
 * Serviço para métricas customizadas de negócio da API FIPE.
 * As métricas são expostas automaticamente em /q/metrics
 */
@ApplicationScoped
public class MetricsService {

	/**
	 * Contador de tokens emitidos para clientes da API
	 */
	@Counted(
		name = "fipe_api_tokens_issued_total",
		description = "Total de tokens JWT emitidos para clientes da API",
		absolute = true
	)
	public void incrementTokensIssued() {
		// Método vazio - a anotação @Counted faz o trabalho
	}

	/**
	 * Contador de tokens emitidos para administradores
	 */
	@Counted(
		name = "fipe_api_admin_tokens_issued_total",
		description = "Total de tokens JWT emitidos para administradores",
		absolute = true
	)
	public void incrementAdminTokensIssued() {
		// Método vazio - a anotação @Counted faz o trabalho
	}

	/**
	 * Contador de logins bem-sucedidos
	 */
	@Counted(
		name = "fipe_api_logins_success_total",
		description = "Total de logins bem-sucedidos",
		absolute = true
	)
	public void incrementSuccessfulLogins() {
		// Método vazio - a anotação @Counted faz o trabalho
	}

	/**
	 * Contador de logins falhados
	 */
	@Counted(
		name = "fipe_api_logins_failed_total",
		description = "Total de tentativas de login falhadas",
		absolute = true
	)
	public void incrementFailedLogins() {
		// Método vazio - a anotação @Counted faz o trabalho
	}

	/**
	 * Contador de atualizações de dados FIPE
	 */
	@Counted(
		name = "fipe_api_data_updates_total",
		description = "Total de atualizações de dados FIPE realizadas",
		absolute = true
	)
	public void incrementDataUpdates() {
		// Método vazio - a anotação @Counted faz o trabalho
	}

	/**
	 * Contador de buscas realizadas
	 */
	@Counted(
		name = "fipe_api_searches_total",
		description = "Total de buscas realizadas na API",
		absolute = true
	)
	public void incrementSearches() {
		// Método vazio - a anotação @Counted faz o trabalho
	}

	/**
	 * Gauge para número de sessões ativas
	 * Esta métrica é atualizada automaticamente quando o método é chamado
	 */
	@Gauge(
		name = "fipe_api_active_sessions",
		description = "Número de sessões ativas no momento",
		unit = MetricUnits.NONE,
		absolute = true
	)
	public long getActiveSessionsCount() {
		return Session.count("isActive = true and expiresAt > ?1", 
			java.time.LocalDateTime.now());
	}

	/**
	 * Timer para medir duração de operações de autenticação
	 */
	@Timed(
		name = "fipe_api_auth_duration",
		description = "Duração das operações de autenticação",
		unit = MetricUnits.MILLISECONDS,
		absolute = true
	)
	public void timeAuthOperation() {
		// Método vazio - a anotação @Timed faz o trabalho
		// Use @Timed em métodos que você quer medir
	}
}

