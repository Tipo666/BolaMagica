package com.example.tipo666.bolamagica;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
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

    //Constantes para manejar giroscopio y vibracion
    public static final int DURACION_DESVANECIMIENTO = 1500;
    public static final int INICIO_INTERVALO = 1000;
    public static final int TIEMPO_VIBRACION = 250;
    public static final int LIMITE = 240;
    public static final int CONTADOR_MOVIMIENTOS = 2;


    private static Random RANDOM = new Random();
    private Vibrator vibrator;
    private SensorManager sensorManager;
    private Sensor sensor;
    //Variables para medir los movimientos en el espacio
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

        //Se obtiene el servicio de sistema del vibrador del celular
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Se obtiene el servicio de sistema del sensor del celular
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Se obtiene el servicio del acelerometro del celular
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //La animacion de la bola se carga con el XML en Anim
        animacionBola = AnimationUtils.loadAnimation(this, R.anim.shake);

        //Se recogen las respuestas desde el XML Strings
        respuestas = cargarRespuestas();
    }

    //Se crea el menu de opciones para que siempre se muestre
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //En caso de que el boton se presione agita la bola y muestra la respuestas
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.shake:
                mostrarRespuestas(sacarRespuesta(), true);
                return true;
        }
        return false;
    }

    //Cuando se retorna de alguna pausa o de alguna otra ventana se muestra el texto por defecto
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        mostrarRespuestas(getString(R.string.shake_me), false);
    }

    //Se activa cuando se deja la aplicacion a un segundo plano
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    //Si el sensor cambia y detecta el acelerometro, inicia a capturar los valores
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            //En el plano espacial se toman los valores de X, Y & Z para emular la agitacin
            if(isShakeEnough(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2])){
                mostrarRespuestas(sacarRespuesta(), false);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //Si ya se detecto que hay suficiente movimiento, retorna verdadero y por ende se muestra una respuesta
    private boolean isShakeEnough(float x, float y, float z){
        double fuerza = 0d;
        fuerza += Math.pow((x - ultimoX) / SensorManager.GRAVITY_EARTH, 2.0);
        fuerza += Math.pow((y - ultimoY) / SensorManager.GRAVITY_EARTH, 2.0);
        fuerza += Math.pow((z - ultimoZ) / SensorManager.GRAVITY_EARTH, 2.0);

        fuerza = Math.sqrt(fuerza);

        ultimoX = x;
        ultimoY = y;
        ultimoZ = z;

        //Si la fuerza con que se agito el celular es mayor al previsto, indica que el movimiento es valido y se inicia la animacion
        if(fuerza > ((float) LIMITE /100f)){
            bola.startAnimation(animacionBola);
            shakeCount++;
            //Si hay mas de dos movimientos en el plano, se reinicia el conteo y se muestra la animacion
            if(shakeCount > CONTADOR_MOVIMIENTOS){
                shakeCount = 0;
                ultimoX = 0;
                ultimoY = 0;
                ultimoZ = 0;
                return true;
            }
        }
        //Si no se cumple esto, la bola no muestra animacion
        return false;
    }

    private void mostrarRespuestas(String respuestas, boolean conAnimacion){
        //Si desde el metodo agitar se cumplio la condicion se procede a sacar la respuestas
        if(conAnimacion){
            bola.startAnimation(animacionBola);
        }

        //El mensaje actual se cambia a invisible
        mensaje.setVisibility(View.INVISIBLE);

        //Se cambia el texto a una respuesta actual
        mensaje.setText(respuestas);


        AlphaAnimation animation = new AlphaAnimation(0, 1);

        //Desde aqui inicia el movimiento de la bola
        animation.setStartOffset(INICIO_INTERVALO);

        //Una vez que se agita la animacion, se muestra el texto
        mensaje.setVisibility(View.VISIBLE);

        //Con un DELAY de tantos segundos
        animation.setDuration(DURACION_DESVANECIMIENTO);

        //El mensaje inicia su animacion
        mensaje.startAnimation(animation);

        //Se elige el tiempo que el celular vibrara
        vibrator.vibrate(TIEMPO_VIBRACION);
    }

    //Se toma una respuesta al azar desde el arreglo de Strings
    private String sacarRespuesta() {
        int randomInt = RANDOM.nextInt(respuestas.size());
        return respuestas.get(randomInt);
    }

    //Se crea una lista de respuestas en Java desde el Source de las respuestas
    public  ArrayList<String> cargarRespuestas(){
        ArrayList<String> list = new ArrayList<>();
        String[] tab = getResources().getStringArray(R.array.answers);

        if(tab.length > 0){
            Collections.addAll(list, tab);
        }

        return list;


    }
}
