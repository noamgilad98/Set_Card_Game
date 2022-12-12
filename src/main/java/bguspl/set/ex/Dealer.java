package bguspl.set.ex;

import bguspl.set.Env;
import bguspl.set.UserInterfaceImpl;
import bguspl.set.UtilImpl;
//import jdk.jshell.execution.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Collections; //import by noam to shuffle

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;
    private final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private final List<Integer> deck;

    /**
     * True iff game should be terminated due to an external event.
     */
    private volatile boolean terminate;

    /**
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime = Long.MAX_VALUE;


private boolean isNewSet;
    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());

        this.isNewSet = true;

    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
//        for (Player p : players) {
//            p.run();
//        }
        System.out.printf("Info: Thread %s starting.%n", Thread.currentThread().getName());
        while (!shouldFinish()) {
            placeCardsOnTable();
            reshuffleTime = System.currentTimeMillis() + 60000;//noam
            timerLoop();
            updateTimerDisplay(false);
            isNewSet = true;
            removeAllPlayersTokens();
            removeAllCardsFromTable();//return to deck if time over
        }
        announceWinners();
        System.out.printf("Info: Thread %s terminated.%n", Thread.currentThread().getName());
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did not time out.
     */
    private void timerLoop() {
        while (!terminate && System.currentTimeMillis() < reshuffleTime) {
            sleepUntilWokenOrTimeout();
            updateTimerDisplay(false);
            removeCardsFromTable();//if set
            placeCardsOnTable();
        }
    }

    /**
     * Called when the game should be terminated due to an external event.
     */
    public void terminate() {
        // TODO implement

        Thread.currentThread().interrupt();
    }

    /**
     * Check if the game should be terminated or the game end conditions are met.
     *
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return terminate || env.util.findSets(deck, 1).size() == 0;
    }

    /**
     * Checks if any cards should be removed from the table.
     */
    private void removeCardsFromTable() {//remove the set
        // TODO implement
        boolean ans = removeCardsFromTableBool();

    }
    /**
     * Checks if any cards should be removed from the table.
     */

    private boolean removeCardsFromTableBool() {//remove the set
        // TODO implement
        List<Player> playersWith3Tokens  = table.getPlayersWith3Tokens();
        if(playersWith3Tokens.size() > 0) {
//
            for (int j=0;j<playersWith3Tokens.size();j++) {
                Player p = playersWith3Tokens.get(j);
                List<Integer> list = p.getPlayerCards();
                int[] arr = new int[list.size()];
                for (int i = 0; i < list.size(); i++)
                    arr[i] = table.slotToCard[list.get(i)];

                if (env.util.testSet(arr) && arr.length==3){//set
                    p.point();
                    for (Player p1 : players)
                        p1.removeTokens(arr);
                    for(int i : arr)
                        table.removeCard(table.cardToSlot[i]);
                    isNewSet = true;
                    placeCardsOnTable();
                    reshuffleTime = System.currentTimeMillis() + 60000;//noam
                    return true;
                }
                else
                {

                    p.penalty();
                }

            }
        }
        return false;
    }


        /**
         * Check if any cards can be removed from the deck and placed on the table.
         */
        private void placeCardsOnTable() {//noam
            // TODO implement
            //Collections.shuffle(deck);
            List<Integer> emptySlots = table.getEmptySlots();
            for (int i : emptySlots)
                if(deck.size()>0)
                    table.placeCard(deck.remove(0), i);

            //int [] arr1 = env.util.findSets(table.getSlotsAsList(),);
            if(deck.size()>0)
            if(env.util.findSets(table.getSlotsAsList(),1).size() == 0 ){
                removeAllCardsFromTable();
                placeCardsOnTable();
            }
            else
            {
                if(isNewSet) {
                    System.out.println(table.cardToSlot[env.util.findSets(table.getSlotsAsList(), 1).get(0)[0]]);
                    System.out.println(table.cardToSlot[env.util.findSets(table.getSlotsAsList(), 1).get(0)[1]]);
                    System.out.println(table.cardToSlot[env.util.findSets(table.getSlotsAsList(), 1).get(0)[2]]);
                    isNewSet = false;
                }
            }



//                System.out.println(env.util.findSets(table.getSlotsAsList(),1).get(0));
//                System.out.println(env.util.findSets(table.getSlotsAsList(),1).get(1));
//                System.out.println(env.util.findSets(table.getSlotsAsList(),1).get(2));


        }

        /**
         * Sleep for a fixed amount of time or until the thread is awakened for some purpose.
         */
        private void sleepUntilWokenOrTimeout() {
            // TODO implement


        }

        /**
         * Reset and/or update the countdown and the countdown display.
         */
        private void updateTimerDisplay(boolean reset) {
            // TODO implement
            env.ui.setElapsed(reshuffleTime - System.currentTimeMillis());


        }//

        /**
         * Returns all the cards from the table to the deck.
         */
        private void removeAllCardsFromTable() {//return to deck if time over
            // TODO implement
            for (int i = 0; i < table.slotToCard.length; i++) {
                deck.add(table.slotToCard[i]);
                table.removeCard(i);
            }
            if(env.util.findSets(deck,1).size()>0)
                this.terminate();
            for (Player p :players)
                p.removeAllTokens();
            if(env.util.findSets(deck,1).size()==0)
                announceWinners();

        }

        /**
         * Check who is/are the winner/s and displays them.
         */
        private void announceWinners() {
            // TODO implement
            List<Player> winners = new ArrayList<Player>();
            int highScor = 0;
            for (Player p : players) {//find the players with highst scor
                if (p.getScore() == highScor)
                    winners.add(p);
                if (p.getScore() > highScor) {
                    winners.clear();
                    winners.add(p);
                    highScor = p.getScore();
                }
            }
            int[] winnersARR = new int[winners.size()];
            int i = 0;
            for (Player p : winners) {//convert the list to array of winners id
                winnersARR[i] = p.id;
                i++;
            }
            env.ui.announceWinner(winnersARR);
        }

        public void penalty(int playerID){

        }


        private void removeAllPlayersTokens() {
        }



    }
