# App de Encuestas Offline — Especificación para Android Studio

## 1. Descripción general del proyecto

La aplicación **App de Encuestas Offline** es una app móvil Android que permite aplicar encuestas de satisfacción sin depender de conexión a internet.

La característica principal del proyecto es que las preguntas **no deben estar escritas directamente en el XML** ni creadas manualmente desde la interfaz. En su lugar, las preguntas deben estar guardadas en una base de datos local **SQLite** y la pantalla de encuesta debe construirse dinámicamente según las preguntas recuperadas desde la base.

---

## 2. Objetivo general

Desarrollar una aplicación Android que gestione el ciclo completo de una encuesta:

1. Crear una base de datos local SQLite.
2. Insertar automáticamente las preguntas base en la tabla `preguntas`.
3. Leer las preguntas desde SQLite.
4. Generar dinámicamente la interfaz de la encuesta.
5. Guardar las respuestas ingresadas por el usuario.
6. Mostrar un historial de encuestas respondidas.

---

## 3. Tecnología requerida

El proyecto debe desarrollarse en:

- **Android Studio**
- **Java**
- **XML**
- **SQLite**
- **SQLiteOpenHelper**
- **RecyclerView o ListView** para el historial

Para este ejercicio se recomienda usar **Java**, ya que el `Contract` proporcionado está escrito en Java.

---

## 4. Regla principal del ejercicio

La regla más importante del proyecto es:

> Las preguntas de la encuesta no deben estar hardcodeadas en el archivo XML de la interfaz.

Esto significa que no se deben escribir preguntas así dentro del XML:

```xml
<TextView
    android:text="¿Cómo califica la atención?" />
```

Tampoco se deben insertar desde botones o formularios de la interfaz.

Las preguntas deben insertarse automáticamente en la base de datos dentro del método `onCreate()` de la clase que extiende de `SQLiteOpenHelper`.

---

## 5. Estructura recomendada del proyecto

Una estructura clara para el proyecto sería la siguiente:

```text
com.example.encuestasbd
│
├── MainActivity.java
├── HistorialActivity.java
│
├── data
│   ├── SurveyContract.java
│   └── SurveyDbHelper.java
│
├── model
│   ├── Pregunta.java
│   └── EncuestaResumen.java
│
└── adapter
    └── HistorialAdapter.java
```

También puede hacerse todo dentro del paquete principal si el proyecto es pequeño, pero separar por carpetas ayuda a mantener el código más ordenado.

---

## 6. Base de datos SQLite

La aplicación usa dos tablas principales:

### 6.1. Tabla `preguntas`

Esta tabla almacena las preguntas que aparecerán en la encuesta.

| Campo | Tipo | Descripción |
|---|---|---|
| `_ID` | INTEGER | Identificador interno autoincremental |
| `id_pregunta` | INTEGER | Identificador lógico de la pregunta |
| `texto_pregunta` | TEXT | Texto de la pregunta |

Ejemplo de datos:

| id_pregunta | texto_pregunta |
|---|---|
| 1 | ¿Cómo califica la atención recibida? |
| 2 | ¿Recomendaría nuestro servicio? |
| 3 | ¿El tiempo de espera fue adecuado? |
| 4 | ¿Qué sugerencia desea dejar? |

---

### 6.2. Tabla `respuestas`

Esta tabla almacena las respuestas dadas por los usuarios.

| Campo | Tipo | Descripción |
|---|---|---|
| `_ID` | INTEGER | Identificador interno autoincremental |
| `id_pregunta_fk` | INTEGER | Pregunta a la que pertenece la respuesta |
| `respuesta_usuario` | TEXT | Respuesta escrita por el usuario |
| `fecha_registro` | DATETIME | Fecha y hora en que se guardó la encuesta |

Una encuesta completa se guarda como varias filas en esta tabla: una fila por cada pregunta respondida.

Ejemplo:

