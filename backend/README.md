# Projeto Residencia Accenture - Reserva de Salas e Equipamentos

Sistema de reservas com front-end React e Supabase como base principal de dados. O backend Quarkus permanece no projeto como camada opcional para regras mais complexas, integrações e IA.

## Tecnologias

- Java 21
- Quarkus
- React
- Vite
- TypeScript
- Supabase
- PostgreSQL/Supabase
- Maven
- Hibernate ORM com Panache
- Hibernate Validator
- Flyway
- OpenAPI/Swagger UI
- H2 para testes automatizados

## Configuracao

### Backend Quarkus

Copie `.env.example` para `.env` e preencha as variaveis se for executar o backend:

```env
DB_USER=seu_usuario
DB_PASSWORD=sua_senha
DB_URL=jdbc:postgresql://host:porta/postgres?sslmode=require

CORS_ORIGINS=http://localhost:3000,http://localhost:5173
OPENAI_API_KEY=sua_chave_openai
```

O arquivo `.env` contem informacoes sensiveis e ja esta listado no `.gitignore`.

### Front-end React

Entre na pasta `frontend`, copie `.env.example` para `.env` e preencha com os dados do projeto Supabase:

```powershell
cd frontend
copy .env.example .env
```

```env
VITE_SUPABASE_URL=https://seu-projeto.supabase.co
VITE_SUPABASE_ANON_KEY=sua_chave_anon_publica
```

O acesso ao Supabase fica centralizado em `frontend/src/services/supabase` e `frontend/src/repositories`.

## Banco Supabase

O schema inicial esperado pelo front esta em:

```text
supabase/schema.sql
```

Ele cria as tabelas principais:

- `profiles`
- `rooms`
- `positions`
- `equipment_types`
- `equipments`
- `reservations`

Antes de executar comandos Maven, entre na pasta do projeto:

```powershell
cd C:\Users\joaol\OneDrive\Documentos\residencia-accenture\Projeto-residencia-accenture
```

## Executando o backend

Windows PowerShell:

```powershell
mvn.cmd quarkus:dev
```

Se a aplicacao ja estiver rodando na porta `8080`, acesse `http://localhost:8080/status` ou pare o processo antes de iniciar novamente. Para rodar em outra porta:

```powershell
mvn.cmd quarkus:dev -Dquarkus.http.port=8082
```

Linux/Mac:

```bash
./mvnw quarkus:dev
```

## Execucao empacotada

Gere o pacote da aplicacao:

```powershell
mvn.cmd clean package -DskipTests
```

Execute o JAR gerado:

```powershell
java -jar target\quarkus-app\quarkus-run.jar
```

As variaveis do `.env` precisam estar disponiveis no ambiente antes da execucao.

## Docker

No Windows, mantenha o Docker Desktop aberto antes de executar os comandos abaixo.
Se o Docker retornar `Access is denied` ao acessar `docker_engine`, execute o terminal como administrador ou ajuste as permissoes do Docker Desktop para o seu usuario.

Gere o pacote antes de construir a imagem:

```powershell
mvn.cmd clean package -DskipTests
```

Construa a imagem JVM:

```powershell
docker build -f src/main/docker/Dockerfile.jvm -t residencia-accenture-api .
```

Execute usando o `.env` local:

```powershell
docker run --rm --env-file .env -p 8080:8080 residencia-accenture-api
```

Tambem e possivel usar Docker Compose para o backend:

```powershell
docker compose up --build
```

## Testes

```powershell
mvn.cmd test
```

Os testes usam H2 em memoria e validam o fluxo principal da API sem depender do banco Supabase.

## Executando o front-end

Na primeira vez:

```powershell
cd frontend
npm install
```

Depois:

```powershell
npm run dev
```

Acesse:

```text
http://localhost:5173
```

Para validar o build:

```powershell
npm run build
```

## Banco de dados

O banco e versionado com Flyway. As migracoes ficam em:

```text
src/main/resources/db/migration/postgresql
src/main/resources/db/migration/h2
```

Na inicializacao, o Flyway aplica as migracoes do banco em uso. O Hibernate esta configurado para validar o schema, nao para criar tabelas automaticamente.

No PostgreSQL/Supabase, a migracao `V2__compatibilizar_schema_existente.sql` ajusta nomes antigos das colunas de data da tabela `reservas`, quando necessario. Em banco novo, a aplicacao cria a estrutura pela migracao `V1__criar_tabelas_reserva.sql`.

## Documentacao da API

Com a aplicacao em execucao, acesse:

- `http://localhost:8080/swagger-ui`
- `http://localhost:8080/openapi`
- `http://localhost:8080/q/health`

## Endpoints

