# High Level Design
## Fresh-Fruit - Same-Day Fresh Fruit Delivery Platform

| Revision | Date | Author | Details |
|----------|------|--------|---------|
| 1.0 | 2026-03-02 | [Tu Nombre] | Initial HLD Document |

---

## Abstract

Fresh-Fruit is a cloud-based e-commerce platform that enables customers to order same-day delivery of fresh produce. The system consists of a customer-facing web application, a courier mobile application, an AI-powered recommendation engine, and integrated LLM-based customer support. The MVP targets Berlin, Germany, with planned expansion to Paris and San Francisco within one year, scaling to support 100M users across 200 cities within three years.

---

## About this document

The architecture of a system describes its most important components, their relations, and how they interact with each other. The purpose of this document is to provide a clear and concise understanding of the system's design before more detailed design and implementation work begins.

---

# 1. General

## 1.1 Introduction

Fresh-Fruit is a startup creating a web-based platform for same-day fresh fruit delivery. The system aims to:

- Enable customers to browse, order, and receive fresh produce on the same day
- Provide personalized recommendations based on user behavior and seasonal availability
- Offer seamless delivery tracking and courier management
- Ensure GDPR compliance and data security for European operations
- Scale from 1 city to 200 cities globally within 3 years

**System Goals:**
- Deliver MVP in 6 months with 15 developers
- Achieve 99.99% uptime
- Support 1M users within year 1
- Minimize operational costs while maintaining scalability

## 1.2 Glossary

| Term | Definition |
|------|------------|
| MVP | Minimum Viable Product - initial release with core features |
| HLD | High Level Design |
| LLM | Large Language Model - AI for customer support |
| GDPR | General Data Protection Regulation - EU data privacy law |
| CDN | Content Delivery Network |
| API | Application Programming Interface |
| SPA | Single Page Application |
| POD | Proof of Delivery |
| SKU | Stock Keeping Unit - unique product identifier |
| i18n | Internationalization |
| CQRS | Command Query Responsibility Segregation |

---

# 2. Requirements

## 2.1 Functional Requirements

### User Management
| ID | Requirement |
|----|-------------|
| FN-010 | The system shall allow customers to register with Name, Address, Phone, and Email |
| FN-011 | The system shall support user authentication via email/password and social login |
| FN-012 | The system shall allow users to manage multiple delivery addresses |
| FN-013 | The system shall store and display customer's previous orders |
| FN-014 | The system shall manage and apply coupons for customer orders |
| FN-015 | The system shall support account deletion for GDPR compliance |

### Internationalization
| ID | Requirement |
|----|-------------|
| FN-020 | The system shall provide user interface in German and English at launch |
| FN-021 | The system shall support French interface within one year |
| FN-022 | The system shall detect user's preferred language automatically |

### Store & Catalog
| ID | Requirement |
|----|-------------|
| FN-030 | The system shall display current fresh produce inventory |
| FN-031 | The system shall update product availability daily at 5:00 AM local time |
| FN-032 | The system shall support 1,000 product types (SKUs) |
| FN-033 | The system shall integrate with 10,000 vendors |
| FN-034 | The system shall show product images, descriptions, prices, and availability |
| FN-035 | The system shall support product search and filtering |

### Shopping Cart & Checkout
| ID | Requirement |
|----|-------------|
| FN-040 | The system shall allow users to add/remove items from cart |
| FN-041 | The system shall persist cart across sessions for authenticated users |
| FN-042 | The system shall calculate order totals including taxes and delivery fees |
| FN-043 | The system shall integrate with payment providers (credit card, PayPal) |
| FN-044 | The system shall validate delivery address and time slot availability |
| FN-045 | The system shall send order confirmation via email |

### Customer Support
| ID | Requirement |
|----|-------------|
| FN-050 | The system shall provide LLM-powered chat support |
| FN-051 | The system shall escalate to human agents when LLM cannot resolve issues |
| FN-052 | The system shall maintain chat history for customer reference |

### Delivery Tracking
| ID | Requirement |
|----|-------------|
| FN-060 | The system shall track order status: Received, Processing, Out for Delivery, Delivered |
| FN-061 | The system shall provide real-time delivery ETA |
| FN-062 | The system shall send push/email notifications on status changes |

