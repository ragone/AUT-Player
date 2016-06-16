package org.ggp.base.apps;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Description of Parser.
 *
 * @author ragone.
 * @version 2/06/16
 */
public class Parser {
    public static void main(String[] args) throws IOException {
        FileReader input = new FileReader("/Users/ragone/IdeaProjects/ggp-base/src/main/java/org/ggp/base/apps/connect15");
        BufferedReader bufRead = new BufferedReader(input);
        String myLine = null;
        HashMap<String, Integer> map = new HashMap<>();
        HashMap<String, Integer> tieMap = new HashMap<>();

        while ( (myLine = bufRead.readLine()) != null)
        {
            String[] array1 = myLine.split(";");
            for(String results : array1) {
                String[] array2 = results.split("=");
                String[] players = array2[0].split(",");
                String[] scores = array2[1].split(",");
                String playerOne = players[0];
                String playerTwo = players[1];
                String scoreOne = scores[0];
                String scoreTwo = scores[1];
                if(scoreOne.equals("50")) {
                    tieMap.putIfAbsent(playerOne + playerTwo, 0);
                    tieMap.put(playerOne + playerTwo, tieMap.get(playerOne + playerTwo) + 1);
                }
//                map.putIfAbsent(playerOne + playerTwo, 0);
//                map.put(playerOne + playerTwo, map.get(playerOne + playerTwo) + Integer.parseInt(scoreTwo));
            }
        }

        for (Map.Entry<String, Integer> entry : tieMap.entrySet())
        {
            System.out.println(entry.getKey() + " / " + entry.getValue());
        }
    }
}
