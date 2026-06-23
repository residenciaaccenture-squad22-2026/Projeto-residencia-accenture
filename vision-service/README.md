# 👁️ Vision Service

O **Vision Service** é o motor de inteligência artificial do ecossistema Grow Up. Desenvolvido sobre o ecossistema **Quarkus (Java 21)**, o serviço utiliza visão computacional via **Google Gemini API** para analisar imagens de plantas baixas ou layouts de escritórios, mapeando de forma totalmente hierárquica e tridimensional a disposição de Salas, Mesas, Posições (Cadeiras) e seus respectivos Recursos Tecnológicos (Monitores, mouses, etc).

Para garantir a integridade dos dados físicos da empresa, o serviço adota o padrão arquitetural **Human-in-the-Loop** (Humano no Ciclo), permitindo que a IA gere um rascunho preciso que só é persistido no **Supabase** após a revisão e confirmação manual do usuário.

---

## 🚀 Principais Funcionalidades

* **Mapeamento Espacial por IA:** Processamento assíncrono de imagens para detecção de mobiliário corporativo.
* **Extração Hierárquica Relacional:** O motor converte a imagem diretamente em uma árvore estruturada: `Sala ➔ Mesas ➔ Posições (X, Y) ➔ Recursos`.
* **Arquitetura Human-in-the-Loop:** Divisão de responsabilidades entre análise preditiva e persistência transacional.
* **Fast-Jar Otimizado para Docker:** Imagem containerizada baseada em Red Hat UBI, utilizando caching inteligente de camadas para builds em milissegundos.

---

## 🛠️ Stack Tecnológica

