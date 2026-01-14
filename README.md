Spring WebFlux – Correct vs Incorrect Reactive Patterns

Este repositorio contiene ejemplos prácticos y ejecutables de programación reactiva con Spring Boot + WebFlux.

El objetivo es educativo y técnico: mostrar casos que compilan, ejecutan y pasan QA, pero que fallan bajo concurrencia o carga, comparándolos con sus implementaciones correctas.


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


OBJETIVO DEL REPOSITORIO

Este repositorio está pensado para:
- capacitación técnica
- entrevistas
- revisión de arquitectura
- evitar errores comunes en producción
