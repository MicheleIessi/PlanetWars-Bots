package Bots.StateBot.GameStates;

import Bots.StateBot.GameFiles.Fleet;
import Bots.StateBot.GameFiles.Planet;
import Bots.StateBot.GameFiles.PlanetWars;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ColonizationState implements IGameState {

    private final int COLONIZATION_FORCE = 3;

    @Override
    public void doTurn(PlanetWars pw) {

        List<Planet> neutralPlanets = pw.NeutralPlanets();
        neutralPlanets.sort(((o1, o2) -> o2.GrowthRate() - o1.GrowthRate()));
        List<Planet> myPlanets = pw.MyPlanets();
        myPlanets.sort(((o1, o2) -> o2.NumShips() - o1.NumShips()));

        Planet sourcePlanet;
        Planet destinationPlanet;

        List<Fleet> myFleets = pw.MyFleets();
        Map<Integer, Integer> colonizationPlans;

        int colonizationScore = COLONIZATION_FORCE;

        if(neutralPlanets.size() < COLONIZATION_FORCE) {
            colonizationScore = neutralPlanets.size();
        }

        for(int i=0; i < colonizationScore; i++) {

            destinationPlanet = neutralPlanets.get(i);
            colonizationPlans = new HashMap<>();

            if(myFleets.size() == 0 || checkIfAlreadyColonized(myFleets, destinationPlanet)) {

                int numberOfShipsOnPlanet = destinationPlanet.NumShips();
                int numberOfShipsColonizing = 0;

                for (Planet p : myPlanets) {
                    if(p.NumShips() > 50) {
                        int colonizingShips = p.NumShips() / 3;
                        colonizationPlans.put(p.PlanetID(), colonizingShips);
                        numberOfShipsColonizing += colonizingShips;
                        if (numberOfShipsColonizing >= numberOfShipsOnPlanet) {
                            break;
                        }
                    }
                }

                for(Entry<Integer,Integer> e : colonizationPlans.entrySet()) {
                    sourcePlanet = pw.GetPlanet(e.getKey());
                    pw.IssueOrder(sourcePlanet, destinationPlanet, e.getValue());
                }
            }
        }
    }

    /**
     *
     * @param myFleets
     * @param p
     * @return
     */
    private boolean checkIfAlreadyColonized(List<Fleet> myFleets, Planet p) {

        int numberOfShipsSent = 0;

        for(Fleet f : myFleets) {
            if(f.DestinationPlanet() == p.PlanetID()) {
                numberOfShipsSent += f.NumShips();
            }
        }
        return numberOfShipsSent <= p.NumShips();
    }
}