# 📘 Relatório de Análise Crítica do Projeto 👨‍💻

## 1. Informações do grupo
- **🎓 Curso:** Engenharia de Software
- **📘 Disciplina:** Laboratório de Desenvolvimento de Software
- **🗓 Período:** 4° Período
- **👨‍🏫 Professor(a):** Prof. Dr. João Paulo Carneiro Aramuni
- **👥 Membros do Grupo:**
  - Ana Luiza de Freitas Rodrigues
  - Kayke Emanoel de Souza Santos
  - Renato Douglas Nascimento Silva de Oliveira
  - Vicenzo Fonseca de Mello Souza

---

## 📌 2. Identificação do Projeto
- **Nome do projeto:** Sistema de Moeda Estudantil
- **Integrantes do outro grupo:** Alice Shikida e demais integrantes do grupo
- **Link do repositório:** https://github.com/aliceshikida/SistMoedaInfantil
- **Pull requests submetidos pelo nosso grupo:**

  | 👤 Integrante | 🔧 Refatoração | Pull Request |
  |--------------|---------------|--------------|
  | Kayke Emanoel de Souza Santos | Extração do `asyncHandler` (remoção de `try/catch` duplicado nos controllers) | https://github.com/aliceshikida/SistMoedaInfantil/pull/1 |
  | Ana Luiza de Freitas Rodrigues | Helper `runWithToast` (centralização do feedback de toast/erro) | https://github.com/aliceshikida/SistMoedaInfantil/pull/2 |
  | Renato Douglas Nascimento Silva de Oliveira | Extração de constantes de domínio (remoção do número mágico `1000`) | https://github.com/aliceshikida/SistMoedaInfantil/pull/3 |
  | Vicenzo Fonseca de Mello Souza | Extração da validação de saldo (`assertSaldoSuficiente`, remoção de checagem duplicada) | https://github.com/aliceshikida/SistMoedaInfantil/pull/4 |

---

## 🧱 3. Arquitetura e Tecnologias Utilizadas

O projeto é um **monorepo** dividido em `backend/` e `frontend/`, com `docker-compose.yml`
para orquestração e uma pasta `docs/` com os diagramas (casos de uso, classes, componentes
e sequência). A separação entre back-end e front-end é clara.

### 🏗️ Backend: Node.js + Express + Prisma

O back-end segue uma arquitetura em camadas bem definidas:

```
routes → middlewares (authenticate/authorize) → controllers → services → dao → Prisma
```

- **Controllers** (`auth`, `core`, `qr`): recebem a requisição e devolvem a resposta.
- **Services** (`auth`, `business`, `email`, `notification`, `professor`): concentram a
  regra de negócio, incluindo transações atômicas com `prisma.$transaction` no envio de
  moedas e no resgate de vantagens.
- **DAO** (8 arquivos): isolam o acesso ao banco via Prisma, com um padrão consistente.
- **Middlewares**: centralizam autenticação, autorização, upload e, principalmente, o
  **tratamento de erro** (`error.middleware.js`), que padroniza os objetos
  `{ status, message }` lançados pelo código e ainda trata a violação de unicidade do
  Prisma (`P2002`).

Tecnologias empregadas:
- Node.js + Express
- Prisma ORM (SQLite no ambiente local; schema pensado para PostgreSQL)
- JWT + bcryptjs (autenticação e hash de senha)
- Multer (upload da foto das vantagens)
- Nodemailer + RabbitMQ (envio de e-mails em fila, com *fallback* síncrono)
- Swagger (documentação da API)

### 🎨 Frontend: React + Vite

- **React + Vite**, **React Router** para rotas e **Axios** para o consumo da API (cliente
  centralizado em `lib/api.js`, com um *interceptor* que injeta o token JWT em toda
  requisição).
- **React Hook Form + Zod** para formulários e validação.
- **TailwindCSS** para o visual (dark-first e responsivo), **Recharts** para os gráficos do
  dashboard e **React Toastify** para o feedback ao usuário.
- Autenticação guardada em um **Context** (`AuthProvider.jsx`) junto com o `localStorage`.

### 🔄 Integração entre camadas

O front-end conversa com o back-end exclusivamente por **APIs REST** (`/api/...`), com o
token JWT sendo enviado automaticamente pelo interceptor do Axios. O upload de imagens é
tratado com Multer no back-end e servido em `/uploads`.

De modo geral, a arquitetura é sólida e madura para um trabalho da disciplina: camadas bem
separadas, uso de transações, fila de mensagens e documentação de API. Os pontos de melhoria
são mais de **consistência e remoção de duplicação** do que de estrutura.

---

