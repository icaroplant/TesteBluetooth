package plant.testebluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;


public class BluetoothCheckActivity extends AppCompatActivity {
    protected BluetoothAdapter btfAdapter;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Bluetooth adapter
        btfAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btfAdapter == null) {
            Toast.makeText(this, "Bluetooth não disponível neste dispositivo.", Toast.LENGTH_LONG).show();
            // Vamos fechar a activity neste caso
            finish();
            return;
        }

        // Se o bluetooth não está ligado
        if (!btfAdapter.isEnabled()) {
            requestBluetooth();
        }
    }

    // Pede pro usuário ligar o bluetooth
    // <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    public void requestBluetooth(){
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (btfAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth foi ligado!", Toast.LENGTH_SHORT).show();
        }
    }
}
