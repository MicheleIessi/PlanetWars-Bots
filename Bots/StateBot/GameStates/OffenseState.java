package Bots.StateBot.GameStates;

import Bots.StateBot.GameFiles.Fleet;
import Bots.StateBot.GameFiles.Planet;
import Bots.StateBot.GameFiles.PlanetWars;

import java.util.List;

public class OffenseState implements IGameState {

    @Override
    public void doTurn(PlanetWars pw) {

        // trova gli 'x' pianeti più forti e attacca il più debole pianeta nemico
        // eventualmente, pianifica l'invio di altre truppe se il nemico si difende

        final int ATTACK_FORCE = 3;
        List<Planet> myPlanets = pw.MyPlanets();
        myPlanets.sort((o1, o2) -> o2.NumShips() - o1.NumShips());

        List<Planet> enemyPlanets  =pw.EnemyPlanets();
        enemyPlanets.sort(((o1, o2) -> o1.NumShips() - o2.NumShips()));

        List<Fleet> myFleets = pw.MyFleets();
        List<Fleet> enemyFleets = pw.EnemyFleets();

        Planet sourcePlanet = null;
        Planet destinationPlanet = enemyPlanets.get(0);
        // Attack mechanism
        for(int i=0; i < ATTACK_FORCE; i++) {
            sourcePlanet = myPlanets.get(i);
            int numberOfShips = sourcePlanet.NumShips()/2;

            pw.IssueOrder(sourcePlanet, destinationPlanet, numberOfShips);
        }


    }
}
