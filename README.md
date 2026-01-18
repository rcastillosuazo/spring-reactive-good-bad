Spring WebFlux – Correct vs Incorrect Reactive Patterns

Este repositorio contiene ejemplos prácticos y ejecutables de programación reactiva con Spring Boot + WebFlux.

El objetivo es educativo y técnico: mostrar casos que compilan, ejecutan y pasan QA, pero que fallan bajo concurrencia o carga, comparándolos con sus implementaciones correctas.

Se recomienda probar cambiando los tiempos de espera (WAITIME de ExternalApiClient), requests o concurrency de los test para visualizar comportamiento en distintos escenarios.


STACK TECNOLÓGICO

- Java 21
- Spring Boot 3.x
- Spring WebFlux
- Project Reactor
- JUnit 5
- WebTestClient

CÓMO LEVANTAR EL PROYECTO

Requisitos:
- Java 21
- Maven

Ejecutar aplicación:

mvn spring-boot:run

La aplicación queda disponible en:
http://localhost:8080


EJECUTAR TESTS

mvn test

Los tests:
- levantan servidor real (RANDOM_PORT)
- ejecutan llamadas concurrentes
- no fallan aunque el resultado sea incorrecto
- imprimen métricas de tiempo y resultados


SERVICIOS INCLUIDOS Y QUÉ DEMUESTRAN

Este repositorio utiliza los siguientes servicios para explicar los casos:

- BalanceReactiveService
- CorrectReactiveService
- IncorrectReactiveService
- JoinDependencyService
- ComplexFlowService


BALANCEREACTIVESERVICE

Qué demuestra:
Problemas clásicos de estado compartido bajo concurrencia en programación reactiva.

Caso incorrecto:
- uso de variable mutable compartida
- bajo concurrencia se producen lost updates
- no se lanza excepción

Método:
withdrawUnsafe(...)

Resultado típico:
UNSAFE balance = 70

Caso correcto:
- uso de AtomicInteger
- lock-free
- thread-safe
- escala correctamente

Método:
withdrawSafe(...)

Resultado esperado:
SAFE balance = 50

Lección clave:
Reactivo no es thread-safe por definición


INCORRECTREACTIVESERVICE

Qué demuestra:
Uso incorrecto de programación reactiva mezclando código bloqueante.

Problemas comunes:
- uso de block() dentro del flujo
- bloqueo del event loop
- error en runtime bajo WebFlux

Error típico:
block()/blockFirst() are blocking, which is not supported in reactor-http-nio

Lección:
Nunca bloquear dentro del event loop reactivo


CORRECTREACTIVESERVICE

Qué demuestra:
Uso correcto de programación reactiva:
- no bloquea
- uso correcto de flatMap
- composición asíncrona de flujos

Ejemplo:
Mono<String> asyncCall(...)

Lección:
Reactivo correcto = no bloquear + componer flujos


JOINDEPENDENCYSERVICE

Qué demuestra:
Diferencia entre paralelismo y dependencia de datos.

Caso incorrecto:
Uso de zip cuando un flujo depende del resultado del otro.

Método:
joinIncorrect(...)

Problemas:
- flujos ejecutados en paralelo
- dependencia ignorada
- resultados inconsistentes o latencia innecesaria

Caso correcto:
Uso de flatMap para respetar la dependencia.

Método:
joinCorrect(...)

Regla práctica:
Independientes -> zip
Dependientes   -> flatMap


COMPLEXFLOWSERVICE

Qué demuestra:
Flujos más cercanos a escenarios reales de negocio:
- múltiples pasos encadenados
- dependencias entre respuestas
- impacto directo en latencia total

Incluye ejemplos de:
- encadenamiento correcto
- joins mal diseñados
- degradación de performance bajo carga

Lección:
La complejidad reactiva está en modelar correctamente las dependencias


TESTS

Los tests utilizan:
- WebTestClient y otros casos WebClient que requieren levantar previamente la aplicación
- Flux.flatMap para concurrencia real
- ejecución simultánea de múltiples requests

Ejemplo típico:
Flux.range(1, 20)
flatMap(request concurrente)
blockLast() //Bloquea el hilo hasta que Flux termina y consume todos los elementos.


CONCLUSIONES CLAVE

- Programación reactiva no elimina problemas de concurrencia
- Los bugs más peligrosos no lanzan excepciones
- block() en WebFlux es un error de diseño
- zip mal usado genera lógica incorrecta
- estado mutable + concurrencia = bug silencioso


REGLAS PRÁCTICAS

Estado compartido  -> Atomic* o base de datos
Dependencias       -> flatMap
Paralelismo real   -> zip
Código bloqueante  -> boundedElastic
Validar performance-> tests concurrentes

## 🧵 Reactivo vs MVC + Virtual Threads (Java 21)

Esta sección documenta **casos reales y comparables** entre:

* **Spring WebFlux (reactivo, no bloqueante)**
* **Spring MVC + Virtual Threads (Java 21)**

El objetivo es mostrar **cuándo ambos funcionan bien**, **cuándo se degradan**, y **cuándo WebFlux sigue siendo necesario incluso con virtual threads**.

