package blackjack.core;

import java.util.Stack;

public class Deck {
	
	private final int         decks;
	private final Stack<Card> deck = new Stack<Card>();
	
	/**
	 * Initialize a shuffled deck with one of deck.
	 */
	public Deck() {
		this(1);
	}
	
	/**
	 * Initialize a shuffled deck with specific number pairs of decks.
	 * @param decks how many decks in this deck
	 */
	public Deck(int decks) {
		this.decks = decks;
		renew();
	}
	
	/**
	 * Get a new deck.
	 */
	public void renew() {
		//Preconditions
		assert(decks > 0);
		
		for(int i = 0;i < decks; ++i)
			for(int suit = 0; suit < Card.SUITS; ++suit)
				for(int rank = 0; rank < Card.RANKS; ++rank)
					deck.push(new Card(suit, rank));
		
		//Shuffle cards
		for(int i = deck.size() * 52; i > 0; --i) {
			int a = (int)Math.floor(Math.random()*deck.size()), b = (int)Math.floor(Math.random()*deck.size());
			Card t = deck.get(a);
			deck.set(a, deck.get(b));
			deck.set(b, t);
		}
	}
	
	/**
	 * Get one card from top of the deck with not set formation, and remove it from the deck.
	 * @return the top-most card
	 */
	public Card pop() {
		return pop(false);
	}
	
	/**
	 * Get one card from top of the deck with specific set formation, and remove it from the deck.
	 * @param isSet if this card is set
	 * @return the top-most card
	 */
	public Card pop(boolean isSet) {
		assert(!deck.isEmpty());
		
		Card card = deck.pop();
		card.isSet = isSet;
		return card;
	}
	
	public static class Card {
		public int     rank;
		public int     suit;
		public boolean isSet;
		
		public Card(int suit, int rank) {
			this.suit  = suit;
			this.rank  = rank;
		}
		
		public Card(int suit, int rank, boolean isSet) {
			this.suit  = suit;
			this.rank  = rank;
			this.isSet = isSet;
		}
		
		public  static final int RANK_A  = 0;
		public  static final int RANK_2  = 1;
		public  static final int RANK_3  = 2;
		public  static final int RANK_4  = 3;
		public  static final int RANK_5  = 4;
		public  static final int RANK_6  = 5;
		public  static final int RANK_7  = 6;
		public  static final int RANK_8  = 7;
		public  static final int RANK_9  = 8;
		public  static final int RANK_10 = 9;
		public  static final int RANK_J  = 10;
		public  static final int RANK_Q  = 11;
		public  static final int RANK_K  = 12;
		public  static final int RANKS   = 13;
		
		public  static final int SUIT_DIAMOND = 0;
		public  static final int SUIT_SPADE   = 1;
		public  static final int SUIT_HEART   = 2;
		public  static final int SUIT_CLUB    = 3;
		public  static final int SUITS        = 4;
	}
}