| id_pregunta_fk | respuesta_usuario | fecha_registro |
|---|---|---|
| 1 | Excelente | 2026-07-07 15:30:00 |
| 2 | Sí | 2026-07-07 15:30:00 |
| 3 | Sí | 2026-07-07 15:30:00 |
| 4 | Mejorar la señalización | 2026-07-07 15:30:00 |

Todas las respuestas de una misma encuesta deben guardar la misma fecha y hora para poder agruparlas luego en el historial.

---

## 7. Contract de la base de datos

El `Contract` define los nombres de tablas, columnas y sentencias SQL.

Archivo sugerido:

```text
SurveyContract.java
```

Código base:

```java
package com.example.encuestasbd.data;

import android.provider.BaseColumns;

public final class SurveyContract {

    private SurveyContract() {}

    public static class PreguntasEntry implements BaseColumns {
        public static final String TABLE_NAME = "preguntas";
        public static final String COLUMN_ID_PREG = "id_pregunta";
        public static final String COLUMN_TEXTO = "texto_pregunta";
    }

    public static class RespuestasEntry implements BaseColumns {
        public static final String TABLE_NAME = "respuestas";
        public static final String COLUMN_ID_PREG_FK = "id_pregunta_fk";
        public static final String COLUMN_RESPUESTA = "respuesta_usuario";
        public static final String COLUMN_FECHA = "fecha_registro";
    }

    public static final String SQL_CREATE_PREGUNTAS =
            "CREATE TABLE " + PreguntasEntry.TABLE_NAME + " (" +
                    PreguntasEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PreguntasEntry.COLUMN_ID_PREG + " INTEGER UNIQUE, " +
                    PreguntasEntry.COLUMN_TEXTO + " TEXT NOT NULL)";

    public static final String SQL_CREATE_RESPUESTAS =
            "CREATE TABLE " + RespuestasEntry.TABLE_NAME + " (" +
                    RespuestasEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    RespuestasEntry.COLUMN_ID_PREG_FK + " INTEGER, " +
                    RespuestasEntry.COLUMN_RESPUESTA + " TEXT, " +
                    RespuestasEntry.COLUMN_FECHA + " DATETIME DEFAULT CURRENT_TIMESTAMP)";

    public static final String SQL_DELETE_PREGUNTAS =
            "DROP TABLE IF EXISTS " + PreguntasEntry.TABLE_NAME;

    public static final String SQL_DELETE_RESPUESTAS =
            "DROP TABLE IF EXISTS " + RespuestasEntry.TABLE_NAME;
}
```

---

## 8. Helper de la base de datos

El helper debe extender de `SQLiteOpenHelper`.

Archivo sugerido:

```text
SurveyDbHelper.java
```

Responsabilidades principales:

1. Crear las tablas.
2. Insertar las preguntas iniciales.
3. Consultar preguntas.
4. Guardar respuestas.
5. Consultar historial.

---

### 8.1. Creación de tablas e inserción automática de preguntas

```java
@Override
public void onCreate(SQLiteDatabase db) {
    db.execSQL(SurveyContract.SQL_CREATE_PREGUNTAS);
    db.execSQL(SurveyContract.SQL_CREATE_RESPUESTAS);

    db.execSQL("INSERT INTO " + SurveyContract.PreguntasEntry.TABLE_NAME +
            " (" + SurveyContract.PreguntasEntry.COLUMN_ID_PREG + ", " +
            SurveyContract.PreguntasEntry.COLUMN_TEXTO + ") VALUES " +
            "(1, '¿Cómo califica la atención recibida?')");

    db.execSQL("INSERT INTO " + SurveyContract.PreguntasEntry.TABLE_NAME +
            " (" + SurveyContract.PreguntasEntry.COLUMN_ID_PREG + ", " +
            SurveyContract.PreguntasEntry.COLUMN_TEXTO + ") VALUES " +
            "(2, '¿Recomendaría nuestro servicio?')");

    db.execSQL("INSERT INTO " + SurveyContract.PreguntasEntry.TABLE_NAME +
            " (" + SurveyContract.PreguntasEntry.COLUMN_ID_PREG + ", " +
            SurveyContract.PreguntasEntry.COLUMN_TEXTO + ") VALUES " +
            "(3, '¿El tiempo de espera fue adecuado?')");

    db.execSQL("INSERT INTO " + SurveyContract.PreguntasEntry.TABLE_NAME +
            " (" + SurveyContract.PreguntasEntry.COLUMN_ID_PREG + ", " +
            SurveyContract.PreguntasEntry.COLUMN_TEXTO + ") VALUES " +
            "(4, '¿Qué sugerencia desea dejar?')");
}
```

