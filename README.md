# 💰 WalletCore API

API REST para gerenciamento financeiro pessoal, construída com **Spring Boot 3** e **Java 21**. Permite controlar usuários, contas bancárias, categorias, transações, além de expor um dashboard e relatórios financeiros — tudo protegido por autenticação JWT e isolado por usuário.

## 📋 Sobre o projeto

O WalletCore API foi desenvolvido com foco em boas práticas de arquitetura, segurança e organização de código: autenticação stateless via JWT, persistência em PostgreSQL, versionamento de schema com Flyway e uma estrutura em camadas (controller → service → repository) que facilita manutenção e evolução.

## 🚀 Tecnologias

- Java 21
- Spring Boot 3.5 (Web, Data JPA, Security, Validation)
- JJWT (JSON Web Token)
- PostgreSQL 16
- Flyway
- Lombok
- Docker / Docker Compose
- Maven

## 📁 Estrutura do projeto

```
src/main/java/br/com/User/walletcore
│
├── config/         # Configuração de segurança e web (CORS, etc.)
├── controllers/     # Endpoints REST
├── services/        # Regras de negócio
├── repositories/     # Acesso a dados (Spring Data JPA)
├── entities/        # Entidades JPA
├── dtos/           # Requests e responses
├── exceptions/       # Exceções de domínio e handler global
└── security/         # JWT, filtros e detalhes do usuário autenticado
```

## 🔐 Autenticação

A API usa autenticação **stateless** baseada em **JWT**. Apenas `/auth/register` e `/auth/login` são públicos; todos os demais endpoints exigem um token válido.

Fluxo:

1. `POST /auth/register` — cria o usuário (senha com BCrypt)
2. `POST /auth/login` — retorna o token JWT
3. Enviar o token em todas as requisições subsequentes:

```
Authorization: Bearer <token>
```

## 📦 Funcionalidades

**Usuários** — cadastro, login, senha criptografada com BCrypt, sessão stateless via JWT.

**Contas bancárias** — CRUD completo. Cada conta pertence a um usuário e tem nome, saldo (`BigDecimal`, não pode ser negativo na criação) e tipo (`CHECKING`, `SAVINGS`, `WALLET`, `INVESTMENT`, `CREDIT_CARD`).

**Categorias** — CRUD completo, do tipo `INCOME` ou `EXPENSE`, usadas para classificar as transações.

**Transações** — criação e listagem de receitas e despesas. Cada transação está vinculada a uma conta e a uma categoria do próprio usuário, tem valor positivo, descrição opcional e data de ocorrência.

**Dashboard** — saldo total, receitas, despesas e resumo por categoria do usuário autenticado.

**Relatórios** — relatório mensal, anual e por categoria (com filtro de período).

## 📚 Endpoints

### Públicos

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/` | Informações básicas da API (nome, versão) |
| POST | `/auth/register` | Cria um novo usuário |
| POST | `/auth/login` | Autentica e retorna o JWT |

### Contas

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/accounts` | Cria uma conta |
| GET | `/accounts` | Lista as contas do usuário |
| GET | `/accounts/{id}` | Detalha uma conta |
| PUT | `/accounts/{id}` | Atualiza uma conta |
| DELETE | `/accounts/{id}` | Remove uma conta |

### Categorias

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/categories` | Cria uma categoria |
| GET | `/categories` | Lista as categorias do usuário |
| GET | `/categories/{id}` | Detalha uma categoria |
| PUT | `/categories/{id}` | Atualiza uma categoria |
| DELETE | `/categories/{id}` | Remove uma categoria |

### Transações

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/transactions` | Registra uma transação |
| GET | `/transactions` | Lista as transações do usuário |
| GET | `/transactions/{id}` | Detalha uma transação |
| PUT | `/transactions/{id}` | Atualiza uma transação |
| DELETE | `/transactions/{id}` | Remove uma transação |

### Dashboard

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/dashboard` | Retorna o resumo financeiro do usuário |

### Relatórios

| Método | Endpoint | Query params | Descrição |
|---|---|---|---|
| GET | `/reports/monthly` | `year`, `month` | Relatório do mês |
| GET | `/reports/year` | `year` | Relatório do ano |
| GET | `/reports/category` | `from`, `to` (opcionais, ISO date) | Relatório por categoria |

Todos os endpoints acima (exceto autenticação) exigem o header `Authorization: Bearer <token>`.

## ⚙️ Regras de negócio

- Apenas usuários autenticados acessam recursos protegidos; requisições sem token válido recebem `401`.
- Cada usuário só enxerga e manipula os próprios dados (contas, categorias, transações).
- O saldo da conta é validado e/ou atualizado a cada transação registrada.
- Não é permitido criar contas com saldo negativo.
- Não é permitido usar categorias que não existam ou que não pertençam ao usuário.
- O sistema impede transações de despesa que resultem em saldo negativo (`InsufficientBalanceException`).
- E-mails duplicados e categorias duplicadas são rejeitados (`EmailAlreadyInUseException`, `CategoryAlreadyExistsException`).

## 🗄️ Banco de dados

PostgreSQL 16, com o schema versionado via **Flyway** (`src/main/resources/db/migration`). As migrações rodam automaticamente na inicialização da aplicação.

## ⚙️ Variáveis de ambiente

Copie `.env.example` para `.env` e ajuste conforme necessário:

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_HOST` | `localhost` | Host do PostgreSQL |
| `DB_PORT` | `5433` | Porta do PostgreSQL (evita conflito com instalação local na 5432) |
| `DB_NAME` | `walletcore` | Nome do banco |
| `DB_USER` | `walletcore` | Usuário do banco |
| `DB_PASSWORD` | `walletcore` | Senha do banco |
| `SERVER_PORT` | `8080` | Porta da aplicação |
| `JWT_SECRET` | — | Segredo usado para assinar o JWT (obrigatório em produção) |
| `JWT_EXPIRATION_MS` | `86400000` | Validade do token em ms (padrão: 24h) |

## 🐳 Executando com Docker

Clone o repositório e entre na pasta:

```bash
git clone <url-do-repositorio>
cd walletcore-api
```

Configure as variáveis de ambiente:

```bash
cp .env.example .env
```

Suba banco e API com Docker Compose:

```bash
docker compose up --build
```

A API estará disponível em `http://localhost:8080`.

## 💻 Executando localmente

Suba apenas o banco via Docker e rode a aplicação com Maven:

```bash
cp .env.example .env
docker compose up -d db
./mvnw spring-boot:run
```

## 🧪 Testes

Suíte de testes de integração (Testcontainers + MockMvc), cobrindo autenticação, CRUD com isolamento por usuário, atualização de saldo, validações, dashboard, relatórios e cenários de concorrência (race condition e deadlock). Requer Docker rodando:

```bash
./mvnw test
```

## 📌 Roadmap

- [x] Cadastro de usuários
- [x] Autenticação JWT
- [x] CRUD de contas
- [x] CRUD de categorias
- [x] CRUD de transações
- [x] Atualização automática de saldo
- [x] Dashboard financeiro
- [x] Relatórios (mensal, anual, por categoria)
- [x] Testes automatizados
- [ ] Documentação Swagger/OpenAPI
- [ ] Deploy em ambiente cloud

## 👨‍💻 Autor

# Cristianlabs

Desenvolvido como projeto de estudo para demonstrar conhecimentos em desenvolvimento backend com Spring Boot, Spring Security, PostgreSQL, Flyway e Docker.
