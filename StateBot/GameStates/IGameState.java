package StateBot.GameStates;

import StateBot.GameFiles.PlanetWars;

public interface IGameState {

    void doTurn(PlanetWars pw);
}
