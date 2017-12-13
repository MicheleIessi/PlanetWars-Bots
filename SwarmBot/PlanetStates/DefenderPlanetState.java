package SwarmBot.PlanetStates;

import SwarmBot.GameFiles.Planet;
import SwarmBot.GameFiles.PlanetWars;
import SwarmBot.MyBot;

import java.util.Map;
import java.util.Map.Entry;

public class DefenderPlanetState implements IPlanetState {

    @Override
    public void performPlanetAction(Planet planet, PlanetWars planetWars) {
        Map<Integer, IPlanetState> planetStateMap = MyBot.getPlanetStateMap(planetWars);
        for(Entry<Integer, IPlanetState> e : planetStateMap.entrySet()) {
            if(e.getValue() instanceof HiveMindPlanetState) {
                planetWars.IssueOrder(planet, planetWars.GetPlanet(e.getKey()), planet.NumShips()/2);
            }
        }
    }
}
