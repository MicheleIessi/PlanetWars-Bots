package Bots.SwarmBot.PlanetStates;

import Bots.SwarmBot.GameFiles.Planet;
import Bots.SwarmBot.GameFiles.PlanetWars;

public interface IPlanetState  {

    public void performPlanetAction(Planet planet, PlanetWars planetWars);

}
