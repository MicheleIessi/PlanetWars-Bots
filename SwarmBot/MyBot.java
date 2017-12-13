package SwarmBot;

import SwarmBot.GameFiles.Planet;
import SwarmBot.GameFiles.PlanetWars;
import SwarmBot.PlanetStates.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class MyBot {
	// The DoTurn function is where your code goes. The SwarmBot.GameFiles.GameFiles.PlanetWars object
    // contains the state of the game, including information about all planets
    // and fleets that currently exist. Inside this function, you issue orders
    // using the pw.IssueOrder() function. For example, to send 10 ships from
    // planet 3 to planet 8, you would say pw.IssueOrder(3, 8, 10).
    //
    // There is already a basic strategy in place here. You can use it as a
    // starting point, or you can throw it out entirely and replace it with
    // your own. Check out the tutorials and articles on the contest website at
    // http://www.ai-contest.com/resources.

	private static final String LOGGER_NAME = "MySwarmBotLogger";
	private static Map<Integer, IPlanetState> planetStateMap = null;

    public static void DoTurn(PlanetWars pw) {
		/* idee:
			1) Trovare il pianeta centrale e usarlo come base
			2) assegnare uno stato a ogni pianeta tramite hashmap
		 */
		try {
			planetStateMap = MyBot.updatePlanetStateMap(pw);

			Planet closestToCentroid = MyBot.getClosestToCentroid(pw);

			List<Planet> myPlanets = pw.MyPlanets();

			boolean hiveMindObtained = false;

			if (myPlanets.contains(closestToCentroid) && !(planetStateMap.get(closestToCentroid.PlanetID()) instanceof HiveMindPlanetState)) {
				planetStateMap.put(closestToCentroid.PlanetID(), new HiveMindPlanetState());
				hiveMindObtained = true;
				logMessage("HiveMind created");
			} else {
				if (hiveMindObtained) {
					logMessage("HiveMind lost");
				}
			}

			if (!myPlanets.contains(closestToCentroid)) {
				pw.IssueOrder(myPlanets.get(0), closestToCentroid, myPlanets.get(0).NumShips() / 2);
			}

			for (Entry<Integer, IPlanetState> e : planetStateMap.entrySet()) {
				e.getValue().performPlanetAction(pw.GetPlanet(e.getKey()), pw);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logMessage(e.toString());
		}
    }

    public static void logMessage(String msg) {
		Logger.getLogger(LOGGER_NAME).info( msg);
	}

    public static Map<Integer, IPlanetState> getPlanetStateMap(PlanetWars pw) {
    	if(planetStateMap == null) {
    		planetStateMap = new HashMap<>();
    		Planet homePlanet = pw.MyPlanets().get(0);
    		planetStateMap.put(homePlanet.PlanetID(), new DefenderPlanetState());
		}
    	return planetStateMap;
	}

	public static Map<Integer, IPlanetState> updatePlanetStateMap(PlanetWars planetWars) {
    	Map<Integer, IPlanetState> planetStateMap = MyBot.getPlanetStateMap(planetWars);
    	List<Planet> myPlanets = planetWars.MyPlanets();
		for(Entry<Integer, IPlanetState> e : planetStateMap.entrySet()) {
			if(!myPlanets.contains(planetWars.GetPlanet(e.getKey()))) {
				planetStateMap.remove(e.getKey());
			}
		}
		for(Planet planet : myPlanets) {
			if(!planetStateMap.containsKey(planet.PlanetID())) {
				planetStateMap.put(planet.PlanetID(), new WorkerPlanetState());
			}
		}
		return planetStateMap;
	}

    private static Planet getClosestToCentroid(PlanetWars pw) {

		List<Planet> neutralPlanets = pw.Planets();
		double coordX = 0;
		double coordY = 0;

		for(Planet p : neutralPlanets) {
			coordX += p.X();
			coordY += p.Y();
		}
		coordX /= neutralPlanets.size();
		coordY /= neutralPlanets.size();

		Planet centroid = new Planet(0,0,0,0, coordX, coordY);
		Planet closestToCentroid = null;
		int minimumDistance = Integer.MAX_VALUE;
		for(Planet p : neutralPlanets) {
			int distance = MyBot.computeDistance(centroid, p);
			if(distance < minimumDistance) {
				closestToCentroid = p;
				minimumDistance = distance;
			}
		}
		return closestToCentroid;
	}

	private static int computeDistance(Planet p1, Planet p2) {
		double dx = p1.X() - p2.X();
		double dy = p1.Y() - p2.Y();
		return (int)Math.ceil(Math.sqrt(dx * dx + dy * dy));
	}

    public static void main(String[] args) {
	String line = "";
	String message = "";
	int c;
	try {
	    while ((c = System.in.read()) >= 0) {
		switch (c) {
		case '\n':
		    if (line.equals("go")) {
			PlanetWars pw = new PlanetWars(message);
			DoTurn(pw);
		        pw.FinishTurn();
			message = "";
		    } else {
			message += line + "\n";
		    }
		    line = "";
		    break;
		default:
		    line += (char)c;
		    break;
		}
	    }
	} catch (Exception e) {
	    // Owned.
	}
    }
}

