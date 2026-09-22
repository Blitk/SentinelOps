# 🛡️ SentinelOps

**Mini SIEM para coleta, processamento e detecção de eventos de segurança.**

O **SentinelOps** é um projeto prático de Backend, Cybersecurity e Security Engineering desenvolvido para explorar a construção de uma pipeline de monitoramento e detecção de eventos de segurança.

A aplicação coleta logs de infraestrutura, transforma dados brutos em eventos estruturados, persiste essas informações, executa regras de detecção e gera **Alerts** e **Incidents** a partir de comportamentos suspeitos.

O projeto foi desenvolvido com foco em compreender, na prática, como componentes de **Backend, processamento de logs, persistência, cache, detecção e correlação de eventos** podem trabalhar em conjunto.

---

## 📑 Índice

* [Arquitetura](#-arquitetura)
* [Fluxo de dados](#-fluxo-de-dados)
* [Detection Engine](#-detection-engine)
* [Alerts e Incidents](#-alerts-e-incidents)
* [Armazenamento](#-armazenamento)
* [API](#-api)
* [Testes](#-testes)
* [Estrutura do projeto](#-estrutura-do-projeto)
* [Tecnologias](#%EF%B8%8F-tecnologias)
* [Roadmap](#%EF%B8%8F-roadmap)
* [Status](#-status)
* [Objetivos](#-objetivos)
* [Autor](#-autor)
* [Aviso](#%EF%B8%8F-aviso)

---

## 🏗️ Arquitetura

O SentinelOps é dividido em componentes responsáveis por diferentes etapas do processamento:

```text
Apache
  │
  │ Access Logs
  ▼
Python Agent
  │
  │ HTTP / REST
  ▼
Spring Boot API
  │
  ├───────────────┐
  ▼               ▼
PostgreSQL       Redis
  │               │
  │               ├── Cooldown
  │               ├── Estado temporário
  │               └── Operações rápidas
  │
  ▼
Detection Engine
  │
  ├── Detection Rules
  │
  ▼
Detection Result
  │
  ▼
Alert
  │
  ▼
Incident
```

A aplicação Java utiliza uma arquitetura baseada em separação de responsabilidades, com componentes específicos para:

* Controllers
* Services
* Repositories
* Specifications
* DTOs
* Detection Engine
* Detection Rules
* Exception Handling
* Models

---

## 🔄 Fluxo de dados

O SentinelOps transforma um log HTTP bruto em um evento estruturado que pode ser armazenado, analisado e correlacionado.

### 1. Coleta

O Apache gera um log de acesso:

```text
192.168.1.50 - - [02/Sep/2026:10:15:32 -0300] "GET /login HTTP/1.1" 401 512
```

### 2. Processamento pelo Python Agent

O Python Agent monitora os logs e transforma as informações em um evento estruturado:

```json
{
  "timestamp": "2026-09-02T10:15:32-03:00",
  "sourceip": "192.168.1.50",
  "method": "GET",
  "path": "/login",
  "statuscode": 401,
  "source": "APACHE"
}
```

### 3. Ingestão

O evento é enviado através de HTTP/REST para a API Spring Boot.

Endpoint principal:

```text
POST /api/v1/events
```

### 4. Validação e normalização

A API valida os dados recebidos e normaliza informações como:

```text
method → GET
source → APACHE
IP     → sem espaços
```

Também são aplicadas validações de campos obrigatórios e códigos HTTP.

### 5. Persistência

O evento é armazenado no PostgreSQL.

### 6. Detecção

Os eventos recentes são analisados pelo Detection Engine.

### 7. Alert

Quando uma regra identifica um comportamento suspeito, o sistema pode gerar um Alert.

### 8. Incident

Alerts relacionados podem ser associados a um Incident, permitindo agrupar ocorrências relacionadas.

---

## 🔎 Detection Engine

O **Detection Engine** é responsável por analisar eventos de segurança e executar as regras de detecção.

As regras utilizam uma abstração comum:

```java
public interface DetectionRule {
    DetectionResult evaluate(SecurityEvent event);
}
```

O resultado da análise contém informações como:

```text
Detection
├── regra
├── severidade
├── descrição
└── origem/IP relacionado
```

### Fluxo

```text
SecurityEvent
      │
      ▼
DetectionEngine
      │
      ├── DetectionRule
      ├── DetectionRule
      ├── DetectionRule
      └── DetectionRule
      │
      ▼
DetectionResult
      │
      ▼
AlertService
```

Essa abordagem permite adicionar novas regras sem concentrar toda a lógica de detecção em um único bloco condicional.

---

## 🚨 Alerts e Incidents

O SentinelOps separa **detecção**, **alerta** e **incidente**.

### Detection Result

Representa o resultado de uma regra de detecção.

Exemplo:

```text
Rule:        BRUTE_FORCE
Severity:    HIGH
Source IP:   192.168.0.10
Description: Multiple failed login attempts
```

### Alert

Representa uma ocorrência de segurança gerada a partir de uma detecção.

Um Alert possui:

* Severidade
* Regra
* Descrição
* Status
* Evento de segurança relacionado
* Incident relacionado
* Data de criação

Exemplo:

```text
Detection Result
      ↓
Alert
      ↓
BRUTE_FORCE
HIGH
OPEN
```

### Cooldown

O `AlertService` utiliza Redis para evitar a criação excessiva de alerts para a mesma regra e origem dentro de uma janela de tempo.

Atualmente o cooldown padrão utilizado pelo serviço é de:

```text
300 segundos
```

A chave é construída a partir da regra e do endereço IP:

```text
sentinelops:alert:BRUTE_FORCE:192.168.0.10
```

### Incident

Um Incident representa um agrupamento de Alerts relacionados.

O sistema pode reutilizar um Incident aberto quando a regra e a origem forem compatíveis.

Exemplo:

```text
Alert 1 ─┐
Alert 2 ─┼──► Incident
Alert 3 ─┘
```

Isso permite evoluir de uma simples coleção de alerts para uma estrutura mais próxima de um processo de investigação de segurança.

---

## 💾 Armazenamento

O projeto utiliza **PostgreSQL** e **Redis**, cada um com responsabilidades diferentes.

### PostgreSQL

Responsável pela persistência dos dados.

Principais entidades:

```text
SecurityEvent
Alert
Incident
```

O PostgreSQL mantém o histórico necessário para consultas, análise e investigação.

### Redis

Responsável principalmente por informações temporárias e operações rápidas.

No fluxo atual, é utilizado para o **cooldown de Alerts**.

Exemplo:

```text
sentinelops:alert:BRUTE_FORCE:192.168.0.10
                    │
                    ▼
                 Redis
                    │
                  TTL
                    │
                    ▼
         evita alertas duplicados
```

O Redis também permite evoluir futuramente o projeto para funcionalidades como:

* Contadores
* Janelas temporais
* Rate limiting
* Estado temporário de detecções
* Cache

---

## 🌐 API

A API REST é construída utilizando Spring Boot.

### Security Events

```text
POST /api/v1/events
GET  /api/v1/events
GET  /api/v1/events/{id}
```

A consulta de eventos suporta filtros como:

```text
sourceip
method
statuscode
from
to
```

Também suporta paginação:

```text
?page=0&size=20
```

E ordenação:

```text
?sort=timestamp,desc
```

### Alerts

```text
GET /api/alerts
GET /api/alerts/{id}
```

Filtros disponíveis:

```text
status
severity
rule
from
to
```

Também suporta paginação e ordenação.

### Incidents

```text
GET /api/incidents
GET /api/incidents/{id}
```

Filtros disponíveis:

```text
status
from
to
```

Também suporta paginação e ordenação.

### Paginação

As consultas utilizam o mecanismo de paginação do Spring Data:

```text
Page<T>
Pageable
Specification<T>
```

O tamanho padrão das consultas é de:

```text
20 registros
```

com limite máximo configurado para:

```text
100 registros
```

---

## 🧩 Specifications

As consultas com filtros utilizam `JpaSpecificationExecutor` e Specifications específicas.

Exemplo:

```text
SecurityEventSpecification
├── sourceip
├── method
├── statuscode
├── timestamp from
└── timestamp to
```

```text
AlertSpecification
├── status
├── severity
├── rule
├── createdAt from
└── createdAt to
```

```text
IncidentSpecification
├── status
├── createdAt from
└── createdAt to
```

Isso permite combinar filtros sem transformar os repositories em uma coleção de métodos específicos para cada combinação possível.

---

## ⚠️ Tratamento de exceções

A API possui um tratamento global de exceções através de `@RestControllerAdvice`.

Exceções específicas incluem:

```text
InvalidSecurityEventException
ResourceNotFoundException
```

As respostas seguem uma estrutura padronizada:

```json
{
  "timestamp": "2026-09-21T12:00:00Z",
  "status": 404,
  "error": "Resource not found",
  "message": "Alert not found with id: 999"
}
```

Isso evita que detalhes internos da aplicação sejam expostos diretamente ao cliente.

---

## 🧪 Testes

O projeto possui testes unitários utilizando **JUnit 5** e **Mockito**.

Atualmente existem testes cobrindo componentes importantes da camada de serviços.

### AlertService

Os testes cobrem:

* Criação de Alert
* Cooldown ativo
* Cooldown disponível
* Integração com IncidentService
* Busca por ID
* Alert inexistente
* Paginação
* Filtro por Severity
* Filtro por Rule
* Validação de período

### IncidentService

Os testes cobrem:

* Criação de Incident
* Reutilização de Incident aberto
* Associação de múltiplos Alerts
* Separação por origem/IP
* Busca por ID
* Incident inexistente
* Paginação
* Filtro por Status
* Validação de período

O objetivo é ampliar gradualmente a cobertura conforme novas funcionalidades são implementadas.

---

## 📁 Estrutura do projeto

```text
SentinelOps/
│
├── SentinelOps_Python/
│   ├── ApacheLogLoader.py
│   ├── ApacheLogConverter.py
│   ├── ApacheLogSender.py
│   ├── SentinelOps.py
│   ├── tests/
│   └── testsTools/
│
├── SentinelOps_Java/
│   ├── config/
│   ├── controller/
│   ├── detection/
│   │   └── rules/
│   ├── dto/
│   ├── exception/
│   ├── model/
│   ├── repository/
│   ├── service/
│   └── specification/
│
├── docs/
└── README.md
```

A estrutura continua evoluindo conforme novos componentes são adicionados ao projeto.

---

## 🛠️ Tecnologias

| Tecnologia          | Função                         |
| :------------------ | :----------------------------- |
| **Java**            | Backend                        |
| **Spring Boot**     | API REST                       |
| **Spring Data JPA** | Persistência e consultas       |
| **JUnit 5**         | Testes                         |
| **Mockito**         | Mocking e testes unitários     |
| **Python**          | Coleta, parsing e normalização |
| **PostgreSQL**      | Persistência                   |
| **Redis**           | Estado temporário e cooldown   |
| **Apache**          | Fonte de logs                  |
| **Linux**           | Ambiente de execução           |
| **Git**             | Controle de versão             |

---

## 🗺️ Roadmap

### Core

* [x] Criar API Spring Boot
* [x] Definir modelo de Security Event
* [x] Implementar persistência PostgreSQL
* [x] Implementar Python Agent
* [x] Criar parser de logs Apache
* [x] Implementar comunicação Python → API
* [x] Validação e normalização dos eventos
* [x] DTOs para entrada e saída
* [x] Tratamento global de exceções
* [x] Paginação
* [x] Filtros de consulta
* [x] Ordenação de resultados

### Detection

* [x] Criar `DetectionRule`
* [x] Implementar `DetectionEngine`
* [x] Sistema de severidade
* [x] Brute Force Detection
* [x] Suspicious Login Detection
* [x] Request Rate Detection
* [x] Suspicious User-Agent Detection
* [x] Alert Management
* [x] Cooldown de Alerts
* [x] Correlation / Incidents
* [x] Agrupamento de Alerts em Incidents

### Persistência e infraestrutura

* [x] Implementar PostgreSQL
* [x] Implementar Redis
* [x] Implementar estado temporário
* [x] Implementar cooldown de Alerts
* [x] Rate limiting
* [x] Janelas temporais avançadas
* [ ] Métricas de performance

### Qualidade

* [x] Testes unitários do AlertService
* [x] Testes unitários do IncidentService
* [x] Testes do SecurityEventService
* [x] Testes dos Detection Rules
* [x] Testes dos Controllers
* [ ] Aumentar cobertura geral

### Interface

* [ ] Dashboard
* [ ] Visualização de Security Events
* [ ] Visualização de Alerts
* [ ] Investigação de Incidents
* [ ] Métricas do sistema
* [x] Filtros avançados
* [x] Visualização temporal de eventos

---

## 🚧 Status

🟡 **Em desenvolvimento ativo**

A pipeline principal já está estruturada:

```text
Apache
  ↓
Python Agent
  ↓
HTTP / REST
  ↓
Spring Boot
  ↓
Security Event
  ↓
PostgreSQL
  ↓
Detection Engine
  ↓
Alert
  ↓
Incident
```

O projeto atualmente está concentrado no fortalecimento do backend, regras de detecção, correlação de eventos, tratamento de exceções e testes automatizados.

---

## 🎯 Objetivos

O SentinelOps foi criado como um laboratório prático para estudar a integração entre **Backend Development, infraestrutura e Cybersecurity**.

### Principais conceitos explorados

* SIEM
* Log Monitoring
* Security Event Processing
* Threat Detection
* Event Correlation
* REST APIs
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Redis
* TTL
* Cooldown
* Rate Limiting
* Pagination
* Specifications
* DTOs
* Exception Handling
* Unit Testing
* Separation of Concerns
* Backend Development
* Security Engineering

O objetivo não é apenas construir um CRUD, mas entender como uma pipeline de segurança pode coletar, processar, persistir, detectar e correlacionar eventos.

---

## 👨‍💻 Autor

**Raphael Rodrigues Oliveira**

* GitHub: [Blitk](https://github.com/Blitk)
* LinkedIn: [Raphael Rodrigues Oliveira](https://www.linkedin.com/in/raphael-rodrigues-oliveira-b5675a174)
* Site: [Do I.T](https://doitsolucoes.github.io)

### Áreas de estudo

* Java
* Python
* Spring Boot
* Linux
* Backend Development
* Cybersecurity
* Security Engineering

---

## ⚠️ Aviso

O SentinelOps possui finalidade **educacional e de laboratório**.

As funcionalidades de coleta, monitoramento e detecção devem ser utilizadas somente em ambientes próprios ou onde exista autorização para monitoramento e testes de segurança.

---

⭐ **Se o projeto for útil para você, considere deixar uma estrela no repositório.**
