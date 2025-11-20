package com.exemplo.services;

import com.exemplo.entities.AdminUser;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AdminSeeder {

	private static final Logger LOG = Logger.getLogger(AdminSeeder.class);

	private static final String DEFAULT_ADMIN_EMAIL = "admin@fipe-api.local";
	private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
	private static final String DEFAULT_ADMIN_NAME = "Administrador";
	private static final String DEFAULT_ADMIN_USERNAME = "admin";

	@Transactional
	void onStart(@Observes StartupEvent ev) {
		LOG.info("Verificando se usuário admin padrão existe...");

		AdminUser existingAdmin = AdminUser.find("email = ?1", DEFAULT_ADMIN_EMAIL).firstResult();
		
		if (existingAdmin != null) {
			LOG.info("Usuário admin padrão já existe: " + DEFAULT_ADMIN_EMAIL);
			return;
		}

		// Verificar também por username para evitar duplicatas
		AdminUser existingByUsername = AdminUser.find("username = ?1", DEFAULT_ADMIN_USERNAME).firstResult();
		if (existingByUsername != null) {
			LOG.warn("Já existe um usuário com username '" + DEFAULT_ADMIN_USERNAME + "', mas com email diferente. Pulando criação do admin padrão.");
			return;
		}

		// Criar admin padrão
		AdminUser admin = new AdminUser();
		admin.name = DEFAULT_ADMIN_NAME;
		admin.username = DEFAULT_ADMIN_USERNAME;
		admin.email = DEFAULT_ADMIN_EMAIL;
		admin.password = DEFAULT_ADMIN_PASSWORD; // Em produção, usar hash
		admin.isActive = true;
		admin.persist();

		LOG.info("Usuário admin padrão criado com sucesso!");
		LOG.info("Email: " + DEFAULT_ADMIN_EMAIL);
		LOG.info("Username: " + DEFAULT_ADMIN_USERNAME);
		LOG.warn("ATENÇÃO: A senha padrão é '" + DEFAULT_ADMIN_PASSWORD + "'. Altere-a em produção!");
	}
}

