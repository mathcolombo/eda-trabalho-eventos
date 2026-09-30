# Sistema de Vendas e Gestão de Eventos (EDA)

Plataforma distribuída orientada a eventos (*Event-Driven Architecture*) composta por 4 microsserviços integrados via RabbitMQ com persistência individual em SQLite.

---

## 1. Dependências e Pré-requisitos

Para executar o projeto completo através dos containers orquestrados, o ambiente requer apenas as seguintes ferramentas instaladas:

* **Docker Engine** (versão 24.0 ou superior) ou **Podman** (versão 4.5 ou superior).
* **Docker Compose** (versão 2.20 ou superior) ou utilitário `podman-compose`.
* **cURL** (ou cliente HTTP equivalente, como Postman/Insomnia) para testes de endpoints.
* **sqlite3 CLI** *(opcional)*: para consulta direta aos arquivos de banco de dados locais.
* **Java 21 JDK + Maven 3.9+** *(opcional)*: necessário apenas caso decida executar os serviços fora dos containers Docker.

---

## 2. Visão Geral das Portas e Serviços

Ao inicializar o Docker Compose, as seguintes portas estarão acessíveis na máquina host:

| Serviço | Porta Host | Finalidade |
| :--- | :--- | :--- |
| **RabbitMQ Management** | `15672` (Web UI) / `5672` (AMQP) | Broker de Mensageria e painel administrativo |
| **Events Service** | `8081` | API de criação e consulta de eventos |
| **Tickets Service** | `8082` | API de compra de ingressos, inventário e check-in |
| **Payments Service** | `8083` | Processamento financeiro e controle de idempotência |
| **Notifications Service** | `8084` | Consumidor multicanal de notificações |

---

## 3. Guia de Execução Passo a Passo

### Passo 1: Clonar o repositório e acessar a pasta raiz
```bash
git clone https://github.com/mathcolombo/eda-trabalho-eventos.git
cd eda-trabalho-eventos

```

### Passo 2: Criar as pastas de persistência do SQLite

Cada serviço utiliza seu próprio arquivo SQLite montado via volume local para manter os dados salvos entre reinicializações. Crie a estrutura de diretórios necessária:

```bash
mkdir -p services/events/data services/tickets/data services/payments/data services/notifications/data

```

### Passo 3: Inicializar todos os serviços com Docker Compose

Execute o comando abaixo na raiz do projeto para compilar as imagens multi-stage e subir os containers em segundo plano:

```bash
docker compose up --build -d

```

> *Nota: Caso esteja utilizando Podman, execute: `podman compose up --build -d`.*

### Passo 4: Acompanhar os logs de inicialização

Acompanhe os logs consolidados em tempo real:

```bash
docker compose logs -f

```

O RabbitMQ possui *healthcheck* configurado no compose. Os microsserviços Spring Boot aguardarão o broker ficar saudável e iniciarão na sequência. Aguarde a mensagem `Started ...Application` em todos os serviços.

### Passo 5: Acessar a Web UI do RabbitMQ

* Abra o navegador em: **`http://localhost:15672`**
* Usuário padrão: `admin`
* Senha padrão: `admin`

---

## 4. Roteiro de Testes e Validação do Fluxo

Execute a sequência de comandos abaixo para validar a coreografia completa de ponta a ponta:

### 1. Criar um evento com 2 vagas

```bash
curl -i -X POST http://localhost:8081/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "name": "DevOps & Cloud Summit 2026",
    "description": "Edição de arquitetura distribuída",
    "eventDate": "2026-11-15T19:00:00",
    "maxCapacity": 2,
    "basePrice": 200.00
  }'

```

### 2. Comprar o 1º ingresso (Fluxo: Tickets -> Payments -> Fanout -> Notifications)

```bash
curl -i -X POST http://localhost:8082/api/tickets/purchase \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": 1,
    "customerName": "Amanda Ribeiro",
    "customerEmail": "amanda@email.com"
  }'

```

### 3. Realizar o Check-in do 1º ingresso

```bash
curl -i -X POST http://localhost:8082/api/tickets/1/check-in

```

### 4. Comprar o 2º ingresso (Gatilho de Evento Esgotado)

```bash
curl -i -X POST http://localhost:8082/api/tickets/purchase \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": 1,
    "customerName": "Bruno Lima",
    "customerEmail": "bruno@email.com"
  }'

```

### 5. Tentar comprar um 3º ingresso (Validação de Bloqueio por Lotação)

```bash
curl -i -X POST http://localhost:8082/api/tickets/purchase \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": 1,
    "customerName": "Lucas Silva",
    "customerEmail": "lucas@email.com"
  }'

```

*A requisição será rejeitada pela aplicação (código HTTP 4xx).*

---

## 5. Inspeção dos Bancos de Dados Locais

Para auditar os registros gravados em cada serviço diretamente pelo terminal:

```bash
# Consultar evento e contagem de confirmados:
sqlite3 -header -column services/events/data/events.db "SELECT id, name, max_capacity, confirmed_tickets, status FROM event;"

# Consultar status dos ingressos emitidos:
sqlite3 -header -column services/tickets/data/tickets.db "SELECT id, customer_name, price, status FROM ticket;"

# Consultar pagamentos processados com sucesso:
sqlite3 -header -column services/payments/data/payments.db "SELECT id, ticket_id, amount, status FROM payments;"

# Consultar notificações e comprovantes gerados:
sqlite3 -header -column services/notifications/data/notifications.db "SELECT id, recipient, title, channel, status FROM notifications;"

```

---

## 6. Parando a Aplicação

Para pausar e remover os containers:

```bash
docker compose down

```

Para remover os containers juntamente com os volumes de mensagens do RabbitMQ:

```bash
docker compose down -v

```

```

```