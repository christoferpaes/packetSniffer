import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.packet.Packet;
import org.pcap4j.util.NifSelector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class PacketSnifferActivity extends AppCompatActivity {

    private static final String TAG = PacketSnifferActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 1;

    private TextView packetTextView;
    private StringBuilder packetBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packet_sniffer);

        packetTextView = findViewById(R.id.packetTextView);
        packetBuilder = new StringBuilder();

        if (checkPermission()) {
            startPacketCapture();
        } else {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE
        );
    }

    private void startPacketCapture() {
        PcapNetworkInterface networkInterface = selectNetworkInterface();

        if (networkInterface == null) {
            Log.d(TAG, "No network interface selected.");
            return;
        }

        PcapHandle handle;
        try {
            handle = networkInterface.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
        } catch (IOException e) {
            Log.e(TAG, "Failed to open network interface: " + e.getMessage());
            return;
        }

        PacketListener listener = new PacketListener() {
            @Override
            public void gotPacket(Packet packet) {
                packetBuilder.append(packet).append("\n");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        packetTextView.setText(packetBuilder.toString());
                    }
                });
            }
        };

        Thread captureThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handle.loop(-1, listener);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Packet capture interrupted: " + e.getMessage());
                } finally {
                    handle.close();
                }
            }
        });
        captureThread.start();
    }

    private PcapNetworkInterface selectNetworkInterface() {
        try {
            List<PcapNetworkInterface> networkInterfaces = PcapNetworkInterface.getAllDevs();
            PcapNetworkInterface[] interfaceArray = new PcapNetworkInterface[networkInterfaces.size()];
            networkInterfaces.toArray(interfaceArray);
            return new NifSelector().selectNetworkInterface(interfaceArray);
        } catch (IOException e) {
            Log.e(TAG, "Failed to retrieve network interfaces: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPacketCapture();
            } else {
                Log.d(TAG, "Permission denied.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        savePacketsToFile();
    }

    private void savePacketsToFile() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(directory, "packets.txt");
            try {
                FileWriter writer = new FileWriter(file);
                writer.write(packetBuilder.toString());
                writer.close();
                Log.d(TAG, "Packets saved to file: " + file.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG, "Failed to save packets to file: " + e.getMessage());
            }
        } else {
            Log.d(TAG, "External storage not mounted.");
        }
    }
}

