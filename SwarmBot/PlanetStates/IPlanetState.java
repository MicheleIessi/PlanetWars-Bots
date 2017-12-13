package SwarmBot.PlanetStates;

import SwarmBot.GameFiles.Planet;
import SwarmBot.GameFiles.PlanetWars;

public interface IPlanetState  {

    public void performPlanetAction(Planet planet, PlanetWars planetWars);

}
