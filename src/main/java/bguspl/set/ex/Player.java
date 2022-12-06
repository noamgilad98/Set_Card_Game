package bguspl.set.ex;
//asddfgghhjghjhghjhghjh
//vghjk

import bguspl.set.Env;

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
    private int[] Tokens; //israel

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
        this.Tokens = new int[12];
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
            if(this.currentTokenUse() < 3) {
                table.placeToken(id, slot);
                this.Tokens[slot]=1;
            }
            else {
                table.removeToken(id, slot);
                this.Tokens[slot]=0;
            }
        }
    }
    /**
     * This return the number of thr players tokens on the table .
     */
    private int currentTokenUse() {
        int sum =0;
        for (int i : Tokens){
            sum+=i;
        }
        return sum;

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
    public void RemoveAllTokens() {
        for(int i : Tokens) {
            if(i==1){
                i=0;
                table.removeToken(id,i);
            }
        }
    }

    public int getScore() {
        return score;
    }
    public int[] getTokens() {
        return Tokens;
    }

    public int[] getPlayerCards (){
        int [] a = new int[3];
        int j=0;
        for (int i : Tokens){
            if(i == 1){
                a[j]=i;
                j++;
            }
        }
        return a;
    }
}
