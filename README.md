# Reserva de Posicoes

Sistema com frontend Next.js e backend Quarkus para cadastro de funcionarios,
administracao de ambientes, mesas e posicoes, reservas de trabalho e agentes de
IA.

## Estrutura

```text
app/        Frontend Next.js
lib/        Clientes e utilitarios do frontend
backend/    Backend Quarkus, API, agentes e migrations
supabase/   Scripts SQL do frontend legado
```

Os agentes estao em:

```text
backend/src/main/java/org/acme/agente
backend/src/main/java/org/acme/agent
```

## Requisitos

- Node.js instalado
- Java 21 e Maven instalados
- Projeto Supabase configurado

Variaveis do frontend em `.env.local`:

```env
NEXT_PUBLIC_SUPABASE_URL=...
NEXT_PUBLIC_SUPABASE_ANON_KEY=...
NEXT_PUBLIC_RESIDENCIA_API_URL=http://localhost:8080
```

Variaveis do backend em `backend/.env`:

```env
DB_USER=...
DB_PASSWORD=...
DB_URL=...
CORS_ORIGINS=http://localhost:3000,http://localhost:5173
OPENAI_API_KEY=...
```

`NEXT_PUBLIC_RESIDENCIA_API_URL` aponta para a API Quarkus em `backend`.
Com o backend rodando, acesse `/integracao` neste app para testar a conexao e
ver um resumo de salas, posicoes, usuarios e reservas.

## Rodar Localmente

Em um terminal, rode o backend:

```bash
npm run backend:dev
```

Em outro terminal, rode o frontend:

```bash
npm run dev
```

Acesse:

```text
http://localhost:3000
```

Pagina de integracao:

```text
http://localhost:3000/integracao
```

API:

```text
http://localhost:8080/status
```

## Scripts

```bash
npm run lint
npm run build
npm run start
npm run backend:test
```

## Fluxo Principal

1. Cadastre ou entre com um usuario.
2. Acesse Home, Admin e Minhas Reservas pelo frontend Next.
3. O backend Quarkus centraliza API, regras e agentes.
4. O Supabase permanece como banco de dados.