### Courier Application
| ID | Requirement |
|----|-------------|
| FN-070 | The system shall provide mobile app for couriers |
| FN-071 | The system shall display optimized delivery routes |
| FN-072 | The system shall capture proof of delivery (photo and/or signature) |
| FN-073 | The system shall allow couriers to update delivery status |

### Smart Recommendations
| ID | Requirement |
|----|-------------|
| FN-080 | The system shall analyze user purchase history and browsing behavior |
| FN-081 | The system shall send daily personalized email recommendations |
| FN-082 | The system shall prioritize seasonal and available products |

## 2.2 Non-Functional Requirements

### 2.2.1 Availability and Recovery

| ID | Requirement |
|----|-------------|
| NF-010 | The system shall maintain 99.99% uptime (52.6 minutes downtime/year max) |
| NF-011 | The system shall implement automated failover for critical services |
| NF-012 | The system shall perform daily automated backups with 30-day retention |
| NF-013 | Recovery Point Objective (RPO): 1 hour |
| NF-014 | Recovery Time Objective (RTO): 15 minutes for critical services |

### 2.2.2 Performance and Capacity

| ID | Requirement |
|----|-------------|
| NF-020 | The system shall support 1M registered users within year 1 |
| NF-021 | The system shall handle 10,000 concurrent users at peak |
| NF-022 | Page load time shall be < 2 seconds (95th percentile) |
| NF-023 | API response time shall be < 200ms (95th percentile) |
| NF-024 | The system shall scale to 100M users within 3 years |

### 2.2.3 Security

| ID | Requirement |
|----|-------------|
| NF-030 | The system shall implement HTTPS for all communications |
| NF-031 | The system shall comply with GDPR regulations |
| NF-032 | User data shall be stored in EU data centers for EU users |
| NF-033 | The system shall support complete user data deletion upon request |
| NF-034 | The system shall implement OAuth 2.0 for authentication |
| NF-035 | Payment data shall be PCI-DSS compliant (via payment provider) |
| NF-036 | The system shall implement rate limiting to prevent abuse |

### 2.2.4 Backward Compatibility

| ID | Requirement |
|----|-------------|
| NF-040 | API versioning shall maintain backward compatibility for 2 major versions |
| NF-041 | Database migrations shall be backward compatible |

### 2.2.5 Upgradability

| ID | Requirement |
|----|-------------|
| NF-050 | The system shall support rolling deployments with zero downtime |
| NF-051 | The system shall support blue-green deployments |
| NF-052 | The system shall support feature flags for gradual rollouts |

### 2.2.6 Monitoring and Debugging

| ID | Requirement |
|----|-------------|
| NF-060 | The system shall provide real-time technical metrics (CPU, memory, latency) |
| NF-061 | The system shall provide business metrics (orders, revenue, conversion) |
| NF-062 | The system shall implement distributed tracing |
| NF-063 | The system shall centralize logs with 30-day retention |
| NF-064 | The system shall provide alerting for anomalies |

### 2.2.7 Deployment

| ID | Requirement |
|----|-------------|
| NF-070 | The system shall be deployed on cloud infrastructure (AWS) |
| NF-071 | The system shall use containerization (Docker/Kubernetes) |
| NF-072 | The system shall support multi-region deployment |
| NF-073 | Infrastructure shall be defined as code (Terraform) |

---

# 3. High Level Design

## 3.1 High Level System Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                    CLIENTS                                           │
├─────────────────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐       │
│  │  Customer    │    │   Courier    │    │    Admin     │    │   Vendor     │       │
│  │  Web App     │    │  Mobile App  │    │   Portal     │    │   Portal     │       │
│  │  (React)     │    │ (React Nat.) │    │   (React)    │    │   (React)    │       │
│  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘    └──────┬───────┘       │
└─────────┼───────────────────┼───────────────────┼───────────────────┼───────────────┘
          │                   │                   │                   │
          ▼                   ▼                   ▼                   ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              CDN (CloudFront)                                        │
│                         Static Assets + Edge Caching                                 │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                            API GATEWAY (Kong/AWS API GW)                             │
│                    Rate Limiting │ Authentication │ Routing                          │
└─────────────────────────────────────────────────────────────────────────────────────┘
                                        │
          ┌─────────────┬───────────────┼───────────────┬─────────────┐
          ▼             ▼               ▼               ▼             ▼
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              MICROSERVICES LAYER                                     │
├─────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌───────────┐  │
│  │    User     │  │   Catalog   │  │    Order    │  │   Delivery  │  │  Payment  │  │
│  │   Service   │  │   Service   │  │   Service   │  │   Service   │  │  Service  │  │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘  └───────────┘  │
│                                                                                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌───────────┐  │
│  │ Notification│  │   Support   │  │Recommendation│ │  Inventory  │  │  Vendor   │  │
│  │   Service   │  │   Service   │  │   Service   │  │   Service   │  │  Service  │  │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘  └───────────┘  │
└─────────────────────────────────────────────────────────────────────────────────────┘
          │                                                       │
          ▼                                                       ▼
