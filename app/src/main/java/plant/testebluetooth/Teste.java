package plant.testebluetooth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

public class Teste extends BluetoothCheckActivity implements ComunicationController.ControleListener {
    // Precisa utilizar o mesmo UUID que o servidor utilizou para abrir o socket servidor
    //UUID padrão para bluetooth é esse abaixo
    protected static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected BluetoothDevice device;
    protected ComunicationController controle;
    private boolean conectou = false;

    private ScrollView scrollView;
    private Button buttonEnviar;
    private Button buttonReceber;
    private Button buttonParar;
    private Button buttonReconectar;
    private Button buttonLimpar;

    private TextView tv_msg;
    private ProgressBar progressBar;

    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teste);

        activity = this;

        scrollView = findViewById(R.id.scrollView);
        buttonEnviar = findViewById(R.id.buttonEnviar);
        buttonReceber = findViewById(R.id.buttonReceber);
        buttonParar = findViewById(R.id.buttonParar);
        buttonReconectar = findViewById(R.id.buttonReconectar);
        buttonLimpar = findViewById(R.id.buttonLimpar);

        tv_msg = findViewById(R.id.tv_msg);
        progressBar = findViewById(R.id.progressBar);

        // Device selecionado na lista
        device = getIntent().getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        conectar();
    }

    private void conectar(){
        new AsyncConnection(this).execute();
    }

    @Override
    public void onMessageReceived(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(getBaseContext() != null){
                    MediaPlayer.create(getBaseContext(), R.raw.beep).start();
                    tv_msg.append(msg);
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            }
        });
    }

    @Override
    public void onConnectionError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(getBaseContext() != null) {
                    Toast.makeText(getBaseContext(), "Conexão Encerrada", Toast.LENGTH_SHORT).show();
                    buttonEnviar.setEnabled(false);
                    buttonReceber.setEnabled(false);
                    buttonParar.setEnabled(false);
                    buttonReconectar.setEnabled(true);
                    buttonLimpar.setEnabled(false);
                }
            }
        });
    }

    private void error(final String msg, final Exception e) {
        Log.e("ERRO_CONEXÃO BLUETOOTH", e.getMessage(), e);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onClickEnviar(View v) {
        EditText editTextEnviar = (EditText) findViewById(R.id.editTextEnviar);
        String msg = editTextEnviar.getText().toString();
        try{
            controle.sendMessage(msg);
            editTextEnviar.setText("");
        } catch (Exception e) {
            error("Erro ao enviar dados: " + e.getMessage(), e);
        }

        Toast.makeText(this, "Enviar dados", Toast.LENGTH_SHORT).show();

    }

    public void onClickLimpar(View v){
        tv_msg.setText("");
    }


    public void onClickReceber(View v) {
        try {
            v.setEnabled(false);
            controle.receiveMessage();
        } catch (Exception e) {
            error("Erro ao receber dados: " + e.getMessage(), e);
            v.setEnabled(true);
        }
        Toast.makeText(this, "Recebendo dados", Toast.LENGTH_SHORT).show();
    }

    public void onClickParar(View v) {
        try {
            controle.stop();
            btfAdapter.disable();
            Toast.makeText(this, "Conexão Encerrada, Bluetooth desligado", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            error("Erro ao encerrar conexão: " + e.getMessage(), e);
        }

    }

    public void onClickReconectar(View v){
        conectar();
    }

    private class AsyncConnection extends AsyncTask<Void,Void,Void>{
        private ProgressDialog progressDialog;
        private Teste activity;
        private Exception e;

        public AsyncConnection(Teste activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(verifica()){
                runOnUiThread(() -> {
                    progressDialog = ProgressDialog.show(Teste.this.activity, getString(R.string.app_name), "Estabelecendo conexão, aguarde...", true, false);
                    progressBar.setVisibility(View.VISIBLE);
                    buttonEnviar.setEnabled(false);
                    buttonReceber.setEnabled(false);
                    buttonParar.setEnabled(false);
                    buttonReconectar.setEnabled(false);
                    buttonLimpar.setEnabled(false);
                    conectou = false;
                });
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(verifica()){
                try {
                    // Faz a conexão se abriu no modo chat cliente
                    if (device != null) {

                        // Faz a conexão utilizando o mesmo UUID que o servidor utilizou
                        UUID uuid = device.getUuids()[0].getUuid();
                        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);

                        btfAdapter.cancelDiscovery();
                        socket.connect();
                        //conectou = true;

                        // Inicia o controlador do controle
                        controle = new ComunicationController(socket, activity);
                        conectou = true;

                    }
                } catch (Exception e) {
                    conectou = false;
                    if(progressDialog != null) progressDialog.dismiss();
                    this.e = e;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(verifica()){
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    progressDialog.dismiss();
                    if(conectou) {
                        Toast.makeText(activity, "Conectado", Toast.LENGTH_SHORT).show();
                        buttonEnviar.setEnabled(true);
                        buttonReceber.setEnabled(true);
                        buttonParar.setEnabled(true);
                        buttonReconectar.setEnabled(false);
                        buttonLimpar.setEnabled(true);
                        onClickReceber(buttonReceber);
                    }
                    else {
                        buttonEnviar.setEnabled(false);
                        buttonReceber.setEnabled(false);
                        buttonParar.setEnabled(false);
                        buttonReconectar.setEnabled(true);
                        buttonLimpar.setEnabled(false);
                        if(e != null)
                            error("Erro ao conectar: " + e.getMessage(), e);
                    }
                });
            }
        }

        private boolean verifica(){
            return getBaseContext() != null
                    && Teste.this.activity != null
                    && activity != null
                    && buttonEnviar != null
                    && buttonReceber != null
                    && buttonParar != null
                    && buttonReconectar != null
                    && buttonLimpar != null
                    && progressBar != null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            controle.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
