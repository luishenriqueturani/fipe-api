-- Migration: Adicionar colunas model e version na tabela model
-- Data: 2025-09-02
-- Descrição: Separa o campo name em model (modelo base) e version (versão específica)

-- Adicionar coluna model
ALTER TABLE model ADD COLUMN IF NOT EXISTS model VARCHAR(100);

-- Adicionar coluna version
ALTER TABLE model ADD COLUMN IF NOT EXISTS version VARCHAR(100);

-- Comentários nas colunas (se o banco suportar)
-- COMMENT ON COLUMN model.model IS 'Nome base do modelo (ex: "Integra", "Legend")';
-- COMMENT ON COLUMN model.version IS 'Versão específica do modelo (ex: "GS 1.8", "3.2/3.5")';