## 🗂️ 4. Organização do GitHub e Fluxo de Trabalho Colaborativo

### 4.1. Estrutura do Repositório e Documentação
- **Estrutura de pastas:** clara e lógica, com `backend/` e `frontend/` bem separados e
  seguindo convenções comuns de projetos Node/React.
- **Documentação:** o `README.md` é o ponto forte: descreve o projeto, lista os requisitos,
  traz o passo a passo de execução (back, front, RabbitMQ e Docker), as credenciais de
  *seed* e os principais endpoints.
- **Observação:** o README descreve as camadas como "Controllers → Services → Prisma", mas
  no código existe uma camada **DAO** entre os services e o Prisma que não é mencionada.

### 4.2. Fluxo de Trabalho (Branches e Pull Requests)
- O repositório trabalha apenas na branch **main**, sem *feature branches* nem Pull Requests
  no fluxo interno do grupo; logo, não há evidência de **Code Review** antes do *merge*.
- Também não há uso perceptível de **Issues**, *labels* ou *milestones* para rastrear
  tarefas e organizar o backlog.

### 4.3. Padrões de Commits e Versionamento
- As mensagens de commit são muito genéricas (`ajuste`, `ULTIMA`, `Add files via upload`
  repetido, `verifc`), o que dificulta entender o propósito de cada mudança.
- A autoria é bastante concentrada: uma integrante tem cerca de 40 commits e os demais
  pouquíssimos.
- Não há uso de **Tags/Releases** nem de **CI/CD** (GitHub Actions).

---

## 🖥️ 5. Dificuldade para Configuração do Ambiente

O `README` é detalhado e o passo a passo funciona, mas encontramos algumas fricções.

### 5.1. Requisitos e Ferramentas de Build
- **Passo redundante no README:** o README instrui rodar `npm install qrcode` como comando
  avulso, mas a dependência já consta no `package.json` do back-end. O ideal é remover esse
  passo manual para evitar confusão no setup.
- **Script de lint quebrado no back-end:** `backend/package.json` define
  `"lint": "eslint src prisma --ext .js"`, mas o projeto usa **ESLint 9**, que exige um
  arquivo `eslint.config.js` (inexistente no back-end) e **não aceita mais** a flag `--ext`.
  Assim, `npm run lint` falha de imediato. No front-end o lint funciona, pois lá existe o
  `eslint.config.js`.

### 5.2. Persistência e Variáveis de Ambiente
- **Dois arquivos `.env`:** é preciso copiar `backend/.env.example` e
  `frontend/.env.example` e preencher as variáveis (SMTP, RabbitMQ, `VITE_API_URL`).
- **SQLite × PostgreSQL:** o banco local é SQLite, mas o de produção é PostgreSQL. É prático
  para rodar rápido, porém essa diferença pode esconder incompatibilidades que só apareceriam
  no Postgres.
- **RabbitMQ opcional:** bem tratado com *fallback* síncrono, mas adiciona uma camada a
  entender no primeiro contato.

### 5.3. Passos para Subir o Projeto
Copiar os dois `.env`; no back-end rodar `npm install`, `npx prisma generate`, `npx prisma db
push`, `npm run prisma:seed` e `npm run dev`; no front-end, `npm install` e `npm run dev`. O
passo `npm install qrcode` citado no README é redundante, pois a dependência já está no
`package.json`. Fora os detalhes acima, foi tranquilo colocar o projeto para rodar.

---

## 🚀 6. Sugestões de Melhorias

1. **Padronizar o tratamento de chamadas assíncronas**, removendo os `try/catch` repetidos
   no back-end e o mesmo bloco de *toast* repetido no front-end (feito nas Refatorações 1
   e 2).
2. **Eliminar números e strings mágicos**, trocando-os por constantes com nome, a começar
   pelo `1000` de moedas (feito na Refatoração 3).
3. **Corrigir o lint do back-end** (adicionar `eslint.config.js`, ajustar o script) e
   **remover o passo manual `npm install qrcode` do README**, já que a dependência está no
   `package.json`.
4. **Adicionar testes automatizados**: o projeto hoje **não possui testes** (cobertura 0%).
   Sugere-se testes unitários nos *services* e de integração nos *controllers* (Vitest/Jest +
   Supertest), cobrindo as regras críticas (envio de moedas, resgate, saldo).
5. **Quebrar componentes/handlers grandes**, como `VantagensPage.jsx` (~347 linhas) e os
   métodos `dashboardHandler`/`extratoHandler`, que concentram a lógica dos quatro perfis num
   único bloco.
6. **Melhorar o fluxo no GitHub**: adotar *feature branches*, Pull Requests com descrição,
   Issues para o backlog e mensagens de commit no padrão Conventional Commits.
