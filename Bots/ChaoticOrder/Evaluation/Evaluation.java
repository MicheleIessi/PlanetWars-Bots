package Bots.ChaoticOrder.Evaluation;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Evaluation {

    public static void main(String[] args) {

        String root = "C:\\Users\\Michele\\Downloads\\PLANETWARS\\PLANETWARS\\";
        String playGame = "java -jar " + root + "tools\\PlayGame-1.2.jar ";
        int numberOfGamesPerBot = 1;
        String timeOutMS = "1000 ";
        String numberOfTurns = "1000 ";
        String logFileName = root + "log.txt ";
        String GAJarBotPath = "\"java -jar out/artifacts/ChaoticOrder/ChaoticOrder.jar ";
        String opponentBotPath = "\"java -jar C:\\Users\\Michele\\Downloads\\PLANETWARS\\PLANETWARS\\example_bots\\";
        //String showGamePath = "| java -jar " + root + "tools\\ShowGame-1.2.jar";


        int populationNumber = 100;
        List<double[]> botParametersArrayList = generateBotParamenters(populationNumber);
        int botNumber = 0;
        System.out.println("There are " + botParametersArrayList.size() + " bot parameters to be simulated");
        for (double[] parameters : botParametersArrayList) {
            botNumber++;

            StringBuilder sb = new StringBuilder();
            for (double botParameter : parameters) {
                sb.append(botParameter);
                sb.append(" ");
            }
            String botParametersNoNumber = sb.toString();
            sb.append(botNumber);
            sb.append("\" ");
            String botParameterString = sb.toString();

            try {
                File file = new File(root + "GENELOGS2\\botlog" + botNumber + ".txt");
                file.delete();
                file.createNewFile();

                System.out.println("Initializing simulation for bot " + botNumber);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Bot parameters are: ");
                for(int i=0; i<parameters.length; i++) {
                    stringBuilder.append(parameters[i]);
                    stringBuilder.append(", ");
                }
                System.out.println(stringBuilder.toString());
                PrintWriter out = new PrintWriter(file);
                System.out.println(botParametersNoNumber);
                out.println(botParametersNoNumber);
                out.close();
                for (int i = 0; i < 100; i++) {

                    int mapNumber = i + 1;
                    String mapPath = root + "maps\\map" + mapNumber + ".txt ";

                    for (EnemyBots enemyBots : EnemyBots.values()) {
                        String opponentBot = enemyBots.name() + ".jar\"";

                        String commandString = playGame + mapPath + timeOutMS + numberOfTurns + logFileName + GAJarBotPath + botParameterString + opponentBotPath + opponentBot;

                        //System.out.println(commandString);

                        for (int j = 0; j < numberOfGamesPerBot; j++) {

                            Process process = Runtime.getRuntime().exec(commandString);
                            InputStream is = process.getInputStream();
                            InputStream isErr = process.getErrorStream();

                            Thread isThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    InputStreamReader isr = new InputStreamReader(is);
                                    BufferedReader bufferedReader = new BufferedReader(isr);
                                    String line;
                                    try {
                                        while ((line = bufferedReader.readLine()) != null) {
                                            String line1 = line;
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            isThread.start();

                            Thread errThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    InputStreamReader esr = new InputStreamReader(isErr);
                                    BufferedReader bufferedReader = new BufferedReader(esr);
                                    String line;
                                    try {
                                        while ((line = bufferedReader.readLine()) != null) {
                                            String line1 = line;
                                            //System.out.println(line1);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            errThread.start();
                            process.waitFor();
                        }
                    }
                    System.out.println("Mappa " + (i + 1) + " provata con tutti i bot");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static List<double[]> generateBotParamenters(int populationNumber) {

        Random random = new Random();
        random.doubles(0,1);
        List<double[]> botParametersArrayList = new ArrayList<>();

        for(int i=0; i<populationNumber; i++) {

            double[] parameters = {
                    random.nextDouble(),
                    random.nextDouble(),
                    random.nextDouble(),
                    random.nextDouble(),
                    random.nextDouble(),
                    random.nextDouble(),
                    random.nextDouble(),
                    random.nextDouble(),
                    random.nextDouble(),
                    random.nextDouble(),
                    random.nextDouble(),
                    (double) (Math.abs(random.nextInt()%10)* 100)
            };
            if(parameters[11] == 0.0) {
                parameters[11] = 100;
            }
            botParametersArrayList.add(i,parameters);
        }

        return botParametersArrayList;

    }
}
