package bguspl.set.ex;

import bguspl.set.Env;
import bguspl.set.UserInterfaceImpl;

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

    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        System.out.printf("Info: Thread %s starting.%n", Thread.currentThread().getName());
        while (!shouldFinish()) {
            placeCardsOnTable();
            reshuffleTime = System.currentTimeMillis()+6000;//noam
            timerLoop();
            updateTimerDisplay(false);
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

    }

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    private void placeCardsOnTable() {//noam
        // TODO implement
        Collections.shuffle(deck);
        List<Integer> emptySlots = table.getEmptySlots();
        for (int i : emptySlots)
            table.placeCard(deck.remove(0),i);
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
        env.ui.setElapsed(reshuffleTime-System.currentTimeMillis());


    }//

    /**
     * Returns all the cards from the table to the deck.
     */
    private void removeAllCardsFromTable() {//return to deck if time over
        // TODO implement
        for (int i=0 ; i < table.slotToCard.length; i++) {
            deck.add(table.slotToCard[i]);
            table.removeCard(i);
        }
    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {
        // TODO implement
        List<Player> winners = new ArrayList<Player>();
        int highScor=0;
        for (Player p : players){//find the players with highst scor
            if(p.getScore() == highScor)
                winners.add(p);
            if(p.getScore() > highScor){
                winners.clear();
                winners.add(p);
                highScor = p.getScore();
            }
        }
        int[]winnersARR = new int[winners.size()];
        int i=0;
        for (Player p : winners){//convert the list to array of winners id
           winnersARR[i] = p.id;
           i++;
       }
        env.ui.announceWinner(winnersARR);
    }

    /**
     * remove all the players tokens from table
     */
    private void removeAllPlayersTokens() {
        for (Player p : players){
            p.removeAllTokens();
        }
    }

    /**
     * check if there any player with leagl set
     * @return the first player who put 3 tokens on leagel set
     */
    private Player getPlayerWithLeagelSet(){
        List<Player> playersWith3Tokens  = table.getPlayersWith3Tokens();
        for (Player p : playersWith3Tokens){
            if(isLeagelSet(p.getPlayerCards())){
                return p;
            }
        }
        return null;//if no player with leagel set
    }

    /**
     * check if the 3 card is leagel set
     * @return true if the 3 cards is leagel set
     */
    private  boolean isLeagelSet(int [] cards){
        int [][] allPlayerCardBinari = new int[3][4];//colum is featur and row is card

    return true;
    }
    private int[] convertCardIntToBinari(int card){
        int[] cardBinari = new int[4];
        int i=0;
        for(i=0; i<4; i++)//init the array with 0
            cardBinari[i]=0;

        for (i=3;i>=0;i--){
            for (int j=2;j>=0;j--)
                if( card -  (j * Math.pow(3,i) ) >= 0){
                    cardBinari[i]=j;
                    card -= j * Math.pow(3,i);
                }
        }
        return cardBinari;
    }

}