Punto importante:

> Estas preguntas se insertan solo cuando la base de datos se crea por primera vez.

Si ya existe la base de datos en el emulador, el método `onCreate()` no se ejecuta otra vez. Para probar cambios en las preguntas, se puede desinstalar la app del emulador o aumentar la versión de la base de datos.

---

### 8.2. Método `onUpgrade`

```java
@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL(SurveyContract.SQL_DELETE_RESPUESTAS);
    db.execSQL(SurveyContract.SQL_DELETE_PREGUNTAS);
    onCreate(db);
}
```

Este método sirve para recrear las tablas cuando cambia la versión de la base de datos.

---

## 9. Modelo `Pregunta`

Para manejar las preguntas de forma ordenada, se recomienda crear una clase modelo.

Archivo sugerido:

```text
Pregunta.java
```

```java
package com.example.encuestasbd.model;

public class Pregunta {

    private int idPregunta;
    private String textoPregunta;

    public Pregunta(int idPregunta, String textoPregunta) {
        this.idPregunta = idPregunta;
        this.textoPregunta = textoPregunta;
    }

    public int getIdPregunta() {
        return idPregunta;
    }

    public String getTextoPregunta() {
        return textoPregunta;
    }
}
```

---

## 10. Consulta de preguntas desde SQLite

El helper debe tener un método para obtener todas las preguntas.

```java
public ArrayList<Pregunta> obtenerPreguntas() {
    ArrayList<Pregunta> lista = new ArrayList<>();

    SQLiteDatabase db = getReadableDatabase();

    Cursor cursor = db.query(
            SurveyContract.PreguntasEntry.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            SurveyContract.PreguntasEntry.COLUMN_ID_PREG + " ASC"
    );

    while (cursor.moveToNext()) {
        int idPregunta = cursor.getInt(
                cursor.getColumnIndexOrThrow(SurveyContract.PreguntasEntry.COLUMN_ID_PREG)
        );

        String texto = cursor.getString(
                cursor.getColumnIndexOrThrow(SurveyContract.PreguntasEntry.COLUMN_TEXTO)
        );

        lista.add(new Pregunta(idPregunta, texto));
    }

    cursor.close();
    db.close();

    return lista;
}
```

Este método será usado por la pantalla principal para saber cuántas preguntas debe mostrar.

---

## 11. Pantalla principal: encuesta dinámica

La pantalla principal debe mostrar las preguntas recuperadas desde SQLite.

La interfaz no debe contener las preguntas directamente. El XML solo debe tener un contenedor vacío donde se agregarán los componentes visuales desde Java.

---

### 11.1. XML recomendado para `activity_main.xml`

```xml
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">

        <LinearLayout
            android:id="@+id/contenedorPreguntas"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical" />

    </ScrollView>

    <Button
        android:id="@+id/btnGuardarEncuesta"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Guardar Encuesta" />

    <Button
        android:id="@+id/btnVerHistorial"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Ver Historial" />

</LinearLayout>
```

Este XML no tiene preguntas escritas. Solo tiene:

- Un contenedor.
- Un botón para guardar.
- Un botón para ver historial.

---

## 12. Generación dinámica de preguntas

En `MainActivity.java`, se deben crear los `TextView` y `EditText` desde Java.

La lógica sería:

