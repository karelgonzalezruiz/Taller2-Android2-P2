package com.example.taller1_p2;

public class Pregunta {

    private final int idPregunta;
    private final String textoPregunta;

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
