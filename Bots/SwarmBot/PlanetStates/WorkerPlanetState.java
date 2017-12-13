package Bots.SwarmBot.PlanetStates;

import Bots.SwarmBot.GameFiles.Planet;
import Bots.SwarmBot.GameFiles.PlanetWars;
import Bots.SwarmBot.MyBot;

import java.util.Map;
import java.util.Map.Entry;

public class WorkerPlanetState implements IPlanetState {

    @Override
    public void performPlanetAction(Planet planet, PlanetWars planetWars) {

        Map<Integer, IPlanetState> planetStateMap = MyBot.getPlanetStateMap(planetWars);
        for(Entry<Integer, IPlanetState> e : planetStateMap.entrySet()) {
            if(e.getValue() instanceof HiveMindPlanetState) {
                planetWars.IssueOrder(planet.PlanetID(), e.getKey(), planet.NumShips());
            }
        }
    }
}