* **Runtime:** [Quarkus 3.x](https://quarkus.io/)
* **Linguagem:** Java 21 (OpenJDK HotSpot)
* **Engine de IA:** Google Gemini Pro Vision API
* **Banco de Dados & API Layer:** [Supabase](https://supabase.com/) (PostgreSQL REST Client)
* **Containerização:** Docker & Docker Compose
* **Build Tool:** Maven

---

## 🏗️ Fluxo da Arquitetura

O ecossistema funciona em dois passos estritos para evitar alocação errônea de patrimônio no banco:

```text
[Usuário] ──(1. URL da Imagem)──>  /analisar  ──> [Gemini API] ──(Retorna JSON Cru)──┐
                                                                                   │
[Usuário] <──(Exibe Rascunho na Tela para Correções/Ajustes)───────────────────────┘
   │
   └─────(2. JSON Revisado + Detalhes)──> /confirmar ──> [Supabase] (Persistência Relacional)

```

---

## ⚙️ Variáveis de Ambiente Necessárias

O serviço depende das seguintes chaves de ambiente para inicialização. **Nunca chube estes valores no `application.properties**`.

| Variável | Descrição | Exemplo |
| --- | --- | --- |
| `SUPABASE_URL` | URL de conexão do projeto Supabase | `https://xxxx.supabase.co` |
| `SUPABASE_KEY` | Chave secreta de acesso (`service_role`) | `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` |
| `AI_API_KEY` | Token de Autenticação do Google AI Studio | `AIzaSyA1...` |

---

## 📦 Como Executar o Projeto

### Localmente (Modo Desenvolvimento)

Certifique-se de exportar as variáveis de ambiente no seu terminal antes de subir o Quarkus:

```bash
export SUPABASE_URL="sua_url"
export SUPABASE_KEY="sua_chave"
export AI_API_KEY="sua_chave_gemini"

./mvnw quarkus:dev

```

### Via Docker (Recomendado)

Para empacotar a aplicação e rodá-la em um container isolado de produção:

1. **Compile o projeto gerando o Fast-Jar:**
```bash
./mvnw clean package

```


2. **Construa a imagem Docker local:**
```bash
docker build -f src/main/docker/Dockerfile.jvm -t growup/vision-service .

```


3. **Suba o container injetando as credenciais:**
```bash
docker run -i --rm -p 8080:8080 \
  -e SUPABASE_URL="sua_url_aqui" \
  -e SUPABASE_KEY="sua_chave_aqui" \
  -e AI_API_KEY="sua_chave_aqui" \
  growup/vision-service

```



---

## 🔌 Documentação da API

### 1. Solicitar Análise da Imagem (Rascunho)

Analisa a imagem e devolve a predição estruturada sem salvar nada no banco de dados.

* **Rota:** `POST /api/setup-sala/analisar`
* **Payload de Entrada:**
```json
{
  "imageUrl": "https://link-da-imagem-publica.com/foto-sala.jpg"
}

```


* **Response (200 OK):**
```json
{
  "nomeSugeridoSala": "Espaço de Convivência e Circulação",
  "mesas": [
    {
      "idFicticioMesa": "mesa_lounge_01",
      "codigoMesa": "LOUNGE-MESA-01",
      "posicoes": [
        {
          "idFicticioPosicao": "posicao_lounge_01",
          "codigoCadeira": "LOUNGE-CAD-01",
          "coordenadasX": 70.0,
          "coordenadasY": 530.0,
          "recursos": [
            { "categoria": "Eletrônico", "nomeModelo": "Monitor Dell 24" }
          ]
        }
      ]
    }
  ]
}

```



### 2. Confirmar e Persistir Dados

Recebe o rascunho revisado pelo operador humano e realiza a inserção atômica em cascata no banco de dados.

* **Rota:** `POST /api/setup-sala/confirmar`
* **Payload de Entrada:**
```json
{
  "nome": "Sala VIP de Inovação",
  "localizacao": "Bloco C - 2º Andar",
  "analiseRevisada": { ... (Cole aqui o JSON retornado pela rota acima, com suas correções) }
}

```


* **Response (200 OK):**
```json
{
  "status": "Setup salvo com sucesso!"
}

```


## ⚙️ Configuração de Credenciais

Como o arquivo `application.properties` contém chaves sensíveis, ele está mascarado pelo `.gitignore`. Para rodar o projeto localmente, siga o passo a passo abaixo para configurar seu ambiente.

### 1. Configurando o arquivo local
1. Na pasta `src/main/resources/`, localize o arquivo `application.properties.example`.
2. Duplique o arquivo e renomeie a cópia para **`application.properties`**.
3. Preencha as propriedades em branco com as suas chaves obtidas nos passos abaixo.

---

### 🔑 Como obter as chaves de acesso

#### 🌐 1. Google AI Studio (API do Gemini)
Para que o motor de visão funcione, você precisará de um token de acesso para a API do Gemini:
1. Acesse o console do [Google AI Studio](https://aistudio.google.com/app/apikey).
2. Faça login com a sua conta Google.
3. No menu lateral ou painel principal, clique no botão **"Create API key"** (Criar chave de API).
4. Escolha se deseja vincular a chave a um projeto existente do Google Cloud ou criar em um novo projeto.
5. Copie o token gerado (ele começa com `AIzaSy...`) e cole na propriedade `AI_API_KEY` do seu ambiente ou arquivo de configuração.

#### ⚡ 2. Supabase (Banco de Dados e API)
Você precisará de duas informações do seu painel do Supabase: a **URL do Projeto** e a **Chave Service Role** (necessária para realizar os inserts ignorando políticas de RLS restritivas).

1. Acesse o painel do [Supabase](https://supabase.com/) e entre no seu projeto.
2. No menu lateral esquerdo, clique no ícone de engrenagem (**Project Settings** / Configurações do Projeto).
3. Na lista de configurações, clique na opção **API**.
4. Na seção **Project API keys**, localize os seguintes campos:
   * **Project URL:** Copie o link completo (ex: `https://xxxx.supabase.co`). Este valor vai para a propriedade `SUPABASE_URL`.
   * **`service_role` (secret):** Clique em *Reveal* (Revelar) para exibir a chave secreta. **Atenção:** Use a `service_role` e não a chave `anon/public`, pois seu agente precisa de permissão de escrita no banco. Copie este valor para a propriedade `SUPABASE_KEY`.