┌──────────────────────────────┐                   ┌──────────────────────────────────┐
│      MESSAGE BROKER          │                   │      EXTERNAL SERVICES           │
│        (Amazon SQS +         │                   ├──────────────────────────────────┤
│         EventBridge)         │                   │  - Payment Gateway (Stripe)      │
└──────────────────────────────┘                   │  - LLM API (OpenAI/Claude)       │
                                                   │  - Maps API (Google Maps)        │
          │                                        │  - Email Service (SendGrid)      │
          ▼                                        │  - SMS Service (Twilio)          │
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                DATA LAYER                                            │
├─────────────────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌───────────┐  │
│  │ PostgreSQL  │  │    Redis    │  │Elasticsearch│  │     S3      │  │ DynamoDB  │  │
│  │  (RDS)      │  │   Cache     │  │   Search    │  │   Storage   │  │  Sessions │  │
│  │             │  │             │  │             │  │             │  │           │  │
│  │ - Users     │  │ - Sessions  │  │ - Products  │  │ - Images    │  │ - Carts   │  │
│  │ - Orders    │  │ - Cart      │  │ - Logs      │  │ - POD Docs  │  │ - Events  │  │
│  │ - Products  │  │ - Catalog   │  │             │  │             │  │           │  │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘  └───────────┘  │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

## 3.2 Design Rules and Principles

### Architecture Principles

1. **Microservices Architecture**: Each business domain has its own service for independent scaling and deployment.

2. **API-First Design**: All services communicate via well-defined REST/GraphQL APIs with OpenAPI specifications.

3. **Event-Driven Communication**: Asynchronous messaging for non-critical operations to improve resilience.

4. **Database per Service**: Each microservice owns its data, preventing tight coupling.

5. **Stateless Services**: All services are stateless; state is stored in distributed caches or databases.

6. **Infrastructure as Code**: All infrastructure defined using Terraform for reproducibility.

7. **Security by Design**: Zero-trust networking, encrypted data at rest and in transit.

8. **Observability First**: Built-in logging, metrics, and tracing from day one.

### Technology Stack

| Layer | Technology | Justification |
|-------|------------|---------------|
| Frontend | React + TypeScript | Component reusability, strong typing |
| Mobile | React Native | Code sharing between iOS/Android |
| API Gateway | AWS API Gateway + Kong | Managed service, rate limiting |
| Backend | Node.js + NestJS | Fast development, good ecosystem |
| Database | PostgreSQL (RDS) | ACID compliance, JSON support |
| Cache | Redis (ElastiCache) | High performance, pub/sub support |
| Search | Elasticsearch | Full-text search, analytics |
| Message Queue | Amazon SQS + EventBridge | Managed, scalable, event routing |
| Storage | S3 | Cost-effective, durable |
| Container Orchestration | EKS (Kubernetes) | Industry standard, auto-scaling |
| CI/CD | GitHub Actions | Native integration, cost-effective |
| Monitoring | Datadog / CloudWatch | APM, logs, metrics unified |

## 3.3 High Level System Flows

### 3.3.1 User Registration Flow

```
┌────────┐     ┌─────────┐     ┌─────────────┐     ┌──────────┐     ┌──────────┐
│ Client │     │   API   │     │    User     │     │PostgreSQL│     │  Email   │
│  App   │     │ Gateway │     │   Service   │     │    DB    │     │ Service  │
└───┬────┘     └────┬────┘     └──────┬──────┘     └────┬─────┘     └────┬─────┘
    │               │                 │                 │                │
    │ POST /register│                 │                 │                │
    │──────────────>│                 │                 │                │
    │               │ Validate + Route│                 │                │
    │               │────────────────>│                 │                │
    │               │                 │ Hash Password   │                │
    │               │                 │ Validate Email  │                │
    │               │                 │                 │                │
    │               │                 │ INSERT user     │                │
    │               │                 │────────────────>│                │
    │               │                 │    Success      │                │
    │               │                 │<────────────────│                │
    │               │                 │                 │                │
    │               │                 │ Publish UserCreated Event        │
    │               │                 │─────────────────────────────────>│
    │               │                 │                 │                │
    │               │                 │                 │ Send Welcome   │
    │               │                 │                 │     Email      │
    │               │  201 Created    │                 │                │
    │<──────────────│<────────────────│                 │                │
    │               │                 │                 │                │
```

