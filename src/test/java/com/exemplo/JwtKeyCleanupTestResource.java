package com.exemplo;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;

/**
 * Test Resource que limpa as chaves JWT antes dos testes iniciarem.
 * Isso evita problemas com chaves inválidas que podem ter sido criadas
 * pelo JwtKeySeeder no startup e que o JwtKeyService.warmUp() não consegue ler.
 * 
 * Esta solução usa JDBC direto para limpar o banco antes do Quarkus iniciar,
 * evitando problemas com transações e contexto JPA.
 */
public class JwtKeyCleanupTestResource implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        // Limpar chaves JWT antes dos testes iniciarem usando JDBC direto
        // Isso evita problemas com chaves inválidas que podem causar falha no warmUp
        String jdbcUrl = System.getProperty("quarkus.datasource.jdbc.url", 
                "jdbc:postgresql://192.168.1.34:5432/fipe");
        String username = System.getProperty("quarkus.datasource.username", "fipe_user");
        String password = System.getProperty("quarkus.datasource.password", "fipe_pass");
        
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             Statement stmt = conn.createStatement()) {
            // Deletar todas as chaves JWT
            stmt.executeUpdate("DELETE FROM jwt_key");
        } catch (Exception e) {
            // Ignorar erros - o banco pode não estar disponível ainda
            // ou pode não haver chaves para deletar
            // Os testes individuais farão a limpeza no setUp se necessário
        }
        return Map.of();
    }

    @Override
    public void stop() {
        // Nada a fazer ao parar
    }
}