7. **Configurar CI/CD** (GitHub Actions) para rodar lint e build a cada PR.

---

## 🔧 7. Refatorações Propostas (partes do código)

Aplicamos **quatro refatorações simples**, todas sem mudar o comportamento do sistema (uma
por integrante). Após cada uma, rodamos o projeto de novo: o front continuou compilando e as
rotas do back seguiram respondendo, para confirmar que nada quebrou.

---

### 1️⃣ Refatoração 1 – Extração do `asyncHandler` (Extract Method)

**Responsável:** Kayke Emanoel de Souza Santos https://github.com/aliceshikida/SistMoedaInfantil/pull/1 |
**Arquivos:** `backend/src/utils/asyncHandler.js` (novo),
`backend/src/controllers/auth.controller.js`, `backend/src/controllers/core.controller.js`
O mesmo `try/catch` aparecia em **todos** os controllers, sempre chamando `next(error)` no
`catch` (4 vezes no `auth.controller.js` e 12 no `core.controller.js`).

#### 🔴 Antes (`core.controller.js`)
```js
export async function listInstituicoes(req, res, next) {
  try {
    const items = await InstituicaoDAO.listAll();
    res.json(items);
  } catch (error) {
    next(error);
  }
}
```

#### 🟢 Depois
```js
// backend/src/utils/asyncHandler.js
export const asyncHandler = (fn) => (req, res, next) =>
  Promise.resolve(fn(req, res, next)).catch(next);
```
```js
// backend/src/controllers/core.controller.js
export const listInstituicoes = asyncHandler(async (_req, res) => {
  const items = await InstituicaoDAO.listAll();
  res.json(items);
});
```

#### ✔ Tipo de refatoração
- **Extract Method / Higher-Order Function** (DRY)

#### 📝 Justificativa
Elimina ~16 blocos idênticos e deixa cada handler focado na regra de negócio. Como o erro
continua indo para o `next`, o middleware de erro que já existe segue funcionando igual, então
o comportamento não muda, e some o risco de esquecer o `next(error)` em algum handler novo.

---

### 2️⃣ Refatoração 2 – Helper `runWithToast` (Remoção de Código Duplicado)

**Responsável:** Ana Luiza de Freitas Rodrigues https://github.com/aliceshikida/SistMoedaInfantil/pull/2 |
**Arquivos:** `frontend/src/lib/toastAction.js` (novo),
`frontend/src/pages/EnvioMoedasPage.jsx`, `frontend/src/pages/VantagensPage.jsx`
O bloco "toast de carregando → chamada da API → toast de sucesso/erro" estava copiado em
vários lugares (envio de moedas e cadastro/exclusão/resgate de vantagens), cada um com ~10
linhas quase iguais, incluindo a mesma extração de `error?.response?.data?.message`.

#### 🔴 Antes (`EnvioMoedasPage.jsx`)
```js
const toastId = toast.loading('Enviando moedas...')
setSubmitting(true)
try {
  await api.post('/professor/enviar-moedas', form)
  toast.update(toastId, { render: 'Moedas enviadas com sucesso', type: 'success', isLoading: false, autoClose: 1200 })
} catch (error) {
  toast.update(toastId, {
    render: error?.response?.data?.message || 'Falha ao enviar moedas',
    type: 'error', isLoading: false, autoClose: 2500,
  })
} finally {
  setSubmitting(false)
}
```

#### 🟢 Depois
```js
// frontend/src/lib/toastAction.js
export async function runWithToast(
  { loading, success, error, successAutoClose = 1200, errorAutoClose = 2500, onStart, onSettle },
  action,
) {
  const toastId = toast.loading(loading)
  onStart?.()
  try {
    const result = await action()
    toast.update(toastId, { render: success, type: 'success', isLoading: false, autoClose: successAutoClose })
    return result
  } catch (err) {
    toast.update(toastId, {
      render: err?.response?.data?.message || error,
      type: 'error', isLoading: false, autoClose: errorAutoClose,
    })
    return undefined
  } finally {
    onSettle?.()
  }
}
```
```js
// frontend/src/pages/EnvioMoedasPage.jsx
await runWithToast(
  {
    loading: 'Enviando moedas...',
    success: 'Moedas enviadas com sucesso',
    error: 'Falha ao enviar moedas',
    onStart: () => setSubmitting(true),
    onSettle: () => setSubmitting(false),
  },
  () => api.post('/professor/enviar-moedas', form),
)
```

#### ✔ Tipo de refatoração
- **Extract Function** (DRY) em utilitário reutilizável

