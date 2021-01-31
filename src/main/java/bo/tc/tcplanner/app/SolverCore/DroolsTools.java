package bo.tc.tcplanner.app.SolverCore;

import bo.tc.tcplanner.datastructure.LocationHierarchyMap;
import bo.tc.tcplanner.datastructure.TimeHierarchyMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static bo.tc.tcplanner.app.SolverCore.Toolbox.castList;
import static bo.tc.tcplanner.app.SolverCore.Toolbox.castString;


public class DroolsTools {

    public static RangeSet<ZonedDateTime> getConstrintedTimeRange(TimeHierarchyMap timeHierarchyMap, String timeRestriction, ZonedDateTime start, ZonedDateTime end) {
        List<Range<ZonedDateTime>> datesOverRange = getDatesOverRange(Range.closed(start, end));
        RangeSet<ZonedDateTime> result = TreeRangeSet.create();

        for (Object or_constraint : castList(timeHierarchyMap.get(timeRestriction))) {
            for (Object constraintDict : castList(or_constraint)) {
                List<Range<ZonedDateTime>> thisRange = new ArrayList<>(datesOverRange);
                RangeSet<ZonedDateTime> thisRangeSet = TreeRangeSet.create();
                thisRangeSet.addAll(thisRange);
                HashMap<String, ArrayList> thisconstraint = (HashMap<String, ArrayList>) constraintDict;

                // Remove invalid days
                if (thisconstraint.containsKey("day")) {
                    RangeSet<ZonedDateTime> toRemove = TreeRangeSet.create();
                    toRemove.addAll(datesOverRange.stream()
                            .filter(r -> !thisconstraint.get("day").contains(r.lowerEndpoint().getDayOfMonth()))
                            .collect(Collectors.toList()));
                    thisRangeSet.removeAll(toRemove);
                }

                // Remove invalid weekdays
                if (thisconstraint.containsKey("weekday")) {
                    RangeSet<ZonedDateTime> toRemove = TreeRangeSet.create();
                    toRemove.addAll(datesOverRange.stream()
                            .filter(r -> !thisconstraint.get("weekday").contains(r.lowerEndpoint().getDayOfWeek()))
                            .collect(Collectors.toList()));
                    thisRangeSet.removeAll(toRemove);
                }

                // Remove invalid time
                if (thisconstraint.containsKey("time")) {
                    RangeSet<ZonedDateTime> or_time = TreeRangeSet.create();
                    for (Object time : thisconstraint.get("time")) {
                        LocalTime reqstartTime = LocalTime.parse(castString(time).split("/")[0]);
                        LocalTime reqendTime = LocalTime.parse(castString(time).split("/")[1]);
                        or_time.addAll(datesOverRange.stream()
                                .map(x -> Range.closed(x.lowerEndpoint().with(reqstartTime), x.lowerEndpoint().with(reqendTime)))
                                .collect(Collectors.toList()));
                    }
                    thisRangeSet.removeAll(or_time.complement());
                }

                // Remove invalid iso8601
                if (thisconstraint.containsKey("iso8601")) {
                    RangeSet<ZonedDateTime> or_time = TreeRangeSet.create();
                    for (Object iso8601 : thisconstraint.get("iso8601")) {
                        ZonedDateTime restrictedStartDate = ZonedDateTime.parse(castString(iso8601).split("/")[0]);
                        ZonedDateTime restrictedEndDate = ZonedDateTime.parse(castString(iso8601).split("/")[1]);
                        or_time.addAll(datesOverRange.stream()
                                .map(x -> Range.closed(restrictedStartDate, restrictedEndDate))
                                .collect(Collectors.toList()));
                    }
                    thisRangeSet.removeAll(or_time.complement());
                }


                result.addAll(thisRangeSet);

            }
        }
        return result;
    }

    public static boolean locationRestrictionCheck(LocationHierarchyMap locationHierarchyMap, String available, String requirement) {
//        boolean fff = locationHierarchyMap.containsKey(available) ?
//                locationHierarchyMap.get(available).contains(requirement) :
//                available.equals(requirement);
//        System.out.println(available + " " + requirement + " "+ fff);
        return locationHierarchyMap.containsKey(available) ?
                locationHierarchyMap.get(available).contains(requirement) :
                available.equals(requirement);
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
                return daysInTimerange.stream().allMatch(zonedDateTimeRange -> castList(constraint.getValue()).stream().anyMatch(or_interval -> {
                            LocalTime reqstartTime = LocalTime.parse(castString(or_interval).split("/")[0]);
                            LocalTime reqendTime = LocalTime.parse(castString(or_interval).split("/")[1]);
                            return zonedDateTimeRange.lowerEndpoint().toLocalTime().compareTo(reqstartTime) >= 0 &&
                                    zonedDateTimeRange.upperEndpoint().toLocalTime().compareTo(reqendTime) <= 0;
                        })
                );

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

    public static List<Range<ZonedDateTime>> getDatesOverRange(Range<ZonedDateTime> timerange) {
        List<ZonedDateTime> dateList = new ArrayList<>();
        List<Range<ZonedDateTime>> dateRangeList = new ArrayList<>();
        ZonedDateTime start = timerange.lowerEndpoint().with(LocalTime.of(0, 0));
        while (start.isBefore(timerange.upperEndpoint().with(LocalTime.MAX))) {
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

    private static boolean sameDay(ZonedDateTime z1, ZonedDateTime z2) {
        return (z1.getDayOfYear() == z2.getDayOfYear()) && (z1.getYear() == z2.getYear());
    }

}