### 3.3.2 Order Placement Flow

```
┌────────┐  ┌───────┐  ┌───────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌────────┐
│Customer│  │  API  │  │ Order │  │Inventory│  │ Payment │  │Delivery │  │Notific.│
│  App   │  │Gateway│  │Service│  │ Service │  │ Service │  │ Service │  │Service │
└───┬────┘  └───┬───┘  └───┬───┘  └────┬────┘  └────┬────┘  └────┬────┘  └───┬────┘
    │           │          │           │            │            │           │
    │POST /orders          │           │            │            │           │
    │──────────>│          │           │            │            │           │
    │           │ Route    │           │            │            │           │
    │           │─────────>│           │            │            │           │
    │           │          │           │            │            │           │
    │           │          │ Check Stock            │            │           │
    │           │          │──────────>│            │            │           │
    │           │          │  Available│            │            │           │
    │           │          │<──────────│            │            │           │
    │           │          │           │            │            │           │
    │           │          │ Reserve Items          │            │           │
    │           │          │──────────>│            │            │           │
    │           │          │  Reserved │            │            │           │
    │           │          │<──────────│            │            │           │
    │           │          │           │            │            │           │
    │           │          │ Process Payment        │            │           │
    │           │          │───────────────────────>│            │           │
    │           │          │    Payment Success     │            │           │
    │           │          │<───────────────────────│            │           │
    │           │          │           │            │            │           │
    │           │          │ Create Order           │            │           │
    │           │          │ (Status: CONFIRMED)    │            │           │
    │           │          │           │            │            │           │
    │           │          │ Schedule Delivery      │            │           │
    │           │          │───────────────────────────────────>│           │
    │           │          │           │            │   Scheduled│           │
    │           │          │<───────────────────────────────────│           │
    │           │          │           │            │            │           │
    │           │          │ Publish OrderCreated Event─────────────────────>│
    │           │          │           │            │            │           │
    │  Order    │          │           │            │            │  Email +  │
    │ Created   │          │           │            │            │   Push    │
    │<──────────│<─────────│           │            │            │           │
```

### 3.3.3 Delivery Tracking Flow

```
┌────────┐  ┌────────┐  ┌───────┐  ┌─────────┐  ┌─────────┐  ┌────────┐
│Courier │  │Customer│  │  API  │  │Delivery │  │  Order  │  │Notific.│
│  App   │  │  App   │  │Gateway│  │ Service │  │ Service │  │Service │
└───┬────┘  └───┬────┘  └───┬───┘  └────┬────┘  └────┬────┘  └───┬────┘
    │           │           │           │            │            │
    │ Update Location       │           │            │            │
    │──────────────────────>│           │            │            │
    │           │           │──────────>│            │            │
    │           │           │           │ Store GPS  │            │
    │           │           │           │            │            │
    │ Mark as Delivered     │           │            │            │
    │ + Upload POD Photo    │           │            │            │
    │──────────────────────>│           │            │            │
    │           │           │──────────>│            │            │
    │           │           │           │            │            │
    │           │           │           │ Update Order Status     │
    │           │           │           │───────────>│            │
    │           │           │           │  Updated   │            │
    │           │           │           │<───────────│            │
    │           │           │           │            │            │
    │           │           │           │ Publish DeliveryCompleted        │
    │           │           │           │────────────────────────>│
    │           │           │           │            │            │
    │           │           │           │            │   Notify   │
    │           │ Push Notification     │            │  Customer  │
    │           │<─────────────────────────────────────────────────
    │           │           │           │            │            │
    │           │ GET /orders/{id}      │            │            │
    │           │──────────>│           │            │            │
    │           │ Order with│           │            │            │
    │           │ POD Photo │           │            │            │
    │           │<──────────│           │            │            │
```

### 3.3.4 Smart Recommendation Flow (Daily Batch)

