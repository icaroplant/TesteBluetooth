package plant.testebluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Lucas on 24/02/2016.
 */

public class ComunicationController {
    private static final String TAG = "ComunicationController";
    private BluetoothSocket socket;
    private InputStream in;
    private OutputStream out;
    private ControleListener listener;
    private boolean running;

    public interface ControleListener {
        public void onMessageReceived(String msg);
        public void onConnectionError();
    }

    public ComunicationController(BluetoothSocket socket, ControleListener listener) throws IOException {
        this.socket = socket;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
        this.listener = listener;
        this.running = true;
    }
    // Inicia a leitura da InputStream
    public void receiveMessage() throws IOException{
        new Thread(){
            @Override
            public void run() {
                running = true;
                // Faz a leitura
                byte[] bytes = new byte[1024];
                int length;
                // Fica em loop para receber as mensagens
                String msg = "";
                while (running) {
                    try {
                        // Lê a mensagem (fica bloqueado até receber)
                        length = in.read(bytes, 0, 1);
                        String c = new String(bytes, 0, length);
                        msg += c;
                        // Recebeu a mensagem (informa o listener)
                        if(c.equals("\n")){
                            Log.d(TAG,"msg Recebida: " + msg);
                            listener.onMessageReceived(msg);
                            msg = "";
                        }
                    } catch (Exception e) {
                        running = false;
                        Log.e(TAG,"Error: " + e.getMessage(),e);
                        listener.onConnectionError();
                    }
                }
            }
        }.start();
    }

    public void sendMessage(String msg) throws IOException {
        if (out != null) {
            new Thread(){
                @Override
                public void run() {
                    try {
                        String data = msg + "\n";
                        out.write(data.getBytes());
                        Log.i(TAG, "msg Enviada: " + data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    super.run();
                }
            }.start();

        }
    }

    public void stop() {
        running = false;
        try {
            if (socket != null) {
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
        }
    }

}
