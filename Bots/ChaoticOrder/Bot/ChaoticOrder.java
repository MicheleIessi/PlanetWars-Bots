package Bots.ChaoticOrder.Bot;

import Bots.ChaoticOrder.Bot.GameFiles.Fleet;
import Bots.ChaoticOrder.Bot.GameFiles.Planet;
import Bots.ChaoticOrder.Bot.GameFiles.PlanetWars;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class ChaoticOrder {

	private static final Random random = new Random();
	private static Map<Integer, Integer> shipsRemaining;
	private static int turn = 0;
	private static PrintWriter printWriter;

    public static void DoTurn(double[] chromosome, String botNumber, PlanetWars pw) {

		try {
			printWriter = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Users\\Michele\\Downloads\\PLANETWARS\\PLANETWARS\\GENELOGS\\botlog"+botNumber+".txt", true)));
		} catch (IOException e) {
			System.err.println(e.toString());
		}

        try {
            /* +-----------------------------------------------------------------------------------------------------+ */
            /* |                            BEGINNING BOT GA PARAMETERS DECLARATION (14)                             | */
            /* +-----------------------------------------------------------------------------------------------------+ */
            /* |                                          Colonization (4)                                           | */
            /* + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - + */
            double colonizationGrowth       = 0.0; // Growth
            double colonizationShips        = 0.0; // Ships
            double colonizationRadius       = 0.0; // Radius
            double expansionPriority        = 0.0; // How much the bot wants to expand with respect to how many planets he already has

            /* + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - + */
            /* |                                             Defense (3)                                             | */
            /* + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - + */
            double defenseRadius            = 0.0; // Radius
            double defenseIntensity         = 0.0; // Intensity
            double defensePriority          = 0.0; // How much the bot is conservative

            /* + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - + */
            /* |                                              Attack (4)                                             | */
            /* + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - + */
            double attackIntensity          = 0.0; // Intensity
            double attackShips              = 0.0; // Ships
            double attackRadius             = 0.0; // Radius
            double attackPriority           = 0.0; // How much the bot is hostile

            /* + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - + */
            /* |                                     General Activity Parameters (1)                                 | */
            /* + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - + */
            double maximumFleetsFlying      = 0.0; // Maximum fleets flying at a said time

            /* + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - + */
            /* |                                   END OF BOT GA PARAMETERS DECLARATION                              | */
            /* +-----------------------------------------------------------------------------------------------------+ */

            random.doubles(0, 1);

            colonizationGrowth       = chromosome[0];
            colonizationShips        = chromosome[1];
            colonizationRadius       = chromosome[2];
            expansionPriority        = chromosome[3];
            defenseRadius            = chromosome[4];
            defenseIntensity         = chromosome[5];
            defensePriority          = chromosome[6];
			attackIntensity          = chromosome[7];
            attackShips              = chromosome[8];
            attackRadius             = chromosome[9];
            attackPriority           = chromosome[10];
            maximumFleetsFlying      = chromosome[11];

            printGameStatus(pw);
			turn++;

			List<Fleet> myFleets = pw.MyFleets();
			List<Planet> myPlanets = pw.MyPlanets();
			shipsRemaining = new HashMap<>();
			for (Planet myPlanet : myPlanets) {
				shipsRemaining.put(myPlanet.PlanetID(), myPlanet.NumShips());
			}
			if (myFleets.size() < maximumFleetsFlying) {
				performDefenseActivity(defenseRadius, defenseIntensity, defensePriority, pw);
			}
			if (myFleets.size() < maximumFleetsFlying) {
				performAttackActivity(attackIntensity, attackShips, attackRadius, attackPriority, pw);
			}
			if (myFleets.size() < maximumFleetsFlying) {
				performColonizationActivity(colonizationRadius, colonizationGrowth, colonizationShips, expansionPriority, pw);
			}
			if (myFleets.size() < maximumFleetsFlying) {
				performReinforcementActivity(pw);
			}

        } catch (Exception e) {
            System.err.println(e.toString());
        }
	}

	private static void printGameStatus(PlanetWars pw) {

		boolean victory = false;

    	if(turn <= 1000) {

			int playerShips = 0;
			List<Fleet> playerFleets = pw.MyFleets();
			List<Planet> playerPlanets = pw.MyPlanets();

			int enemyShips = 0;
			List<Fleet> enemyFleets = pw.EnemyFleets();
			List<Planet> enemyPlanets = pw.EnemyPlanets();

			for (Planet enemyPlanet : enemyPlanets) {
				enemyShips += enemyPlanet.NumShips();
			}
			for (Planet playerPlanet : playerPlanets) {
				playerShips += playerPlanet.NumShips();
			}

			for (Fleet enemyFleet : enemyFleets) {
				if(enemyFleet.TurnsRemaining() == 1 && playerPlanets.contains(pw.GetPlanet(enemyFleet.DestinationPlanet()))) {
					if(pw.GetPlanet(enemyFleet.DestinationPlanet()).NumShips() > enemyFleet.NumShips()) {
						playerShips -= enemyFleet.NumShips();
					}
					else {
						int shipsOnPlanet = pw.GetPlanet(enemyFleet.DestinationPlanet()).NumShips();
						playerShips -= shipsOnPlanet;
						enemyShips -= shipsOnPlanet;
					}
				}
				else {
					enemyShips += enemyFleet.NumShips();
				}
			}

			for (Fleet playerFleet : playerFleets) {
				if(playerFleet.TurnsRemaining() == 1 && enemyPlanets.contains(pw.GetPlanet(playerFleet.DestinationPlanet()))) {
					if(pw.GetPlanet(playerFleet.DestinationPlanet()).NumShips() > playerFleet.NumShips()) {
						enemyShips -= playerFleet.NumShips();
					}
					else {
						int shipsOnPlanet = pw.GetPlanet(playerFleet.DestinationPlanet()).NumShips();
						enemyShips -= shipsOnPlanet;
						playerShips -= shipsOnPlanet;
					}
				}
				else {
					playerShips += playerFleet.NumShips();
				}
			}

			int winner=0;
			if((turn < 1000 && enemyShips <= 0) || (turn == 1000 && enemyShips < playerShips)) {
				victory = true;
				winner = 1;
			}
			else if ((turn < 1000 && playerShips <= 0) || (turn == 1000 && playerShips < enemyShips)) {
				victory = true;
				winner = 2;
			}
			if(victory) {
				printWriter.println(turn + ";" + winner + ";" + playerShips + ";" + enemyShips);
				printWriter.close();
			}
			else if (turn == 1000) {
				printWriter.println(1000 + ";" + 0 + ";" + playerShips + ";" + enemyShips);
			}
		}
	}
	/**
	 * Computes and executes colonization tasks
	 * @param colRad Colonization radius used in score computation
	 * @param colGrow Growth parameter used in score computation
	 * @param colShips Ships parameter used in score computation
	 * @param expPriority Expansion priority parameter
	 * @param pw PlanetWars instance to control the game
	 */
	private static void performColonizationActivity(double colRad, double colGrow, double colShips, double expPriority, PlanetWars pw) {
		if(pw.NeutralPlanets().size()>0) {
			try {
				List<Planet> myPlanets = pw.MyPlanets();
				List<Planet> neutralPlanets = pw.NeutralPlanets();
				List<Fleet> enemyFleets = pw.EnemyFleets();
				Map<Integer, double[]> distanceMap = new HashMap<Integer, double[]>();

				// Calculate, for each planet, the best neutral one, depending on current parameters
				for (Planet myPlanet : myPlanets) {
					double currentBestScore = Double.MIN_VALUE;
					Planet currentBestPlanet = null;
					for (Planet neutralPlanet : neutralPlanets) {
						double distance = pw.Distance(myPlanet.PlanetID(), neutralPlanet.PlanetID());
						double growthRate = neutralPlanet.GrowthRate();
						double numberOfShips = neutralPlanet.NumShips();
						double myNumberOfShips = shipsRemaining.get(myPlanet.PlanetID());

						double colonizationScore = 1000 * (myNumberOfShips * expPriority) * (colRad / Math.pow(distance, 3.25)) * (Math.pow(growthRate,2) / colGrow) / ((numberOfShips + 1) * (1 + colShips));
						//double score = ((1+growthRate)*colGrow*2.5)/(1+(colShips * numberOfShips)*(distance/(10*colRad)));

						if (colonizationScore > currentBestScore) {
							currentBestScore = colonizationScore;
							currentBestPlanet = neutralPlanet;
						}
					}
					if(currentBestPlanet != null) {
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
								int numberOfShipsToMe = 0;
								for(Fleet enemyFleet : enemyFleets) {
									if (enemyFleet.DestinationPlanet() == sourcePlanet.PlanetID()) {
										numberOfShipsToMe += enemyFleet.NumShips();
									}
								}
								int realNumberOfShips = shipsRemaining.get(sourcePlanet.PlanetID());
								if(realNumberOfShips/2 > numberOfShipsToMe) {
									//System.err.println("Mando in colonizzazione " + realNumberOfShips/2 + "/" + realNumberOfShips + " navi da " + sourcePlanet.PlanetID() + " a " + destinationPlanet.PlanetID());
									if(destinationPlanet.NumShips() < realNumberOfShips/2) {
										pw.IssueOrder(sourcePlanet, destinationPlanet, (destinationPlanet.NumShips()/2) + 1);
										shipsRemaining.put(sourcePlanet.PlanetID(), realNumberOfShips - ((destinationPlanet.NumShips()/2) + 1));
									}
									else {
										pw.IssueOrder(sourcePlanet, destinationPlanet, realNumberOfShips / 2);
										shipsRemaining.put(sourcePlanet.PlanetID(), realNumberOfShips - realNumberOfShips / 2);
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				System.err.println(e);
			}
		}
	}

	private static void performDefenseActivity(double defRad, double defIntensity, double defPriority, PlanetWars pw) {

		List<Fleet> enemyFleets = pw.EnemyFleets();
		List<Fleet> myFleets = pw.MyFleets();
		List<Planet> myPlanets = pw.MyPlanets();

		/* --- ENEMY ATTACK PLANS --- */
		// Initialize the map
		Map<Integer, Integer> enemyAttackPlansMap = getPlans(enemyFleets, myPlanets, pw);
		// Find out if player has already sent defenses
		for (Fleet myFleet : myFleets) {
			Planet destinationPlanet = pw.GetPlanet(myFleet.DestinationPlanet());
			if (myPlanets.contains(destinationPlanet)) {
				int oldNumberOfShips = enemyAttackPlansMap.get(destinationPlanet.PlanetID());
				int newNumberOfShips = oldNumberOfShips - myFleet.NumShips();
				enemyAttackPlansMap.put(destinationPlanet.PlanetID(), newNumberOfShips);
			}
		}
		// Remove successfully defended (or not attacked) planets from the map

		Map<Integer, Map<Integer, Double>> planetDistanceMap = new HashMap<>();

		for(Planet centerPlanet : myPlanets) {
			Map<Integer,Double> idList = new HashMap<>();
			for(Planet radiusPlanet : myPlanets) {
				if(centerPlanet != radiusPlanet) {
					int distance = pw.Distance(centerPlanet.PlanetID(), radiusPlanet.PlanetID());
					int myNumberOfShips = shipsRemaining.get(radiusPlanet.PlanetID());
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
					int numberOfShipsOnAttacked = shipsRemaining.get(planetToDefend);
					int numberOfShipsOnDefender = shipsRemaining.get(defendingPlanet.PlanetID());
					int urgeToDefend = numberOfShipsOnAttacked - numberOfShipsAttacking;

					if(urgeToDefend < 0 && random.nextDouble()>(1-defPriority)) {

						int shipsToSend = (int) (numberOfShipsOnDefender*defendingValue*defIntensity*(-urgeToDefend)/(1+numberOfShipsOnDefender));
						if(shipsToSend >= numberOfShipsOnDefender) {
							shipsToSend = (int) (numberOfShipsOnDefender * defIntensity) - 1;
						}
						//System.err.println("Mando in difesa " + shipsToSend + "/" + numberOfShipsOnDefender + " navi da " + defendingPlanet.PlanetID() + " a " + planetToDefend);
						//System.err.println("Su " + defendingPlanet.PlanetID() + " rimangono " + (numberOfShipsOnDefender-shipsToSend) + " navi");
						pw.IssueOrder(defendingPlanet.PlanetID(), planetToDefend, shipsToSend);

						shipsRemaining.put(defendingPlanet.PlanetID(),(numberOfShipsOnDefender - shipsToSend));
						enemyAttackPlansMap.put(planetToDefend, enemyAttackPlansMap.get(planetToDefend)-shipsToSend);
					}
				}
			}
		}
	}

	private static void performAttackActivity(double attInt, double attShips, double attRad, double attPriority, PlanetWars pw) {

		try {
			List<Fleet> myFleets = pw.MyFleets();
			List<Fleet> enemyFleets = pw.EnemyFleets();
			List<Planet> myPlanets = pw.MyPlanets();
			List<Planet> enemyPlanets = pw.EnemyPlanets();

			// Initialize my attack map
			Map<Integer, Integer> myAttackPlans = getPlans(myFleets, enemyPlanets, pw);

			// Find out if enemy is defending the planets I'm attacking
			for (Fleet enemyFleet : enemyFleets) {
				if (myAttackPlans.containsKey(enemyFleet.DestinationPlanet())) {
					myAttackPlans.put(enemyFleet.DestinationPlanet(), myAttackPlans.get(enemyFleet.DestinationPlanet()) - enemyFleet.NumShips());
					// If the new value is less than 0, it means that the enemy is successfully defending its planet
				}
			}

			Map<Integer, Map<Integer, Double>> planetScoreMap = new HashMap<>();

			for (Planet centerPlanet : myPlanets) {
				Map<Integer, Double> attackScoreList = new HashMap<>();
				for (Planet enemyRadiusPlanet : enemyPlanets) {

					int enemyDistance = pw.Distance(centerPlanet.PlanetID(), enemyRadiusPlanet.PlanetID());
					int enemyGrowthRate = enemyRadiusPlanet.GrowthRate();
					int enemyShipNumber = enemyRadiusPlanet.NumShips();
					int myShipNumber = shipsRemaining.get(centerPlanet.PlanetID());

					double attackScore = 1000 * (myShipNumber * attPriority) * (attRad / Math.pow(enemyDistance, 3.25)) * Math.pow(enemyGrowthRate,2) / ((enemyShipNumber + 1) * (1 + attShips));
					//System.err.println("Planet " + centerPlanet.PlanetID() + " -> " + enemyRadiusPlanet.PlanetID() + ": " + attackScore);
					attackScoreList.put(enemyRadiusPlanet.PlanetID(), attackScore);
				}
				planetScoreMap.put(centerPlanet.PlanetID(), attackScoreList);
			}

			for (Entry<Integer, Map<Integer, Double>> entry : planetScoreMap.entrySet()) {

				int attackSourcePlanet = entry.getKey();
				Map<Integer, Double> planetsToAttack = entry.getValue();
				planetsToAttack = sortByValue(planetsToAttack);

				for (Entry<Integer, Double> planetToAttack : planetsToAttack.entrySet()) {

					int attackDestinationPlanet = planetToAttack.getKey();
					double attackDestinationScore = planetToAttack.getValue();

					int numberOfShipsOnDefender = pw.GetPlanet(attackDestinationPlanet).NumShips();
					int numberOfShipsOnAttacker = shipsRemaining.get(attackSourcePlanet);

					// The highest this value, the best time it is to attack
					int occasionToAttack = (int) (numberOfShipsOnAttacker * attInt) - numberOfShipsOnDefender;

					if (occasionToAttack > 0 && random.nextDouble() > (1 - attPriority)) {

						int shipsToSend = (int) (numberOfShipsOnAttacker * attackDestinationScore * attInt * occasionToAttack) / (1 + numberOfShipsOnDefender);
						if (shipsToSend > numberOfShipsOnAttacker) {
							shipsToSend = (int) (numberOfShipsOnAttacker * attInt);
						}
						if(shipsToSend > 0) {
							//System.err.println("Mando in attacco " + shipsToSend + "/" + numberOfShipsOnAttacker + " navi da " + attackSourcePlanet + " a " + attackDestinationPlanet);
							pw.IssueOrder(attackSourcePlanet, attackDestinationPlanet, shipsToSend);
							myAttackPlans.put(attackDestinationPlanet, myAttackPlans.get(attackDestinationPlanet) + shipsToSend);
							shipsRemaining.put(attackSourcePlanet, (numberOfShipsOnAttacker - shipsToSend));
						}
					}
				}
			}

		} catch (Exception e) {
			//System.err.println("Eccezione nell'attacco");
		}
	}

	private static void performReinforcementActivity(PlanetWars pw) {

	}

	private static Map<Integer,Integer> getPlans(List<Fleet> fleetList, List<Planet> planetList, PlanetWars pw) {
		Map<Integer,Integer> fleetPlans = new HashMap<>();
		for(Planet planet : planetList) {
			fleetPlans.put(planet.PlanetID(),0);
		}
		for (Fleet enemyFleet : fleetList) {
			Planet destinationPlanet = pw.GetPlanet(enemyFleet.DestinationPlanet());
			if (planetList.contains(destinationPlanet)) {
				int oldNumberOfShips = fleetPlans.get(destinationPlanet.PlanetID());
				int newNumberOfShips = oldNumberOfShips + enemyFleet.NumShips();
				fleetPlans.put(destinationPlanet.PlanetID(), newNumberOfShips);
			}
		}
		return fleetPlans;
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

		double[] doubleValues = new double[12];
		for(int i=0; i<12; i++) {
			doubleValues[i] = Double.parseDouble(args[i]);
		}
		String botNumber = args[12];
        String line = "";
        String message = "";
        int c;
        try {
            while ((c = System.in.read()) >= 0) {
				switch (c) {
				case '\n':
					if (line.trim().equals("go")) {
						PlanetWars pw = new PlanetWars(message);
						DoTurn(doubleValues,botNumber,pw);
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

