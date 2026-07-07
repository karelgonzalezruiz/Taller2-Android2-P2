package com.example.taller1_p2;

import android.provider.BaseColumns;

public final class SurveyContract {

    private SurveyContract() {
    }

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
