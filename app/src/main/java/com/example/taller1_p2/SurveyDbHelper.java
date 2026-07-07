package com.example.taller1_p2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class SurveyDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "encuestas_offline.db";
    private static final int DATABASE_VERSION = 5;

    public SurveyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SurveyContract.SQL_CREATE_PREGUNTAS);
        db.execSQL(SurveyContract.SQL_CREATE_RESPUESTAS);

        insertarPregunta(db, 1, "Nombre");
        insertarPregunta(db, 2, "Apellidos");
        insertarPregunta(db, 3, "\u00bfC\u00f3mo califica la atenci\u00f3n recibida?");
        insertarPregunta(db, 4, "\u00bfEl tiempo de espera fue adecuado?");
        insertarPregunta(db, 5, "\u00bfC\u00f3mo califica la claridad de la informaci\u00f3n?");
        insertarPregunta(db, 6, "\u00bfC\u00f3mo califica la amabilidad del personal?");
        insertarPregunta(db, 7, "\u00bfRecomendar\u00eda nuestro servicio?");
    }

    private void insertarPregunta(SQLiteDatabase db, int idPregunta, String textoPregunta) {
        ContentValues values = new ContentValues();
        values.put(SurveyContract.PreguntasEntry.COLUMN_ID_PREG, idPregunta);
        values.put(SurveyContract.PreguntasEntry.COLUMN_TEXTO, textoPregunta);
        db.insert(SurveyContract.PreguntasEntry.TABLE_NAME, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SurveyContract.SQL_DELETE_RESPUESTAS);
        db.execSQL(SurveyContract.SQL_DELETE_PREGUNTAS);
        onCreate(db);
    }

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
            String textoPregunta = cursor.getString(
                    cursor.getColumnIndexOrThrow(SurveyContract.PreguntasEntry.COLUMN_TEXTO)
            );

            lista.add(new Pregunta(idPregunta, textoPregunta));
        }

        cursor.close();
        db.close();
        return lista;
    }

    public void guardarRespuesta(int idPregunta, String respuesta, String fecha) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SurveyContract.RespuestasEntry.COLUMN_ID_PREG_FK, idPregunta);
        values.put(SurveyContract.RespuestasEntry.COLUMN_RESPUESTA, respuesta);
        values.put(SurveyContract.RespuestasEntry.COLUMN_FECHA, fecha);

        db.insert(SurveyContract.RespuestasEntry.TABLE_NAME, null, values);
        db.close();
    }

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

        if (historial.isEmpty()) {
            historial.add("Todavia no hay encuestas guardadas.");
        }

        return historial;
    }
}
