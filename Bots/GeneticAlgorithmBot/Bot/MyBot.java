package Bots.GeneticAlgorithmBot.Bot;

import Bots.StateBot.GameFiles.Fleet;
import Bots.StateBot.GameFiles.Planet;
import Bots.StateBot.GameFiles.PlanetWars;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Logger;


// MANDARE PRESENTAZIONE PER MARTEDI MATTINA
public class MyBot {

	private static final Random random = new Random();
	private static final Logger logger = Logger.getLogger("MyGeneticAlgorithmLogger");


    public static void DoTurn(PlanetWars pw) {

		random.doubles(0,1);

		/* --- BOT GA PARAMETERS --- */

		// Defense
		double defenseProbability 		= 0.5;	// Probability
		double defenseIntensity			= 0.5;	// Intensity
		double defenseRadius			= 0.5;	// Radius

		// Colonization
		double colonizationProbability 	= 1;	// Probability
		double colonizationGrowth		= 0.5;	// Growth
		double colonizationShips		= 0.5;	// Ships
		double colonizationRadius		= 0.25;	// Radius
		double expansionPriority		= 0.5;	// How much the bot wants to expand with respect to how many planets he already has

		// Attack
		double attackProbability		= 0.5;	// Probability
		double attackIntensity			= 0.5;	// Intensity
		double attackRadius				= 0.01;	// Radius

		// General activity parameters
		double activityProbability		= 1;	// Probability to do a turn
		double maximumFleetsFlying		= 50;	// Maximum fleets flying at a said time


		// Algorithm
		if(random.nextDouble() > (1-activityProbability)) {
			try {
				performColonizationActivity(colonizationProbability, colonizationRadius, colonizationGrowth, colonizationShips, expansionPriority, pw);
				performDefenseActivity(defenseProbability, defenseIntensity, pw);
				performAttackActivity(attackProbability, attackRadius, pw);
			} catch (Exception e) {
				Logger.getLogger("MyBot").info(e.toString());
				System.err.println(e.toString());
				e.printStackTrace();
			} finally {
				return;
			}
		}
	}

	private static void performColonizationActivity(double colProb, double colRad, double colGrow, double colShips, double expPriority, PlanetWars pw) {
		if(random.nextDouble() >= (1.0-colProb) && pw.NeutralPlanets().size()>0) {
			try {
				List<Planet> myPlanets = pw.MyPlanets();
				List<Planet> neutralPlanets = pw.NeutralPlanets();
				Map<Integer, double[]> distanceMap = new HashMap<Integer, double[]>();

				// Calculate, for each planet, the best neutral one, depending on current parameters
				for (Planet myPlanet : myPlanets) {
					double currentBestScore = Double.MIN_VALUE;
					Planet currentBestPlanet = null;
					for (Planet neutralPlanet : neutralPlanets) {
						double distance = pw.Distance(myPlanet.PlanetID(), neutralPlanet.PlanetID());
						double growthRate = neutralPlanet.GrowthRate();
						double numberOfShips = neutralPlanet.NumShips();

						double score = 1000 * (colRad / Math.pow(distance, 2.75)) * (growthRate / colGrow) / ((numberOfShips+1) * (1 + colShips));
						//double score = ((1+growthRate)*colGrow*2.5)/(1+(colShips * numberOfShips)*(distance/(10*colRad)));

						if (score > currentBestScore) {
							currentBestScore = score;
							currentBestPlanet = neutralPlanet;
						}
					}
					double[] bestPlanetInfo = {currentBestPlanet.PlanetID(), currentBestScore};
					distanceMap.put(myPlanet.PlanetID(), bestPlanetInfo);
				}

				Planet sourcePlanet;
				Planet destinationPlanet;

				for (Entry<Integer, double[]> e : distanceMap.entrySet()) {

					int myPlanetID = e.getKey();
					double[] neutralPlanetInfo = e.getValue();
					int neutralPlanetID = (int) neutralPlanetInfo[0];
					double planetScore = neutralPlanetInfo[1];
					if(planetScore > Math.pow(1-expPriority,3)) {
						sourcePlanet = pw.GetPlanet(myPlanetID);
						destinationPlanet = pw.GetPlanet(neutralPlanetID);
						if (sourcePlanet != null && destinationPlanet != null) {
							if (sourcePlanet.NumShips() > 30) {
								pw.IssueOrder(sourcePlanet, destinationPlanet, sourcePlanet.NumShips() / 2);
							}
						}
					}
				}
			} catch (Exception e) {
				Logger.getLogger("MyBot").info(e.toString());
				System.err.println(e.toString());
			}
		}
	}

	private static void performDefenseActivity(double defProb, double defRad, PlanetWars pw) {



	}

	private static void performAttackActivity(double attProb, double attRad, PlanetWars pw) {

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

