# Pipeline de eventos bancarios

Aplicacion de ejemplo que recibe transacciones por HTTP, las publica en Kafka y
usa Kafka Streams para procesarlas. Las transacciones mayores a 10.000 generan
una alerta en el topic `banco-alertas`.

## Requisitos

- Java 17 o superior.
- Maven.
- Docker Desktop con Docker Compose.
- Postman para probar el endpoint (opcional; tambien se puede usar `curl`).

## Puertos utilizados

| Servicio | URL o puerto |
| --- | --- |
| API Spring Boot | `http://localhost:8081` |
| Kafka | `localhost:9092` |
| Kafka UI | `http://localhost:8082` |

La API y Kafka UI usan puertos distintos. Para enviar transacciones usa siempre
la URL de la API; Kafka UI es solo para consultar los topics y mensajes.

## 1. Abrir una terminal en el proyecto

Ejecuta los comandos desde la carpeta que contiene `pom.xml` y
`docker-compose.yml`:

```powershell
cd C:\Users\valen\Desktop\NC-End-to-End
```

## 2. Iniciar Kafka y Kafka UI

Arranca los servicios de Docker Compose:

```powershell
docker compose up -d
```

Comprueba que los tres servicios aparecen como `Up`:

```powershell
docker compose ps
```

Deberias ver `zookeeper`, `kafka` y `kafka-ui`. Si Kafka aun esta iniciando,
espera unos segundos y vuelve a comprobarlo.

Puedes abrir Kafka UI en el navegador:

```text
http://localhost:8082
```

## 3. Iniciar la aplicacion Java

Abre otra terminal en la carpeta del proyecto y ejecuta:

```powershell
mvn spring-boot:run
```

Deja esa terminal abierta. Espera a que Spring Boot indique que la aplicacion
inicio; la API escucha en el puerto `8081`.

Tambien puedes crear el JAR y ejecutarlo:

```powershell
mvn clean package
java -jar target\nc-end-to-end-0.0.1-SNAPSHOT.jar
```

## 4. Enviar una transaccion desde Postman

1. Crea una solicitud nueva.
2. Selecciona el metodo **POST**.
3. Usa esta URL:

   ```text
   http://localhost:8081/api/banco/transaccion
   ```

4. En **Body**, selecciona **raw** y el tipo **JSON**.
5. Envia este ejemplo:

   ```json
   {
     "usuario": "usuario1",
     "monto": 15000,
     "origen": "web"
   }
   ```

La respuesta esperada es `200 OK`, con un mensaje parecido a:

```text
Enviado: 15000.0 de usuario1
```

El campo `timestamp` es opcional; la API lo completa automaticamente si no se
envia.

## 5. Probar las reglas del flujo

Prueba los siguientes montos cambiando el valor de `monto` en Postman:

| Monto | Resultado esperado |
| --- | --- |
| `5` | Se publica en Kafka, pero el flujo la ignora porque es menor o igual a 10. |
| `500` | Se procesa; no genera alerta. |
| `15000` | Se procesa y genera una alerta de severidad `ALTA`. |

La aplicacion imprime mensajes del procesamiento en la terminal donde corre
Spring Boot.

## 6. Consultar el resultado en Kafka UI

1. Abre `http://localhost:8082` (no `localhost:8081`, que es la API).
2. En el cluster `local`, abre **Topics**.
3. Busca `banco-transacciones` para ver las transacciones recibidas.
4. Busca `banco-alertas` y abre **Messages** para consultar las alertas.

Una alerta para la transaccion de ejemplo tendra datos similares a estos:

```json
{
  "usuario": "usuario1",
  "monto": 15000.0,
  "motivo": "Transacción inusualmente alta en web",
  "severidad": "ALTA"
}
```

## Alternativa: probar con curl

Desde PowerShell puedes enviar una solicitud de ejemplo con:

```powershell
curl.exe -X POST "http://localhost:8081/api/banco/transaccion" `
  -H "Content-Type: application/json" `
  -d '{"usuario":"usuario1","monto":15000,"origen":"web"}'
```

## Detener los servicios

Deten la aplicacion Java con `Ctrl+C` en su terminal. Luego detiene los
contenedores:

```powershell
docker compose down
```

## Solucion de problemas

- **La pagina de `localhost:8081` muestra 404:** es normal en la ruta raiz; la
  API no tiene una pagina de inicio. Prueba el endpoint con `POST` en
  `/api/banco/transaccion`.
- **Kafka UI muestra 404 o no abre:** confirma que `kafka-ui` este `Up` con
  `docker compose ps` y abre `http://localhost:8082`.
- **Error al enviar desde Postman:** confirma que Spring Boot este corriendo y
  usa el metodo `POST`, la URL completa y un body JSON.
- **Error de conexion con Kafka:** comprueba que `kafka` este `Up`. Puedes ver
  sus logs con `docker compose logs -f kafka`.
- **Un puerto ya esta ocupado:** libera el puerto correspondiente (`8081` para
  la API, `8082` para Kafka UI o `9092` para Kafka), o cambia la configuracion
  del servicio.
