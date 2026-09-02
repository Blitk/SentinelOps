# 🛡️ SentinelOps

Mini SIEM para coleta, análise e detecção de eventos de segurança.

O **SentinelOps** é um projeto de estudos em Backend, Cybersecurity e Security Engineering, desenvolvido para explorar, na prática, conceitos de monitoramento de logs e detecção de comportamentos suspeitos.

A aplicação coleta logs de infraestrutura, transforma essas informações em eventos estruturados, armazena o histórico e utiliza regras de detecção para identificar possíveis eventos de segurança.

---

## 📑 Índice

- [Fluxo de dados](#-fluxo de-dados)
- [Detection Engine](#-detection-engine)
- [Armazenamento](#-armazenamento)
- [Detecções](#-detecções)
- [Estrutura do projeto](#-estrutura-do-projeto)
- [Tecnologias](#%EF%B8%8F-tecnologias)
- [Roadmap](#%EF%B8%8F-roadmap)
- [Status](#-status)
- [Objetivos](#-objetivos)
- [Autor](#-autor)
- [Aviso](#%EF%B8%8F-aviso)

---

## 🔄 Fluxo de dados

O SentinelOps transforma um log bruto em um evento estruturado que pode ser armazenado e analisado.

### 1. Coleta
O Apache gera um log de acesso:
```text
192.168.1.50 - - [02/Sep/2026:10:15:32 -0300] "GET /login HTTP/1.1" 401 512
```

### 2. Normalização
O Python Agent monitora o arquivo e transforma a entrada em um evento estruturado:
```json
{
  "timestamp": "2026-09-02T10:15:32-03:00",
  "sourceIp": "192.168.1.50",
  "method": "GET",
  "path": "/login",
  "statusCode": 401,
  "source": "APACHE"
}
```

### 3. Ingestão
O evento é enviado através de HTTP/REST para a API Spring Boot.

### 4. Processamento
A API valida o evento e o encaminha para persistência e análise.

### 5. Detecção
O Detection Engine executa as regras configuradas.

### 6. Resultado
Uma detecção pode gerar um Alert e, posteriormente, contribuir para a criação de um Incident.

---

## 🔎 Detection Engine

O Detection Engine é responsável por analisar os eventos recebidos.

As regras são separadas através de uma abstração comum:

```java
public interface DetectionRule {
    DetectionResult evaluate(SecurityEvent event);
}
```

### Exemplos de regras:
* `BruteForceRule`
* `SuspiciousLoginRule`
* `PortScanRule`
* `HighRequestRateRule`
* `SuspiciousUserAgentRule`

### Fluxo:
```text
Security Event
      │
      ▼
Detection Engine
      │
      ├── BruteForceRule
      ├── SuspiciousLoginRule
      ├── PortScanRule
      ├── HighRequestRateRule
      └── SuspiciousUserAgentRule
      │
      ▼
Detection Result
      │
      ▼
Alert
```

Essa abordagem permite adicionar novas regras sem concentrar toda a lógica em um único bloco condicional.

---

## 💾 Armazenamento

O projeto utiliza dois mecanismos principais de armazenamento, cada um com uma responsabilidade específica.

### PostgreSQL
Responsável pelos dados persistentes e pelo histórico.

**Possíveis entidades:**
* `SecurityEvent`
* `Alert`
* `Incident`
* `DetectionRule`

O banco permite manter o histórico necessário para consultas e investigações.

### Redis
Responsável pelo estado temporário e por operações que precisam de acesso rápido.

**Exemplo:**
```text
failed-login:185.10.20.30 = 5
```

Com TTL, o sistema pode trabalhar com janelas temporais:
```text
5 falhas
   ↓
dentro de 60 segundos
   ↓
BRUTE FORCE
   ↓
HIGH
```

**Possíveis utilizações:**
* Contadores
* TTL
* Rate limiting
* Janelas temporais
* Estado temporário
* Cooldown de alertas

---

## 🚨 Detecções

O sistema será projetado para identificar padrões de comportamento, como:

### Brute Force
```text
IP
 │
 ├── 401
 ├── 401
 ├── 401
 ├── 401
 └── 401
        ↓
BRUTE FORCE
```

### Alta taxa de requisições
```text
IP
 │
 ├── Request
 ├── Request
 ├── Request
 ├── Request
 ├── Request
 └── ...
        ↓
HIGH REQUEST RATE
```

### User-Agent suspeito
* sqlmap
* nikto
* masscan
* nmap

Os resultados poderão gerar alertas classificados por severidade:
* `LOW`
* `MEDIUM`
* `HIGH`
* `CRITICAL`

Alertas relacionados poderão posteriormente ser correlacionados em Incidents.

---

## 📁 Estrutura do projeto

```text
SentinelOps/
│
├── SentinelOps_Python/
│   ├── ApacheLogLoader.py
│   ├── ApacheLogConverter.py
│   ├── ApacheLogSender.py
│   └── tests/
│
├── SentinelOps_Java/
│   ├── controller/
│   ├── service/
│   ├── domain/
│   ├── repository/
│   ├── detection/
│   └── configuration/
│
├── docs/
└── README.md
```

A estrutura poderá evoluir conforme novos módulos forem implementados.

---

## 🛠️ Tecnologias

| Tecnologia | Função |
| :--- | :--- |
| **Java** | Backend |
| **Spring Boot** | API REST |
| **Python** | Coleta e normalização |
| **PostgreSQL** | Persistência |
| **Redis** | Estado temporário |
| **Apache** | Fonte de logs |
| **Linux** | Ambiente de execução |
| **Git** | Controle de versão |

---

## 🗺️ Roadmap

### Core
- [ ] Criar API Spring Boot
- [ ] Definir modelo de Security Event
- [ ] Implementar persistência PostgreSQL
- [x] Implementar Python Agent
- [ ] Criar parser de logs Apache
- [ ] Implementar comunicação Python → API

### Detection
- [ ] Criar DetectionRule
- [ ] Implementar DetectionEngine
- [ ] Brute Force Detection
- [ ] Suspicious Login Detection
- [ ] Request Rate Detection
- [ ] Suspicious User-Agent Detection
- [ ] Sistema de severidade
- [ ] Alert Management
- [ ] Correlation / Incidents

### Infraestrutura
- [ ] Implementar Redis
- [ ] Configurar persistência e cache
- [ ] Criar mecanismos de rate limiting
- [ ] Implementar gerenciamento de estado das detecções

### Interface
- [ ] Dashboard
- [ ] Visualização de eventos
- [ ] Visualização de alertas
- [ ] Investigação de incidents
- [ ] Métricas do sistema

---

## 🚧 Status

🟡 **Em desenvolvimento**

A implementação está sendo construída de forma incremental, começando pela pipeline principal:

```text
Apache
  ↓
Python Agent
  ↓
Spring Boot
  ↓
PostgreSQL + Redis
  ↓
Detection Engine
  ↓
Alerts
```

---

## 🎯 Objetivos

O SentinelOps foi criado como um laboratório prático para explorar a integração entre Backend, infraestrutura e Cybersecurity.

### Principais conceitos envolvidos:
* SIEM
* Log Monitoring
* Threat Detection
* Event Correlation
* REST APIs
* Separation of Concerns
* Design Patterns
* Caching
* TTL
* Rate Limiting
* Backend Development
* Security Engineering

Mais do que construir um CRUD, o projeto busca compreender como coleta, processamento, armazenamento e detecção podem ser integrados em uma única solução.

---

## 👨‍💻 Autor

**Raphael Rodrigues Oliveira**

* GitHub: [Blitk](https://github.com)
* LinkedIn: [Raphael Rodrigues Oliveira](https://www.linkedin.com/in/raphael-rodrigues-oliveira-b5675a174)

### Áreas de estudo:
* Java
* Python
* Spring Boot
* Linux
* Backend
* Cybersecurity
* Security Engineering

---

## ⚠️ Aviso

O SentinelOps possui finalidade educacional e de laboratório.

As funcionalidades de coleta, monitoramento e detecção devem ser utilizadas somente em ambientes próprios ou onde exista autorização para monitoramento e testes de segurança.

---

⭐ *Se o projeto for útil para você, considere deixar uma estrela no repositório.*
