package bo.tc.tcplanner.app.DataServer;

import bo.tc.tcplanner.datastructure.Timeline;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseServer extends Thread {
    public static void main(String[] args) throws InterruptedException, IOException {
        FirebaseServer firebaseServer = new FirebaseServer();
        firebaseServer.createServer();
    }

    public FirebaseServer createServer() throws InterruptedException, IOException {
        Timeline timeline;
        FileInputStream serviceAccount = new FileInputStream(
                "C:\\Users\\bobob\\Downloads\\tcplanner-4bbab-firebase-adminsdk-95h9b-f77d329628.json");
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://tcplanner-4bbab.firebaseio.com")
                .build();
        FirebaseApp.initializeApp(options);


        Thread.currentThread().join();
        return this;
    }

    public Timeline fullUpload(String dataName, Object data) {
        FirebaseDatabase.getInstance().getReference(dataName).getRoot().setValue(data, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                System.out.println(dataName + " Completed");
            }
        });
        return null;
    }
}