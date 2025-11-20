package com.exemplo.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilitário para separar o nome completo do modelo em modelo base e versão.
 * 
 * Exemplos:
 * - "Integra GS 1.8" -> model: "Integra", version: "GS 1.8"
 * - "Legend 3.2/3.5" -> model: "Legend", version: "3.2/3.5"
 * - "100 2.8 V6" -> model: "100", version: "2.8 V6"
 * - "718 Boxster 2.0 300cv" -> model: "718 Boxster", version: "2.0 300cv"
 * - "Hummer Hard-Top 6.5 4x4 Diesel TB" -> model: "Hummer", version: "Hard-Top 6.5 4x4 Diesel TB"
 */
public class ModelVersionParser {

    /**
     * Resultado da separação de modelo e versão
     */
    public static class ParseResult {
        public final String model;
        public final String version;

        public ParseResult(String model, String version) {
            this.model = model;
            this.version = version;
        }
    }

    // Padrões para identificar onde separar modelo de versão (ordem importa!)
    private static final Pattern[] SEPARATION_PATTERNS = {
        // Padrão 1: Nome seguido de palavra-chave de versão (ex: "Hard-Top", "Super", "Luxo", "GS")
        // Ex: "Hummer Hard-Top 6.5" -> modelo: "Hummer", versão: "Hard-Top 6.5"
        // Ex: "Integra GS 1.8" -> modelo: "Integra", versão: "GS 1.8"
        // Ex: "Vantage Cupê  4.0 V8" -> modelo: "Vantage", versão: "Cupê 4.0 V8"
        Pattern.compile("^([A-Za-z-]+)\\s+(Hard-Top|Open-Top|Wagon|Super|Luxo|Elegant|Sport|Cupê|Coupe|Pick-up|Buggy|GS|SE|CD|CS|TB|TDI|V6|V8|16V|8V)(.*)$", Pattern.CASE_INSENSITIVE),
        
        // Padrão 2: Modelo numérico seguido de especificações (ex: "100 2.8 V6")
        // Ex: "100 2.8 V6" -> modelo: "100", versão: "2.8 V6"
        Pattern.compile("^(\\d+)\\s+(.+)$"),
        
        // Padrão 3: Modelo alfanumérico seguido de número decimal (ex: "718 Boxster 2.0")
        // Ex: "718 Boxster 2.0 300cv" -> modelo: "718 Boxster", versão: "2.0 300cv"
        Pattern.compile("^([A-Z0-9-]+(?:\\s+[A-Za-z-]+)?)\\s+(\\d+[./]?\\d*[\\s]?[A-Za-z0-9]+.*)$"),
        
        // Padrão 4: Palavra seguida de número decimal ou fração (ex: "2.8", "3.2/3.5")
        // Ex: "Legend 3.2/3.5" -> modelo: "Legend", versão: "3.2/3.5"
        // Ex: "NSX 3.0" -> modelo: "NSX", versão: "3.0"
        Pattern.compile("^([A-Za-z-]+(?:\\s+[A-Za-z-]+)?)\\s+(\\d+[./]?\\d*[\\s/]?\\d*.*)$"),
        
        // Padrão 5: Modelo com código seguido de descrição (ex: "AM-825 Luxo")
        // Ex: "AM-825 Luxo 4.0 Diesel" -> modelo: "AM-825", versão: "Luxo 4.0 Diesel"
        // Ex: "K01 Pick-up CS 1.0" -> modelo: "K01", versão: "Pick-up CS 1.0"
        Pattern.compile("^([A-Z0-9-]+)\\s+(.+)$"),
        
        // Padrão 6: Qualquer texto seguido de número e característica técnica
        // Ex: "Integra GS 1.8" (fallback se padrão 1 não funcionar)
        Pattern.compile("^(.+?)\\s+(\\d+[./]?\\d*[\\s/]?\\d*[\\s]?[A-Za-z0-9/]+.*)$")
    };

    /**
     * Separa o nome completo do modelo em modelo base e versão.
     * 
     * @param fullName Nome completo do modelo (ex: "Integra GS 1.8")
     * @return ParseResult com model e version separados
     */
    public static ParseResult parse(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new ParseResult("", "");
        }

        // Normalizar espaços múltiplos e remover espaços extras
        String trimmed = fullName.trim().replaceAll("\\s+", " ");

        // Tentar cada padrão de separação
        for (Pattern pattern : SEPARATION_PATTERNS) {
            Matcher matcher = pattern.matcher(trimmed);
            if (matcher.matches()) {
                String model = matcher.group(1).trim();
                String version = matcher.group(2).trim();
                
                // Validar que ambos não estão vazios e que fazem sentido
                if (!model.isEmpty() && !version.isEmpty() && model.length() < trimmed.length()) {
                    return new ParseResult(model, version);
                }
            }
        }

        // Se nenhum padrão funcionou, tentar heurística simples:
        // Dividir no primeiro número ou palavra-chave de versão
        String[] words = trimmed.split("\\s+");
        if (words.length > 1) {
            // Procurar pelo primeiro número ou palavra-chave de versão
            int splitIndex = -1;
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                // Se encontrar um número ou palavra-chave comum de versão
                if (word.matches(".*\\d+.*") || 
                    word.matches("(?i)(Hard-Top|Open-Top|Wagon|Super|Luxo|Elegant|Sport|Cupê|Coupe|Pick-up|Buggy|GS|SE|CD|CS|TB|TDI|V6|V8|16V|8V).*")) {
                    splitIndex = i;
                    break;
                }
            }
            
            if (splitIndex > 0 && splitIndex < words.length) {
                StringBuilder modelBuilder = new StringBuilder();
                StringBuilder versionBuilder = new StringBuilder();
                
                for (int i = 0; i < splitIndex; i++) {
                    if (i > 0) modelBuilder.append(" ");
                    modelBuilder.append(words[i]);
                }
                
                for (int i = splitIndex; i < words.length; i++) {
                    if (i > splitIndex) versionBuilder.append(" ");
                    versionBuilder.append(words[i]);
                }
                
                String model = modelBuilder.toString().trim();
                String version = versionBuilder.toString().trim();
                
                if (!model.isEmpty() && !version.isEmpty()) {
                    return new ParseResult(model, version);
                }
            }
        }

        // Se não conseguiu separar, retornar tudo como modelo
        return new ParseResult(trimmed, "");
    }
}

