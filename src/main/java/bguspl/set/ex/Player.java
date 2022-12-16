package bguspl.set.ex;
//asddfgghhjghjhghjhghjh
//vghjk

import bguspl.set.Env;

import javax.swing.plaf.TableHeaderUI;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;

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


    public boolean isPenalty;
    public boolean isPoint;


    /**
     * The current amount of tokens of the player.
     */
    private List<Integer> tokensOnCards; //israel


    private Queue<Integer> keyPresQueue;
    private   boolean stopKeyPress;
    private Dealer dealer;

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
        this.isPenalty = false;
        this.isPoint = false;
        stopKeyPress = false;
        keyPresQueue = new PriorityQueue<>();
        this.dealer = dealer;

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

            try {
                penalty();
                executeQueue();
                point();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }



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
       // this.playerThread.interrupt();
    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public  void keyPressed(int slot) {//israel
        // TODO implement
        if(!stopKeyPress)
            keyPresQueue.add(slot);
    }

    public synchronized   void executeQueue() throws InterruptedException {//israel
        // TODO implement
        if(keyPresQueue.peek() != null) {
            int slot = keyPresQueue.poll();
                if (table.slotToCard[slot] != null) {
                    if (tokensOnCards.size() == 2 && !tokensOnCards.contains(slot)) {
                        env.ui.placeToken(id, slot);
                        tokensOnCards.add(slot);

                       // Semaphore semaphore = new Semaphore(1);
                      //  semaphore.acquire();



                        dealer.addSetToQueue(this);
                        //wait();



                        //semaphore.release();



                    } else if (!tokensOnCards.contains(slot) && tokensOnCards.size() < 2) {
                        env.ui.placeToken(id, slot);
                        tokensOnCards.add(slot);
                    } else if (tokensOnCards.contains(slot)) {
                        removeToken(slot);
                    }
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
        if(isPoint) {
        table.setScore(id,score++);
        /*
        int ignored = table.countCards(); // this part is just for demonstration in the unit test s
        env.ui.setScore(id, ++score);*/
        try {

                System.out.println(Thread.currentThread());
                stopKeyPress = true;
                long penaltyFreezeMillis = 10000;
                env.ui.setFreeze(this.id, penaltyFreezeMillis);
                long freezeTime = System.currentTimeMillis() + penaltyFreezeMillis;
                removeAllTokens();
                while (System.currentTimeMillis() < freezeTime)
                    env.ui.setFreeze(this.id, freezeTime - System.currentTimeMillis());
                env.ui.setFreeze(this.id, 0);
                stopKeyPress = false;
                isPoint = false;


        }
        catch (Exception ex){

        }}
    }

    /**
     * Penalize a player and perform other related actions.
     */
    public void penalty() {
        // TODO implement
        try {
            if (isPenalty) {
                stopKeyPress = true;
                long penaltyFreezeMillis = 10000;
                env.ui.setFreeze(this.id, penaltyFreezeMillis);
                long freezeTime = System.currentTimeMillis() + penaltyFreezeMillis;
                removeAllTokens();
                //Thread.sleep(5000);
                while (System.currentTimeMillis() < freezeTime)
                    env.ui.setFreeze(this.id, freezeTime - System.currentTimeMillis());
                env.ui.setFreeze(this.id, 0);
                stopKeyPress = false;
                isPenalty = false;
            }

        }
        catch (Exception ex){
        }
    }

    /**
     * remove tokens by cards numbers
     */
    public void removeTokens(int[] arr) {
        for (int i = 0 ; i < arr.length ; i++)
        {
            Integer slot = table.cardToSlot[arr[i]];
            removeToken(slot);
//            if (tokensOnCards.contains(slot)){
//                if( tokensOnCards.remove(slot) )
//                table.removeToken(this.id,slot);
//                table.setPlayersWith3Tokens(this,false);
//            }
        }
    }

    public void removeToken(int slot){
        Integer s = slot;
        if(tokensOnCards.contains(slot))
            if(tokensOnCards.remove(s)){

            }
        table.removeToken(this.id,slot);

    }

    public void removeAllTokens() {
        for(int i =tokensOnCards.size()-1 ;i>=0 ;i--)
            removeToken(tokensOnCards.get(i));

//        for (int i=0; i<tokensOnCards.size();i++) {
//            table.removeToken(this.id,tokensOnCards.get(i));
//            tokensOnCards.remove(i);
//        }
//        table.setPlayersWith3Tokens(this,false);
    }

    public int getScore() {
        return score;
    }

    public List<Integer> getPlayerCards (){
        return tokensOnCards;
    }
    public void setPenalty(){
        this.isPenalty = true;
    }
    public void setStopKeyPress(){
        this.stopKeyPress = true;
    }

}
