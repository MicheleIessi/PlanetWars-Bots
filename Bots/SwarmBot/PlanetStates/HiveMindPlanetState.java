package Bots.SwarmBot.PlanetStates;

import Bots.SwarmBot.GameFiles.Fleet;
import Bots.SwarmBot.GameFiles.Planet;
import Bots.SwarmBot.GameFiles.PlanetWars;
import Bots.SwarmBot.MyBot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HiveMindPlanetState implements IPlanetState {

    @Override
    public void performPlanetAction(Planet planet, PlanetWars planetWars) {

        reinforceDefenderPlanets(planet, planetWars);
        reinforceAttackerPlanets(planet, planetWars);
    }

    private void reinforceDefenderPlanets(Planet planet, PlanetWars planetWars) {

        Map<Integer, IPlanetState> planetStateMap = MyBot.getPlanetStateMap(planetWars);
        List<Fleet> enemyFleets = planetWars.EnemyFleets();
        List<Planet> myPlanets = planetWars.MyPlanets();
        Map<Integer, Integer> defensePlans = new HashMap<>();

        for (Planet p : myPlanets) {
            if (planetStateMap.get(p.PlanetID()) instanceof DefenderPlanetState ||
                    planetStateMap.get(p.PlanetID()) instanceof WorkerPlanetState) {
                MyBot.logMessage("Planet with ID " + p.PlanetID() + " added to defensePlans");
                defensePlans.put(p.PlanetID(), 0);
            }
        }

        for (Fleet f : enemyFleets) {
            Planet destinationPlanet = planetWars.GetPlanet(f.DestinationPlanet());
            if (defensePlans.containsKey(destinationPlanet.PlanetID())) {

                MyBot.logMessage("HiveMind detected a defender planet under attack");
                defensePlans.put(destinationPlanet.PlanetID(), defensePlans.get(destinationPlanet.PlanetID()) + f.NumShips());

                List<Fleet> myFleets = planetWars.MyFleets();
                defensePlans = updateDefenseSituation(defensePlans, myFleets);

                // At this point, the defense plans are ready to be executed
                for (Entry<Integer, Integer> e : defensePlans.entrySet()) {

                    if (e.getValue() > 0) {
                        int distance = planetWars.Distance(planet.PlanetID(), destinationPlanet.PlanetID());
                        if(f.TurnsRemaining() == distance) {
                            MyBot.logMessage("Planet " + e.getKey() + " not properly defended, trying to correct...");
                            if(planet.NumShips() > e.getValue()) {
                                MyBot.logMessage("Sending " + e.getValue() + " ships to defend planet " + e.getKey());
                                planetWars.IssueOrder(planet.PlanetID(), e.getKey(), e.getValue());
                            } else {
                                MyBot.logMessage("Cannot defend planet " + e.getKey() + ", not enough ships on the HiveMind");
                            }
                        }
                        else if(f.TurnsRemaining() < distance) {
                            MyBot.logMessage("Planet " + e.getKey() + " under close attack, trying to apply distance measures");
                            int numberOfShipsToSend = e.getValue() + (distance - f.TurnsRemaining()) * destinationPlanet.GrowthRate();
                            if(planet.NumShips() > numberOfShipsToSend) {
                                MyBot.logMessage("Sending " + e.getValue() + " ships to defend planet " + e.getKey() + " (Late Defense)");
                                planetWars.IssueOrder(planet.PlanetID(), e.getKey(), numberOfShipsToSend);
                            }
                            else {
                                MyBot.logMessage("Cannot defend planet " + e.getKey() + ", not enough ships on the HiveMind");
                            }
                        }
                    } else {
                        MyBot.logMessage("Planet " + e.getKey() + " is already properly defended");
                    }
                }
            }
        }
    }

    private void reinforceAttackerPlanets(Planet planet, PlanetWars planetWars) {

    }

    private Map<Integer, Integer> updateDefenseSituation(Map<Integer, Integer> defensePlans, List<Fleet> myFleets) {

        for(Fleet f : myFleets) {
            if(defensePlans.containsKey(f.DestinationPlanet())) {
                defensePlans.put(f.DestinationPlanet(), defensePlans.get(f.DestinationPlanet()) - f.NumShips());
            }
        }

        return defensePlans;
    }
}
