# Proximas ideias

## Agente de planta sem custo

- Avaliar provider local com Ollama para custo zero real.
- Modelo sugerido: `qwen2.5vl:7b`; se a maquina estiver fraca, testar `qwen2.5vl:3b`.
- Fluxo desejado: front envia imagem -> backend chama Ollama local -> backend recebe JSON -> usuario revisa -> backend importa no Supabase/PostgreSQL.
- Manter OpenAI como provider opcional quando houver quota ativa.
- Provider online gratuito para avaliar depois: Gemini API free tier, com limites de uso.

## Configuracao futura sugerida

```properties
planta.reconhecimento.provider=ollama
planta.reconhecimento.ollama.url=http://localhost:11434
planta.reconhecimento.ollama.model=qwen2.5vl:7b
```

## Observacao

O teste real com OpenAI chegou na API, mas retornou `insufficient_quota`. O codigo do agente esta funcional; falta quota ativa ou provider alternativo.
