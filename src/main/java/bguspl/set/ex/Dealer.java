package bguspl.set.ex;

import bguspl.set.Env;
import bguspl.set.UserInterfaceImpl;
import bguspl.set.UtilImpl;
//import jdk.jshell.execution.Util;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private Semaphore semaphore;

    private Queue<Player> waitingQueue;
    private Queue<Integer> waitingQueueNumbers;

private boolean isNewSet;
    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
        this.waitingQueue = new PriorityQueue<>();
        this.isNewSet = true;
        this.semaphore = new Semaphore(1,true);
        this.waitingQueueNumbers = new PriorityQueue<>();

    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        for (Player p : players) {
           // char c = '0';
            Thread playerThread = new Thread(p,"player");
            playerThread.start();
            //c++;
        }
        System.out.printf("Info: Thread %s starting.%n", Thread.currentThread().getName());
        while (!shouldFinish()) {
            placeCardsOnTable();
            reshuffleTime = System.currentTimeMillis() + 60000;//noam

            try {
                timerLoop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            updateTimerDisplay(false);
            isNewSet = true;
            removeAllCardsFromTable();//return to deck if time over

        }
        announceWinners();
        System.out.printf("Info: Thread %s terminated.%n", Thread.currentThread().getName());
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did not time out.
     */
    private void timerLoop() throws InterruptedException {
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
        if (waitingQueueNumbers.size() > 0) {
            Player p = players[waitingQueueNumbers.poll()];
            List slots = p.getPlayerCards();
            int[] cards = new int[3];
            for (int i = 0; i < slots.size(); i++)
                cards[i] = table.slotToCard[(int)slots.get(i)];

            if (env.util.testSet(cards) && cards.length == 3) {//set
                for (int i : cards)
                    table.removeCard(table.cardToSlot[i]);
                isNewSet = true;
                placeCardsOnTable();
                reshuffleTime = System.currentTimeMillis() + 60000;//noam

                //p.notify();
                p.point();
            }
            else
                p.setPenalty();


        }

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
        }
        /**
         * Sleep for a fixed amount of time or until the thread is awakened for some purpose.
         */
        private void sleepUntilWokenOrTimeout() throws InterruptedException {
            Thread.sleep(9);
            // TODO implement
        }

        /**
         * Reset and/or update the countdown and the countdown display.
         */
        private void updateTimerDisplay(boolean reset) {
            // TODO implement
            env.ui.setElapsed(reshuffleTime - System.currentTimeMillis());
        }

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

        public void addSetToQueue(Player p)  {
            waitingQueueNumbers.add(p.id);
        }




    }