1. Consultar la lista de preguntas.
2. Recorrer la lista.
3. Por cada pregunta:
   - Crear un `TextView`.
   - Crear un `EditText`.
   - Agregarlos al contenedor.
4. Guardar una relación entre el ID de la pregunta y el `EditText`.

Ejemplo:

```java
private LinearLayout contenedorPreguntas;
private Button btnGuardarEncuesta;
private Button btnVerHistorial;
private SurveyDbHelper dbHelper;

private ArrayList<Pregunta> preguntas;
private HashMap<Integer, EditText> mapaRespuestas = new HashMap<>();
```

Código recomendado:

```java
private void cargarPreguntasDinamicas() {
    preguntas = dbHelper.obtenerPreguntas();

    for (Pregunta pregunta : preguntas) {
        TextView tvPregunta = new TextView(this);
        tvPregunta.setText(pregunta.getTextoPregunta());
        tvPregunta.setTextSize(18);
        tvPregunta.setPadding(0, 16, 0, 8);

        EditText etRespuesta = new EditText(this);
        etRespuesta.setHint("Escriba su respuesta");

        contenedorPreguntas.addView(tvPregunta);
        contenedorPreguntas.addView(etRespuesta);

        mapaRespuestas.put(pregunta.getIdPregunta(), etRespuesta);
    }
}
```

La variable `mapaRespuestas` permite saber qué respuesta pertenece a cada pregunta.

---

## 13. Guardado de respuestas

Cuando el usuario presiona el botón **Guardar Encuesta**, la app debe:

1. Leer todos los `EditText`.
2. Validar que no estén vacíos.
3. Crear una fecha única para toda la encuesta.
4. Insertar cada respuesta en la tabla `respuestas`.
5. Mostrar un mensaje de confirmación.
6. Limpiar los campos.

---

### 13.1. Método para guardar respuestas en el helper

```java
public void guardarRespuesta(int idPregunta, String respuesta, String fecha) {
    SQLiteDatabase db = getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(SurveyContract.RespuestasEntry.COLUMN_ID_PREG_FK, idPregunta);
    values.put(SurveyContract.RespuestasEntry.COLUMN_RESPUESTA, respuesta);
    values.put(SurveyContract.RespuestasEntry.COLUMN_FECHA, fecha);

    db.insert(SurveyContract.RespuestasEntry.TABLE_NAME, null, values);

    db.close();
}
```

---

### 13.2. Guardar encuesta completa desde `MainActivity`

```java
private void guardarEncuesta() {
    String fechaActual = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
    ).format(new Date());

    for (Pregunta pregunta : preguntas) {
        EditText editText = mapaRespuestas.get(pregunta.getIdPregunta());
        String respuesta = editText.getText().toString().trim();

        if (respuesta.isEmpty()) {
            Toast.makeText(this, "Debe responder todas las preguntas", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    for (Pregunta pregunta : preguntas) {
        EditText editText = mapaRespuestas.get(pregunta.getIdPregunta());
        String respuesta = editText.getText().toString().trim();

        dbHelper.guardarRespuesta(
                pregunta.getIdPregunta(),
                respuesta,
                fechaActual
        );
    }

    Toast.makeText(this, "Encuesta guardada correctamente", Toast.LENGTH_SHORT).show();
    limpiarCampos();
}
```

---

### 13.3. Limpiar respuestas

```java
private void limpiarCampos() {
    for (EditText editText : mapaRespuestas.values()) {
        editText.setText("");
    }
}
```

---

## 14. Pantalla de historial

La pantalla de historial debe mostrar todas las encuestas guardadas.

Como el diseño entregado solo tiene la tabla `respuestas`, una forma simple de identificar cada encuesta es agrupar las respuestas por `fecha_registro`.

Por eso es importante que todas las respuestas de una encuesta tengan exactamente la misma fecha y hora.

---

### 14.1. XML recomendado para `activity_historial.xml`

