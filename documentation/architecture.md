# Arquitetura e Fluxo de Eventos

## 1. Visão Geral
O sistema implementa o padrão **Event-Driven Architecture (EDA)** aliado ao princípio **Database-per-Service**. Cada microsserviço opera de forma autônoma, mantém seu próprio esquema de banco de dados relacional isolado (SQLite) e comunica-se assincronamente via **RabbitMQ**.

Essa topologia elimina dependências temporais em cascata, garante que picos de compra (flash sales) sejam enfileirados sem sobrecarregar a camada de persistência e viabiliza a inclusão de novos consumidores sem alterações nos serviços emissores.

---

## 2. Diagrama de Componentes e Topologia RabbitMQ

```mermaid
graph TB
    subgraph Services["Microsserviços"]
        Events["Events Service<br/>(Porta 8081)<br/>[SQLite: eventsdb]"]
        Tickets["Tickets Service<br/>(Porta 8082)<br/>[SQLite: ticketsdb]"]
        Payments["Payments Service<br/>(Porta 8083)<br/>[SQLite: paymentsdb]"]
        Notif["Notifications Service<br/>(Porta 8084)<br/>[SQLite: notificationsdb]"]
    end

    subgraph Broker["RabbitMQ Broker"]
        subgraph TopicEx["Exchange: events.topic (Topic)"]
            Q_EventsCreated["tickets.event-create.queue"]
            Q_NotifEvents["notifications.events.queue"]
            Q_EventsSoldOut["events.soldout.queue"]
            Q_NotifSoldOut["notif.soldout.queue"]
            Q_EventsCheckIn["events.checkin.queue"]
            Q_NotifCheckIn["notif.checkin.queue"]
        end

        subgraph DirectEx["Exchange: tickets.direct (Direct)"]
            Q_PaymentProcess["payment.process.queue"]
        end

        subgraph FanoutEx["Exchange: payments.fanout (Fanout)"]
            Q_TicketsPaid["tickets.payment-confirmed.queue"]
            Q_EventsPaid["events.payment-confirmed.queue"]
            Q_NotifPaid["notif.payment-confirmed.queue"]
        end
    end

    %% EventCreated
    Events -- "EventCreated [event.created]" --> TopicEx
    TopicEx --> Q_EventsCreated --> Tickets
    TopicEx --> Q_NotifEvents --> Notif

    %% TicketPurchased
    Tickets -- "TicketPurchased [ticket.purchased]" --> DirectEx
    DirectEx --> Q_PaymentProcess --> Payments

    %% PaymentConfirmed
    Payments -- "PaymentConfirmed" --> FanoutEx
    FanoutEx --> Q_TicketsPaid --> Tickets
    FanoutEx --> Q_EventsPaid --> Events
    FanoutEx --> Q_NotifPaid --> Notif

    %% TicketChecked
    Tickets -- "TicketChecked [ticket.checked]" --> TopicEx
    TopicEx --> Q_EventsCheckIn --> Events
    TopicEx --> Q_NotifCheckIn --> Notif

    %% EventSoldOut
    Tickets -- "EventSoldOut [event.soldout]" --> TopicEx
    TopicEx --> Q_EventsSoldOut --> Events
    TopicEx --> Q_NotifSoldOut --> Notif
```
## 3. Serviços e Responsabilidades

### 3.1 Events Service (events)

- Porta: 8081
- Banco de Dados: SQLite (eventsdb)
- Responsabilidades:
    - Manter o catálogo de eventos (nome, data, capacidade máxima, preço base).
    - Controlar o estado de disponibilidade do evento (ACTIVE, SOLD_OUT, CANCELLED).
    - Atualizar contadores de presença física e métricas de capacidade geral.
- Mensageria:
    - Produtor: EventCreated (na exchange events.topic).
    - Consumidor: EventSoldOut, PaymentConfirmed, TicketChecked.

### 3.2 Tickets Service (tickets)

- Porta: 8082
- Banco de Dados: SQLite (ticketsdb)
- Responsabilidades:
    - Gerenciar a cota local de ingressos disponíveis por evento.
    - Processar ordens de reserva e emitir bilhetes.
    - Efetuar a validação do ingresso na portaria (check-in / validação de bilhete).
    - Detectar esgotamento de lote e disparar o alerta de lotação máxima.
- Mensageria:
    - Produtor: TicketPurchased (na exchange tickets.direct), TicketChecked e EventSoldOut (na exchange events.topic).
    - Consumidor: EventCreated (para criar o inventário local do evento) e PaymentConfirmed (para promover o bilhete de RESERVED para VALID).

### 3.3 Payments Service (payments-service)

- Porta: 8083
- Banco de Dados: SQLite (paymentsdb)
- Responsabilidades:
    - Receber intenções de compra e processar a liquidação financeira (simulação de cartão/Pix).
    - Garantir idempotência transacional por transação/ticket.
- Mensageria:
    - Consumidor: TicketPurchased (via fila dedicada ligada a tickets.direct).
    - Produtor: PaymentConfirmed (na exchange payments.fanout).

### 3.4 Notifications Service (notifications-service)

- Porta: 8084
- Banco de Dados: SQLite (notificationsdb)
- Responsabilidades:
    - Enviar alertas de confirmação de compra, ingressos gerados e alertas de evento esgotado.
    - Armazenar log de auditoria de mensagens enviadas aos clientes.
- Mensageria:
    - Consumidor: EventCreated, PaymentConfirmed, TicketChecked, EventSoldOut.
    - Produtor: Nenhum (nó terminal de consumo).

## 4. Fluxo Detalhado de Eventos

