package bo.tc.tcplanner.app.SolverCore;

import org.optaplanner.core.api.score.Score;

import java.io.Serializable;

public class SolverScoreDelta implements Serializable {
    public long startTime, prevTime, currTime;
    public Score prevScore, currScore;

    public void update(Score newScore) {
        if (newScore == null) {
            startTime = System.currentTimeMillis();
            return;
        }
        prevTime = currTime;
        prevScore = currScore;
        currTime = System.currentTimeMillis();
        currScore = newScore;
    }

    public Score getRate() {
        if (prevScore == null || currScore == null) return null;
        return (currScore.subtract(prevScore)).divide(
                ((double) (currTime - prevTime)) / 1000);
    }

    public long getSinceLastImproved() {
        return currTime - prevTime;
    }

    public long getElapsed() {
        return currTime - startTime;
    }

    public Score getStepScore() {
        return currScore.subtract(prevScore);
    }
}