package bo.tc.tcplanner.app.SolverCore;

import bo.tc.tcplanner.PropertyConstants;
import bo.tc.tcplanner.domain.planningstructures.Allocation;
import bo.tc.tcplanner.domain.planningstructures.ResourceTotal;
import bo.tc.tcplanner.domain.planningstructures.Schedule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jakewharton.fliptables.FlipTable;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

import static bo.tc.tcplanner.PropertyConstants.defaultTimeZone;

public class Toolbox {
    public static TrayIcon trayIcon;

    public static LinkedHashMap<String, Object> castDict(Object obj) {
        return (LinkedHashMap<String, Object>) obj;
    }

    public static List<Object> castList(Object obj) {
        return (List<Object>) obj;
    }

    public static String castString(Object obj) {
        return (String) obj;
    }

    public static int castInt(Object obj) {
        return ((Number) obj).intValue();
    }

    public static long castLong(Object obj) {
        return ((Number) obj).longValue();
    }

    public static double castDouble(Object obj) {
        return ((Number) obj).doubleValue();
    }

    public static boolean castBoolean(Object obj) {
        return (boolean) obj;
    }

    public static Object jacksonDeepCopy(Object obj) {
        try {
            String serielizedStr = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            Map serielizedMap = new ObjectMapper().readValue(serielizedStr, Map.class);
            return new ObjectMapper().convertValue(serielizedMap, obj.getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void displayTray(String caption, String text) {
        try {
            //Obtain only one instance of the SystemTray object
            SystemTray tray = SystemTray.getSystemTray();
            tray.remove(trayIcon);
            //If the icon is a file
            Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
            //Alternative (if the icon is on the classpath):
            //Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));

            trayIcon = new TrayIcon(image, "Tray Demo");
            //Let the system resize the image if needed
            trayIcon.setImageAutoSize(true);
            //Set tooltip text for the tray icon
            trayIcon.setToolTip("System tray icon demo");
            trayIcon.addActionListener(e -> {

            });
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }

            trayIcon.displayMessage(caption, text, TrayIcon.MessageType.INFO);
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        }
    }

    public static String hardConstraintMatchToString(Set<ConstraintMatch> ConstraintMatchSet) {
        StringBuilder result = new StringBuilder();
        Iterator<ConstraintMatch> constraintMatchSetIterator = ConstraintMatchSet.iterator();
        while (constraintMatchSetIterator.hasNext()) {
            ConstraintMatch constraintMatch = constraintMatchSetIterator.next();
            if (((HardMediumSoftLongScore) constraintMatch.getScore()).getHardScore() < 0) {
                result.append(constraintMatch.getConstraintName())
                        .append("[")
                        .append(((HardMediumSoftLongScore) constraintMatch.getScore()).getHardScore())
                        .append("]\n");
            }
        }
        return result.toString();
    }

    public static String genPathFromConstants(String filename, HashMap<String, Object> ConstantsJson) {
        return castString(castDict(castDict(ConstantsJson.get("Paths")).get("Folders")).get(castString(castDict(castDict(ConstantsJson.get("Paths")).get("Files")).get(filename)))) + filename;
    }

    public static class PrettyPrintAlloc {
        public List<String[]> breakByRules = new ArrayList<>();
        public Map<Allocation, Indictment> breakByTasks = new HashMap<>();
        ScoreDirector<Schedule> scoreDirector;

        public PrettyPrintAlloc(ScoreDirector<Schedule> scoreDirector) {
            this.scoreDirector = scoreDirector;
            this.scoreDirector.calculateScore();
            for (ConstraintMatchTotal constraintMatch : scoreDirector.getConstraintMatchTotals()) {
                if (((HardMediumSoftLongScore) (constraintMatch.getScore())).getHardScore() < 0)
                    breakByRules.add(new String[]{constraintMatch.toString()});
            }
            for (Map.Entry<Object, Indictment> indictmentEntry : scoreDirector.getIndictmentMap().entrySet()) {
                if (indictmentEntry.getValue().getJustification() instanceof Allocation &&
                        ((HardMediumSoftLongScore) indictmentEntry.getValue().getScore()).getHardScore() < 0) {
                    Allocation matchAllocation = (Allocation) indictmentEntry.getValue().getJustification();
                    breakByTasks.put(matchAllocation, indictmentEntry.getValue());
                }
            }
        }

        public String percentStr(Allocation allocation) {
            return allocation.getConstrainedProgressDelta() + "\n/"
                    + allocation.getTimelineEntry().getProgressChange().getProgressDelta100();
        }

        public String timelineStr(Allocation allocation) {
            // rownum, {id}, [idx]
            int rownum = allocation.getTimelineEntry().getTimelineProperty().getRownum();
            Integer id = allocation.getTimelineEntry().getTimelineProperty().getTimelineid();
            int idx = allocation.getWeight();
            return ((allocation.getTimelineEntry().getTimelineProperty().getPlanningWindowType()
                    .equals(PropertyConstants.PlanningWindowTypes.types.Draft.name())) ? "****" : rownum)
                    + "\n{" + (id != null ? String.valueOf(id) : "N/A")
                    + "}\n[" + idx + "]";
        }

        public String durationStr(Allocation allocation) {
            return LocalTime.MIN.plus(Duration.between(
                    allocation.getConstrainedStartDate(), allocation.getEndDate())).toString();
        }

        public String datetimeStr(Allocation allocation) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
            return formatter.format(
                    allocation.getConstrainedStartDate().withZoneSameInstant(defaultTimeZone)) + "\n" +
                    formatter.format(
                            allocation.getEndDate().withZoneSameInstant(defaultTimeZone));
        }

        public String standstillStr(Allocation allocation) {
            return "P:" + allocation.getPreviousStandstill() +
                    "\nC:" + allocation.getTimelineEntry().getHumanStateChange().getCurrentLocation() +
                    "\nM:" + allocation.getTimelineEntry().getHumanStateChange().getMovetoLocation();
        }

        public String mvrestrictStr(Allocation allocation) {
            return (allocation.getTimelineEntry().getChronoProperty().getDraggable() == 0 ? "" : "Draggable") + "\n" +
                    (allocation.getTimelineEntry().getChronoProperty().getSubstitutable() == 0 ? "" : "Substitutable") + "\n" +
                    (allocation.getTimelineEntry().getChronoProperty().getSplittable() == 0 ? "" : "Splittable") + "\n" +
                    (allocation.isPinned() ? "Pinned" : "");
        }

        public String plrestrictStr(Allocation allocation) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
            return ">" + formatter.format(
                    allocation.getTimelineEntry().getChronoProperty().getZonedAliveline()
                            .withZoneSameInstant(defaultTimeZone)) + "\n" +
                    "<" + formatter.format(
                    allocation.getTimelineEntry().getChronoProperty().getZonedDeadline()
                            .withZoneSameInstant(defaultTimeZone)) + "\n" +
                    "[" + allocation.getTimelineEntry().getHumanStateChange().getRequirementTimerange() + "]\n" +
                    "_" + allocation.getTimelineEntry().getHumanStateChange().getAdviceTimerange() + "_";
        }

        public String scoreStr(Allocation allocation) {
            return (breakByTasks.containsKey(allocation) ?
                    hardConstraintMatchToString(breakByTasks.get(allocation).getConstraintMatchSet()) : "");
        }

        public String indictmentStr(Allocation allocation) {
            if (!scoreDirector.getIndictmentMap().containsKey(allocation)) return "";
            String[] indictmentHeader = {"Contraint Match", "Score"};
            return FlipTable.of(indictmentHeader, scoreDirector.getIndictmentMap().get(allocation).getConstraintMatchSet().stream()
                    .map(x -> new String[]{
                            x.getConstraintName(), x.getScore().toShortString()
                    }).toArray(String[][]::new));
        }

        public String resourceTotalStr(Allocation allocation) {
            ResourceTotal resourceTotal = allocation.getThisResourceTotal();
            if (resourceTotal == null) return "";
            String[] resourceTotalHeader = {"Resource Name", "Location", "TimelineId", "Amt"};
            return FlipTable.of(resourceTotalHeader, resourceTotal.entrySet().stream()
                    .map(x -> new String[]{
                            x.getKey().getResourceName(),
                            x.getKey().getLocation(),
                            Optional.ofNullable(x.getKey().getTimelineId()).map(Object::toString).orElse(""),
                            x.getValue().toString()
                    }).toArray(String[][]::new));
        }

        public String titleStr(Allocation allocation) {

            return allocation.getTimelineEntry().getTitle();
        }

        public String prettyAllocation(Allocation allocation) {
            String[] timelineHeader = {"Row", "%", "Date", "Duration", "Plan Restrict", "Move Restrict", "Score", "Location", "Task"};
            String[] timelineentry = (new String[]{
                    timelineStr(allocation),
                    percentStr(allocation),
                    datetimeStr(allocation),
                    durationStr(allocation),
                    plrestrictStr(allocation),
                    mvrestrictStr(allocation),
                    scoreStr(allocation),
                    standstillStr(allocation),
                    titleStr(allocation)});
            return FlipTable.of(timelineHeader, new String[][]{timelineentry});
        }
    }
}

