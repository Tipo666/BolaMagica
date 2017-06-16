package com.example.tipo666.bolamagica;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class cicloJuego extends AppCompatActivity implements SensorEventListener{


    public static final int DURACION_DESVANECIMIENTO = 1500;
    public static final int INICIO_INTERVALO = 1000;
    public static final int TIEMPO_VIBRACION = 250;
    public static final int LIMITE = 240;
    public static final int CONTADOR_MOVIMIENTOS = 2;
    private static Random RANDOM = new Random();
    private Vibrator vibrator;
    private SensorManager sensorManager;
    private Sensor sensor;
    private float ultimoX, ultimoY, ultimoZ;
    private int shakeCount = 0;
    private TextView mensaje;
    private ImageView bola;
    private Animation animacionBola;
    private ArrayList<String> respuestas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ciclo_juego);
        bola = (ImageView) findViewById(R.id.ball);
        mensaje = (TextView) findViewById(R.id.msgTv);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        animacionBola = AnimationUtils.loadAnimation(this, R.anim.shake);
        respuestas = cargarRespuestas();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.shake:
                showAnswer(getAnswer(), true);
                return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        showAnswer(getString(R.string.shake_me), false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            if(isShakeEnough(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2])){
                showAnswer(getAnswer(), false);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private boolean isShakeEnough(float x, float y, float z){
        double force = 0d;
        force += Math.pow((x - ultimoX) / SensorManager.GRAVITY_EARTH, 2.0);
        force += Math.pow((y - ultimoY) / SensorManager.GRAVITY_EARTH, 2.0);
        force += Math.pow((z - ultimoZ) / SensorManager.GRAVITY_EARTH, 2.0);

        force = Math.sqrt(force);

        ultimoX = x;
        ultimoY = y;
        ultimoZ = z;

        if(force > ((float) LIMITE /100f)){
            bola.startAnimation(animacionBola);
            shakeCount++;

            if(shakeCount > CONTADOR_MOVIMIENTOS){
                shakeCount = 0;
                ultimoX = 0;
                ultimoY = 0;
                ultimoZ = 0;
                return true;
            }
        }
        return false;
    }

    private void showAnswer(String answer, boolean withAnim){
        if(withAnim){
            bola.startAnimation(animacionBola);
        }

        mensaje.setVisibility(View.INVISIBLE);
        mensaje.setText(answer);
        AlphaAnimation animation = new AlphaAnimation(0, 1);
        animation.setStartOffset(INICIO_INTERVALO);
        mensaje.setVisibility(View.VISIBLE);
        animation.setDuration(DURACION_DESVANECIMIENTO);

        mensaje.startAnimation(animation);
        vibrator.vibrate(TIEMPO_VIBRACION);
    }

    private String getAnswer() {
        int randomInt = RANDOM.nextInt(respuestas.size());
        return respuestas.get(randomInt);
    }

    public  ArrayList<String> cargarRespuestas(){
        ArrayList<String> list = new ArrayList<>();
        String[] tab = getResources().getStringArray(R.array.answers);

        if(tab.length > 0){
            Collections.addAll(list, tab);
        }

        return list;


    }
}
