# Taller2-Android2-P2
Resolución del taller 2 de Android (segundo parcial).

Qué hice en este taller:

* **Creación del proyecto:** Se trabajó el proyecto en Android Studio para desarrollar una aplicación de encuestas offline, usando Java como lenguaje principal y SQLite como base de datos local.

* **Interfaz de encuesta:** Se diseñó la pantalla principal para mostrar una encuesta de satisfacción, pero sin escribir las preguntas directamente en el archivo XML. El layout solo contiene el contenedor principal y los botones necesarios para guardar la encuesta y ver el historial.

* **Base de datos:** Se implementó SQLite mediante las clases `SurveyContract` y `SurveyDbHelper`. La clase `SurveyContract` define las tablas, columnas y sentencias SQL, mientras que `SurveyDbHelper` se encarga de crear la base de datos, insertar las preguntas iniciales y consultar la información guardada.

* **Inserción automática de preguntas:** Se agregaron las preguntas de la encuesta directamente en el método `onCreate()` de la clase `SurveyDbHelper`, cumpliendo con la regla de que las preguntas no deben ser ingresadas manualmente por el usuario ni estar quemadas en la interfaz XML.

* **Encuesta dinámica:** Se programó la pantalla principal para consultar la tabla `preguntas` y generar dinámicamente los componentes visuales, creando por código los `TextView` para las preguntas y los campos de respuesta correspondientes.

* **Guardado de respuestas:** Se implementó el botón Guardar Encuesta para registrar todas las respuestas del usuario en la tabla `respuestas`, asociando cada respuesta con su pregunta correspondiente y guardando la misma fecha y hora para toda la encuesta.

* **Historial de encuestas:** Se creó una segunda pantalla para mostrar el historial de encuestas guardadas. En esta pantalla se listan las encuestas agrupadas por fecha, mostrando un resumen de las respuestas registradas.

* **Conexión vista-código:** Se conectaron los botones de la interfaz con sus acciones en Java, permitiendo guardar la encuesta, limpiar los campos y navegar hacia la pantalla de historial mediante `Intent`.

* **Consulta de datos:** Se utilizaron consultas con `SQLiteDatabase` y `Cursor` para leer las preguntas desde la base de datos y recuperar las respuestas guardadas junto con su fecha de registro.

* **Diseño:** Se mantuvo una interfaz sencilla y clara para que el usuario pueda responder la encuesta de forma directa, con campos generados dinámicamente y una pantalla separada para visualizar el historial.

* **Pruebas:** Se verificó que las preguntas se carguen desde SQLite, que no estén escritas en el XML, que las respuestas se guarden correctamente, que todas las respuestas de una misma encuesta tengan la misma fecha y que el historial muestre la información registrada.
