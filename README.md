# Projeto Residencia Accenture - Reserva de Salas e Equipamentos

Backend em Quarkus para gerenciamento de salas, equipamentos, reservas e consultas de disponibilidade.

## Tecnologias

- Java 21
- Quarkus
- PostgreSQL/Supabase
- Maven
- Hibernate ORM com Panache
- Hibernate Validator
- Flyway
- OpenAPI/Swagger UI
- H2 para testes automatizados

## Configuracao

Copie `.env.example` para `.env` e preencha as variaveis:

```env
DB_USER=seu_usuario
DB_PASSWORD=sua_senha
DB_URL=jdbc:postgresql://host:porta/postgres?sslmode=require

SUPABASE_URL=https://seu_projeto.supabase.co
SUPABASE_ANON_KEY=sua_chave

CORS_ORIGINS=http://localhost:3000,http://localhost:5173
```

O arquivo `.env` contem informacoes sensiveis e ja esta listado no `.gitignore`.

Antes de executar comandos Maven, entre na pasta do projeto:

```powershell
cd C:\Users\joaol\Downloads\residencia-accenture\residencia-accenture
```

## Executando

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

Tambem e possivel usar Docker Compose:

```powershell
docker compose up --build
```

## Testes

```powershell
mvn.cmd test
```

Os testes usam H2 em memoria e validam o fluxo principal da API sem depender do banco Supabase.

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
- `GET /salas`
- `POST /salas`
- `GET /salas/{id}`
- `PUT /salas/{id}`
- `DELETE /salas/{id}`
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
- `GET /reservas/disponibilidade/equipamento/{equipamentoId}?inicio=2026-06-01T09:00:00&fim=2026-06-01T10:00:00`
- `GET /disponibilidade/salas?inicio=2026-06-01T09:00:00&fim=2026-06-01T10:00:00`
- `GET /disponibilidade/equipamentos?inicio=2026-06-01T09:00:00&fim=2026-06-01T10:00:00`

## Status dos recursos

Salas e equipamentos aceitam os seguintes valores:

- `DISPONIVEL`
- `INDISPONIVEL`
- `MANUTENCAO`

Somente recursos com status `DISPONIVEL` podem aparecer como disponiveis ou serem usados em novas reservas.

## Validacoes principais

- Sala deve ter `nome` e `capacidade` maior que zero.
- Equipamento deve ter `nome` e `tipo`.
- Reserva deve ter `salaId`, `responsavel`, `dataHoraInicio` e `dataHoraFim`.
- `dataHoraInicio` deve ser anterior a `dataHoraFim`.

## Fluxo de teste manual

Use o Swagger UI e execute nesta ordem:

1. Crie uma sala com `POST /salas`.
2. Crie um equipamento com `POST /equipamentos`.
3. Crie uma reserva com `POST /reservas`, usando o `id` da sala e, opcionalmente, o `id` do equipamento.
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

## Exemplo de reserva

```json
{
  "salaId": 1,
  "equipamentoId": 1,
  "responsavel": "Maria Silva",
  "dataHoraInicio": "2026-06-01T09:00:00",
  "dataHoraFim": "2026-06-01T10:00:00"
}
```