```
┌──────────┐  ┌─────────────┐  ┌─────────┐  ┌─────────┐  ┌───────┐  ┌────────┐
│Scheduler │  │Recommendation│  │  User   │  │ Catalog │  │  LLM  │  │ Email  │
│(CloudWatch│  │   Service   │  │ Service │  │ Service │  │  API  │  │Service │
└────┬─────┘  └──────┬──────┘  └────┬────┘  └────┬────┘  └───┬───┘  └───┬────┘
     │               │              │            │           │          │
     │ Trigger 6AM   │              │            │           │          │
     │──────────────>│              │            │           │          │
     │               │              │            │           │          │
     │               │ Get Active Users          │           │          │
     │               │─────────────>│            │           │          │
     │               │  User List   │            │           │          │
     │               │<─────────────│            │           │          │
     │               │              │            │           │          │
     │               │ For each user:            │           │          │
     │               │──────────────────────────────────────────────────│
     │               │ │            │            │           │          │
     │               │ │Get Purchase History     │           │          │
     │               │ │───────────>│            │           │          │
     │               │ │  History   │            │           │          │
     │               │ │<───────────│            │           │          │
     │               │ │            │            │           │          │
     │               │ │Get Available Products   │           │          │
     │               │ │───────────────────────>│           │          │
     │               │ │  Products  │            │           │          │
     │               │ │<───────────────────────│           │          │
     │               │ │            │            │           │          │
     │               │ │Generate Personalized Message       │          │
     │               │ │───────────────────────────────────>│          │
     │               │ │  Recommendation Text    │           │          │
     │               │ │<───────────────────────────────────│          │
     │               │ │            │            │           │          │
     │               │ │Send Email  │            │           │          │
     │               │ │─────────────────────────────────────────────>│
     │               │ │            │            │           │  Sent   │
     │               │ │<─────────────────────────────────────────────│
     │               │──────────────────────────────────────────────────│
     │               │              │            │           │          │
```

## 3.4 Message Schemas

### 3.4.1 Event Schemas

**OrderCreated Event**
```json
{
  "eventType": "OrderCreated",
  "eventId": "uuid",
  "timestamp": "2026-03-02T10:30:00Z",
  "version": "1.0",
  "payload": {
    "orderId": "ORD-12345",
    "customerId": "USR-67890",
    "items": [
      {
        "productId": "PRD-001",
        "quantity": 2,
        "unitPrice": 3.50
      }
    ],
    "totalAmount": 7.00,
    "currency": "EUR",
    "deliveryAddress": {
      "street": "Alexanderplatz 1",
      "city": "Berlin",
      "postalCode": "10178",
      "country": "DE"
    },
    "deliverySlot": {
      "date": "2026-03-02",
      "startTime": "14:00",
      "endTime": "16:00"
    }
  }
}
```

**DeliveryStatusUpdated Event**
```json
{
  "eventType": "DeliveryStatusUpdated",
  "eventId": "uuid",
  "timestamp": "2026-03-02T14:30:00Z",
  "version": "1.0",
  "payload": {
    "orderId": "ORD-12345",
    "deliveryId": "DEL-54321",
    "courierId": "COU-111",
    "status": "DELIVERED",
    "location": {
      "latitude": 52.5200,
      "longitude": 13.4050
    },
    "proofOfDelivery": {
      "photoUrl": "s3://bucket/pod/DEL-54321.jpg",
      "signatureUrl": "s3://bucket/signatures/DEL-54321.png",
      "timestamp": "2026-03-02T14:28:00Z"
    }
  }
}
```

### 3.4.2 API Schemas

**Create Order Request**
```json
{
  "items": [
    {
      "productId": "string",
      "quantity": "integer"
    }
  ],
  "deliveryAddressId": "string",
  "deliverySlotId": "string",
  "couponCode": "string (optional)",
  "paymentMethodId": "string"
}
```

**Create Order Response**
```json
{
  "orderId": "string",
  "status": "CONFIRMED",
  "estimatedDelivery": "ISO8601 datetime",
  "totalAmount": "decimal",
  "currency": "string"
}
```

## 3.5 Upgradability

### Deployment Strategy

1. **Blue-Green Deployments**: Maintain two identical production environments
    - Blue: Current production
    - Green: New version staging
    - Switch traffic via load balancer after validation

2. **Rolling Updates**: Kubernetes rolling deployment strategy
    - maxUnavailable: 25%
    - maxSurge: 25%

