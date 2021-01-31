package bo.tc.tcplanner.app.DataServer;

import bo.tc.tcplanner.app.SolverCore.GuiSolverThread;
import bo.tc.tcplanner.app.TCApp;
import bo.tc.tcplanner.datastructure.*;
import bo.tc.tcplanner.datastructure.converters.DataStructureBuilder;
import bo.tc.tcplanner.datastructure.converters.DataStructureWriter;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static bo.tc.tcplanner.PropertyConstants.*;

public class FileServer {
    private static final int BACKLOG = 1;
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    //Objects
    TCApp application;
    GuiSolverThread solverThread;
    FirebaseServer firebaseServer;

    //Latest Solutions
    private TimelineBlock problemTimelineBlock;

    public static void main(final String... args) throws IOException {
        FileServer fileServer = new FileServer();
        fileServer.createServer().start();
    }

    public HttpServer createServer() throws IOException {
        final HttpServer server = HttpServer.create(new InetSocketAddress(fileserveradd, fileserverport), BACKLOG);
        var scoreTimelineBlockHandler = new ScoreTimelineBlockHandler();
        server.createContext("/scoreTimelineBlock", scoreTimelineBlockHandler);
        var patchTimelineBlockHandler = new PatchTimelineBlockHandler();
        server.createContext("/patchTimelineBlock", patchTimelineBlockHandler);
        var updateOptaFilesHandler = new UpdateOptaFilesHandler();
        server.createContext("/updateOptaFiles", updateOptaFilesHandler);
        var consoleHandler = new ConsoleHandler();
        server.createContext("/updateConsole", consoleHandler);
        return server;
    }

