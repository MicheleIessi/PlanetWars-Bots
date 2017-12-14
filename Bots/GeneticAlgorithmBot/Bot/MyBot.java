package Bots.GeneticAlgorithmBot.Bot;

import Bots.StateBot.GameFiles.Fleet;
import Bots.StateBot.GameFiles.Planet;
import Bots.StateBot.GameFiles.PlanetWars;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;


// MANDARE PRESENTAZIONE PER MARTEDI MATTINA
public class MyBot {

	private static final Random random = new Random();
	private static final Logger logger = Logger.getLogger("MyGeneticAlgorithmLogger");


    public static void DoTurn(PlanetWars pw) {

		random.doubles(0,1);

		/* --- BOT GA PARAMETERS (16) --- */

		// Colonization (5)
		double colonizationProbability 	= 0.75;	// Probability
		double colonizationGrowth		= 0.5;	// Growth
		double colonizationShips		= 0.5;	// Ships
		double colonizationRadius		= 0.75;	// Radius
		double expansionPriority		= 0.5;	// How much the bot wants to expand with respect to how many planets he already has

		// Defense (4)
		double defenseProbability 		= 0.5;	// Probability
		double defenseRadius			= 0.75;	// Radius
		double defenseIntensity			= 0.5;	// Intensity
		double defensePriority			= 0.5;	// How much the bot is conservative

		// Attack (5)
		double attackProbability		= 0.5;	// Probability
		double attackIntensity			= 0.5;	// Intensity
		double attackShips				= 0.5;	// Ships
		double attackRadius				= 0.01;	// Radius
		double attackPriority			= 0.5;	// How much the bot is hostile

		// General activity parameters (2)
		double activityProbability		= 1;	// Probability to do a turn
		double maximumFleetsFlying		= 100;	// Maximum fleets flying at a said time


		// Algorithm
		if(random.nextDouble() > (1-activityProbability)) {
			List<Fleet> myFleets = pw.MyFleets();
			try {
				if(myFleets.size() < maximumFleetsFlying) {
					performDefenseActivity(defenseProbability, defenseRadius, defenseIntensity, defensePriority, pw);
					performColonizationActivity(colonizationProbability, colonizationRadius, colonizationGrowth, colonizationShips, expansionPriority, pw);
					performAttackActivity(attackProbability, attackRadius, pw);
					//performReinforcementActivity(pw);
				}
			} catch (Exception e) {
				System.err.println(e.toString());
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Computes and executes colonization tasks
	 * @param colProb Colonization probability
	 * @param colRad Colonization radius used in score computation
	 * @param colGrow Growth parameter used in score computation
	 * @param colShips Ships parameter used in score computation
	 * @param expPriority Expansion priority parameter
	 * @param pw PlanetWars instance to control the game
	 */
	private static void performColonizationActivity(double colProb, double colRad, double colGrow, double colShips, double expPriority, PlanetWars pw) {
		if(pw.NeutralPlanets().size()>0) {
			try {
				List<Planet> myPlanets = pw.MyPlanets();
				List<Planet> neutralPlanets = pw.NeutralPlanets();
				Map<Integer, double[]> distanceMap = new HashMap<Integer, double[]>();

				// Calculate, for each planet, the best neutral one, depending on current parameters
				for (Planet myPlanet : myPlanets) {
					if (random.nextDouble() >= (1.0 - colProb)) {
						double currentBestScore = Double.MIN_VALUE;
						Planet currentBestPlanet = null;
						for (Planet neutralPlanet : neutralPlanets) {
							double distance = pw.Distance(myPlanet.PlanetID(), neutralPlanet.PlanetID());
							double growthRate = neutralPlanet.GrowthRate();
							double numberOfShips = neutralPlanet.NumShips();
							double myNumberOfShips = myPlanet.NumShips();

							double colonizationScore = 1000 * (myNumberOfShips * expPriority) * (colRad / Math.pow(distance, 3.25)) * (growthRate / colGrow) / ((numberOfShips + 1) * (1 + colShips));
							//double score = ((1+growthRate)*colGrow*2.5)/(1+(colShips * numberOfShips)*(distance/(10*colRad)));

							if (colonizationScore > currentBestScore) {
								currentBestScore = colonizationScore;
								currentBestPlanet = neutralPlanet;
							}
						}
						double[] bestPlanetInfo = {currentBestPlanet.PlanetID(), currentBestScore};
						distanceMap.put(myPlanet.PlanetID(), bestPlanetInfo);
					}
				}
				Planet sourcePlanet;
				Planet destinationPlanet;

				for (Entry<Integer, double[]> e : distanceMap.entrySet()) {

					int myPlanetID = e.getKey();
					double[] neutralPlanetInfo = e.getValue();
					int neutralPlanetID = (int) neutralPlanetInfo[0];
					double planetScore = neutralPlanetInfo[1];
					if(planetScore > Math.pow(1-expPriority,2)) {
						sourcePlanet = pw.GetPlanet(myPlanetID);
						destinationPlanet = pw.GetPlanet(neutralPlanetID);
						if (sourcePlanet != null && destinationPlanet != null) {
							if (sourcePlanet.NumShips() > (100*(1-expPriority))) {
								pw.IssueOrder(sourcePlanet, destinationPlanet, sourcePlanet.NumShips() / 2);
							}
						}
					}
				}
			} catch (Exception e) {
				Logger.getLogger("MyBot").info(e.toString());
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}

	private static void performDefenseActivity(double defProb, double defRad, double defIntensity, double defPriority, PlanetWars pw) {

		List<Fleet> enemyFleets = pw.EnemyFleets();
		List<Fleet> myFleets = pw.MyFleets();
		List<Planet> myPlanets = pw.MyPlanets();

		/* --- ENEMY ATTACK PLANS --- */
		Map<Integer, Integer> enemyAttackPlansMap = new HashMap<>();
		// Initialize the map
		for (Planet myPlanet : myPlanets) {
			enemyAttackPlansMap.put(myPlanet.PlanetID(), 0);
		}
		// Find enemy attacks
		for (Fleet enemyFleet : enemyFleets) {
			Planet destinationPlanet = pw.GetPlanet(enemyFleet.DestinationPlanet());
			if (myPlanets.contains(destinationPlanet)) {
				int oldNumberOfShips = enemyAttackPlansMap.get(destinationPlanet.PlanetID());
				int newNumberOfShips = oldNumberOfShips + enemyFleet.NumShips();
				enemyAttackPlansMap.put(destinationPlanet.PlanetID(), newNumberOfShips);
			}
		}
		// Find player already sent defenses
		for (Fleet myFleet : myFleets) {
			Planet destinationPlanet = pw.GetPlanet(myFleet.DestinationPlanet());
			if (myPlanets.contains(destinationPlanet)) {
				int oldNumberOfShips = enemyAttackPlansMap.get(destinationPlanet.PlanetID());
				int newNumberOfShips = oldNumberOfShips - myFleet.NumShips();
				enemyAttackPlansMap.put(destinationPlanet.PlanetID(), newNumberOfShips);
			}
		}
		// Remove successfully defended (or not attacked) planets from the map

		if (random.nextDouble() > (1-defProb)) {


			Map<Integer, Map<Integer, Double>> planetDistanceMap = new HashMap<>();

			for(Planet centerPlanet : myPlanets) {
				Map<Integer,Double> idList = new HashMap<>();
				for(Planet radiusPlanet : myPlanets) {
					if(centerPlanet != radiusPlanet) {
						int distance = pw.Distance(centerPlanet.PlanetID(), radiusPlanet.PlanetID());
						int myNumberOfShips = radiusPlanet.NumShips();
						//double colonizationScore = 1000 * (myNumberOfShips * expPriority) * (colRad / Math.pow(distance, 3.25)) * (growthRate / colGrow) / ((numberOfShips + 1) * (1 + colShips));

						double distanceScore = (myNumberOfShips * (1 / defIntensity)) * (Math.pow(defRad,3.75) / Math.pow(distance, 2.5));
						if(distanceScore > defRad && distanceScore > 1) {
							idList.put(radiusPlanet.PlanetID(), distanceScore);
						}
						//System.err.println("DistScore from " + centerPlanet.PlanetID() + " to " + radiusPlanet.PlanetID() + " = " + distanceScore);
					}
				}
				planetDistanceMap.put(centerPlanet.PlanetID(), idList);
				//System.err.println("Planet " + centerPlanet.PlanetID() + " is defended from " + idList.size() + " other planets");
			}

			for(Entry<Integer,Map<Integer,Double>> entry : planetDistanceMap.entrySet()) {
				if(enemyAttackPlansMap.containsKey(entry.getKey())) {

					int planetToDefend = entry.getKey();
					int numberOfShipsAttacking = enemyAttackPlansMap.get(planetToDefend);
					Map<Integer,Double> defendingPlanets = entry.getValue();
					defendingPlanets = sortByValue(defendingPlanets);

					for(Entry<Integer,Double> defenderPlanet: defendingPlanets.entrySet()) {
						//System.err.println("Planet " + planetToDefend + ", Value: " + defenderPlanet.getValue());
						Planet defendingPlanet = pw.GetPlanet(defenderPlanet.getKey());
						double defendingValue  = defenderPlanet.getValue();
						int numberOfShipsOnAttacked = pw.GetPlanet(planetToDefend).NumShips();
						int numberOfShipsOnDefender = defendingPlanet.NumShips();
						int urgeToDefend = numberOfShipsOnAttacked - numberOfShipsAttacking;

						if(urgeToDefend < 0 && random.nextDouble()>(1-defPriority)) {

							int shipsToSend = (int) (numberOfShipsOnDefender*defendingValue*defIntensity*(-urgeToDefend)/(1+numberOfShipsOnDefender));
							if(shipsToSend > numberOfShipsOnDefender) {
								shipsToSend = (int) (numberOfShipsOnDefender * defIntensity);
							}
							System.err.println("Provo a mandare " + shipsToSend + "/" + numberOfShipsOnDefender + " navi da " + defendingPlanet.PlanetID() + " a " + planetToDefend);
							pw.IssueOrder(defendingPlanet.PlanetID(), planetToDefend, shipsToSend);
							enemyAttackPlansMap.put(planetToDefend, enemyAttackPlansMap.get(planetToDefend)-shipsToSend);
						}
					}
				}
			}
		}
	}

	private static void performAttackActivity(double attProb, double attRad, PlanetWars pw) {


	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {

		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
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

