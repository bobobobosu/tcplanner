package bo.tc.tcplanner.app;

import bo.tc.tcplanner.Gui.TCGuiController;
import bo.tc.tcplanner.app.DataServer.FileServer;
import bo.tc.tcplanner.app.RMIServer.RMIServer;
import bo.tc.tcplanner.app.SolverCore.GuiSolverThread;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.PrintStream;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

import static bo.tc.tcplanner.PropertyConstants.*;

public class TCCoreApp extends TCApp {
    @Override
    public void start(Stage stage) throws Exception {
        //Initialization
//        setConstants();
        stage.getIcons().add(app_icon);
        System.setOut(new PrintStream(new StreamCapturer("STDOUT", System.out)));
        System.setErr(new PrintStream(new StreamCapturer("STDERR", System.err)));

        // Start Gui
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TCGui.fxml"));
        Parent root = loader.load();
        TCGuiController tcGuiController = loader.getController();
        tcGuiController.setApplication(this);
        tcGuiController.setPrimaryStage(stage);
        tcGuiController.setOnlineGui();
        setTcGuiController(tcGuiController);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("TCCoreApp");
        stage.show();

        //Declaration
        FileServer fileServer = new FileServer();
        GuiSolverThread solverThread = new GuiSolverThread();
//        FirebaseServer firebaseServer = new FirebaseServer();

        //RMI Server
        try {
            rmiInterface = new RMIServer(solverThread, this);
            LocateRegistry.createRegistry(rmiport);
            Naming.rebind("//" + rmilistenadd + ":" + rmiport + "/MyServer", rmiInterface);
            System.err.println("Server ready");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //JsonServer
        fileServer.setApplication(this);
        fileServer.setSolverThread(solverThread);
//        jsonServer.setFirebaseServer(firebaseServer);

        //SolverThread
        solverThread.setApplication(this);
        solverThread.setFileServer(fileServer);

        //Start Threads
        fileServer.createServer().start();
        solverThread.start();
//        firebaseServer.createServer().start();

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String input = scanner.nextLine();
                if (input.equals("p")) {
                    solverThread.terminate();
                } else if (input.equals("r")) {
                    solverThread.solve(GuiSolverThread.SolverModes.RESTART);
                } else if (input.equals("x")) {
                    System.exit(0);
                } else if (input.equals("e")) {
//                    solverThread.getCurrentSolver().explainBestScore();
                } else if (input.startsWith("t")) {
                } else {
                    // Do Nothing
                }
            }

        }).start();

    }

}