#### 📝 Justificativa
Cada página fica bem menor e o tratamento de erro da API passa a ficar num lugar só. Os
*callbacks* `onStart`/`onSettle` mantêm o controle de "carregando" de cada página
(`setSubmitting`, `setDeletingId`, `setRescuingId`) e o `errorAutoClose` preserva o tempo
específico do caso de exclusão (2800 ms), então na tela tudo funciona igual.

---

### 3️⃣ Refatoração 3 – Constantes de domínio (Replace Magic Number with Constant)

**Responsável:** Renato Douglas Nascimento Silva de Oliveira https://github.com/aliceshikida/SistMoedaInfantil/pull/3 |
**Arquivos:** `backend/src/config/constants.js` (novo),
`backend/src/services/professor.service.js`
O valor `1000` (moedas que o professor ganha por semestre) aparecia solto em mais de um lugar
que precisa ter o mesmo valor.

#### 🔴 Antes (`professor.service.js`)
```js
saldoMoedas: { increment: 1000 },
```

#### 🟢 Depois
```js
// backend/src/config/constants.js
export const MOEDAS_CREDITO_SEMESTRAL = 1000;
export const SALDO_INICIAL_PROFESSOR = MOEDAS_CREDITO_SEMESTRAL;
```
```js
// backend/src/services/professor.service.js
saldoMoedas: { increment: MOEDAS_CREDITO_SEMESTRAL },
```

> O mesmo valor também é consumido no extrato do professor (`core.controller.js`), que passa
> a usar `SALDO_INICIAL_PROFESSOR`.

#### ✔ Tipo de refatoração
- **Replace Magic Number with Symbolic Constant**

#### 📝 Justificativa
O nome deixa claro o que o número significa e, como o saldo inicial vem do crédito do
semestre, os dois valores não correm mais o risco de ficar diferentes. Se um dia o valor
mudar, muda só num lugar.

---

### 4️⃣ Refatoração 4 – Extração da validação de saldo (Remoção de Código Duplicado)

**Responsável:** Vicenzo Fonseca de Mello Souza |
**Arquivos:** `backend/src/utils/saldo.js` (novo),
`backend/src/services/business.service.js`
A mesma verificação de **"Saldo insuficiente."** estava duplicada em dois métodos do
`business.service.js`: no envio de moedas (`enviarMoedas`) e no resgate de vantagem
(`resgatarVantagem`).

#### 🔴 Antes (`business.service.js`)
```js
// em enviarMoedas
if (professor.saldoMoedas < quantidade) throw { status: 400, message: "Saldo insuficiente." };

// em resgatarVantagem
if (aluno.saldoMoedas < vantagem.custoMoedas) throw { status: 400, message: "Saldo insuficiente." };
```

#### 🟢 Depois
```js
// backend/src/utils/saldo.js
export function assertSaldoSuficiente(saldoAtual, valorNecessario) {
  if (saldoAtual < valorNecessario) {
    throw { status: 400, message: "Saldo insuficiente." };
  }
}
```
```js
// backend/src/services/business.service.js
assertSaldoSuficiente(professor.saldoMoedas, quantidade);
// ...
assertSaldoSuficiente(aluno.saldoMoedas, vantagem.custoMoedas);
```

#### ✔ Tipo de refatoração
- **Extract Function / Replace Duplicated Code with Method**

#### 📝 Justificativa
Junta a regra de "saldo suficiente" num único ponto, com um nome que deixa a intenção clara.
Se a validação ou a mensagem mudar, muda só num lugar, e o mesmo helper pode ser reutilizado
em novas operações que envolvam saldo.

---

## 📄 8. Conclusão

A análise crítica permitiu identificar pontos fortes e pontos de melhoria do **Sistema de
Moeda Estudantil**. Como pontos positivos, destacam-se a **arquitetura em camadas bem
separada** (Controllers → Services → DAO → Prisma), o uso de **transações** nas operações
sensíveis, a **fila de e-mails** com *fallback* e uma **documentação de setup** clara no
README. Como pontos de atenção, aparecem código duplicado, números mágicos, métodos longos,
ausência de testes automatizados e um fluxo de GitHub pouco colaborativo (branch única e
commits genéricos).

As quatro refatorações aplicadas atacaram diretamente a **duplicação de código** e a
**clareza**: o `asyncHandler` e o `runWithToast` removeram blocos repetidos no back e no
front, a extração de constantes eliminou um número mágico e a extração do
`assertSaldoSuficiente` unificou a validação de saldo duplicada, tudo sem alterar o
comportamento do sistema. Somadas às
sugestões de maior fôlego (testes, CI/CD, padronização de commits e correção do lint), essas
mudanças ajudam a manter o projeto mais legível e sustentável ao longo do tempo.
