package Bots.SwarmBot.PlanetStates;

import Bots.SwarmBot.GameFiles.Fleet;
import Bots.SwarmBot.GameFiles.Planet;
import Bots.SwarmBot.GameFiles.PlanetWars;
import Bots.SwarmBot.MyBot;

import java.util.*;
import java.util.Map.Entry;

public class HiveMindPlanetState implements IPlanetState {

    private static Map<Integer, Integer> realPlanetStatus;
    private static Map<Integer,Integer> neutralPlanetStatus;
    private static final int ATTACK_TRIGGER = 100;
    private static final int COLONIZATION_INTENSITY = 2;

    @Override
    public void performPlanetAction(Planet planet, PlanetWars planetWars) {

        computeRealPlanetStatus(planetWars);
        colonizeBestPlanets(planet, planetWars);
        reinforceDefenderPlanets(planet, planetWars);
        reinforceAttackerPlanets(planet, planetWars);
        attackWeakPlanets(planet, planetWars);
    }

    private void attackWeakPlanets(Planet planet, PlanetWars planetWars) {

        if (planetWars.EnemyPlanets().size() > 0) {
            if (realPlanetStatus.get(planet.PlanetID()) > ATTACK_TRIGGER) {

                List<Planet> enemyPlanets = planetWars.EnemyPlanets();
                Map<Integer, Double> attackScoreList = new HashMap<>();

                for (Planet enemyPlanet : enemyPlanets) {

                    int distance = planetWars.Distance(planet.PlanetID(), enemyPlanet.PlanetID());
                    int growthRate = enemyPlanet.GrowthRate();
                    int numberOfShips = enemyPlanet.NumShips();

                    double score = growthRate / (Math.pow(numberOfShips, 2) * distance) * 1000;

                    attackScoreList.put(enemyPlanet.PlanetID(), score);

                }

                attackScoreList = sortByValue(attackScoreList);

                Entry<Integer, Double> attackTarget = attackScoreList.entrySet().iterator().next();

                if (realPlanetStatus.get(planet.PlanetID()) > 4 * planetWars.GetPlanet(attackTarget.getKey()).NumShips()) {

                    planetWars.IssueOrder(planet.PlanetID(), attackTarget.getKey(), realPlanetStatus.get(planet.PlanetID()) / 2);
                    realPlanetStatus.put(planet.PlanetID(), realPlanetStatus.get(planet.PlanetID()) - realPlanetStatus.get(planet.PlanetID()) / 2);

                }
            }
        }
    }

    private void computeRealPlanetStatus(PlanetWars planetWars) {
        realPlanetStatus = new HashMap<>();

        for(Planet myPlanet : planetWars.MyPlanets()) {
            realPlanetStatus.put(myPlanet.PlanetID(), myPlanet.NumShips());
        }
    }

    private void colonizeBestPlanets(Planet planet, PlanetWars planetWars) {

        computeNeutralPlanetStatus(planetWars);
        List<Planet> neutralPlanets = planetWars.NeutralPlanets();
        Map<Integer, Double> colonizationScoreMap = new HashMap<>();

        if (neutralPlanets.size() > 0) {
            for (Planet neutral : neutralPlanets) {

                int distance = planetWars.Distance(planet.PlanetID(), neutral.PlanetID());
                int growthRate = neutral.GrowthRate();
                int numberOfShips = neutral.NumShips();

                double score = 100 * (Math.pow(growthRate, 2)) / (numberOfShips + distance);

                colonizationScoreMap.put(neutral.PlanetID(), score);

            }

            colonizationScoreMap = sortByValue(colonizationScoreMap);

            Entry<Integer, Double> entry = colonizationScoreMap.entrySet().iterator().next();
            int status = neutralPlanetStatus.get(entry.getKey());
            int shipsToSend = 0;
            if (status <= 0) {
                Planet destination = planetWars.GetPlanet(entry.getKey());
                if (realPlanetStatus.get(planet.PlanetID()) > 50) {
                    if (destination.NumShips() < (realPlanetStatus.get(planet.PlanetID())) / 5) {
                        shipsToSend = destination.NumShips() + 1;
                    } else {
                        shipsToSend = realPlanetStatus.get(planet.PlanetID()) / 5 - 1;
                    }
                    //System.err.println("Sending " + shipsToSend + "/" + planet.NumShips() + " ships from " + planet.PlanetID() + " to " + destination.PlanetID());
                    planetWars.IssueOrder(planet, destination, shipsToSend);
                    realPlanetStatus.put(planet.PlanetID(), realPlanetStatus.get(planet.PlanetID()) - shipsToSend);
                }
            }
        }
    }

