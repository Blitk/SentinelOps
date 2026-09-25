# 🛡️ SentinelOps

**Mini SIEM para coleta, processamento, detecção e gerenciamento de eventos de segurança.**

O **SentinelOps** é um projeto prático de Backend, Cybersecurity e Security Engineering desenvolvido para explorar, na prática, a construção de uma pipeline de monitoramento e detecção de eventos de segurança.

A aplicação coleta logs de infraestrutura, transforma dados brutos em eventos estruturados, persiste essas informações, executa regras de detecção e gera **Alerts** e **Incidents** a partir de comportamentos suspeitos.

Além da detecção, o projeto possui recursos para **consulta, filtragem, correlação, gerenciamento de status, investigação de incidentes, registro de notas e métricas operacionais**.

O projeto foi desenvolvido com foco em compreender como componentes de **Backend, processamento de logs, persistência, cache, detecção, correlação e gerenciamento de incidentes** podem trabalhar em conjunto.

---

## 📑 Índice

* [Arquitetura](#-arquitetura)
* [Fluxo de dados](#-fluxo-de-dados)
* [Detection Engine](#-detection-engine)
* [Detection Rules](#-detection-rules)
* [Alerts e Incidents](#-alerts-e-incidents)
* [Investigação de Incidents](#-investigação-de-incidents)
* [Dashboard e métricas](#-dashboard-e-métricas)
* [Armazenamento](#-armazenamento)
* [API](#-api)
* [Specifications](#-specifications)
* [Tratamento de exceções](#-tratamento-de-exceções)
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
  ├──────────────────────┐
  ▼                      ▼
PostgreSQL              Redis
  │                      │
  │                      ├── Alert Cooldown
  │                      └── Estado temporário
  │
  ▼
Security Events
  │
  ▼
Detection Engine
  │
  ├── Detection Rules
  │
  ▼
Detection Results
  │
  ▼
Alerts
  │
  ▼
Incidents
  │
  ├── Status
  ├── Alerts relacionados
  └── Investigation Notes
```

A aplicação Java utiliza separação de responsabilidades através de:

* Controllers
* Services
* Repositories
* Specifications
* DTOs
* Detection Engine
* Detection Rules
* Exception Handling
* JPA Entities

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

O evento é enviado através de HTTP/REST para a API Spring Boot:

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

Também são aplicadas validações dos campos e dos códigos HTTP.

### 5. Persistência

O evento é armazenado no PostgreSQL.

### 6. Detecção

Os eventos recentes são analisados pelo `DetectionEngine`.

### 7. Alert

Quando uma regra identifica um comportamento suspeito, um Alert pode ser criado.

### 8. Incident

O Alert pode ser associado a um Incident existente ou gerar um novo Incident.

### 9. Investigação

O Incident pode ser atualizado durante a investigação e receber notas contendo observações do analista.

```text
Security Event
      ↓
Detection
      ↓
Alert
      ↓
Incident
      ↓
Investigation
      ↓
Resolution
      ↓
Closure
```

---

## 🔎 Detection Engine

O **Detection Engine** é responsável por executar as regras de detecção disponíveis no sistema.

A arquitetura utiliza uma abstração comum:

```java
public interface DetectionRule {

    String getName();

    default String getDescription() {
        return "Security detection rule";
    }

    DetectionResult evaluate(List<SecurityEvent> events);
}
```

O `DetectionEngine` recebe uma coleção de eventos e executa as regras registradas pelo Spring:

```text
Security Events
      │
      ▼
DetectionEngine
      │
      ├── BruteForceRule
      ├── PathTraversalRule
      ├── SuspiciousStatusCodeRule
      ├── SuspiciousMethodRule
      └── HighErrorRateRule
      │
      ▼
DetectionResult
```

O `DetectionResult` contém informações como:

* Regra responsável pela detecção
* Severidade
* Descrição
* IP relacionado
* ID do evento que originou a detecção

O ID do evento permite que o Alert seja associado ao **evento que efetivamente originou a detecção**, e não simplesmente ao último evento recebido.

---

## 🧠 Detection Rules

Atualmente o projeto possui regras para diferentes comportamentos suspeitos.

### Brute Force

Detecta múltiplas tentativas de autenticação malsucedidas provenientes do mesmo endereço IP dentro de uma janela temporal.

```text
5 × HTTP 401
      ↓
mesmo IP
      ↓
janela de 5 minutos
      ↓
BRUTE_FORCE
```

### Suspicious Status Code

Detecta requisições HTTP com código:

```text
401 Unauthorized
```

### Path Traversal

Detecta padrões associados a tentativas de exploração de diretórios, incluindo:

```text
../
..\
```

e padrões codificados em URL.

### Suspicious Method

Detecta métodos HTTP considerados suspeitos no contexto do monitoramento:

```text
TRACE
CONNECT
```

### High Error Rate

Detecta uma quantidade elevada de erros de servidor provenientes do mesmo endereço IP dentro de uma janela temporal.

São considerados:

```text
500
502
503
```

---

## 🚨 Alerts e Incidents

O SentinelOps separa os conceitos de **Detection**, **Alert** e **Incident**.

### Detection Result

Representa o resultado de uma regra de detecção.

Exemplo:

```text
Rule:        BRUTE_FORCE
Severity:    HIGH
Source IP:   192.168.0.10
Description: Multiple failed authentication attempts
```

### Alert

Representa uma ocorrência de segurança gerada a partir de uma detecção.

Um Alert possui:

* ID
* Severidade
* Regra
* Descrição
* Status
* Evento de segurança relacionado
* Incident relacionado
* Data de criação

Os status disponíveis são:

```text
OPEN
ACKNOWLEDGED
RESOLVED
```

O status pode ser atualizado através da API.

```text
PATCH /api/v1/alerts/{id}/status
```

Exemplo:

```json
{
  "status": "ACKNOWLEDGED"
}
```

### Alert Cooldown

O `AlertService` utiliza Redis para evitar a criação excessiva de Alerts para a mesma regra e origem dentro de uma janela de tempo.

O cooldown padrão é:

```text
300 segundos
```

A chave é construída a partir da regra e do endereço IP:

```text
sentinelops:alert:BRUTE_FORCE:192.168.0.10
```

O Redis mantém essa chave temporariamente utilizando TTL.

---

## 🚨 Incident

Um Incident representa um agrupamento de Alerts relacionados.

Exemplo:

```text
Alert 1 ─┐
Alert 2 ─┼──► Incident
Alert 3 ─┘
```

O sistema utiliza a combinação da regra e da origem para identificar um Incident aberto relacionado.

Um Incident possui:

* ID
* Título
* Descrição
* Severidade
* Status
* Data de criação
* Data de atualização
* Quantidade de Alerts relacionados
* Notas de investigação

Os status disponíveis são:

```text
OPEN
INVESTIGATING
RESOLVED
CLOSED
```

O status pode ser atualizado através de:

```text
PATCH /api/v1/incidents/{id}/status
```

Exemplo:

```json
{
  "status": "INVESTIGATING"
}
```

---

## 🔬 Investigação de Incidents

O SentinelOps possui suporte a **Incident Notes**, permitindo registrar informações durante a investigação.

Cada nota possui:

* ID
* Autor
* Conteúdo
* Data de criação
* Incident relacionado

Exemplo:

```text
Incident #10

09:10 → Identificadas múltiplas requisições 401.
09:25 → IP colocado em investigação.
09:40 → Confirmado comportamento compatível com brute force.
```

### Criar nota

```text
POST /api/v1/incidents/{incidentId}/notes
```

Exemplo:

```json
{
  "author": "Raphael",
  "content": "Identificadas múltiplas tentativas de autenticação."
}
```

### Listar notas

```text
GET /api/v1/incidents/{incidentId}/notes
```

### Remover nota

```text
DELETE /api/v1/incidents/{incidentId}/notes/{noteId}
```

A API verifica se a nota realmente pertence ao Incident informado antes de realizar a remoção.

As notas possuem validação de:

```text
Author
├── obrigatório
└── máximo de 100 caracteres

Content
├── obrigatório
└── máximo de 2000 caracteres
```

---

## 📊 Dashboard e métricas

O SentinelOps possui endpoints específicos para fornecer métricas agregadas do sistema.

### Dashboard geral

```text
GET /api/v1/dashboard
```

Retorna informações como:

```text
Total de Events
Total de Alerts
Alerts abertos
Alerts críticos
Total de Incidents
Incidents abertos
Regras ativas
```

### Métricas temporais

```text
GET /api/v1/dashboard/metrics
```

Inclui:

```text
Eventos nas últimas 1 hora
Alerts nas últimas 24 horas
Incidents nas últimas 24 horas
```

### Alerts por regra

```text
GET /api/v1/dashboard/alerts-by-rule
```

Exemplo conceitual:

```json
[
  {
    "rule": "BRUTE_FORCE",
    "count": 12
  },
  {
    "rule": "PATH_TRAVERSAL",
    "count": 5
  }
]
```

### Alerts por severidade

```text
GET /api/v1/dashboard/alerts-by-severity
```

### Events por endereço IP

```text
GET /api/v1/dashboard/events-by-source-ip
```

A consulta retorna os endereços IP com maior quantidade de eventos.

### Alerts por endereço IP

```text
GET /api/v1/dashboard/alerts-by-source-ip
```

### Eventos por hora

```text
GET /api/v1/dashboard/events-by-hour
```

A consulta agrupa os eventos das últimas 24 horas por hora.

Esses endpoints permitem futuramente construir uma interface visual para monitoramento do SentinelOps.

---

## 📋 Detection Rules API

As regras disponíveis podem ser consultadas através de:

```text
GET /api/v1/rules
```

A resposta apresenta informações como:

```json
[
  {
    "name": "BRUTE_FORCE",
    "description": "Detects multiple failed authentication attempts from the same IP within a short time window."
  }
]
```

Isso permite que uma futura interface consiga descobrir dinamicamente quais regras estão disponíveis no sistema.

---

## 💾 Armazenamento

O projeto utiliza **PostgreSQL** e **Redis**, com responsabilidades diferentes.

### PostgreSQL

Responsável pela persistência dos dados.

Principais entidades:

```text
SecurityEvent
Alert
Incident
IncidentNote
```

O PostgreSQL mantém o histórico necessário para:

* Consultas
* Filtragem
* Detecção
* Correlação
* Investigação
* Métricas

O Hibernate/JPA utiliza:

```properties
spring.jpa.hibernate.ddl-auto=update
```

para atualizar automaticamente a estrutura das tabelas durante o desenvolvimento.

### Redis

Responsável por informações temporárias e operações rápidas.

Atualmente é utilizado principalmente pelo mecanismo de **Alert Cooldown**.

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
          evita Alert duplicado
```

---

## 🌐 API

A API REST é construída utilizando Spring Boot.

### Security Events

```text
POST /api/v1/events
GET  /api/v1/events
GET  /api/v1/events/{id}
```

Consultas podem utilizar filtros como:

```text
sourceip
method
statuscode
from
to
```

Também há suporte a paginação e ordenação:

```text
?page=0&size=20
```

```text
?sort=timestamp,desc
```

### Alerts

```text
GET   /api/v1/alerts
GET   /api/v1/alerts/{id}
PATCH /api/v1/alerts/{id}/status
```

Filtros:

```text
status
severity
rule
from
to
```

### Incidents

```text
GET   /api/v1/incidents
GET   /api/v1/incidents/{id}
PATCH /api/v1/incidents/{id}/status
```

Filtros:

```text
status
from
to
```

### Incident Notes

```text
POST   /api/v1/incidents/{incidentId}/notes
GET    /api/v1/incidents/{incidentId}/notes
DELETE /api/v1/incidents/{incidentId}/notes/{noteId}
```

### Detection Rules

```text
GET /api/v1/rules
```

### Dashboard

```text
GET /api/v1/dashboard
GET /api/v1/dashboard/metrics
GET /api/v1/dashboard/alerts-by-rule
GET /api/v1/dashboard/alerts-by-severity
GET /api/v1/dashboard/events-by-source-ip
GET /api/v1/dashboard/alerts-by-source-ip
GET /api/v1/dashboard/events-by-hour
```

---

## 📄 Paginação e Specifications

As consultas utilizam os recursos do Spring Data:

```text
Page<T>
Pageable
JpaSpecificationExecutor
Specification<T>
```

Isso permite combinar diferentes filtros sem criar um método de repository para cada combinação possível.

### SecurityEventSpecification

Filtros relacionados a eventos:

```text
sourceip
method
statuscode
timestamp from
timestamp to
```

### AlertSpecification

Filtros relacionados a Alerts:

```text
status
severity
rule
createdAt from
createdAt to
```

### IncidentSpecification

Filtros relacionados a Incidents:

```text
status
createdAt from
createdAt to
```

---

## ⚠️ Tratamento de exceções

A API possui tratamento global de exceções através de `@RestControllerAdvice`.

Exceções específicas incluem:

```text
InvalidSecurityEventException
ResourceNotFoundException
```

Exemplo de resposta:

```json
{
  "timestamp": "2026-09-21T12:00:00Z",
  "status": 404,
  "error": "Resource not found",
  "message": "Alert not found with id: 999"
}
```

Isso mantém as respostas da API padronizadas e evita expor detalhes internos da aplicação diretamente ao cliente.

---

## 🧪 Testes

O projeto utiliza:

* JUnit 5
* Mockito

A cobertura de testes está sendo ampliada conforme novas funcionalidades são implementadas.

Entre os componentes que já possuem cobertura estão:

### AlertService

Incluindo cenários relacionados a:

* Criação de Alerts
* Cooldown
* Busca por ID
* Paginação
* Filtros
* Validação de períodos
* Integração com IncidentService

### IncidentService

Incluindo cenários relacionados a:

* Criação de Incidents
* Reutilização de Incidents abertos
* Associação de Alerts
* Separação por origem
* Busca por ID
* Paginação
* Filtros
* Validação de períodos

O projeto continua recebendo testes adicionais conforme novas funcionalidades são desenvolvidas.

---

## 📁 Estrutura do projeto

```text
SentinelOps/
│
├── Scripts/
│   ├── start-python.sh
│   ├── start-spring.sh
│   ├── start-all.sh
│   ├── stop-all.sh
│   ├── start-python.bat
│   ├── start-spring.bat
│   ├── start-all.bat
│   └── stop-all.bat
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
│   ├── specification/
│   └── tests/
│
├── docs/
└── README.md
```

---

## 🛠️ Tecnologias

| Tecnologia          | Função                         |
| :------------------ | :----------------------------- |
| **Java**            | Backend                        |
| **Spring Boot**     | API REST                       |
| **Spring Data JPA** | Persistência e consultas       |
| **JUnit 5**         | Testes                         |
| **Mockito**         | Mocking                        |
| **Python**          | Coleta, parsing e normalização |
| **PostgreSQL**      | Persistência                   |
| **Redis**           | Cooldown e estado temporário   |
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
* [x] Suspicious Status Code Detection
* [x] Path Traversal Detection
* [x] Suspicious Method Detection
* [x] High Error Rate Detection
* [x] Alert Management
* [x] Alert Cooldown
* [x] Correlation / Incidents
* [x] Agrupamento de Alerts em Incidents
* [x] Consulta das Detection Rules

### Incident Management

* [x] Status de Alerts
* [x] Status de Incidents
* [x] Investigation Notes
* [x] Criar notas
* [x] Consultar notas
* [x] Remover notas
* [x] Associar notas aos Incidents
* [x] Exibir notas no Incident Response

### Dashboard

* [x] Dashboard geral
* [x] Métricas temporais
* [x] Alerts por regra
* [x] Alerts por severidade
* [x] Events por Source IP
* [x] Alerts por Source IP
* [x] Eventos por hora
* [ ] Interface Web do Dashboard
* [ ] Gráficos interativos

### Persistência e infraestrutura

* [x] PostgreSQL
* [x] Redis
* [x] Estado temporário
* [x] Alert Cooldown
* [ ] Rate limiting
* [ ] Janelas temporais avançadas
* [ ] Métricas de performance
* [ ] Health checks da infraestrutura
* [ ] Processo automatizado de setup para PostgreSQL e Redis

### Qualidade

* [x] Testes do AlertService
* [x] Testes do IncidentService
* [x] Testes do SecurityEventService
* [x] Testes das Detection Rules
* [ ] Ampliar cobertura geral
* [ ] Testes de integração
* [ ] Testes dos novos recursos de Incident Notes

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
Detection Result
  ↓
Alert
  ↓
Incident
  ↓
Investigation
  ↓
Resolution
```

O backend atualmente possui recursos de:

```text
✓ Ingestão de eventos
✓ Persistência
✓ Detecção
✓ Alertas
✓ Cooldown com Redis
✓ Correlação em Incidents
✓ Gerenciamento de status
✓ Notas de investigação
✓ Filtros
✓ Paginação
✓ Specifications
✓ Dashboard de métricas
✓ Consulta das regras
✓ Tratamento global de exceções
```

O desenvolvimento continua focado no fortalecimento do backend e na evolução das funcionalidades de monitoramento e investigação.

---

## 🎯 Objetivos

O SentinelOps foi criado como um laboratório prático para estudar a integração entre **Backend Development, infraestrutura e Cybersecurity**.

### Principais conceitos explorados

* SIEM
* Log Monitoring
* Security Event Processing
* Threat Detection
* Event Correlation
* Incident Management
* Incident Investigation
* REST APIs
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Redis
* TTL
* Cooldown
* Pagination
* Specifications
* DTOs
* Exception Handling
* Unit Testing
* Separation of Concerns
* Backend Development
* Security Engineering

O objetivo não é apenas construir um CRUD, mas compreender como uma pipeline de segurança pode:

```text
Coletar
   ↓
Processar
   ↓
Persistir
   ↓
Detectar
   ↓
Alertar
   ↓
Correlacionar
   ↓
Investigar
   ↓
Resolver
   ↓
Encerrar
```

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
