package bo.tc.tcplanner.app;

import bo.tc.tcplanner.Gui.TCGuiController;
import bo.tc.tcplanner.app.DataServer.FileServer;
import bo.tc.tcplanner.app.RMIServer.RMIInterface;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import static bo.tc.tcplanner.PropertyConstants.*;

public class TCRemoteApp extends TCApp {

    @Override
    public void start(Stage stage) throws Exception {
        //Initialization
//        setConstants();
        stage.getIcons().add(app_icon);

        // Start Gui
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TCGui.fxml"));
        Parent root = loader.load();
        TCGuiController tcGuiController = loader.getController();
        tcGuiController.setApplication(this);
        tcGuiController.setPrimaryStage(stage);
        tcGuiController.setOfflineGui();
        setTcGuiController(tcGuiController);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("TCRemoteApp");
        stage.show();

        // Json Server
        FileServer fileServer = new FileServer();
        fileServer.setApplication(this);
        fileServer.createServer().start();

        // Connect to Remote Solver
        new Thread(() -> {
            // Connect to RMI Server
            if (getParameters().getNamed().containsKey("address")) {
                rmiip = getParameters().getNamed().get("address");
            }
            if (getParameters().getNamed().containsKey("port")) {
                rmiport = Integer.parseInt(getParameters().getNamed().get("port"));
            }
            while (true) {
                try {
                    if (rmiInterface == null)
                        rmiInterface = (RMIInterface) Naming.lookup("//" + rmiip + ":" + rmiport + "/MyServer");
                    rmiInterface.ping();
                    getTcGuiController().setOnlineGui();
                    Thread.sleep(10000);
                } catch (NotBoundException | MalformedURLException | RemoteException | InterruptedException ce) {
                    getTcGuiController().setOfflineGui();
                    rmiInterface = null;
                }
            }

        }).start();

    }
}