3. **Database Migrations**:
    - Use Flyway/Liquibase for versioned migrations
    - Backward-compatible changes only
    - Separate deploy and migrate phases

4. **Feature Flags**: LaunchDarkly integration
    - Gradual rollout (1% -> 10% -> 50% -> 100%)
    - Instant rollback capability
    - A/B testing support

### Version Strategy

| Component | Versioning | Backward Compat |
|-----------|------------|-----------------|
| REST APIs | URL versioning (/v1/, /v2/) | 2 versions |
| Events | Schema version in payload | 3 versions |
| Mobile App | Semantic versioning | Force update for major |
| Database | Migration number | Always backward |

## 3.6 Sizing and Cost Estimation

### Infrastructure Sizing (Year 1 - 1M Users)

**Assumptions:**
- 1M registered users
- 10% daily active users (DAU) = 100,000
- Peak concurrent users: 10,000
- Average 3 orders per user per month = 250,000 orders/month
- Average order: 5 items

**Compute Resources (EKS)**

| Service | Instances | vCPU | Memory | Monthly Cost |
|---------|-----------|------|--------|--------------|
| User Service | 3 | 2 | 4GB | $180 |
| Catalog Service | 4 | 2 | 8GB | $320 |
| Order Service | 4 | 2 | 4GB | $240 |
| Delivery Service | 3 | 2 | 4GB | $180 |
| Payment Service | 2 | 2 | 4GB | $120 |
| Notification Service | 2 | 1 | 2GB | $60 |
| Recommendation Service | 2 | 4 | 8GB | $240 |
| Support Service | 2 | 2 | 4GB | $120 |

**Database Resources**

| Service | Type | Size | Monthly Cost |
|---------|------|------|--------------|
| PostgreSQL (RDS) | db.r6g.xlarge Multi-AZ | 500GB | $800 |
| Redis (ElastiCache) | cache.r6g.large | 3 nodes | $450 |
| Elasticsearch | 3 x r6g.large.search | 300GB | $600 |
| DynamoDB | On-demand | Variable | $200 |
| S3 | Standard | 1TB | $25 |

**Other Services**

| Service | Usage | Monthly Cost |
|---------|-------|--------------|
| API Gateway | 50M requests | $175 |
| CloudFront CDN | 5TB transfer | $425 |
| SQS + EventBridge | 10M messages | $50 |
| Route 53 | DNS queries | $25 |
| CloudWatch | Logs + Metrics | $150 |
| Secrets Manager | 50 secrets | $20 |

**External Services**

| Service | Usage | Monthly Cost |
|---------|-------|--------------|
| SendGrid (Email) | 500K emails | $90 |
| Twilio (SMS) | 100K messages | $750 |
| OpenAI/Claude API | LLM calls | $500 |
| Google Maps API | Routing | $300 |
| Stripe (Payment) | 2.9% + $0.30 | Variable |

### Total Monthly Cost Estimate (Year 1)

| Category | Monthly Cost |
|----------|--------------|
| Compute (EKS) | $1,460 |
| Databases | $2,075 |
| Networking & CDN | $625 |
| Messaging | $50 |
| Monitoring | $170 |
| External APIs | $1,640 |
| **Total** | **~$6,020/month** |
| **Annual** | **~$72,240/year** |

### Scaling Projection

| Phase | Users | Monthly Cost |
|-------|-------|--------------|
| MVP (6 months) | 100K | $3,000 |
| Year 1 | 1M | $6,000 |
| Year 2 | 10M | $25,000 |
| Year 3 | 100M | $150,000 |

---

# 4. Assumptions

| ID | Assumption |
|----|------------|
| A-01 | Users have stable internet connectivity |
| A-02 | Vendors can integrate via provided APIs or upload inventory manually |
| A-03 | Daily inventory refresh at 5 AM is sufficient for freshness tracking |
| A-04 | Couriers have smartphones with GPS capability |
| A-05 | Third-party payment gateways handle PCI compliance |
| A-06 | LLM API (OpenAI/Claude) maintains >99.9% availability |
| A-07 | AWS Frankfurt region is suitable for GDPR compliance |
| A-08 | Delivery radius is limited to metropolitan areas |
| A-09 | Peak ordering hours are 7-9 AM and 5-8 PM |
| A-10 | Average delivery time is 2-4 hours from order placement |

---

# 5. Time Estimation

