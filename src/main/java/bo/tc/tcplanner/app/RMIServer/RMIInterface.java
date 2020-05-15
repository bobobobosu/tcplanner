package bo.tc.tcplanner.app.RMIServer;

import bo.tc.tcplanner.app.SolverCore.SolverProgress;
import bo.tc.tcplanner.domain.planningstructures.Schedule;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote {
    void startSolver(Schedule schedule) throws RemoteException;

    void stopSolver() throws RemoteException;

    void resetSolver() throws RemoteException;

    Schedule getCurrentSchedule() throws RemoteException;

    String getConsoleBuffer() throws RemoteException;

    String getScoreExplaination() throws RemoteException;

    SolverProgress getSolvingProgress() throws RemoteException;

    boolean ping() throws RemoteException;
}