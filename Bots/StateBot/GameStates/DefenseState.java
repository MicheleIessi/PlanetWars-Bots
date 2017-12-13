package Bots.StateBot.GameStates;

import Bots.StateBot.GameFiles.Fleet;
import Bots.StateBot.GameFiles.Planet;
import Bots.StateBot.GameFiles.PlanetWars;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DefenseState implements IGameState {
    @Override
    public void doTurn(PlanetWars pw) {

        // re-maneuver ships from most populated planets to the ones under attack

        List<Planet> myPlanets = pw.MyPlanets();
        myPlanets.sort((o1, o2) -> o2.NumShips() - o1.NumShips());

        List<Fleet>  myFleets = pw.MyFleets();
        List<Fleet>  enemyFleets = pw.EnemyFleets();

        Planet sourcePlanet;
        Planet destinationPlanet;

        Map<Integer, Integer> defensePlans;

        // Defense mechanism
        for(Fleet f : enemyFleets) {
            if(myPlanets.contains(pw.GetPlanet(f.DestinationPlanet()))) {
                if(!checkIfAlreadyDefended(myFleets, f.DestinationPlanet(), f.NumShips())) {
                    destinationPlanet = pw.GetPlanet(f.DestinationPlanet());
                    defensePlans = new HashMap<>();
                    int numberOfShipsAttacking = f.NumShips();
                    int numberOfShipsDefending = 0;

                    for (Planet p : myPlanets) {
                        if(p.PlanetID() != destinationPlanet.PlanetID()) {
                            int defendingShips = p.NumShips() / 3;
                            defensePlans.put(p.PlanetID(), defendingShips);
                            numberOfShipsAttacking += defendingShips;
                            if (numberOfShipsDefending >= numberOfShipsAttacking) {
                                break;
                            }

                        }
                    }

                    for (Entry<Integer, Integer> e : defensePlans.entrySet()) {
                        sourcePlanet = pw.GetPlanet(e.getKey());
                        pw.IssueOrder(sourcePlanet, destinationPlanet, e.getValue());
                    }
                }
            }
        }
    }

    /**
     *
     * @param myFleets
     * @param planetID
     * @param numberOfShipsAttacking
     * @return true if already defended, false otherwise
     */
    private boolean checkIfAlreadyDefended(List<Fleet> myFleets, int planetID, int numberOfShipsAttacking) {

        int numberOfShipsDefending = 0;

        for(Fleet f : myFleets) {
            if(f.DestinationPlanet() == planetID) {
                numberOfShipsDefending += f.NumShips();
            }
        }
        return numberOfShipsDefending > numberOfShipsAttacking;
    }
}
