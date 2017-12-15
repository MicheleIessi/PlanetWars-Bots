package Bots.GeneticAlgorithmBot.Evaluation;

import java.io.*;

public class Evaluation {

    public static void main(String[] args) {

        String root = "C:\\Users\\Michele\\Downloads\\PLANETWARS\\PLANETWARS\\";
        String playGame = "java -jar " + root + "tools\\PlayGame-1.2.jar ";
        int mapNumber = 10;
        String mapPath = "\"" + root + "maps\\map" + mapNumber + ".txt\" ";
        String timeOutMS = "1000 ";
        String numberOfTurns = "1000 ";
        String logFileName = root + "log.txt ";
        String GAJarBotPath = "\"java -jar out/artifacts/GeneticAlgorithmBot/GeneticAlgorithmBot.jar ";
        String opponentBotPath = "\"java -jar C:\\Users\\Michele\\Downloads\\PLANETWARS\\PLANETWARS\\example_bots\\RandomBot.jar\" ";
        //String showGamePath = "| java -jar " + root + "tools\\ShowGame-1.2.jar";

        double colonizationProbability  = 0.5;
        double colonizationGrowth       = 0.5;
        double colonizationShips        = 0.5;
        double colonizationRadius       = 0.5;
        double expansionPriority        = 0.5;
        double defenseRadius            = 0.5;
        double defenseIntensity         = 0.5;
        double defensePriority          = 0.5;
        double attackProbability        = 0.5;
        double attackIntensity          = 0.5;
        double attackShips              = 0.5;
        double attackRadius             = 0.5;
        double attackPriority           = 0.5;
        double activityProbability      = 1;
        double maximumFleetsFlying      = 100.0;

        double[] botParameters = {
                colonizationProbability,
                colonizationGrowth,
                colonizationShips,
                colonizationRadius,
                expansionPriority,
                defenseRadius,
                defenseIntensity,
                defensePriority,
                attackProbability,
                attackIntensity,
                attackShips,
                attackRadius,
                attackPriority,
                activityProbability,
                maximumFleetsFlying
        };

        StringBuilder sb = new StringBuilder();
        for (double botParameter : botParameters) {
            sb.append(botParameter);
            sb.append(" ");
        }
        sb.append("\" ");
        String botParameterString = sb.toString();

        String commandString = playGame + mapPath + timeOutMS + numberOfTurns + logFileName + GAJarBotPath + botParameterString + opponentBotPath;

        String[] command = {
                "cmd", "/c", "start", "cmd","/c", commandString
        };

        try {
            Process process = Runtime.getRuntime().exec(commandString);


            InputStream inputStream = process.getErrorStream();

            FileInputStream fileInputStream = new FileInputStream(logFileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

            String aux = bufferedReader.readLine();
            while(bufferedReader.ready() && aux != null) {
                System.out.println(aux);
                aux = bufferedReader.readLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
