package bo.tc.tcplanner.app;

import bo.tc.tcplanner.datastructure.LocationHierarchyMap;
import bo.tc.tcplanner.datastructure.TimeHierarchyMap;
import bo.tc.tcplanner.datastructure.TimelineBlock;
import bo.tc.tcplanner.datastructure.ValueEntryMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Range;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;

import static bo.tc.tcplanner.PropertyConstants.*;
import static bo.tc.tcplanner.app.SolverCore.Toolbox.*;

public class Test {
    public static String fpath_Constants = "C:\\_DATA\\_Storage\\_Sync\\Devices\\root\\Constants.json";
    public static String path_Notebook;
    public static String fpath_TimelineBlock;
    public static String fpath_ValueEntryMap;
    public static String fpath_LocationHierarchyMap;
    public static String fpath_TimeHierarchyMap;


    private static DatabaseReference database;

    public static void startListeners() {
        database.child("Calendar").addChildEventListener(new ChildEventListener() {

            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildName) {

            }

            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildName) {
                int g = 0;
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildName) {
            }

            public void onCancelled(DatabaseError databaseError) {
                System.out.println("startListeners: unable to attach listener to posts");
                System.out.println("startListeners: " + databaseError.getMessage());
            }
        });
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        setConstants();
        initializeFiles();

        // Fetch the service account key JSON file contents
        FileInputStream serviceAccount = new FileInputStream("C:\\Users\\bobob\\Downloads\\tcplanner-4bbab-firebase-adminsdk-95h9b-f77d329628.json");

// Initialize the app with a service account, granting admin privileges
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://tcplanner-4bbab.firebaseio.com")
                .build();
        FirebaseApp.initializeApp(options);

