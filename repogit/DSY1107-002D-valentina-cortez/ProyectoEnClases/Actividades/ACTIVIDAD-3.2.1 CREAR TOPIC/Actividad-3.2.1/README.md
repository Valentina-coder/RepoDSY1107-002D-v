🚀 Guía Práctica de Apache Kafka con Spring BootUn proyecto práctico y educativo para entender visualmente cómo Apache Kafka distribuye mensajes entre particiones, mantiene el control de lectura mediante offsets y garantiza el orden de mensajes utilizando keys.📌 
¿Qué construimos?Cluster de Kafka en Docker:ZooKeeper: Coordinador de metadatos del cluster.Kafka Broker: Servidor de mensajería principal.Kafka UI: Interfaz gráfica en el navegador (http://localhost:8080) para monitorear topics, particiones y offsets en tiempo real.Aplicación Spring Boot (Puerto 8888):Productor (KafkaController): Endpoint REST (/api/kafka/send) que nos permite enviar mensajes al topic, probando envíos con y sin clave (key).Consumidor (KafkaConsumer): Servicio listener que escucha los mensajes entrantes e imprime en la consola la partición, el offset, la clave y el contenido del mensaje.

🛠️ Requisitos PreviosAntes de comenzar, asegúrate de tener instalado:Java 17 o superiorMavenDocker y Docker Compose

📂 Estructura del Proyecto.
├── docker-compose.yml
├── pom.xml
└── src
    └── main
        ├── java
        │   └── com
        │       └── kafka
        │           ├── KafkaTutorialApplication.java
        │           ├── controller
        │           │   └── KafkaController.java
        │           └── consumer
        │               └── KafkaConsumer.java
        └── resources
            └── application.yml

🚀 Paso a Paso: 
Cómo Levantarlo
1️⃣ Levantar los Contenedores (Kafka + ZooKeeper + Kafka UI)En la raíz del proyecto donde se encuentra el archivo docker-compose.yml, ejecuta:docker-compose up -d
Verificación: Abre en tu navegador http://localhost:8080 (o el puerto configurado en tu docker-compose) para confirmar que Kafka UI esté arriba.
2️⃣ Crear el Topic en Kafka UIEntra a Kafka UI (http://localhost:8080).Ve al menú Topics en el panel izquierdo.Haz clic en + Add a Topic.Configura el topic con los siguientes parámetros:Topic Name: tutorial-particionesPartitions: 3Replication Factor: 1Haz clic en Create topic.
3️⃣ Compilar y Ejecutar la Aplicación Spring BootEn una terminal ubicada en la raíz del proyecto, ejecuta:# 1. Compilar el proyecto
mvn clean install

# 2. Ejecutar la aplicación
mvn spring-boot:run
Verificación: En otra terminal puedes verificar el estado de salud ejecutando:curl http://localhost:8888/api/kafka/health
Deberías recibir: Aplicación de Kafka Tutorial está corriendo en puerto 8888🧪 Pruebas y Experimentos🔹 Prueba 1: Envío de Mensajes Sin Key (Distribución Automática)Envía 3 peticiones GET desde tu navegador o cliente HTTP:http://localhost:8888/api/kafka/send?message=Mensaje1
http://localhost:8888/api/kafka/send?message=Mensaje2
http://localhost:8888/api/kafka/send?message=Mensaje3

¿Qué observar en la consola?
Los mensajes se distribuyen entre las particiones disponibles (0, 1 y 2). Cada partición lleva su propio contador independiente de Offset iniciando en 0.🔹 Prueba 2: Envío de Mensajes Con Key (Garantía de Orden)Simularemos los eventos de una orden de compra enviando mensajes con la misma clave (key=orden-555):http://localhost:8888/api/kafka/send?message=Paso1-Creada&key=orden-555
http://localhost:8888/api/kafka/send?message=Paso2-Pagada&key=orden-555
http://localhost:8888/api/kafka/send?message=Paso3-Enviada&key=orden-555

¿Qué observar en la consola?

Todos los mensajes con la misma key entran a la MISMA partición e incrementan secuencialmente el Offset ($0 \rightarrow 1 \rightarrow 2$). Esto garantiza que los eventos se procesen en el orden estricto en que fueron enviados.
🔹Prueba 3: Tolerancia a Fallos y Persistencia de OffsetsDetén la aplicación Spring Boot presionando Ctrl + C en la terminal.Ve a Kafka UI (http://localhost:8080), entra al topic tutorial-particiones y haz clic en Produce Message.Envía un mensaje manual:Key: orden-555Value: Paso4-CompletadaVuelve a encender la aplicación Spring Boot: mvn spring-boot:run.Resultado:
La aplicación NO volverá a leer los mensajes antiguos (Paso1-Creada, Paso2-Pagada, etc.) porque Kafka guardó el progreso (offset) del grupo mi-grupo-educativo. Solo procesará directamente el nuevo mensaje pendiente (Paso4-Completada) en la partición correspondiente con el offset siguiente.
🧠 Resumen de Conceptos lógico donde se publican y organizan los mensajes (tutorial-particiones).ParticiónSubcarpeta física en Kafka que permite paralelizar y distribuir el almacenamiento.OffsetNúmero secuencial e incremental que indica la posición exacta de un mensaje dentro de una partición.KeyClave utilizada por el algoritmo Hash de Kafka para determinar a qué partición enviar el mensaje.Consumer GroupGrupo de lectura (mi-grupo-educativo) que mantiene el registro de los offsets leídos para no duplicar datos tras reiniciar la app.
