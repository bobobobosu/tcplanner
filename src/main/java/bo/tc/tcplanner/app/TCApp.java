package bo.tc.tcplanner.app;

import bo.tc.tcplanner.Gui.TCGuiController;
import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.Tools.ByteRingBuffer;
import bo.tc.tcplanner.app.RMIServer.RMIInterface;
import bo.tc.tcplanner.app.SolverCore.SolverScoreDelta;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.text.Font;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TCApp extends Application {
    RMIInterface rmiInterface;
    private final ConcurrentHashMap<String, Object> appStatusLock = new ConcurrentHashMap<>() {
        {
            put("console", new Object());
            put("scoreexplaination", new StringBuilder());
            put("currentScoreDelta", new SolverScoreDelta());
        }
    };

    private final ByteRingBuffer consoleByteBuffer = new ByteRingBuffer(1024000);
    TCGuiController tcGuiController = null;


    public TCApp() {
        PropertyConstants.sarasa_font = Font.loadFont(getClass().getResourceAsStream("sarasa-mono-sc-regular.ttf"), 12);
        PropertyConstants.app_icon = new Image(getClass().getResourceAsStream("app_icon.png"));
    }

    public void updateConsole(byte b, String source) {
        synchronized (getAppStatusLock().get("console")) {
            consoleByteBuffer.write(new byte[]{b});
            if ((char) b == '\n') {
                getAppStatusLock().get("console").notify();
            }
        }
    }

    public String flushConsole() {
        byte[] buffer = new byte[getConsoleByteBuffer().getUsed()];
        getConsoleByteBuffer().read(buffer);
        String buff = new String(buffer, 0, buffer.length, StandardCharsets.UTF_8);
        getConsoleByteBuffer().clear();
        return buff;
    }

    public void updateScheduleLocal(Schedule schedule) {
        if (tcGuiController != null) tcGuiController.refreshScheduleLocal(schedule);
    }

    public ConcurrentHashMap<String, Object> getAppStatusLock() {
        return appStatusLock;
    }

    public ByteRingBuffer getConsoleByteBuffer() {
        return consoleByteBuffer;
    }

    public class StreamCapturer extends OutputStream {
        private String prefix;
        private PrintStream old;

        public StreamCapturer(String prefix, PrintStream old) {
            super();
            this.prefix = prefix;
            this.old = old;
        }

        @Override
        public void write(int b) {
            updateConsole((byte) b, prefix);
            old.write(b);
        }
    }


    public RMIInterface getRmiInterface() {
        return rmiInterface;
    }

    public void setRmiInterface(RMIInterface rmiInterface) {
        this.rmiInterface = rmiInterface;
    }

    public TCGuiController getTcGuiController() {
        return tcGuiController;
    }

    public void setTcGuiController(TCGuiController tcGuiController) {
        this.tcGuiController = tcGuiController;
    }

}