// As an admin, the app has access to read and write all data, regardless of Security Rules
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object document = dataSnapshot.getValue();
                System.out.println(document);
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        while (true) {
            Thread.sleep(1000);
            FirebaseDatabase.getInstance().getReference().getRoot().setValue(Arrays.asList(System.currentTimeMillis(), 2, 3), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    System.out.println("Co");
                }
            });
        }
    }

    static boolean checkConstraintDict(HashMap<String, ArrayList> constraintDict, Range<ZonedDateTime> timerange) {
        List<Range<ZonedDateTime>> daysInTimerange = getDatesOverRange(timerange);
        for (Map.Entry constraint : constraintDict.entrySet()) {
            if (constraint.getKey().equals("day")) {
                if (!daysInTimerange.stream().allMatch(thisDay -> castList(constraint.getValue()).contains(thisDay.lowerEndpoint().getDayOfMonth())))
                    return false;
            }
            if (constraint.getKey().equals("weekday")) {
                if (!daysInTimerange.stream().allMatch(thisDay -> castList(constraint.getValue()).contains(thisDay.lowerEndpoint().getDayOfWeek().getValue())))
                    return false;
            }
            if (constraint.getKey().equals("time")) {
                for (Range<ZonedDateTime> zonedDateTimeRange : daysInTimerange) {
                    LocalTime reqstartTime = LocalTime.parse(castString(constraint.getValue()).split("/")[0]);
                    LocalTime reqendTime = LocalTime.parse(castString(constraint.getValue()).split("/")[1]);
                    if (zonedDateTimeRange.lowerEndpoint().toLocalTime().compareTo(reqstartTime) < 0 ||
                            zonedDateTimeRange.upperEndpoint().toLocalTime().compareTo(reqendTime) > 0) return false;
                }
            }
            if (constraint.getKey().equals("iso8601")) {
                ZonedDateTime restrictedStartDate = ZonedDateTime.parse(castString(constraint.getValue()).split("/")[0]);
                ZonedDateTime restrictedEndDate = ZonedDateTime.parse(castString(constraint.getValue()).split("/")[1]);
                if (timerange.lowerEndpoint().compareTo(restrictedStartDate) < 0 ||
                        timerange.upperEndpoint().compareTo(restrictedEndDate) > 0) return false;
            }

        }
        return true;
    }

    static List<Range<ZonedDateTime>> getDatesOverRange(Range<ZonedDateTime> timerange) {
        List<ZonedDateTime> dateList = new ArrayList<>();
        List<Range<ZonedDateTime>> dateRangeList = new ArrayList<>();
        ZonedDateTime start = timerange.lowerEndpoint();
        while (start.getYear() <= timerange.upperEndpoint().getYear() &&
                start.getDayOfYear() <= timerange.upperEndpoint().getDayOfYear()) {
            dateList.add(start);
            start = start.plusDays(1);
        }

        for (ZonedDateTime zonedDateTime : dateList) {
            dateRangeList.add(Range.closed(
                    sameDay(zonedDateTime, timerange.lowerEndpoint()) ? timerange.lowerEndpoint() : zonedDateTime.with(LocalTime.MIN),
                    sameDay(zonedDateTime, timerange.upperEndpoint()) ? timerange.upperEndpoint() : zonedDateTime.with(LocalTime.MAX)));
        }

        return dateRangeList;
    }

    static boolean sameDay(ZonedDateTime z1, ZonedDateTime z2) {
        return (z1.getDayOfYear() == z2.getDayOfYear()) && (z1.getYear() == z2.getYear());
    }

    static void setConstants() throws IOException {
        HashMap ConstantsJson = new ObjectMapper().readValue(
                IOUtils.toString(new FileInputStream(new File(fpath_Constants)), StandardCharsets.UTF_8), HashMap.class);
        path_Notebook = castString(castDict(castDict(ConstantsJson.get("Paths")).get("Folders")).get("Notebook"));
        fpath_TimelineBlock = genPathFromConstants("TimelineBlockProblem.json", ConstantsJson);
        fpath_ValueEntryMap = genPathFromConstants("ValueEntryMap.json", ConstantsJson);
        fpath_LocationHierarchyMap = genPathFromConstants("LocationHierarchyMap.json", ConstantsJson);
        fpath_TimeHierarchyMap = genPathFromConstants("TimeHierarchyMap.json", ConstantsJson);
    }

    public static void initializeFiles() {
        // Load TimelineBlock & ValueEntryMap
        try {
            valueEntryMap = new ObjectMapper().readValue(
                    IOUtils.toString(new FileInputStream(new File(fpath_ValueEntryMap)), StandardCharsets.UTF_8), ValueEntryMap.class);
            locationHierarchyMap = new ObjectMapper().readValue(
                    IOUtils.toString(new FileInputStream(new File(fpath_LocationHierarchyMap)), StandardCharsets.UTF_8),
                    LocationHierarchyMap.class);
            timeHierarchyMap = new ObjectMapper().readValue(
                    IOUtils.toString(new FileInputStream(new File(fpath_TimeHierarchyMap)), StandardCharsets.UTF_8),
                    TimeHierarchyMap.class);
            timelineBlock = new ObjectMapper().readValue(
                    IOUtils.toString(new FileInputStream(new File(fpath_TimelineBlock)), StandardCharsets.UTF_8),
                    TimelineBlock.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void initializeExampleFiles() {
        // Load TimelineBlock & ValueEntryMap
        try {
            valueEntryMap = new ObjectMapper().readValue(
                    IOUtils.toString(Test.class.getResourceAsStream("example_data/ValueEntryMap.json"), StandardCharsets.UTF_8), ValueEntryMap.class);
            locationHierarchyMap = new ObjectMapper().readValue(
                    IOUtils.toString(Test.class.getResourceAsStream("example_data/LocationHierarchyMap.json"), StandardCharsets.UTF_8),
                    LocationHierarchyMap.class);
            timeHierarchyMap = new ObjectMapper().readValue(
                    IOUtils.toString(Test.class.getResourceAsStream("example_data/TimeHierarchyMap.json"), StandardCharsets.UTF_8),
                    TimeHierarchyMap.class);
            timelineBlock = new ObjectMapper().readValue(
                    IOUtils.toString(Test.class.getResourceAsStream("example_data/TimelineBlockProblem.json"), StandardCharsets.UTF_8),
                    TimelineBlock.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
