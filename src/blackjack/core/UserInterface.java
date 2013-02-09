package blackjack.core;

import blackjack.core.Deck.Card;

public interface UserInterface {
	
	/** Called to initialize dealer ui */
	void onDealerNewGame(DealerInfo dealer);
	
	/** Called to initialize player ui */
	void onPlayerNewGame(PlayerInfo player);
	
	/** Called when a player should set down bet. */
	int onDownBet(PlayerInfo player);
	
	/**
	 * Called when dealer hits a card
	 * @param card
	 */
	void onDealerHit(Card card);
	
	/**
	 * Called when player hits a card
	 * @param card
	 */
	void onPlayerHit(PlayerInfo player, Card card);
	
	/**
	 * Called when ask if player want to hit a card
	 */
	boolean onAskPlayerHit(PlayerInfo player);
	
	/**
	 * Called when player busts
	 */
	void onPlayerBust(PlayerInfo player);
	
	/**
	 * Called when dealer busts
	 */
	void onDealerBust(DealerInfo dealer);
	
	/**
	 * Called when a player lose in calculate phase
	 */
	void onPlayerLose(PlayerInfo player, int amount);
	
	/**
	 * Called when a player win in calculate phase
	 */
	void onPlayerWin(PlayerInfo player, int amount);
	
	/**
	 * Called when a player even with dealer in calculate phase
	 * @param player
	 */
	void onPlayerEven(PlayerInfo player);
	
	/**
	 * Called when dealer's cards view need to refresh
	 * @param dealer
	 */
	void onRefreshDealerCards(DealerInfo dealer);
	
	/**
	 * Called when game over.
	 */
	void onGameOver(DealerInfo dealer);
}
