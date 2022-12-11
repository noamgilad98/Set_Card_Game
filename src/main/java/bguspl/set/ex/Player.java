package bguspl.set.ex;
//asddfgghhjghjhghjhghjh
//vghjk

import bguspl.set.Env;

import java.util.ArrayList;
import java.util.List;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;

    /**
     * The id of the player (starting from 0).
     */
    public final int id;

    /**
     * The thread representing the current player.
     */
    private Thread playerThread;

    /**
     * The thread of the AI (computer) player (an additional thread used to generate key presses).
     */
    private Thread aiThread;

    /**
     * True iff the player is human (not a computer player).
     */
    private final boolean human;

    /**
     * True iff game should be terminated due to an external event.
     */
    private volatile boolean terminate;

    /**
     * The current score of the player.
     */
    private int score;

    /**
     * The current amount of tokens of the player.
     */
    private List<Integer> tokensOnCards; //israel

    /**
     * The class constructor.
     *
     * @param env    - the environment object.
     * @param dealer - the dealer object.
     * @param table  - the table object.
     * @param id     - the id of the player.
     * @param human  - true iff the player is a human player (i.e. input is provided manually, via the keyboard).
     */
    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {
        this.env = env;
        this.table = table;
        this.id = id;
        this.human = human;
        this.tokensOnCards = new ArrayList<Integer>();

    }

    /**
     * The main player thread of each player starts here (main loop for the player thread).
     */
    @Override
    public void run() {
        playerThread = Thread.currentThread();
        System.out.printf("Info: Thread %s starting.%n", Thread.currentThread().getName());
        if (!human) createArtificialIntelligence();

        while (!terminate) {
            // TODO implement main player loop

        }
        if (!human) try { aiThread.join(); } catch (InterruptedException ignored) {}
        System.out.printf("Info: Thread %s terminated.%n", Thread.currentThread().getName());
    }

    /**
     * Creates an additional thread for an AI (computer) player. The main loop of this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it is not full.
     */
    private void createArtificialIntelligence() {
        // note: this is a very very smart AI (!)
        aiThread = new Thread(() -> {
            System.out.printf("Info: Thread %s starting.%n", Thread.currentThread().getName());
            while (!terminate) {
                // TODO implement player key press simulator
                try {
                    synchronized (this) { wait(); }
                } catch (InterruptedException ignored) {}
            }
            System.out.printf("Info: Thread %s terminated.%n", Thread.currentThread().getName());
        }, "computer-" + id);
        aiThread.start();
    }

    /**
     * Called when the game should be terminated due to an external event.
     */
    public void terminate() {//israel
        // TODO implement
        this.playerThread.interrupt();
    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public void keyPressed(int slot) {//israel
        // TODO implement
        if(table.slotToCard[slot] != null){
            if(tokensOnCards.size() == 2 && !tokensOnCards.contains(slot)){
                table.placeToken(id, slot);
                tokensOnCards.add(slot);
                table.setPlayersWith3Tokens(this,true);
            }
            else if (!tokensOnCards.contains(slot) && tokensOnCards.size()<2){
                table.placeToken(id, slot);
                tokensOnCards.add(slot);
            }
            else if (tokensOnCards.contains(slot)){
                table.removeToken(id, slot);
                tokensOnCards.remove(slot);
                table.setPlayersWith3Tokens(this,false);
            }
        }
    }

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() {//israel
        // TODO implement
        score ++;
        table.setScore(id,score++);
        /*
        int ignored = table.countCards(); // this part is just for demonstration in the unit tests
        env.ui.setScore(id, ++score);*/
        try {
            this.playerThread.wait(1000);
        }
        catch (Exception ex){

        }
    }

    /**
     * Penalize a player and perform other related actions.
     */
    public void penalty() {
        // TODO implement
        try {
            this.playerThread.wait(1000);
        }
        catch (Exception ex){

        }
    }

    /**
     * Penalize a player and perform other related actions.
     */
    public void removeAllTokens() {
        for(int i : this.tokensOnCards) {
            if(i>0){
                i=0;
                table.removeToken(id,i);
            }
        }
    }

    public int getScore() {
        return score;
    }

    public List<Integer> getPlayerCards (){
        return tokensOnCards;
    }
}