| Subsystem/Team | Workdays | Team Size |
|----------------|----------|-----------|
| Infrastructure Setup (IaC, CI/CD) | 20 | 2 |
| User Service + Auth | 25 | 2 |
| Catalog Service | 20 | 2 |
| Order Service | 30 | 3 |
| Payment Integration | 15 | 2 |
| Delivery Service | 25 | 2 |
| Notification Service | 15 | 1 |
| Courier Mobile App | 30 | 2 |
| Customer Web App | 40 | 3 |
| Admin Portal | 20 | 2 |
| Support Service (LLM) | 20 | 2 |
| Recommendation Engine | 25 | 2 |
| Testing & QA | 30 | 2 |
| Documentation | 10 | 1 |
| **Total** | **~6 months** | **15 devs** |

---

# 6. Limitations

| Limitation | Description |
|------------|-------------|
| L-01 | MVP supports only credit card and PayPal payments; no cash on delivery |
| L-02 | Real-time inventory may have up to 15-minute delay from vendor systems |
| L-03 | Same-day delivery only available for orders placed before 2 PM |
| L-04 | Delivery limited to metropolitan areas with clear address validation |
| L-05 | LLM support available only in English and German initially |
| L-06 | Maximum order size limited to 20kg due to courier capacity |
| L-07 | No support for recurring/subscription orders in MVP |
| L-08 | Vendor onboarding requires manual approval process |

---

# 7. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| LLM API downtime | Medium | Low | Implement fallback to human agents; cache common responses |
| Payment gateway failure | High | Low | Integrate secondary payment provider; implement retry logic |
| Data breach | Critical | Low | Encryption at rest/transit; regular security audits; WAF |
| Vendor inventory sync failure | Medium | Medium | Manual override capability; alert system; offline queue |
| Courier app offline | Medium | Medium | Offline mode with sync; local storage of routes |
| GDPR non-compliance fine | Critical | Low | Legal review; automated data deletion; consent management |
| Single region failure | High | Low | Multi-AZ deployment; disaster recovery plan |
| Scaling bottleneck | High | Medium | Load testing; auto-scaling policies; performance monitoring |
| Third-party API rate limits | Medium | Medium | Implement caching; request queuing; rate limit monitoring |
| Courier shortage | High | Medium | Surge pricing integration; partner with multiple courier services |

---

# 8. Open Issues

| Issue | Description | Next Steps |
|-------|-------------|------------|
| OI-01 | Final payment provider selection (Stripe vs Adyen) | Compare fees and EU support |
| OI-02 | LLM provider selection (OpenAI vs Claude vs self-hosted) | Evaluate cost and quality |
| OI-03 | Courier app platform priority (iOS first vs Android first) | Analyze courier demographics |
| OI-04 | Multi-region data residency strategy for expansion | Legal consultation required |
| OI-05 | Vendor onboarding portal MVP scope | Product decision needed |
| OI-06 | Recommendation algorithm complexity for MVP | Data science input required |
| OI-07 | Support for cash payments in San Francisco | Market research required |
| OI-08 | Integration with existing vendor ERP systems | Vendor survey needed |

---

# Appendix A: Data Model (ERD)

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│     USERS       │       │     ORDERS      │       │   ORDER_ITEMS   │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id (PK)         │       │ id (PK)         │       │ id (PK)         │
│ email           │       │ user_id (FK)    │───────│ order_id (FK)   │
│ password_hash   │       │ status          │       │ product_id (FK) │
│ name            │       │ total_amount    │       │ quantity        │
│ phone           │       │ delivery_fee    │       │ unit_price      │
│ preferred_lang  │       │ address_id (FK) │       │ created_at      │
│ created_at      │       │ delivery_slot   │       └────────┬────────┘
│ updated_at      │       │ created_at      │                │
│ gdpr_consent    │       │ updated_at      │                │
└────────┬────────┘       └────────┬────────┘                │
         │                         │                         │
         │                         │                         ▼
         │                         │               ┌─────────────────┐
         │                         │               │    PRODUCTS     │
         ▼                         │               ├─────────────────┤
