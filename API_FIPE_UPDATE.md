# API de Atualização de Dados FIPE

## Endpoint

**POST** `/fipe-data/update`

## Descrição

Este endpoint permite atualizar o banco de dados com os dados da FIPE recebidos do scraper. A rota processa o JSON, verifica se as marcas e modelos já existem, e atualiza as informações conforme necessário.

## Autenticação

A rota requer autenticação via JWT com role `API_CLIENT`.

## Estrutura do JSON

O endpoint espera um JSON com a seguinte estrutura:

```json
{
  "cars": [
    {
      "id": 1,
      "name": "Acura",
      "models": [
        {
          "id": 1,
          "name": "Integra GS 1.8",
          "years": [
            {
              "referenceMonth": "setembro de 2025",
              "fipeCode": "038003-2",
              "brand": "Acura",
              "model": "Integra GS 1.8",
              "modelYear": "1992 Gasolina",
              "authentication": "ghs7m37lbc",
              "queryDate": "terça-feira, 2 de setembro de 2025 09:33",
              "averagePrice": {
                "value": 11043,
                "formattedValue": "R$ 11.043,00"
              }
            }
          ]
        }
      ]
    }
  ],
  "trucks": [
    {
      "id": 1,
      "name": "AGRALE",
      "models": [
        {
          "id": 1,
          "name": "10000 / 10000 S 2p (diesel) (E5)",
          "years": [
            {
              "referenceMonth": "setembro de 2025",
              "fipeCode": "501034-9",
              "brand": "AGRALE",
              "model": "10000 / 10000 S  2p (diesel) (E5)",
              "modelYear": "2022 Diesel",
              "authentication": "mmlkr18wlldnc",
              "queryDate": "terça-feira, 2 de setembro de 2025 13:07",
              "averagePrice": {
                "value": 245859,
                "formattedValue": "R$ 245.859,00"
              }
            }
          ]
        }
      ]
    }
  ],
  "motorCycles": [
    {
      "id": 1,
      "name": "ADLY",
      "models": [
        {
          "id": 1,
          "name": "ATV 100",
          "years": [
            {
              "referenceMonth": "setembro de 2025",
              "fipeCode": "840015-6",
              "brand": "ADLY",
              "model": "ATV 100",
              "modelYear": "2002 Gasolina",
              "authentication": "sk8n8k2mzgq",
              "queryDate": "terça-feira, 2 de setembro de 2025 16:32",
              "averagePrice": {
                "value": 3769,
                "formattedValue": "R$ 3.769,00"
              }
            }
          ]
        }
      ]
    }
  ]
}
```

## Comportamento

### Verificações e Atualizações

1. **Tipo de Veículo**: Cria se não existir (Carros, Caminhões, Motocicletas)
2. **Marca**: Cria se não existir, atualiza nome se necessário
3. **Modelo**: Cria se não existir, atualiza nome se necessário
4. **Ano do Modelo**: Cria se não existir, atualiza dados se necessário
5. **Preço**: Cria se não existir, atualiza valor e data de consulta se necessário

### Campos Adicionados às Entidades

- **ModelYear**: Adicionados campos `fipeCode` e `authentication`
- **Price**: Adicionado campo `authentication` e ajustado formato do `referenceMonth`

## Respostas

### Sucesso (200)
```json
{
  "message": "Dados da FIPE atualizados com sucesso"
}
```

### Erro - Dados não fornecidos (400)
```json
{
  "error": "Dados da FIPE não fornecidos"
}
```

### Erro - Nenhum tipo de veículo (400)
```json
{
  "error": "Pelo menos um tipo de veículo deve ser fornecido"
}
```

### Erro - Servidor (500)
```json
{
  "error": "Erro interno do servidor: [detalhes do erro]"
}
```

## Exemplo de Uso

```bash
curl -X POST http://localhost:8080/fipe-data/update \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer [JWT_TOKEN]" \
  -d @modelo.json
```

## Logs

O serviço registra logs detalhados sobre:
- Início e fim do processamento
- Criação de novas entidades
- Atualizações de entidades existentes
- Erros durante o processamento
