package Bots.ChaoticOrder.Evaluation;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class GeneticAlgorithm {

    private static final String ROOT = "C:\\Users\\Michele\\Downloads\\PLANETWARS\\PLANETWARS\\GENELOGS2\\";
    private static final int EVALUATIONS_COMPLETED = 13;

    static String firstBotParameters;
    static String secondBotParameters;

    static String[] firstBotArray;
    static String[] secondBotArray;

    public static void main(String args[]) {

        int firstBotNumber = 38;
        int secondBotNumber = 75;

        Map<int[], Double> scoreMap = new HashMap<>();
        for (int i = 0; i < EVALUATIONS_COMPLETED; i++) {

            String logPath = ROOT + "botlog" + (i+1) + ".txt";
            try {
                FileInputStream fileInputStream = new FileInputStream(logPath);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

                String parameterLine = bufferedReader.readLine();
                if((i+1) == firstBotNumber) {
                    firstBotParameters = parameterLine;
                    firstBotArray = firstBotParameters.split(" ");
                    //System.out.println(firstBotArray[4]);
                    System.out.println("Bot parameters for bot " + (i+1) + ": " + firstBotParameters);
                }
                else if((i+1) == secondBotNumber) {
                    secondBotParameters = parameterLine;
                    secondBotArray = secondBotParameters.split(" ");
                    //System.out.println(secondBotArray[4]);
                    System.out.println("Bot parameters for bot " + (i+1) + ": " + secondBotParameters);
                }

                if(firstBotArray != null && secondBotArray != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int j = 0; j < firstBotArray.length; j++) {
                        double firstValue = Double.parseDouble(firstBotArray[j]);
                        double secondValue = Double.parseDouble(secondBotArray[j]);

                        double median = (firstValue + secondValue) / 2;
                        stringBuilder.append(median);
                        stringBuilder.append(" ");
                    }

                    //System.out.println("New parameters: " + stringBuilder.toString());
                }
                //System.out.println(parameterLine);

                double numberOfWonGames = 0;
                double numberOfLostGames = 0;
                double numberOfTurnsPerWonGame = 0;
                double numberOfShipsPerWonGame = 0;
                double numberOfTurnsPerLostGame = 0;
                double numberOfShipsPerLostGame = 0;

                String gameInfo = null;
                while ((gameInfo = bufferedReader.readLine()) != null) {
                    String[] gameInfoArray = gameInfo.split(";");
                    String turns = gameInfoArray[0];
                    String result = gameInfoArray[1];
                    String playerShips = gameInfoArray[2];
                    String enemyShips = gameInfoArray[3];

                    if (result.equals("1")) {
                        numberOfWonGames++;
                        numberOfTurnsPerWonGame += Double.parseDouble(turns);
                        numberOfShipsPerWonGame += Double.parseDouble(playerShips);
                    } else if (result.equals("2")) {
                        numberOfLostGames++;
                        numberOfTurnsPerLostGame += Double.parseDouble(turns);
                        numberOfShipsPerLostGame += Double.parseDouble(enemyShips);
                    }



                    //System.out.println(turns + " turns, " + result + " was the winner. " + playerShips + " - " + enemyShips + " ships");
                }
                double totalNumberOfGames = numberOfLostGames + numberOfWonGames;
                System.out.println("Total games found: " + totalNumberOfGames);
//                System.out.println("Games won: " + numberOfWonGames + "/" + totalNumberOfGames);
                double percentage = (100 * numberOfWonGames) / totalNumberOfGames;
//                System.out.println("Percentage: " + (Double.toString(percentage)) + "%");
//                System.out.println("Average number of turns per won game: " + (Double.toString(numberOfTurnsPerWonGame/numberOfWonGames)));
//                System.out.println("Average number of ships per won game: " + (Double.toString(numberOfShipsPerWonGame/numberOfWonGames)));
//                System.out.println("Games lost: " + numberOfLostGames + "/" + totalNumberOfGames);
//                System.out.println("Percentage: " + (Double.toString((100*numberOfLostGames)/totalNumberOfGames)) + "%");
//                System.out.println("Average number of turns per lost game: " + (Double.toString(numberOfTurnsPerLostGame/numberOfLostGames)));
//                System.out.println("Average number of ships per lost game: " + (Double.toString(numberOfShipsPerLostGame/numberOfLostGames)));

                double score = 4 - (5 * numberOfLostGames / totalNumberOfGames);
                int[] keyData = {(i + 1), (int) percentage};
                scoreMap.put(keyData, score);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        scoreMap = sortByValue(scoreMap);

        int j = 1;
        for (Entry<int[], Double> entry : scoreMap.entrySet()) {
            int botNumber = entry.getKey()[0];
            int percentage = entry.getKey()[1];
            System.out.println(j + ") Bot " + botNumber + " with " + percentage + "%");
            j++;

        }


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

}
