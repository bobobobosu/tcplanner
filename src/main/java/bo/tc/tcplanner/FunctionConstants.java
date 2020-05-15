package bo.tc.tcplanner;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FunctionConstants {
    public static Map<String, ZonedDateTime> ZonedDateTimeParseCache = new ConcurrentHashMap<>();

}
