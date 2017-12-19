package Bots.SwarmBot.PlanetStates;

import Bots.SwarmBot.GameFiles.Planet;
import Bots.SwarmBot.GameFiles.PlanetWars;
import Bots.SwarmBot.MyBot;

import java.util.Map;
import java.util.Map.Entry;

public class WorkerPlanetState implements IPlanetState {

    private static final int REINFORCE_INTENSITY = 16;

    @Override
    public void performPlanetAction(Planet planet, PlanetWars planetWars) {

        Map<Integer, IPlanetState> planetStateMap = MyBot.getPlanetStateMap(planetWars);
        //System.err.println(planetStateMap.entrySet());

        Planet closestToCentroid = MyBot.getClosestToCentroid(planetWars);
        for(Entry<Integer, IPlanetState> e : planetStateMap.entrySet()) {
            if(e.getKey() == closestToCentroid.PlanetID()) {
                if(planet.NumShips() > REINFORCE_INTENSITY) {
                    //System.err.println("Invio " + planet.NumShips()/2 + " navi da " + planet.PlanetID() + " a " + e.getKey());
                    planetWars.IssueOrder(planet, closestToCentroid, planet.NumShips() / 2);
                }
            }
        }
    }
}