```xml
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Historial de Encuestas"
        android:textSize="22sp"
        android:textStyle="bold"
        android:paddingBottom="12dp" />

    <ListView
        android:id="@+id/listViewHistorial"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</LinearLayout>
```

Para un proyecto básico, `ListView` es suficiente. Si el profesor exige una estructura más profesional, se puede usar `RecyclerView`.

---

## 15. Consulta del historial

El historial debe mostrar:

- Fecha de la encuesta.
- Pregunta respondida.
- Respuesta del usuario.

Una consulta útil sería unir la tabla `respuestas` con la tabla `preguntas`.

```sql
SELECT 
    r.fecha_registro,
    p.texto_pregunta,
    r.respuesta_usuario
FROM respuestas r
INNER JOIN preguntas p
ON r.id_pregunta_fk = p.id_pregunta
ORDER BY r.fecha_registro DESC, p.id_pregunta ASC;
```

Esta consulta permite mostrar algo como:

```text
Encuesta: 2026-07-07 15:30:00
¿Cómo califica la atención recibida?: Excelente
¿Recomendaría nuestro servicio?: Sí
¿El tiempo de espera fue adecuado?: Sí
¿Qué sugerencia desea dejar?: Mejorar la señalización
```

---

## 16. Método recomendado para obtener historial

Una forma sencilla es retornar una lista de textos ya armados para mostrar en el `ListView`.

```java
public ArrayList<String> obtenerHistorial() {
    ArrayList<String> historial = new ArrayList<>();

    SQLiteDatabase db = getReadableDatabase();

    String query =
            "SELECT r." + SurveyContract.RespuestasEntry.COLUMN_FECHA + ", " +
                    "p." + SurveyContract.PreguntasEntry.COLUMN_TEXTO + ", " +
                    "r." + SurveyContract.RespuestasEntry.COLUMN_RESPUESTA +
                    " FROM " + SurveyContract.RespuestasEntry.TABLE_NAME + " r " +
                    " INNER JOIN " + SurveyContract.PreguntasEntry.TABLE_NAME + " p " +
                    " ON r." + SurveyContract.RespuestasEntry.COLUMN_ID_PREG_FK +
                    " = p." + SurveyContract.PreguntasEntry.COLUMN_ID_PREG +
                    " ORDER BY r." + SurveyContract.RespuestasEntry.COLUMN_FECHA + " DESC, " +
                    "p." + SurveyContract.PreguntasEntry.COLUMN_ID_PREG + " ASC";

    Cursor cursor = db.rawQuery(query, null);

    String fechaActual = "";
    StringBuilder bloqueEncuesta = new StringBuilder();

    while (cursor.moveToNext()) {
        String fecha = cursor.getString(0);
        String pregunta = cursor.getString(1);
        String respuesta = cursor.getString(2);

        if (!fecha.equals(fechaActual)) {
            if (bloqueEncuesta.length() > 0) {
                historial.add(bloqueEncuesta.toString());
            }

            fechaActual = fecha;
            bloqueEncuesta = new StringBuilder();
            bloqueEncuesta.append("Encuesta: ").append(fecha).append("\n\n");
        }

        bloqueEncuesta.append(pregunta)
                .append("\nRespuesta: ")
                .append(respuesta)
                .append("\n\n");
    }

    if (bloqueEncuesta.length() > 0) {
        historial.add(bloqueEncuesta.toString());
    }

    cursor.close();
    db.close();

    return historial;
}
```

---

## 17. Lógica de `HistorialActivity`

```java
public class HistorialActivity extends AppCompatActivity {

    private ListView listViewHistorial;
    private SurveyDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        listViewHistorial = findViewById(R.id.listViewHistorial);
        dbHelper = new SurveyDbHelper(this);

        ArrayList<String> historial = dbHelper.obtenerHistorial();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                historial
        );

        listViewHistorial.setAdapter(adapter);
    }
}
```

---

## 18. Navegación entre pantallas

Desde `MainActivity`, el botón **Ver Historial** debe abrir `HistorialActivity`.

