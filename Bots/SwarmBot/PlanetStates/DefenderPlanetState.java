package Bots.SwarmBot.PlanetStates;

import Bots.SwarmBot.GameFiles.Planet;
import Bots.SwarmBot.GameFiles.PlanetWars;
import Bots.SwarmBot.MyBot;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Defends worker planets
 */
public class DefenderPlanetState implements IPlanetState {

    @Override
    public void performPlanetAction(Planet planet, PlanetWars planetWars) {

        System.err.println("DEFENDER PLANET WITH ID " + planet.PlanetID());
    }
}
