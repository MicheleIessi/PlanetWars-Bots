package StateBot;

import StateBot.GameStates.ColonizationState;
import StateBot.GameStates.DefenseState;
import StateBot.GameStates.IGameState;
import StateBot.GameStates.OffenseState;

import StateBot.GameFiles.*;

import java.util.*;
import java.util.Map.Entry;

public class MyBot {

	public static IGameState gameState = null;
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
		gameState = MyBot.checkState(pw);
		if(pw.EnemyPlanets().size() > 0) {
			gameState.doTurn(pw);
		}
		else {
			return;
		}

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