┌─────────────────┐               │               │ id (PK)         │
│   ADDRESSES     │               │               │ vendor_id (FK)  │
├─────────────────┤               │               │ name            │
│ id (PK)         │               │               │ description     │
│ user_id (FK)    │               │               │ price           │
│ street          │               │               │ category        │
│ city            │               │               │ image_url       │
│ postal_code     │               │               │ stock_quantity  │
│ country         │               │               │ is_available    │
│ is_default      │               │               │ created_at      │
└─────────────────┘               │               └────────┬────────┘
                                  │                        │
                                  ▼                        ▼
                        ┌─────────────────┐      ┌─────────────────┐
                        │   DELIVERIES    │      │    VENDORS      │
                        ├─────────────────┤      ├─────────────────┤
                        │ id (PK)         │      │ id (PK)         │
                        │ order_id (FK)   │      │ name            │
                        │ courier_id (FK) │      │ contact_email   │
                        │ status          │      │ contact_phone   │
                        │ current_lat     │      │ address         │
                        │ current_lng     │      │ is_active       │
                        │ pod_photo_url   │      │ created_at      │
                        │ pod_signature   │      └─────────────────┘
                        │ delivered_at    │
                        └─────────────────┘

┌─────────────────┐       ┌─────────────────┐
│    COUPONS      │       │   COURIERS      │
├─────────────────┤       ├─────────────────┤
│ id (PK)         │       │ id (PK)         │
│ code            │       │ name            │
│ discount_type   │       │ email           │
│ discount_value  │       │ phone           │
│ min_order       │       │ vehicle_type    │
│ valid_from      │       │ is_active       │
│ valid_until     │       │ current_lat     │
│ max_uses        │       │ current_lng     │
│ current_uses    │       │ created_at      │
└─────────────────┘       └─────────────────┘
```

---

# Appendix B: API Endpoints Summary

## User Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /v1/auth/register | Register new user |
| POST | /v1/auth/login | Authenticate user |
| POST | /v1/auth/logout | Logout user |
| GET | /v1/users/me | Get current user profile |
| PUT | /v1/users/me | Update user profile |
| DELETE | /v1/users/me | Delete user account (GDPR) |
| GET | /v1/users/me/addresses | List user addresses |
| POST | /v1/users/me/addresses | Add new address |

## Catalog Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /v1/products | List products (paginated) |
| GET | /v1/products/{id} | Get product details |
| GET | /v1/products/search | Search products |
| GET | /v1/categories | List categories |

## Order Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /v1/orders | Create new order |
| GET | /v1/orders | List user orders |
| GET | /v1/orders/{id} | Get order details |
| POST | /v1/orders/{id}/cancel | Cancel order |
| GET | /v1/cart | Get shopping cart |
| POST | /v1/cart/items | Add item to cart |
| DELETE | /v1/cart/items/{id} | Remove item from cart |

## Delivery Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /v1/deliveries/{id} | Get delivery status |
| GET | /v1/deliveries/{id}/track | Get real-time tracking |
| PUT | /v1/deliveries/{id}/status | Update delivery status (courier) |
| POST | /v1/deliveries/{id}/pod | Upload proof of delivery |

## Support Service
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /v1/support/chat | Send message to LLM |
| GET | /v1/support/chat/history | Get chat history |
| POST | /v1/support/escalate | Escalate to human agent |

---

# Appendix C: Security Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         INTERNET                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    AWS WAF (Web Application Firewall)            │
│                 DDoS Protection │ SQL Injection │ XSS            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     CloudFront (CDN)                             │
│                    TLS 1.3 │ HTTPS Only                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway                                 │
│           Rate Limiting │ API Keys │ JWT Validation              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    VPC (Private Network)                         │
├─────────────────────────────────────────────────────────────────┤
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐     │
│  │  Public Subnet │  │ Private Subnet │  │ Database Subnet│     │
│  │    (ALB)       │  │  (EKS Pods)    │  │   (RDS/Redis)  │     │
│  └────────────────┘  └────────────────┘  └────────────────┘     │
│         │                    │                   │               │
│         │      Security Groups (Firewall Rules)  │               │
│         └────────────────────┼───────────────────┘               │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Secrets Manager                               │
│            API Keys │ DB Credentials │ Encryption Keys           │
└─────────────────────────────────────────────────────────────────┘
```

### Security Measures

1. **Authentication**: OAuth 2.0 / JWT tokens
2. **Authorization**: Role-based access control (RBAC)
3. **Encryption**: AES-256 at rest, TLS 1.3 in transit
4. **Data Privacy**: GDPR-compliant data handling
5. **Audit Logging**: All access logged to CloudWatch
6. **Vulnerability Scanning**: Regular penetration testing
