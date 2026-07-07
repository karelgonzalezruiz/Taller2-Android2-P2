package com.example.taller1_p2;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LinearLayout contenedorPreguntas;
    private Button btnGuardarEncuesta;
    private Button btnVerHistorial;
    private Button btnVolverInicio;
    private SurveyDbHelper dbHelper;

    private ArrayList<Pregunta> preguntas;
    private final HashMap<Integer, View> mapaRespuestas = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contenedorPreguntas = findViewById(R.id.contenedorPreguntas);
        btnGuardarEncuesta = findViewById(R.id.btnGuardarEncuesta);
        btnVerHistorial = findViewById(R.id.btnVerHistorial);
        btnVolverInicio = findViewById(R.id.btnVolverInicio);
        dbHelper = new SurveyDbHelper(this);

        cargarPreguntasDinamicas();

        btnGuardarEncuesta.setOnClickListener(v -> guardarEncuesta());
        btnVerHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistorialActivity.class);
            startActivity(intent);
        });
        btnVolverInicio.setOnClickListener(v -> volverInicio());
    }

    private void cargarPreguntasDinamicas() {
        preguntas = dbHelper.obtenerPreguntas();
        mapaRespuestas.clear();
        contenedorPreguntas.removeAllViews();

        if (preguntas.isEmpty()) {
            TextView tvSinPreguntas = new TextView(this);
            tvSinPreguntas.setText("No hay preguntas registradas en la base de datos.");
            tvSinPreguntas.setTextSize(16);
            tvSinPreguntas.setPadding(0, 24, 0, 24);
            contenedorPreguntas.addView(tvSinPreguntas);
            btnGuardarEncuesta.setEnabled(false);
            return;
        }

        for (Pregunta pregunta : preguntas) {
            LinearLayout tarjetaPregunta = new LinearLayout(this);
            tarjetaPregunta.setOrientation(LinearLayout.VERTICAL);
            tarjetaPregunta.setBackgroundResource(R.drawable.bg_question_card);
            tarjetaPregunta.setGravity(Gravity.CENTER_HORIZONTAL);
            tarjetaPregunta.setPadding(dp(14), dp(12), dp(14), dp(14));

            LinearLayout.LayoutParams paramsTarjeta = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            paramsTarjeta.setMargins(0, 0, 0, dp(12));
            tarjetaPregunta.setLayoutParams(paramsTarjeta);

            TextView tvPregunta = new TextView(this);
            tvPregunta.setText(pregunta.getTextoPregunta());
            tvPregunta.setTextColor(getColor(R.color.text_primary));
            tvPregunta.setTextSize(17);
            tvPregunta.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            tvPregunta.setGravity(Gravity.CENTER);
            tvPregunta.setPadding(0, 0, 0, dp(10));

            View vistaRespuesta;
            if (esDatoPersonal(pregunta.getIdPregunta())) {
                vistaRespuesta = crearCampoTexto(pregunta.getIdPregunta());
            } else {
                vistaRespuesta = crearGrupoOpciones(pregunta.getIdPregunta());
            }

            tarjetaPregunta.addView(tvPregunta);
            tarjetaPregunta.addView(vistaRespuesta);
            contenedorPreguntas.addView(tarjetaPregunta);
            mapaRespuestas.put(pregunta.getIdPregunta(), vistaRespuesta);
        }
    }

    private void guardarEncuesta() {
        for (Pregunta pregunta : preguntas) {
            View vistaRespuesta = mapaRespuestas.get(pregunta.getIdPregunta());
            String respuesta = obtenerRespuesta(vistaRespuesta);

            if (respuesta.isEmpty()) {
                Toast.makeText(this, "Debe responder todas las preguntas", Toast.LENGTH_SHORT).show();
                if (vistaRespuesta != null) {
                    vistaRespuesta.requestFocus();
                }
                return;
            }
        }

        String fechaActual = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
        ).format(new Date());

        for (Pregunta pregunta : preguntas) {
            View vistaRespuesta = mapaRespuestas.get(pregunta.getIdPregunta());
            String respuesta = obtenerRespuesta(vistaRespuesta);

            dbHelper.guardarRespuesta(
                    pregunta.getIdPregunta(),
                    respuesta,
                    fechaActual
            );
        }

        Toast.makeText(this, "Encuesta guardada correctamente", Toast.LENGTH_SHORT).show();
        limpiarCampos();
        Intent intent = new Intent(MainActivity.this, CompletedActivity.class);
        startActivity(intent);
        finish();
    }

    private void limpiarCampos() {
        for (View vistaRespuesta : mapaRespuestas.values()) {
            if (vistaRespuesta instanceof EditText) {
                ((EditText) vistaRespuesta).setText("");
            } else if (vistaRespuesta instanceof RadioGroup) {
                ((RadioGroup) vistaRespuesta).clearCheck();
            }
        }
    }

    private boolean esDatoPersonal(int idPregunta) {
        return idPregunta == 1 || idPregunta == 2;
    }

    private EditText crearCampoTexto(int idPregunta) {
        EditText etRespuesta = new EditText(this);
        etRespuesta.setHint(idPregunta == 1 ? "Escriba su nombre" : "Escriba sus apellidos");
        etRespuesta.setTextColor(getColor(R.color.text_primary));
        etRespuesta.setHintTextColor(getColor(R.color.text_secondary));
        etRespuesta.setBackgroundResource(R.drawable.bg_answer_input);
        etRespuesta.setPadding(dp(12), dp(8), dp(12), dp(8));
        etRespuesta.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        etRespuesta.setSingleLine(true);
        etRespuesta.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return etRespuesta;
    }

    private RadioGroup crearGrupoOpciones(int idPregunta) {
        RadioGroup grupoOpciones = new RadioGroup(this);
        grupoOpciones.setOrientation(RadioGroup.VERTICAL);
        grupoOpciones.setPadding(0, dp(2), 0, 0);
        grupoOpciones.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        for (String opcion : obtenerOpciones(idPregunta)) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(View.generateViewId());
            radioButton.setText(opcion);
            radioButton.setTextColor(getColor(R.color.text_primary));
            radioButton.setTextSize(15);
            radioButton.setPadding(dp(6), dp(6), dp(6), dp(6));
            radioButton.setButtonTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.brand_primary)));
            grupoOpciones.addView(radioButton);
        }

        return grupoOpciones;
    }

    private String[] obtenerOpciones(int idPregunta) {
        if (idPregunta == 4) {
            return new String[]{"Muy adecuado", "Adecuado", "Poco adecuado", "Nada adecuado"};
        }

        if (idPregunta == 7) {
            return new String[]{"Si", "Tal vez", "No"};
        }

        return new String[]{"Excelente", "Bueno", "Regular", "Malo"};
    }

    private String obtenerRespuesta(View vistaRespuesta) {
        if (vistaRespuesta instanceof EditText) {
            return ((EditText) vistaRespuesta).getText().toString().trim();
        }

        if (vistaRespuesta instanceof RadioGroup) {
            RadioGroup grupoOpciones = (RadioGroup) vistaRespuesta;
            int idSeleccionado = grupoOpciones.getCheckedRadioButtonId();
            if (idSeleccionado == -1) {
                return "";
            }

            RadioButton radioButton = grupoOpciones.findViewById(idSeleccionado);
            return radioButton.getText().toString();
        }

        return "";
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void volverInicio() {
        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
