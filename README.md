# WalletCore API

API pública de gestão financeira, desenvolvida com Spring Boot.

## Stack

- Java 21
- Spring Boot 3.5
- Spring Web, Spring Data JPA, Spring Security
- PostgreSQL
- Flyway (migrações)
- Docker / Docker Compose
- Maven

## Rodando localmente

### Pré-requisitos

- Java 21
- Docker e Docker Compose

### Subindo o banco de dados

```bash
cp .env.example .env
docker compose up -d db
```

### Rodando a aplicação

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.

### Rodando tudo via Docker

```bash
docker compose up --build
```

## Estrutura do projeto

```
src/main/java/br/com/litto/walletcore/
├── config/
├── controllers/
├── services/
├── repositories/
├── entities/
├── dtos/
├── exceptions/
├── security/
└── utils/
```

## Roadmap

- [x] Commit 1 — Inicialização
- [ ] Commit 2 — Estrutura
- [ ] Commit 3 — Usuários
- [ ] Commit 4 — Migrações
- [ ] Commit 5 — Segurança (JWT)
- [ ] Commit 6 — Contas
- [ ] Commit 7 — Categorias
- [ ] Commit 8 — Transações
- [ ] Commit 9 — Atualização automática do saldo
- [ ] Commit 10 — Validações
- [ ] Commit 11 — Dashboard
- [ ] Commit 12 — Relatórios