    private void computeNeutralPlanetStatus(PlanetWars pw) {

        neutralPlanetStatus = new HashMap<>();
        List<Planet> neutralPlanets = pw.NeutralPlanets();
        List<Fleet> myFleets = pw.MyFleets();
        List<Fleet> enemyFleets = pw.EnemyFleets();

        for(Planet neutral : neutralPlanets) {

            neutralPlanetStatus.put(neutral.PlanetID(), 0);
            for(Fleet myFleet : myFleets) {
                if(myFleet.DestinationPlanet() == neutral.PlanetID()) {
                    int oldValue = neutralPlanetStatus.get(neutral.PlanetID());
                    neutralPlanetStatus.put(neutral.PlanetID(), oldValue + myFleet.NumShips());
                }
            }
            for(Fleet enemyFleet : enemyFleets) {
                if(enemyFleet.DestinationPlanet() == neutral.PlanetID()) {
                    int oldValue = neutralPlanetStatus.get(neutral.PlanetID());
                    neutralPlanetStatus.put(neutral.PlanetID(), oldValue - enemyFleet.NumShips());
                }
            }
        }
    }

    private void reinforceDefenderPlanets(Planet planet, PlanetWars planetWars) {

        Map<Integer, IPlanetState> planetStateMap = MyBot.getPlanetStateMap(planetWars);
        List<Fleet> enemyFleets = planetWars.EnemyFleets();
        List<Planet> myPlanets = planetWars.MyPlanets();
        Map<Integer, Integer> defensePlans = new HashMap<>();

        for (Planet p : myPlanets) {
            if (planetStateMap.get(p.PlanetID()) instanceof DefenderPlanetState ||
                    planetStateMap.get(p.PlanetID()) instanceof WorkerPlanetState) {
                //System.err.println("Planet with ID " + p.PlanetID() + " added to defensePlans");
                defensePlans.put(p.PlanetID(), 0);
            }
        }

        for (Fleet f : enemyFleets) {
            Planet destinationPlanet = planetWars.GetPlanet(f.DestinationPlanet());
            if (defensePlans.containsKey(destinationPlanet.PlanetID())) {

                //System.err.println("HiveMind detected a defender planet under attack");
                defensePlans.put(destinationPlanet.PlanetID(), defensePlans.get(destinationPlanet.PlanetID()) + f.NumShips());

                List<Fleet> myFleets = planetWars.MyFleets();
                defensePlans = updateDefenseSituation(defensePlans, myFleets);

                // At this point, the defense plans are ready to be executed
                for (Entry<Integer, Integer> e : defensePlans.entrySet()) {

                    if (e.getValue() > 0) {
                        int distance = planetWars.Distance(planet.PlanetID(), destinationPlanet.PlanetID());
                        if(f.TurnsRemaining() == distance) {
                            //System.err.println("Planet " + e.getKey() + " not properly defended, trying to correct...");
                            if(realPlanetStatus.get(planet.PlanetID()) > e.getValue()) {
                                //System.err.println("Sending " + e.getValue() + " ships to defend planet " + e.getKey());
                                planetWars.IssueOrder(planet.PlanetID(), e.getKey(), e.getValue());
                                realPlanetStatus.put(planet.PlanetID(), realPlanetStatus.get(planet.PlanetID()) - e.getValue());
                            } else {
                                //System.err.println("Cannot defend planet " + e.getKey() + ", not enough ships on the HiveMind");
                            }
                        }
                        else if(f.TurnsRemaining() < distance) {
                            //System.err.println("Planet " + e.getKey() + " under close attack, trying to apply distance measures");
                            int numberOfShipsToSend = e.getValue() + (distance - f.TurnsRemaining()) * destinationPlanet.GrowthRate();
                            if(realPlanetStatus.get(planet.PlanetID()) > numberOfShipsToSend) {
                                //System.err.println("Sending " + e.getValue() + " ships to defend planet " + e.getKey() + " (Late Defense)");
                                planetWars.IssueOrder(planet.PlanetID(), e.getKey(), numberOfShipsToSend);
                                realPlanetStatus.put(planet.PlanetID(), realPlanetStatus.get(planet.PlanetID()) - numberOfShipsToSend);
                            }
                            else {
                                //System.err.println("Cannot defend planet " + e.getKey() + ", not enough ships on the HiveMind");
                            }
                        }
                    } else {
                        //System.err.println("Planet " + e.getKey() + " is already properly defended");
                    }
                }
            }
        }
    }

    private void reinforceAttackerPlanets(Planet planet, PlanetWars planetWars) {
        Map<Integer, IPlanetState> planetStateMap = MyBot.getPlanetStateMap(planetWars);

        for(Entry<Integer, IPlanetState> entry : planetStateMap.entrySet()) {
            if(entry.getValue() instanceof SoldierPlanetState) {
                // reinforce if needed
            }
        }
    }

    private Map<Integer, Integer> updateDefenseSituation(Map<Integer, Integer> defensePlans, List<Fleet> myFleets) {

        for(Fleet f : myFleets) {
            if(defensePlans.containsKey(f.DestinationPlanet())) {
                defensePlans.put(f.DestinationPlanet(), defensePlans.get(f.DestinationPlanet()) - f.NumShips());
            }
        }

        return defensePlans;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {

        List<Map.Entry<K, V>> list = new LinkedList<Entry<K, V>>(map.entrySet());
        Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

}
