package Bots.StateBot;

import Bots.StateBot.GameStates.ColonizationState;
import Bots.StateBot.GameStates.DefenseState;
import Bots.StateBot.GameStates.IGameState;
import Bots.StateBot.GameStates.OffenseState;

import Bots.StateBot.GameFiles.*;

import java.util.*;
import java.util.Map.Entry;

public class MyBot {

	public static IGameState gameState = null;
	public static Map<Integer, Integer> realGameState;



    public static void DoTurn(PlanetWars pw) {
		// (1) If we currently have a fleet in flight, just do nothing.
//		if (pw.MyFleets().size() >= 1) {
//			return;
//		}
		/* idee:
			1) colonizzare il prima possibile i pianeti più grandi per più produzione e contrastare le colonizzazioni del nemico
			2) attaccare i pianeti nemici più deboli con i pianeti più forti
			3) difendere i pianeti attaccati SE sono difendibili, altrimenti spostare tutta la flotta da quel pianeta visto che verrebbe comunque distrutta
		 */

		try {

			gameState = MyBot.checkState(pw);
			if (pw.EnemyPlanets().size() > 0) {
				gameState.doTurn(pw);
			} else {
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());

		}
    }

    public static Map<Integer,Integer> getRealGameState(PlanetWars planetWars) {

    	realGameState = new HashMap<>();
    	for(Planet myPlanet : planetWars.MyPlanets()) {
    		realGameState.put(myPlanet.PlanetID(), myPlanet.NumShips());
		}
		return realGameState;
	}

    public static IGameState checkState(PlanetWars pw) {

    	List<Planet> neutralPlanets = pw.NeutralPlanets();
    	List<Planet> enemyPlanets   = pw.EnemyPlanets();
    	List<Planet> myPlanets 		= pw.MyPlanets();
    	IGameState gameState = new OffenseState();
		List<Fleet> enemyFleets = pw.EnemyFleets();

		if(neutralPlanets.size() > 0) {
			gameState = new ColonizationState();
		}
		Map<Integer, Integer> enemyPlans = new HashMap<>();
		for(Fleet f : enemyFleets) {
			if(myPlanets.contains(pw.GetPlanet(f.DestinationPlanet()))) {
				if(!enemyPlans.containsKey(f.DestinationPlanet())) {
					enemyPlans.put(f.DestinationPlanet(), f.NumShips());
				}
				else {
					int oldData = enemyPlans.get(f.DestinationPlanet());
					enemyPlans.put(f.DestinationPlanet(), oldData + f.NumShips());
				}
			}
		}

		for(Entry<Integer,Integer> e : enemyPlans.entrySet()) {
			Planet p = pw.GetPlanet(e.getKey());
			if(p.NumShips() <= e.getValue()) {
				gameState = new DefenseState();
			}
		}

		return gameState;
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