---

## 🧠 Contexto general

Desde Java 21, **Virtual Threads** permiten manejar miles de operaciones bloqueantes con bajo costo, lo que ha reabierto el debate:

> ¿Sigue siendo necesario WebFlux?

**Respuesta corta:**
👉 *Sí, pero solo en ciertos escenarios.*

Este repositorio muestra esos escenarios con código ejecutable y tests comparables.

---

## 📦 Componentes involucrados

* **Controller**

    * Endpoints MVC y WebFlux
* **Service**

    * Lógica fan-out, joins y llamadas externas
* **External Client**

    * Simula servicios lentos (`sleep`)
* **Tests**

    * Funcionales
    * De carga / concurrencia

---

## 🔥 Escenario 1: Fan-out (1 request → N llamadas externas)

### Qué es fan-out

Un request entrante dispara **múltiples llamadas externas en paralelo**:

```
Request
   ├─ Call A
   ├─ Call B
   ├─ Call C
   ├─ Call D
   └─ Call E
```

Este patrón es **muy común** en:

* APIs Gateway
* BFF
* Agregadores de microservicios

---

## ❌ Caso 1: MVC fan-out incorrecto

**Tecnología**

* Spring MVC
* `CompletableFuture.supplyAsync()`
* ForkJoinPool común

**Problema**

* Bloquea threads
* Pool compartido
* Escala mal bajo carga

```java
CompletableFuture.supplyAsync(() -> client.callSlowServiceSleep(...))
```

### Resultado

* Tiempo de respuesta crece rápidamente
* Saturación del pool
* No hay back-pressure

---

## ✅ Caso 2: MVC fan-out CORRECTO con Virtual Threads

**Tecnología**

* Spring MVC
* Java 21 Virtual Threads
* Executor dedicado

```java
ExecutorService vtExecutor =
    Executors.newVirtualThreadPerTaskExecutor();
```

### Por qué funciona

✔ Cada llamada usa un virtual thread
✔ No bloquea threads de plataforma
✔ Escala bien con I/O bloqueante
✔ Código simple (imperativo)

### Cuándo usarlo

✔ Sistemas MVC existentes
✔ Integraciones legacy bloqueantes
✔ Migraciones graduales
✔ Equipos no reactivos

---

## ⚠️ Límite de Virtual Threads (caso que falla)

Virtual Threads **NO solucionan**:

* Fan-out masivo (N × M llamadas)
* Streaming continuo
* Alto churn de requests
* Back-pressure ausente

Ejemplo:

* 50 requests concurrentes
* cada una hace fan-out de 5 llamadas
* Total = 250 llamadas simultáneas

👉 Aquí **WebFlux empieza a ganar**

---

## ⚡ Caso 3: WebFlux fan-out reactivo

**Tecnología**

* Spring WebFlux
* `Flux.merge`
* No bloqueante

```java
Flux.merge(
    client.callSlowService(...),
    client.callSlowService(...),
    client.callSlowService(...)
)
```

### Ventajas reales

✔ Back-pressure
✔ Menor consumo de memoria
✔ Mejor bajo alta concurrencia
✔ Ideal para I/O intensivo

### Trade-offs

⚠ Mayor complejidad mental
⚠ Debug más complejo
⚠ Requiere stack reactivo completo

---

## 🧪 Tests incluidos

### Tests funcionales

* Verifican respuestas correctas
* Validan orden cuando corresponde

### Tests de carga

* Simulan concurrencia real
* Comparan tiempos:

    * MVC incorrecto
    * MVC + Virtual Threads
    * WebFlux

Ejemplo:

```java
Flux.range(1, 50)
    .flatMap(i ->
        client.get()
            .uri("/api/mvc/fanout-fixed?v=req-" + i)
            .retrieve()
            .bodyToMono(String.class),
        20 // concurrencia real
    )
    .blockLast();
```

---

## 📊 Conclusiones clave

| Escenario               | Mejor opción    |
| ----------------------- | --------------- |
| MVC legacy              | Virtual Threads |
| I/O bloqueante moderado | Virtual Threads |
| Fan-out masivo          | WebFlux         |
| Streaming               | WebFlux         |
| Back-pressure requerido | WebFlux         |
| Simplicidad             | MVC + VT        |

---

## 🧭 Regla práctica para elegir

> **Si puedes expresar tu flujo como una lista de eventos → WebFlux**
> **Si tu flujo es request/response clásico → Virtual Threads**

---

## 🏁 Resumen ejecutivo

* Virtual Threads **no reemplazan** WebFlux
* Reducen enormemente la necesidad de usarlo
* WebFlux sigue siendo clave para:

    * alta concurrencia
    * fan-out masivo
    * control fino del flujo

Este repositorio demuestra **cuándo usar cada uno**, con código real y medible.



OBJETIVO DEL REPOSITORIO

Este repositorio está pensado para:
- capacitación técnica
- entrevistas
- revisión de arquitectura
- evitar errores comunes en producción
- probar distintos escenarios para visualizar como se comportan los casos.

Repositorio generado por IA. 