package bguspl.set.ex;

import bguspl.set.Env;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class contains the data that is visible to the player.
 *
 * @inv slotToCard[x] == y iff cardToSlot[y] == x
 */
public class Table {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Mapping between a slot and the card placed in it (null if none).
     */
    protected final Integer[] slotToCard; // card per slot (if any)

    /**
     * Mapping between a card and the slot it is in (null if none).
     */
    protected final Integer[] cardToSlot; // slot per card (if any)

    /**
     * list of players that need to be check
     */
    private  List<Player> playersWith3Tokens ;

    /**
     * Constructor for testing.
     *
     * @param env        - the game environment objects.
     * @param slotToCard - mapping between a slot and the card placed in it (null if none).
     * @param cardToSlot - mapping between a card and the slot it is in (null if none).
     */
    public Table(Env env, Integer[] slotToCard, Integer[] cardToSlot) {

        this.env = env;
        this.slotToCard = slotToCard;
        this.cardToSlot = cardToSlot;
        this.playersWith3Tokens = new ArrayList<Player>();
    }

    /**
     * Constructor for actual usage.
     *
     * @param env - the game environment objects.
     */
    public Table(Env env) {

        this(env, new Integer[env.config.tableSize], new Integer[env.config.deckSize]);
    }

    /**
     * This method prints all possible legal sets of cards that are currently on the table.
     */
    public void hints() {
        List<Integer> deck = Arrays.stream(slotToCard).filter(Objects::nonNull).collect(Collectors.toList());
        env.util.findSets(deck, Integer.MAX_VALUE).forEach(set -> {
            StringBuilder sb = new StringBuilder().append("Hint: Set found: ");
            List<Integer> slots = Arrays.stream(set).mapToObj(card -> cardToSlot[card]).sorted().collect(Collectors.toList());
            int[][] features = env.util.cardsToFeatures(set);
            System.out.println(sb.append("slots: ").append(slots).append(" features: ").append(Arrays.deepToString(features)));
        });
    }

    /**
     * Count the number of cards currently on the table.
     *
     * @return - the number of cards on the table.
     */
    public int countCards() {
        int cards = 0;
        for (Integer card : slotToCard)
            if (card != null)
                ++cards;
        return cards;
    }

    /**
     * Places a card on the table in a grid slot.
     * @param card - the card id to place in the slot.
     * @param slot - the slot in which the card should be placed.
     *
     * @post - the card placed is on the table, in the assigned slot.
     */
    public void placeCard(int card, int slot) {
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {}
        cardToSlot[card] = slot;
        slotToCard[slot] = card;
        // TODO implement
        env.ui.placeCard(card,slot);
    }

    /**
     * Removes a card from a grid slot on the table.
     * @param slot - the slot from which to remove the card.
     */
    public void removeCard(int slot) {
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {}

        // TODO implement
        if( slotToCard[slot] != null) {
            cardToSlot[slotToCard[slot]] = null;
            slotToCard[slot] = null;
            env.ui.removeCard(slot);
        }

    }

    /**
     * Places a player token on a grid slot.
     * @param player - the player the token belongs to.
     * @param slot   - the slot on which to place the token.
     */
    public void placeToken(int player, int slot) {//israel
        // TODO implement
        env.ui.placeToken(player,slot);
    }

    /**
     * Removes a token of a player from a grid slot.
     * @param player - the player the token belongs to.
     * @param slot   - the slot from which to remove the token.
     * @return       - true iff a token was successfully removed.
     */
    public boolean removeToken(int player, int slot) {///////////////////////////////////////////////////////
        // TODO implement
        try {
            env.ui.removeToken(player, slot);
            return true;
        }
        catch (Exception ex){return false;}
    }

    /**
     * Removes a token from a card.
     * @param card - the card the token belongs to.
     * @return       - true iff a token was successfully removed.
     */
    public boolean removeTokenByCard(int player ,int card) {
        // TODO implement
        try {
            env.ui.removeToken(player, cardToSlot[card]);
            return true;
        }
        catch (Exception ex){return false;}
    }

    /**
     *
     * @return       - list of index of empty slots.
     */
    public List<Integer> getEmptySlots() {//noam new
        List <Integer> emptySlots = new ArrayList<Integer>();
        for (int i=0;i<slotToCard.length;i++)
            if (slotToCard[i] == null)
                emptySlots.add(i);
        return emptySlots;
    }
    /**
     *
     * @return       true if there is already a token in this slot .

    public boolean isTokenExsist(int slot) {/////////////////////////////////////////////////////

        return true;
    }*/

    /**
     *
     * @return       - list of players with 3 tokens on the table.
     */
    public List<Player> getPlayersWith3Tokens () {//israel
        return playersWith3Tokens;
    }

    public void setPlayersWith3Tokens (Player p, boolean add){
        if(add)
        playersWith3Tokens.add(p);
        else
            playersWith3Tokens.remove(p);

    }
    public List<Integer> getSlotsAsList(){
        List<Integer> cards = new ArrayList<Integer>();
        for (int i=0; i<this.slotToCard.length; i++)
            cards.add(slotToCard[i]);
        return cards;
    }

    public int getCardToSlot(int card){
        return cardToSlot[card];
    }

    public void setScore(int player,int score){
        env.ui.setScore(player, ++score);
    }

}



