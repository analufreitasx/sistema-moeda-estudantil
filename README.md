# 🏷️ Karv Coins: Sistema de Mérito Acadêmico 👨‍💻

## 🚧 Status do Projeto

[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-21-DD0031?logo=angular&logoColor=white)](https://angular.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-DB-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Versão](https://img.shields.io/badge/Versão-v1.0.0-blue)](https://github.com/analufreitasx/sistema-moeda-estudantil)


## 📚 Índice
- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades Principais](#-funcionalidades-principais)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Arquitetura](#-arquitetura)
  - [Diagramas](#diagramas)
- [Instalação e Execução](#-instalação-e-execução)
- [Usuários de Demonstração](#-usuários-de-demonstração)
- [Documentação da API (Swagger)](#-documentação-da-api-swagger)
- [Principais Endpoints](#-principais-endpoints)
- [Estrutura de Pastas](#-estrutura-de-pastas)
- [Autores](#-autores)
---

## 📝 Sobre o Projeto
O **Sistema de Mérito Estudantil** é uma plataforma web desenvolvida para estimular o reconhecimento acadêmico por meio de uma moeda virtual. Professores distribuem moedas aos alunos como forma de reconhecimento por bom desempenho, participação e comportamento. Os alunos, por sua vez, podem trocar essas moedas por vantagens oferecidas por empresas parceiras, como descontos em restaurantes, mensalidades ou materiais acadêmicos.

## ✨ Funcionalidades Principais

- 🔐 **Autenticação:** Login com email e senha obrigatório para todos os perfis — aluno, professor e empresa parceira, com emissão de token JWT.
- 📝 **Cadastro de Usuários:** Registro de alunos com dados pessoais e vínculo à instituição; cadastro autônomo de empresas parceiras; professores inseridos pela instituição (perfis de demonstração são criados na inicialização).
- 🪙 **Distribuição de Moedas:** Professores enviam moedas a um ou mais alunos com motivo obrigatório, sujeito à validação de saldo.
- 📆 **Crédito Semestral:** Cada professor possui um saldo de moedas para distribuir ao longo do semestre de forma acumulativa, sem perda do saldo anterior.
- 📋 **Extrato de Conta:** Professores e alunos consultam saldo atual e histórico completo de transações realizadas.
- 🛍️ **Catálogo de Vantagens:** Empresas parceiras cadastram e editam benefícios com descrição, foto e custo em moedas; alunos consultam e selecionam o que desejam resgatar.
- 🎁 **Resgate de Vantagens:** Alunos trocam moedas por vantagens com débito automático do saldo após confirmação, gerando um cupom com **código único** e **QR Code**.
- ✅ **Validação de Cupom:** A empresa verifica o cupom pelo código/QR Code e marca o resgate como utilizado (uso único), registrando quem realizou a validação.
- 📨 **Notificações por Email (assíncronas):** Alunos são notificados ao receber moedas; aluno e empresa recebem o cupom com QR Code a cada resgate. O envio é processado de forma assíncrona via fila (RabbitMQ).

---

## 🛠 Tecnologias Utilizadas

As seguintes ferramentas, frameworks e bibliotecas foram utilizados na construção deste projeto. Recomenda-se o uso das versões listadas (ou superiores) para garantir a compatibilidade.

### 💻 Front-end

* **Framework:** Angular 21 (standalone components + lazy loading)
* **Linguagem:** TypeScript
* **Estilização:** Tailwind CSS
* **Build Tool:** Angular CLI
* **Testes:** Vitest

### 🖥️ Back-end

* **Linguagem/Runtime:** Java 21 (JDK)
* **Framework:** Spring Boot 4.x (Spring Web MVC)
* **Banco de Dados:** PostgreSQL
* **ORM / Query Builder:** Hibernate / JPA (Spring Data JPA)
* **Autenticação:** JWT (OAuth2 Resource Server) + Spring Security
* **Mensageria:** RabbitMQ (Spring AMQP) para notificações assíncronas
* **Envio de E-mail:** Spring Mail + SMTP da [Brevo](https://www.brevo.com/)
* **Geração de QR Code:** ZXing (`com.google.zxing`)
* **Configuração:** springboot4-dotenv (carregamento de variáveis via `.env`)
* **Produtividade:** Lombok
* **Documentação da API:** springdoc-openapi (Swagger UI)

---

## 🏗 Arquitetura

O back-end é um **monólito organizado em camadas**, com um fluxo de notificações **orientado a eventos**.

### Visão geral das camadas (back-end)

- **Controller** — expõe os endpoints REST, valida a entrada (Bean Validation) e converte DTOs ⇄ serviços.
- **Service** — concentra as regras de negócio (envio de moedas, resgate, extrato, validação de cupom etc.).
- **Repository** — acesso a dados via Spring Data JPA sobre PostgreSQL.
- **Model** — entidades JPA. `Usuario` é a superclasse de `Aluno`, `Professor` e `Empresa` (herança), além de `Vantagem`, `Resgate`, `Transacao` e `Endereco`.
- **DTO** — objetos de entrada (`request/`) e saída (`response/`), isolando o modelo interno da API pública.

### Segurança

Autenticação stateless com **JWT**: o `AuthController` emite o token no login e a API o valida como *OAuth2 Resource Server*. O acesso aos endpoints é restringido por perfil (`ALUNO`, `PROFESSOR`, `EMPRESA`) na `SecurityConfig`.

### Notificações orientadas a eventos

O envio de e-mails é desacoplado das transações principais para não impactar o tempo de resposta:

```
Service → publica ApplicationEvent (após commit) → Listener → RabbitMQ (fila/exchange)
        → consumidor → gera QR Code (ZXing) → envia e-mail (Brevo SMTP)
```

- `MoedasEnviadasEvent` / `ResgateCriadoEvent` são publicados pelos serviços e tratados em `AFTER_COMMIT`.
- As mensagens trafegam por uma *exchange* direta (`notificacao-exchange`) para a fila `notificacao-moedas`.
- O consumidor (`MoedasEmailListener`) gera o QR Code do cupom e dispara o e-mail via `EmailNotificationService`.

### Padrões adotados

Service Layer, Repository, DTOs, herança de entidades (single-table) e Observer/Event-Driven para as notificações.

### Diagramas

Para melhor visualização e entendimento da estrutura do sistema, os diagramas principais estão organizados lado a lado. As fontes em PlantUML/Mermaid ficam em [`docs/codigos/`](docs/codigos/).

<table>
  <tr>
    <td align="center" width="50%">
      <strong>🧩 Diagrama de Componentes</strong><br/>
      <a href="docs/Diagrama%20de%20Componentes.png"><img src="docs/Diagrama%20de%20Componentes.png" width="100%"/></a><br/>
      <sub>Front-end, API, banco, mensageria e serviço de e-mail.</sub>
    </td>
    <td align="center" width="50%">
      <strong>📦 Diagrama de Classes</strong><br/>
      <a href="docs/Diagrama%20de%20Classes.png"><img src="docs/Diagrama%20de%20Classes.png" width="100%"/></a><br/>
      <sub>Modelo de domínio e a herança de <code>Usuario</code>.</sub>
    </td>
  </tr>
  <tr>
    <td align="center" width="50%">
      <strong>🗂️ Modelo ER (DER)</strong><br/>
      <a href="docs/DER.png"><img src="docs/DER.png" width="100%"/></a><br/>
      <sub>Estrutura das tabelas no PostgreSQL.</sub>
    </td>
    <td align="center" width="50%">
      <strong>🎭 Diagrama de Casos de Uso</strong><br/>
      <a href="docs/Diagrama%20de%20Casos%20de%20Uso.png"><img src="docs/Diagrama%20de%20Casos%20de%20Uso.png" width="100%"/></a><br/>
      <sub>Interações de aluno, professor e empresa.</sub>
    </td>
  </tr>
</table>

<details>
<summary><strong>🔁 Diagramas de sequência (SSD + por caso de uso)</strong></summary>

| # | Caso de uso | Diagrama |
|---|---|---|
| 00 | Visão geral do sistema (SSD) | [`00-ssd-sistema.png`](docs/00-ssd-sistema.png) |
| 01 | Autenticar | [`01-seq-autenticar.png`](docs/01-seq-autenticar.png) |
| 02 | Cadastrar aluno | [`02-seq-cadastrar-aluno.png`](docs/02-seq-cadastrar-aluno.png) |
| 03 | Cadastrar empresa | [`03-seq-cadastrar-empresa.png`](docs/03-seq-cadastrar-empresa.png) |
| 04 | Consultar extrato do aluno | [`04-seq-consultar-extrato-aluno.png`](docs/04-seq-consultar-extrato-aluno.png) |
| 05 | Visualizar catálogo de vantagens | [`05-seq-visualizar-catalogo-vantagens.png`](docs/05-seq-visualizar-catalogo-vantagens.png) |
| 06 | Resgatar vantagem | [`06-seq-resgatar-vantagem.png`](docs/06-seq-resgatar-vantagem.png) |
| 07 | Enviar moedas | [`07-seq-enviar-moedas.png`](docs/07-seq-enviar-moedas.png) |
| 08 | Cadastrar vantagem | [`08-seq-cadastrar-vantagem.png`](docs/08-seq-cadastrar-vantagem.png) |
| 09 | Validar resgate (cupom) | [`09-seq-validar-resgate-cupom.png`](docs/09-seq-validar-resgate-cupom.png) |
| 10 | Crédito semestral | [`10-seq-credito-semestral.png`](docs/10-seq-credito-semestral.png) |
| 11 | Consultar extrato do professor | [`11-seq-consultar-extrato-professor.png`](docs/11-seq-consultar-extrato-professor.png) |

</details>

---

## 🔧 Instalação e Execução

### Pré-requisitos

* **Java JDK:** Versão **21** ou superior (Back-end Spring Boot)
* **Node.js:** Versão LTS (v20.x ou superior) e **npm** (Front-end Angular)
* **PostgreSQL:** instância acessível para a aplicação
* **RabbitMQ:** *broker* acessível para as notificações assíncronas
* **Conta SMTP (Brevo):** credenciais para o envio de e-mails

### Configuração (`.env`)

O back-end carrega suas configurações de um arquivo `.env` (via `springboot4-dotenv`). Copie o modelo e preencha os valores:

```powershell
cd backend
Copy-Item .env.example .env
```

As principais variáveis a preencher são:

| Variável | Descrição |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexão com o PostgreSQL |
| `JWT_SECRET` | Segredo usado para assinar/validar os tokens JWT |
| `QUEUE_ADDRESS` | Endereço de conexão do RabbitMQ |
| `BREVO_SMTP_LOGIN`, `BREVO_SMTP_KEY`, `BREVO_MAIL_FROM` | Credenciais e remetente do SMTP (Brevo) |
| `FRONTEND_BASE_URL` | URL do front-end (usada nos links dos e-mails) |

> ℹ️ As demais variáveis (host/porta de e-mail, timeouts SMTP, `JPA_DDL_AUTO`, etc.) já vêm com valores padrão no `.env.example`. Para desabilitar o envio real de e-mails durante o desenvolvimento, ajuste `MAIL_ENABLED=false`.

### Back-end

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

A API ficará disponível em `http://localhost:8080/api/karv-coins`.

### Front-end

```powershell
cd frontend
npm install
npm start
```

A aplicação web ficará disponível em `http://localhost:4200`.

---

## 👤 Usuários de Demonstração

Na primeira execução, o back-end cria automaticamente três usuários de demonstração (um por perfil). Todos usam a senha **`Teste@123`**:

| Perfil | E-mail |
|---|---|
| Aluno | `aluno@moeda.local` |
| Professor | `professor@moeda.local` |
| Empresa | `empresa@moeda.local` |

---

## 📖 Documentação da API (Swagger)

A documentação interativa dos endpoints é gerada automaticamente pelo springdoc-openapi e fica disponível com o back-end em execução:

| Recurso | URL |
|---|---|
| Swagger UI | `http://localhost:8080/api/karv-coins/swagger-ui.html` |
| OpenAPI (JSON) | `http://localhost:8080/api/karv-coins/v3/api-docs` |

> ℹ️ A interface do Swagger é disponibilizada **apenas para visualização** dos contratos da API (endpoints, parâmetros, schemas dos DTOs e códigos de resposta). Não é o canal recomendado para testes manuais — utilize o front-end ou uma ferramenta como Postman/Insomnia importando o documento OpenAPI.

---

## 🔌 Principais Endpoints

> Todos os caminhos abaixo são relativos ao *context-path* `/api/karv-coins`.

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/auth/login` | Autentica e retorna o token JWT |
| `GET` | `/auth/validate` | Valida o token atual |
| `POST` | `/alunos` | Cadastra um aluno |
| `GET` | `/alunos/me/resumo` · `/alunos/me/extrato` | Resumo e extrato do aluno autenticado |
| `POST` | `/empresas` | Cadastra uma empresa parceira |
| `GET` · `POST` · `PUT` | `/empresas/me/vantagens` | Lista, cadastra e edita vantagens da empresa |
| `GET` | `/vantagens` | Catálogo de vantagens disponíveis ao aluno |
| `POST` | `/vantagens/{vantagemId}/resgates` | Resgata uma vantagem |
| `POST` | `/moedas/envios` | Professor envia moedas a alunos |
| `GET` | `/professores/me/resumo` · `/professores/me/extrato` | Resumo e extrato do professor autenticado |
| `GET` | `/resgates/{codigoCupom}/verificacao` | Empresa verifica um cupom |
| `PATCH` | `/resgates/{codigoCupom}/utilizacao` | Empresa marca o cupom como utilizado |

---

## 📂 Estrutura de Pastas

```
sistema-moeda-estudantil/
├── backend/                          # API Spring Boot (Java 21)
│   ├── .env.example                  # Modelo de variáveis de ambiente
│   ├── src/main/java/br/pucminas/karv_coins/
│   │   ├── config/                   # Security, OpenAPI, RabbitMQ, Mail, AuthDataInitializer
│   │   ├── controller/               # Endpoints REST
│   │   ├── dto/
│   │   │   ├── request/              # *RequestDto
│   │   │   ├── response/             # *ResponseDto
│   │   │   └── notification/         # Mensagens de notificação (e-mail/fila)
│   │   ├── event/                    # Eventos de aplicação (moedas/resgate)
│   │   ├── listener/                 # Consumidores de eventos e da fila RabbitMQ
│   │   ├── model/                    # Entidades JPA (Usuario, Aluno, Professor, Empresa, Vantagem, Resgate, Transacao)
│   │   ├── repository/               # Spring Data JPA
│   │   ├── security/                 # JwtTokenService
│   │   └── service/                  # Regras de negócio (moedas, resgate, e-mail, QR Code)
│   └── src/main/resources/
│       └── application.yaml
├── frontend/                         # Aplicação Angular + Tailwind
│   └── src/app/
│       ├── components/               # Header, toasts
│       ├── config/                   # API_BASE_URL
│       ├── guards/                   # Proteção de rotas por perfil
│       ├── interceptors/             # Injeção do token JWT nas requisições
│       ├── models/
│       ├── pages/
│       └── services/
└── docs/                             # Especificações e diagramas do projeto
```

---


## 👥 Autores

| 👤 Nome | GitHub | 💼 LinkedIn |
|---------|-----------------|-------------|
| Ana Luiza | <div align="center"><a href="https://github.com/analufreitasx"><img src="https://joaopauloaramuni.github.io/image/github6.png" width="50px" height="50px"></a></div> | <div align="center"><a href="https://www.linkedin.com/in/ana-luizadefreitas/"><img src="https://joaopauloaramuni.github.io/image/linkedin2.png" width="50px" height="50px"></a></div> |
| Kayke Emanoel | <div align="center"><a href="https://github.com/Eman134"><img src="https://joaopauloaramuni.github.io/image/github6.png" width="50px" height="50px"></a></div> | <div align="center"><a href="https://www.linkedin.com/in/kaykeeman/"><img src="https://joaopauloaramuni.github.io/image/linkedin2.png" width="50px" height="50px"></a></div> |
| Renato Douglas | <div align="center"><a href="https://github.com/RenatoDNS"><img src="https://joaopauloaramuni.github.io/image/github6.png" width="50px" height="50px"></a></div> | <div align="center"><a href="https://www.linkedin.com/in/renatodns/"><img src="https://joaopauloaramuni.github.io/image/linkedin2.png" width="50px" height="50px"></a></div> |
| Vicenzo Fonseca | <div align="center"><a href="https://github.com/vicenzofms"><img src="https://joaopauloaramuni.github.io/image/github6.png" width="50px" height="50px"></a></div> | <div align="center"><a href="https://www.linkedin.com/in/vicenzo-fonseca/"><img src="https://joaopauloaramuni.github.io/image/linkedin2.png" width="50px" height="50px"></a></div> |
