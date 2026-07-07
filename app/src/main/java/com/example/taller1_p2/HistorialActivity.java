package com.example.taller1_p2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class HistorialActivity extends AppCompatActivity {


    private ListView listViewHistorial;
    private Button btnVolverEncuesta;
    private Button btnVolverInicioHistorial;
    private SurveyDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        listViewHistorial = findViewById(R.id.listViewHistorial);
        btnVolverEncuesta = findViewById(R.id.btnVolverEncuesta);
        btnVolverInicioHistorial = findViewById(R.id.btnVolverInicioHistorial);
        dbHelper = new SurveyDbHelper(this);

        ArrayList<String> historial = dbHelper.obtenerHistorial();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_historial, historial) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = getLayoutInflater().inflate(R.layout.item_historial, parent, false);
                }

                TextView tvHistorialItem = view.findViewById(R.id.tvHistorialItem);
                tvHistorialItem.setText(getItem(position));
                return view;
            }
        };

        listViewHistorial.setAdapter(adapter);
        btnVolverEncuesta.setOnClickListener(v -> {
            Intent intent = new Intent(HistorialActivity.this, MainActivity.class);
            startActivity(intent);
        });
        btnVolverInicioHistorial.setOnClickListener(v -> volverInicio());
    }

    private void volverInicio() {
        Intent intent = new Intent(HistorialActivity.this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