- `GET /status`
- `POST /api/chat`
- `GET /usuarios`
- `POST /usuarios`
- `GET /usuarios/{id}`
- `PUT /usuarios/{id}`
- `DELETE /usuarios/{id}`
- `GET /salas`
- `POST /salas`
- `GET /salas/{id}`
- `PUT /salas/{id}`
- `DELETE /salas/{id}`
- `GET /posicoes`
- `POST /posicoes`
- `GET /posicoes/{id}`
- `PUT /posicoes/{id}`
- `DELETE /posicoes/{id}`
- `GET /equipamentos`
- `POST /equipamentos`
- `GET /equipamentos/{id}`
- `PUT /equipamentos/{id}`
- `DELETE /equipamentos/{id}`
- `GET /reservas`
- `POST /reservas`
- `GET /reservas/{id}`
- `PUT /reservas/{id}`
- `PUT /reservas/{id}/cancelar`
- `DELETE /reservas/{id}`
- `GET /reservas/disponibilidade/sala/{salaId}?inicio=2026-06-01T09:00:00&fim=2026-06-01T10:00:00`
- `GET /reservas/disponibilidade/posicao/{posicaoId}?inicio=2026-06-01T09:00:00&fim=2026-06-01T10:00:00`
- `GET /disponibilidade/salas?inicio=2026-06-01T09:00:00&fim=2026-06-01T10:00:00`
- `GET /disponibilidade/posicoes?inicio=2026-06-01T09:00:00&fim=2026-06-01T10:00:00`
- `POST /plantas/analisar`
- `POST /plantas/importar`
- `POST /plantas/importar/resultado`

## Status dos recursos

Salas, posicoes e equipamentos aceitam os seguintes valores:

- `DISPONIVEL`
- `INDISPONIVEL`
- `MANUTENCAO`

Somente recursos com status `DISPONIVEL` podem aparecer como disponiveis ou serem usados em novas reservas.

## Validacoes principais

- Sala deve ter `nome` e `capacidade` maior que zero.
- Equipamento deve ter `nome` e `tipo`.
- Reserva deve ter `salaId` ou `posicaoId`, `responsavel`, `dataHoraInicio` e `dataHoraFim`.
- Uma reserva nao pode informar `salaId` e `posicaoId` ao mesmo tempo.
- `dataHoraInicio` deve ser anterior a `dataHoraFim`.

## Fluxo de teste manual

Use o Swagger UI e execute nesta ordem:

1. Crie uma sala com `POST /salas`.
2. Crie um equipamento com `POST /equipamentos`.
3. Crie uma reserva com `POST /reservas`, usando o `id` da sala ou o `id` da posicao.
4. Consulte `GET /reservas` para confirmar a reserva criada.
5. Consulte disponibilidade em `GET /reservas/disponibilidade/sala/{salaId}`.
6. Cancele a reserva com `PUT /reservas/{id}/cancelar`.
7. Consulte disponibilidade novamente para confirmar que o horario foi liberado.

Exemplo de sala:

```json
{
  "nome": "Sala Reuniao 1",
  "capacidade": 8,
  "localizacao": "Bloco A",
  "status": "DISPONIVEL"
}
```

Exemplo de equipamento:

```json
{
  "nome": "Projetor 1",
  "descricao": "Projetor HDMI",
  "tipo": "VIDEO",
  "status": "DISPONIVEL"
}
```

Exemplo de posicao:

```json
{
  "codigo": "P12",
  "descricao": "Mesa de trabalho proxima a janela",
  "localizacao": "Bloco B",
  "recursos": "Monitor ultrawide e cadeira ergonomica",
  "status": "DISPONIVEL"
}
```

## Exemplo de reserva

```json
{
  "salaId": 1,
  "responsavel": "Maria Silva",
  "dataHoraInicio": "2026-06-01T09:00:00",
  "dataHoraFim": "2026-06-01T10:00:00"
}
```

Exemplo de reserva de posicao:

```json
{
  "posicaoId": 1,
  "usuarioId": 1,
  "responsavel": "Maria Silva",
  "dataHoraInicio": "2026-06-01T09:00:00",
  "dataHoraFim": "2026-06-01T10:00:00"
}
```

## Importacao por foto da planta

O endpoint `POST /plantas/importar` recebe uma imagem em Base64, envia para a OpenAI Responses API e cadastra a sala, as posicoes e os equipamentos detectados no banco configurado, incluindo Supabase quando `DB_URL` aponta para ele.

```json
{
  "mimeType": "image/png",
  "nomeArquivo": "planta-andar-3.png",
  "imagemBase64": "iVBORw0KGgoAAA...",
  "cadastrar": true
}
```

Para revisar antes de cadastrar, use `POST /plantas/analisar` ou envie `"cadastrar": false`. Depois, o JSON revisado pode ser persistido com `POST /plantas/importar/resultado`:

```json
{
  "resumo": "Planta com uma sala e duas posicoes",
  "observacoes": "Confianca alta nos rotulos visiveis",
  "sala": {
    "nome": "Sala Andar 3",
    "descricao": "Sala identificada na planta",
    "localizacao": "Bloco B",
    "capacidade": 2,
    "confianca": 0.95,
    "equipamentos": [
      {
        "nome": "Projetor",
        "tipo": "PRO",
        "descricao": "Projetor fixo na sala",
        "confianca": 0.9
      }
    ]
  },
  "posicoes": [
    {
      "codigo": "P12",
      "descricao": "Mesa proxima a janela",
      "localizacao": "Bloco B",
      "recursos": "Monitor e dock",
      "confianca": 0.95,
      "equipamentos": [
        {
          "nome": "Monitor Dell",
          "tipo": "MON",
          "descricao": "Monitor sobre a mesa",
          "confianca": 0.92
        }
      ]
    }
  ]
}
```

Configure `OPENAI_API_KEY` para a analise real da imagem. Opcionalmente, ajuste `OPENAI_VISION_MODEL`, `OPENAI_VISION_MAX_OUTPUT_TOKENS` e `OPENAI_VISION_IMAGE_DETAIL`; o padrao atual do projeto e `gpt-4.1-mini`, `1800` tokens de saida e `low` para reduzir custo.
