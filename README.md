# Projeto Residência Accenture - Reserva de Salas e Equipamentos

## 📌 Descrição
Sistema backend desenvolvido com Quarkus para gerenciamento de reservas de salas e equipamentos.  
O projeto utiliza Supabase como banco de dados PostgreSQL e está preparado para integração com APIs e inteligência artificial.

---

## 🚀 Tecnologias
- Java
- Quarkus
- PostgreSQL (Supabase)
- Maven
- Git/GitHub

---

## ⚙️ Configuração

Crie um arquivo `.env` na raiz do projeto com as seguintes variáveis:

```env
DB_USER=seu_usuario
DB_PASSWORD=sua_senha
DB_URL=jdbc:postgresql://host:porta/postgres?sslmode=require

SUPABASE_URL=https://seu_projeto.supabase.co
SUPABASE_ANON_KEY=sua_chave
▶️ Executando o projeto
Windows (PowerShell)
.\mvnw quarkus:dev
Linux/Mac
./mvnw quarkus:dev
🧪 Teste inicial

Acesse no navegador:

http://localhost:8080/hello

Resposta esperada:

Hello from Quarkus REST
🔒 Segurança
O arquivo .env contém informações sensíveis
NÃO deve ser enviado ao GitHub
Está configurado no .gitignore
📊 Status do projeto

✔️ Projeto Quarkus configurado
✔️ Integração com Supabase realizada
✔️ Banco PostgreSQL conectado
✔️ Variáveis protegidas com .env
✔️ Código versionado no GitHub


Projeto desenvolvido para a Residência Accenture.