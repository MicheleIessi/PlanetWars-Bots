package Bots.SwarmBot;

import Bots.SwarmBot.GameFiles.Planet;
import Bots.SwarmBot.GameFiles.PlanetWars;
import Bots.SwarmBot.PlanetStates.*;

import java.util.*;
import java.util.Map.Entry;

public class MyBot {
	// The DoTurn function is where your code goes. The Bots.SwarmBot.GameFiles.GameFiles.PlanetWars object
    // contains the state of the game, including information about all planets
    // and fleets that currently exist. Inside this function, you issue orders
    // using the pw.IssueOrder() function. For example, to send 10 ships from
    // planet 3 to planet 8, you would say pw.IssueOrder(3, 8, 10).
    //
    // There is already a basic strategy in place here. You can use it as a
    // starting point, or you can throw it out entirely and replace it with
    // your own. Check out the tutorials and articles on the contest website at
    // http://www.ai-contest.com/resources.

	private static Map<Integer, IPlanetState> planetStateMap = null;
	private static boolean firstTurn = true;
	private static final int COLONIZATION_FORCE = 4;
	private static final int GROUP_RADIUS = 50;
	private static int CLOSEST_TO_CENTROID;

    public static void DoTurn(PlanetWars pw) {
		/* idee:
			1) Trovare il pianeta centrale e usarlo come base
			2) assegnare uno stato a ogni pianeta tramite hashmap
		 */
		try {
			Planet closestToCentroid = MyBot.getClosestToCentroid(pw);
			CLOSEST_TO_CENTROID = closestToCentroid.PlanetID();

			//planetStateMap = ChaoticOrder.updatePlanetStateMap(pw);
			planetStateMap = computePlanetRoles(pw);

			List<Planet> myPlanets = pw.MyPlanets();


			if (myPlanets.contains(closestToCentroid) && !(planetStateMap.get(closestToCentroid.PlanetID()) instanceof HiveMindPlanetState)) {
				planetStateMap.put(closestToCentroid.PlanetID(), new HiveMindPlanetState());
				//System.err.println("HiveMind created");
			}

			if(!firstTurn && !myPlanets.contains(closestToCentroid)) {
				pw.IssueOrder(myPlanets.get(0), closestToCentroid, myPlanets.get(0).NumShips()/2);
			}

			if(firstTurn) {
				List<Planet> neutralPlanets = pw.NeutralPlanets();
				neutralPlanets.sort(((o1, o2) -> o2.GrowthRate() - o1.GrowthRate()));

				int myShips = myPlanets.get(0).NumShips();
				Map<Integer, Integer> colonizationPlans = new HashMap<>();

				int numberOfFleetsSent = 0;
				for(Planet neutralPlanet : neutralPlanets) {

					if(numberOfFleetsSent < COLONIZATION_FORCE) {
						if ((neutralPlanet.NumShips() + 1) < myShips) {
							colonizationPlans.put(neutralPlanet.PlanetID(), neutralPlanet.NumShips() + 1);
							myShips -= (neutralPlanet.NumShips() + 1);
							numberOfFleetsSent ++;
						}
					}
				}

				for(Entry<Integer, Integer> entry : colonizationPlans.entrySet()) {
					pw.IssueOrder(myPlanets.get(0), pw.GetPlanet(entry.getKey()), entry.getValue());
				}
				firstTurn = false;
			}

			for (Entry<Integer, IPlanetState> e : planetStateMap.entrySet()) {
				try {
					//System.err.println(e.getKey() + " - " + e.getValue().getClass().getSimpleName());
					e.getValue().performPlanetAction(pw.GetPlanet(e.getKey()), pw);
				} catch (Exception ex) {
					System.err.println(ex.getMessage());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			//System.err.println(e.toString());
		}
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
    	try {

			Map<Integer, IPlanetState> planetStateMap = MyBot.getPlanetStateMap(planetWars);
			List<Planet> myPlanets = planetWars.MyPlanets();
			for (Entry<Integer, IPlanetState> e : planetStateMap.entrySet()) {
				if (!myPlanets.contains(planetWars.GetPlanet(e.getKey()))) {
					planetStateMap.remove(e.getKey());
				}
			}
			for (Planet planet : myPlanets) {
				if (!planetStateMap.containsKey(planet.PlanetID())) {
					planetStateMap.put(planet.PlanetID(), new WorkerPlanetState());
				}
			}

			computePlanetRoles(planetWars);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return planetStateMap;
	}

	private static Map<Integer, IPlanetState> computePlanetRoles(PlanetWars planetWars) {

		Map<Integer, IPlanetState> planetStateMap = getPlanetStateMap(planetWars);

		try {

			//System.err.println(planetStateMap.entrySet());
			List<Planet> myPlanets = planetWars.MyPlanets();

			for(Entry<Integer, IPlanetState> planetStateEntry : planetStateMap.entrySet()) {
				if (!myPlanets.contains(planetWars.GetPlanet(planetStateEntry.getKey()))) {
					planetStateMap.remove(planetStateEntry.getKey());
				}
			}
			for(Planet myPlanet : myPlanets) {
				if(!planetStateMap.containsKey(myPlanet.PlanetID())) {
					planetStateMap.put(myPlanet.PlanetID(), new WorkerPlanetState());
				}
			}

			Map<Integer, Map<Integer, Integer>> planetDistancesMap = new HashMap<>();

			if(myPlanets.contains(planetWars.GetPlanet(CLOSEST_TO_CENTROID))) {
				planetStateMap.put(CLOSEST_TO_CENTROID, new HiveMindPlanetState());
			}

			for (Planet center : myPlanets) {
				if (center.PlanetID() != CLOSEST_TO_CENTROID) {
					Map<Integer, Integer> planetDistance = new HashMap<>();
					for (Planet radius : myPlanets) {
						if (center != radius) {
							int distance = planetWars.Distance(center.PlanetID(), radius.PlanetID());
							if (distance < GROUP_RADIUS) {
								planetDistance.put(radius.PlanetID(), distance);
							}
						}
						//System.err.println(planetDistance.entrySet());
						planetDistancesMap.put(center.PlanetID(), planetDistance);
					}
				}
			}
			// radius - center
			if(planetDistancesMap.size() > 2) {

				List<Integer> alreadyInNeighborhood = new ArrayList<>();
				int neighborhoodNumber = 1;

				for (Entry<Integer, Map<Integer, Integer>> mapEntry : planetDistancesMap.entrySet()) {

					int centerPlanet = mapEntry.getKey();

					if (!alreadyInNeighborhood.contains(centerPlanet)) {

						Map<Integer, Integer> neighborhoodMap = new HashMap<>();
						int growingValue = 0;

						Map<Integer, Integer> neighborhood = mapEntry.getValue();
						alreadyInNeighborhood.add(centerPlanet);

						for (Entry<Integer, Integer> neighborhoodPlanet : neighborhood.entrySet()) {
							growingValue += planetWars.GetPlanet(neighborhoodPlanet.getKey()).GrowthRate();
							neighborhoodMap.put(neighborhoodPlanet.getKey(), centerPlanet);
							alreadyInNeighborhood.add(neighborhoodPlanet.getKey());
						}

						//System.err.println("Neighborhood " + neighborhoodNumber + " created");
						neighborhoodNumber++;

						Iterator iterator = neighborhoodMap.entrySet().iterator();
						boolean defender = true;
						while(iterator.hasNext()) {
							Entry planet = (Entry) iterator.next();
							if(defender) {
								planetStateMap.put((Integer) planet.getValue(), new DefenderPlanetState());
								defender = false;
							}
							else {
								planetStateMap.put((Integer) planet.getValue(), new WorkerPlanetState());
							}
						}
					}
				}

				int alonePlanets = 0;
				for(Planet alonePlanet : myPlanets) {
					if(!alreadyInNeighborhood.contains(alonePlanet.PlanetID())) {
						planetStateMap.put(alonePlanet.PlanetID(), new WorkerPlanetState());
						alonePlanets++;
					}
				}
				//System.err.println("Found " + alonePlanets + " alone planets");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return planetStateMap;
	}

	public static Planet getClosestToCentroid(PlanetWars pw) {

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