```java
btnVerHistorial.setOnClickListener(v -> {
    Intent intent = new Intent(MainActivity.this, HistorialActivity.class);
    startActivity(intent);
});
```

También se debe registrar la actividad en el archivo `AndroidManifest.xml`.

```xml
<activity android:name=".HistorialActivity" />
```

---

## 19. Flujo completo de la aplicación

El flujo esperado es el siguiente:

```text
Inicio de la app
        ↓
Se crea la base de datos si no existe
        ↓
Se insertan preguntas automáticamente en SQLite
        ↓
MainActivity consulta la tabla preguntas
        ↓
La interfaz se genera dinámicamente
        ↓
El usuario responde la encuesta
        ↓
Presiona Guardar Encuesta
        ↓
Las respuestas se guardan en SQLite
        ↓
El usuario abre Historial
        ↓
La app muestra encuestas guardadas agrupadas por fecha
```

---

## 20. Validaciones mínimas

La app debe validar que:

1. Existan preguntas en la base de datos.
2. El usuario responda todas las preguntas.
3. No se guarden respuestas vacías.
4. El historial no se rompa si todavía no hay encuestas guardadas.

Ejemplo para historial vacío:

```java
if (historial.isEmpty()) {
    historial.add("Todavía no hay encuestas guardadas.");
}
```

---

## 21. Consideraciones importantes para el estudiante

### 21.1. Las preguntas no van en XML

El XML solo contiene el contenedor. Las preguntas se crean desde Java después de leer SQLite.

---

### 21.2. Las preguntas no las escribe el usuario

El usuario solo responde. No debe existir una pantalla para agregar preguntas manualmente.

---

### 21.3. SQLite funciona offline

Toda la información queda guardada localmente en el dispositivo. No se necesita internet para responder encuestas ni para ver el historial.

---

### 21.4. El historial depende de la fecha

Como el contrato entregado no tiene una tabla `encuestas`, se usa `fecha_registro` para agrupar respuestas de una misma encuesta.

Una mejora posible sería agregar una tabla `encuestas` con un `id_encuesta`, pero si el profesor pidió usar exactamente el contrato dado, se debe trabajar con las dos tablas actuales.

---

## 22. Mejora opcional si se permite modificar la base de datos

Si el docente permite mejorar el diseño, sería recomendable agregar una tercera tabla:

### Tabla `encuestas`

| Campo | Tipo | Descripción |
|---|---|---|
| `id_encuesta` | INTEGER | Identificador de la encuesta |
| `fecha_registro` | DATETIME | Fecha en la que se respondió |

Y en la tabla `respuestas` agregar:

```text
id_encuesta_fk
```

Esto permitiría agrupar respuestas de forma más correcta.

Sin embargo, para cumplir estrictamente el planteamiento dado, se puede usar solo:

- `preguntas`
- `respuestas`

---

## 23. Checklist de cumplimiento

Antes de entregar, verificar lo siguiente:

- [ ] El proyecto abre correctamente en Android Studio.
- [ ] La app usa SQLite.
- [ ] Existe una clase `SurveyContract`.
- [ ] Existe una clase que extiende de `SQLiteOpenHelper`.
- [ ] Las preguntas se insertan en `onCreate()`.
- [ ] Las preguntas no están hardcodeadas en el XML.
- [ ] La pantalla principal consulta la tabla `preguntas`.
- [ ] La interfaz se genera dinámicamente.
- [ ] Cada pregunta tiene un campo de respuesta.
- [ ] El botón `Guardar Encuesta` guarda todas las respuestas.
- [ ] Las respuestas se guardan en la tabla `respuestas`.
- [ ] El historial muestra encuestas guardadas.
- [ ] El historial muestra fecha y resumen de respuestas.
- [ ] La app funciona sin internet.

---

## 24. Resumen del proyecto en una frase

La aplicación debe crear una encuesta dinámica leyendo preguntas desde SQLite, permitir que el usuario las responda offline y guardar un historial local de las encuestas completadas.
