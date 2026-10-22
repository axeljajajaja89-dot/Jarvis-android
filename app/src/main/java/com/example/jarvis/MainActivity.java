
package com.example.jarvis;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.speech.tts.TextToSpeech;
import android.speech.RecognizerIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import java.util.Locale;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private TextToSpeech voz;
    private TextView estado;
    private static final int VOZ_ID = 100;
    private static final int PERMISO_ID = 101;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(3, 12, 24));
        getWindow().setNavigationBarColor(Color.rgb(3, 12, 24));

        LinearLayout pantalla = new LinearLayout(this);
        pantalla.setOrientation(LinearLayout.VERTICAL);
        pantalla.setGravity(Gravity.CENTER);
        pantalla.setPadding(32, 32, 32, 32);
        pantalla.setBackgroundColor(Color.rgb(3, 12, 24));

        TextView titulo = new TextView(this);
        titulo.setText("J.A.R.V.I.S.");
        titulo.setTextSize(34);
        titulo.setTextColor(Color.rgb(0, 220, 255));
        titulo.setGravity(Gravity.CENTER);

        TextView subtitulo = new TextView(this);
        subtitulo.setText("ASISTENTE PERSONAL");
        subtitulo.setTextColor(Color.WHITE);
        subtitulo.setTextSize(14);
        subtitulo.setGravity(Gravity.CENTER);

        estado = new TextView(this);
        estado.setText("SISTEMA INICIADO\nEsperando instrucciones...");
        estado.setTextColor(Color.rgb(0, 220, 255));
        estado.setTextSize(17);
        estado.setGravity(Gravity.CENTER);
        estado.setPadding(0, 50, 0, 50);

        Button hablar = new Button(this);
        hablar.setText("🎙 HABLAR CON JARVIS");

        hablar.setOnClickListener(v -> escuchar());

        Button saludo = new Button(this);
        saludo.setText("INICIAR ASISTENTE");

        saludo.setOnClickListener(v -> decir(
            "Hola. Soy Jarvis, tu asistente personal. ¿En qué puedo ayudarte?"
        ));

        pantalla.addView(titulo);
        pantalla.addView(subtitulo);
        pantalla.addView(estado);
        pantalla.addView(hablar);
        pantalla.addView(saludo);

        setContentView(pantalla);

        voz = new TextToSpeech(this, resultado -> {
            if (resultado == TextToSpeech.SUCCESS) {
                voz.setLanguage(new Locale("es", "ES"));
                voz.setPitch(0.8f);
                voz.setSpeechRate(0.9f);
                estado.setText("JARVIS ACTIVO\nSistema de voz preparado.");
            }
        });
    }

    private void decir(String texto) {
        estado.setText(texto);
        if (voz != null) {
            voz.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "jarvis");
        }
    }

    private void escuchar() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISO_ID
            );
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-AR");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hablá con JARVIS");

        try {
            startActivityForResult(intent, VOZ_ID);
        } catch (Exception e) {
            estado.setText("No se encontró un servicio de reconocimiento de voz.");
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOZ_ID && resultCode == RESULT_OK
                && data != null) {
            ArrayList<String> resultados =
                data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                );

            if (resultados != null && !resultados.isEmpty()) {
                String texto = resultados.get(0);
                estado.setText("Escuché:\n" + texto);
                decir("Escuché: " + texto);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults);

        if (requestCode == PERMISO_ID && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            escuchar();
        } else if (requestCode == PERMISO_ID) {
            estado.setText("Necesito permiso de micrófono para escucharte.");
        }
    }

    @Override
    protected void onDestroy() {
        if (voz != null) {
            voz.stop();
            voz.shutdown();
        }
        super.onDestroy();
    }
}