    public boolean compareTimelineBlock(TimelineBlock TB1, TimelineBlock TB2) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(TB1).equals(mapper.writeValueAsString(TB2));
    }

    private Map<String, List<String>> getRequestParameters(final URI requestUri) {
        final Map<String, List<String>> requestParameters = new LinkedHashMap<>();
        final String requestQuery = requestUri.getRawQuery();
        if (requestQuery != null) {
            final String[] rawRequestParameters = requestQuery.split("[&;]", -1);
            for (final String rawRequestParameter : rawRequestParameters) {
                final String[] requestParameter = rawRequestParameter.split("=", 2);
                final String requestParameterName = decodeUrlComponent(requestParameter[0]);
                requestParameters.putIfAbsent(requestParameterName, new ArrayList<>());
                final String requestParameterValue = requestParameter.length > 1
                        ? decodeUrlComponent(requestParameter[1])
                        : null;
                requestParameters.get(requestParameterName).add(requestParameterValue);
            }
        }
        return requestParameters;
    }

    private String decodeUrlComponent(final String urlComponent) {
        try {
            return URLDecoder.decode(urlComponent, CHARSET.name());
        } catch (final UnsupportedEncodingException ex) {
            throw new InternalError(ex);
        }
    }

    public GuiSolverThread getSolverThread() {
        return solverThread;
    }

    public void setSolverThread(GuiSolverThread solverThread) {
        this.solverThread = solverThread;
    }

    public TimelineBlock getProblemTimelineBlock() {
        return problemTimelineBlock;
    }

    public void setProblemTimelineBlock(TimelineBlock problemTimelineBlock) {
        this.problemTimelineBlock = problemTimelineBlock;
    }

    public FirebaseServer getFirebaseServer() {
        return firebaseServer;
    }

    public void setFirebaseServer(FirebaseServer firebaseServer) {
        this.firebaseServer = firebaseServer;
    }

    private void setFiles(Map<String, Map> updatedfiles) throws IllegalArgumentException {
        for (Map.Entry<String, Map> entry : updatedfiles.entrySet()) {
            try {
                new ObjectMapper().writeValue(Paths.get(entry.getKey()).toFile(), entry.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (entry.getKey().equals("TimeHierarchyMap.json")) {
                TimeHierarchyMap tmptimeHierarchyMap = new ObjectMapper().convertValue(entry.getValue(), TimeHierarchyMap.class);
                tmptimeHierarchyMap.checkValid();
                timeHierarchyMap = tmptimeHierarchyMap;
//                    firebaseServer.fullUpload("TimeHierarchyMap", timeHierarchyMap);
                System.out.println("TimeHierarchyMap Updated");
            }
            if (entry.getKey().equals("LocationHierarchyMap.json")) {
                LocationHierarchyMap tmplocationHierarchyMap = new ObjectMapper().convertValue(entry.getValue(), LocationHierarchyMap.class);
                tmplocationHierarchyMap.checkValid();
                locationHierarchyMap = tmplocationHierarchyMap;
//                    firebaseServer.fullUpload("LocationHierarchyMap", locationHierarchyMap);
                System.out.println("LocationHierarchyMap Updated");

            }
            if (entry.getKey().equals("ValueHierarchyMap.json")) {
                ValueHierarchyMap tmpvalueHierarchyMap = new ObjectMapper().convertValue(entry.getValue(), ValueHierarchyMap.class);
                tmpvalueHierarchyMap.checkValid();
                valueHierarchyMap = tmpvalueHierarchyMap;
//                    firebaseServer.fullUpload("ValueHierarchyMap", valueHierarchyMap);
                System.out.println("ValueHierarchyMap Updated");
            }
            if (entry.getKey().equals("ValueEntryMap.json")) {
                ValueEntryMap tmpvalueEntryMap = new ObjectMapper().convertValue(entry.getValue(), ValueEntryMap.class);
                tmpvalueEntryMap.checkValid();
                valueEntryMap = tmpvalueEntryMap;
//                    firebaseServer.fullUpload("ValueEntryMap", valueEntryMap);
                System.out.println("ValueEntryMap Updated");
            }
            if (entry.getKey().equals("TimelineBlock.json")) {
                TimelineBlock timelineBlock = new ObjectMapper().convertValue(entry.getValue(), TimelineBlock.class);
                timelineBlock.checkValid();
                setProblemTimelineBlock(timelineBlock);
                System.out.println("TimelineBlock Updated");
            }
        }
    }

    public enum StatusCode {
        OK(200), CREATED(201), ACCEPTED(202),

        BAD_REQUEST(400), METHOD_NOT_ALLOWED(405);

        private final int code;

        StatusCode(final int newValue) {
            code = newValue;
        }

        public int getCode() {
            return code;
        }
    }

    public class ConsoleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                throw new UnsupportedOperationException();
            }

            new Thread(() -> {
                try {
                    exchange.getResponseHeaders().set(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                    exchange.sendResponseHeaders(StatusCode.CREATED.getCode(), 0);
                    OutputStream responseBody = exchange.getResponseBody();
                    String buff = "";
                    synchronized (application.getAppStatusLock().get("console")) {
                        try {
                            application.getAppStatusLock().get("console").wait();
                        } catch (InterruptedException ignored) {
                        }
                        buff = application.flushConsole();
                    }
                    responseBody.write(buff.getBytes(StandardCharsets.UTF_8));
                    responseBody.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        }
    }

    public class ScoreTimelineBlockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                throw new UnsupportedOperationException();
            }

            new Thread(() -> {
                try {
                    byte[] response = new byte[0];
                    String javaString = URLDecoder.decode(IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8),
                            StandardCharsets.UTF_8);
                    Map<String, Map> updatedfiles = new ObjectMapper().readValue(javaString, Map.class);
                    Schedule result = null;
                    try {
                        setFiles(updatedfiles);
                        TimelineBlock timelineBlock = problemTimelineBlock;
                        if (timelineBlock.getOrigin().equals("TCxlsb")) {
                            result = new DataStructureBuilder(
                                    valueEntryMap, timelineBlock, timeHierarchyMap, locationHierarchyMap, true)
                                    .constructChainProperty().getSchedule();
                            timelineBlock = new DataStructureWriter().generateTimelineBlockScore(result);
                            response = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(timelineBlock).getBytes(StandardCharsets.UTF_8);
                        }
                    } catch (IllegalArgumentException ex) {
                        String msg = Throwables.getStackTraceAsString(ex);
                        System.err.println(msg);
                        response = msg.getBytes(StandardCharsets.UTF_8);
                    }

                    System.out.println("Sending Scored TimelineBlock");
                    exchange.getResponseHeaders().set(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                    exchange.sendResponseHeaders(StatusCode.CREATED.getCode(), 0);
                    OutputStream responseBody = exchange.getResponseBody();
                    responseBody.write(response);
                    responseBody.close();

                    if (result != null) application.updateScheduleLocal(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        }
    }

    public class PatchTimelineBlockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                throw new UnsupportedOperationException();
            }

            new Thread(() -> {
                try {
                    TimelineBlock timelineBlock = new DataStructureWriter().generateTimelineBlockScore(solverThread.getCurrentSchedule());
                    System.out.println("Sending Patched TimelineBlock");
                    exchange.getResponseHeaders().set(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                    exchange.sendResponseHeaders(StatusCode.CREATED.getCode(), 0);
                    OutputStream responseBody = exchange.getResponseBody();
                    responseBody.write(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(timelineBlock).getBytes(StandardCharsets.UTF_8));
                    responseBody.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        }
    }

    public class UpdateOptaFilesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                throw new UnsupportedOperationException();
            }
            new Thread(() -> {
                try {
                    byte[] response = new byte[0];
                    String javaString = URLDecoder.decode(IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8),
                            StandardCharsets.UTF_8);
                    Map<String, Map> updatedfiles = new ObjectMapper().readValue(javaString, Map.class);

                    try {
                        setFiles(updatedfiles);
                        if (updatedfiles.containsKey("TimelineBlock.json")) {
                            solverThread.terminate();
                            solverThread.setCurrentSchedule(new DataStructureBuilder(
                                    valueEntryMap, problemTimelineBlock, timeHierarchyMap, locationHierarchyMap, false)
                                    .constructChainProperty().getSchedule());
                            solverThread.solve(GuiSolverThread.SolverModes.RESTART);
                            response = "{\"Updated\":true}".getBytes(StandardCharsets.UTF_8);
                        }
                    } catch (IllegalArgumentException ex) {
                        String msg = Throwables.getStackTraceAsString(ex);
                        System.err.println(msg);
                        response = msg.getBytes(StandardCharsets.UTF_8);
                    }

                    exchange.getResponseHeaders().
                            set(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                    exchange.sendResponseHeaders(StatusCode.CREATED.getCode(), 0);
                    OutputStream responseBody = exchange.getResponseBody();
                    responseBody.write(response);
                    responseBody.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();


        }
    }

    public class Constants {
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String APPLICATION_JSON = "application/json; charset=UTF-8";
    }


    public TCApp getApplication() {
        return application;
    }

    public void setApplication(TCApp application) {
        this.application = application;
    }
}