### 4.1 Cenário A: Criação de Evento

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Organizador / Admin
    participant Events as Events Service
    participant Rabbit as RabbitMQ (events.topic)
    participant Tickets as Tickets Service
    participant Notif as Notifications Service

    Admin->>Events: POST /api/events
    activate Events
    Events->>Events: Salva evento no banco (status: ACTIVE)
    Events->>Rabbit: Publica EventCreated (key: event.created)
    Events-->>Admin: 201 Created (detalhes do evento)
    deactivate Events

    par Inicialização de Estoque
        Rabbit->>Tickets: Entrega via tickets.event-create.queue
        activate Tickets
        Tickets->>Tickets: Cria registro de inventário de ingressos
        deactivate Tickets
    and Divulgação
        Rabbit->>Notif: Entrega via notifications.events.queue
        activate Notif
        Notif->>Notif: Registra notificação de novo evento
        deactivate Notif
    end
```

### 4.2 Cenário B: Compra e Confirmação de Pagamento

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as Usuário / Cliente
    participant Tickets as Tickets Service
    participant RabbitD as RabbitMQ (tickets.direct)
    participant Payments as Payments Service
    participant RabbitF as RabbitMQ (payments.fanout)
    participant Events as Events Service
    participant Notif as Notifications Service

    Cliente->>Tickets: POST /api/tickets/purchase
    activate Tickets
    Tickets->>Tickets: Valida cota e cria Ticket (status: RESERVED)
    Tickets->>RabbitD: Publica TicketPurchased (key: ticket.purchased)
    Tickets-->>Cliente: 202 Accepted (Ticket reservado)
    deactivate Tickets

    RabbitD->>Payments: Entrega via payment.process.queue
    activate Payments
    Payments->>Payments: Executa liquidação financeira (status: PAID)
    Payments->>RabbitF: Publica PaymentConfirmed (broadcast)
    deactivate Payments

    par Atualização de Ingresso
        RabbitF->>Tickets: Entrega via tickets.payment-confirmed.queue
        activate Tickets
        Tickets->>Tickets: Atualiza Ticket para status VALID
        deactivate Tickets
    and Métricas do Evento
        RabbitF->>Events: Entrega via events.payment-confirmed.queue
        activate Events
        Events->>Events: Incrementa total de ingressos confirmados
        deactivate Events
    and Notificação ao Cliente
        RabbitF->>Notif: Entrega via notif.payment-confirmed.queue
        activate Notif
        Notif->>Notif: Dispara comprovante e ingresso por e-mail
        deactivate Notif
    end
```

### 4.3 Cenário C: Evento Esgotado (Sold Ou

```mermaid
sequenceDiagram
    autonumber
    participant Tickets as Tickets Service
    participant Rabbit as RabbitMQ (events.topic)
    participant Events as Events Service
    participant Notif as Notifications Service

    Tickets->>Tickets: Ingressos confirmados/reservados atingem o total
    Tickets->>Rabbit: Publica EventSoldOut (key: event.soldout)

    par Atualização de Status
        Rabbit->>Events: Entrega via events.soldout.queue
        activate Events
        Events->>Events: Atualiza status do evento para SOLD_OUT
        deactivate Events
    and Notificação aos Organizadores
        Rabbit->>Notif: Entrega via notif.soldout.queue
        activate Notif
        Notif->>Notif: Emite alerta de esgotamento aos organizadores
        deactivate Notif
    end
```

### 4.4 Cenário D: Validação na Portaria (Check-in)

``` mermaid
sequenceDiagram
    autonumber
    actor Portaria as Operador de Portaria
    participant Tickets as Tickets Service
    participant Rabbit as RabbitMQ (events.topic)
    participant Events as Events Service
    participant Notif as Notifications Service

    Portaria->>Tickets: POST /api/tickets/{ticketId}/check-in
    activate Tickets
    Tickets->>Tickets: Valida status VALID e atualiza para CHECKED_IN
    Tickets->>Rabbit: Publica TicketChecked (key: ticket.checked)
    Tickets-->>Portaria: 200 OK (Entrada liberada)
    deactivate Tickets

    par Atualização de Ocupação em Tempo Real
        Rabbit->>Events: Entrega via events.checkin.queue
        activate Events
        Events->>Events: Incrementa contador de presença física
        deactivate Events
    and Auditoria e Boas-Vindas
        Rabbit->>Notif: Entrega via notif.checkin.queue
        activate Notif
        Notif->>Notif: Registra log de entrada e envia boas-vindas
        deactivate Notif
    end
```
## 5. Matriz de Roteamento AMQP

| Exchange | Tipo | Routing Key | Filas Vinculadas | Objetivo |
|---|---|---|---|---|
| `events.topic` | Topic | `event.created` | `tickets.event-create.queue`<br>`notifications.events.queue` | Notificar serviços para criação de estoque local e disparos de divulgação. |
| `events.topic` | Topic | `ticket.checked` | `events.checkin.queue`<br>`notif.checkin.queue` | Registrar entrada física no evento e auditoria de acesso. |
| `events.topic` | Topic | `event.soldout` | `events.soldout.queue`<br>`notif.soldout.queue` | Sinalizar encerramento de novas reservas para o evento. |
| `tickets.direct` | Direct | `ticket.purchased` | `payment.process.queue` | Enfileirar pedidos de pagamento ponto a ponto diretamente para o gateway. |
| `payments.fanout` | Fanout | *(broadcast)* | `tickets.payment-confirmed.queue`<br>`events.payment-confirmed.queue`<br>`notif.payment-confirmed.queue` | Propagar confirmação financeira em broadcast para liberação de ingresso, métricas e comprovantes. |