package blackjack.core;

import java.util.Vector;

import blackjack.core.Deck.Card;

public class BlackJack {
	
	private Dealer         dealer;
	private Vector<Player> players;
	private Deck           deck;
	private UserInterface  ui;
	
	public void start() {
		while(true) {
		//赛局初始化
		deck.renew();
		dealer.newGame();
		Vector<Player> players = new Vector<Player>();
		for(Player player : this.players) {
			player.newGame();
			if(player.isAlive()) players.add(player);
		}
		
		//判断游戏是否结束
		if(!dealer.isAlive() || players.isEmpty()) {
			ui.onGameOver(dealer);
			break;
		}
		
		//下注
		for(Player player : players) player.downBet();
		
		//庄家拿牌
		dealer.hit(true);dealer.hit();
		
		//玩家拿牌
		for(Player player : players){player.hit();player.hit();}
		
		//轮询玩家加牌
		for(Player player : players) while(player.askHit()) {
			player.hit();
			if(player.isBust())break;
		}
		
		//庄家补牌
		while(dealer.getPoints() < 17) dealer.hit();
		dealer.showAllCards();
		
		//计算结果
		for(Player player : players) {
			if(player.isBust()){player.lose();dealer.win(player.getBet());}
			else if(dealer.isBust()){player.win();dealer.lose(player.getBet());}
			else if(dealer.getPoints() > player.getPoints()){player.lose();dealer.win(player.getBet());}
			else if(dealer.getPoints() < player.getPoints()){player.win();dealer.lose(player.getBet());}
			else player.even();
		}
		}
	}
	
	public static class Builder {
		
		private int players;
		public void setPlayers(int players) {
			this.players = players;
		}
		
		private int bet;
		public void setInitialBet(int bet) {
			this.bet = bet;
		}
		
		private int dealer_bet;
		/**
		 * Set dealer's initial bets, 0 stands unlimited.
		 * @param bet
		 */
		public void setDealerBet(int bet) {
			dealer_bet = bet;
		}
		
		private int decks;
		public void setDecks(int decks) {
			this.decks = decks;
		}
		
		private UserInterface ui;
		public void setUI(UserInterface ui) {
			this.ui = ui;
		}
		
		public BlackJack create() {
			BlackJack bj = new BlackJack();
			bj.players   = new Vector<Player>();
			bj.deck      = new Deck(decks);
			bj.ui        = ui;
			for(int i = 0; i < players; ++i)
				bj.players.add(new Player(i, bet, bj.deck, ui));
			bj.dealer = new Dealer(dealer_bet, bj.deck, ui);
			return bj;
		}
	}
}

class Dealer extends Gamer implements DealerInfo {
	
	private final boolean unlimited;
	
	public Dealer(int totalBet, Deck deck, UserInterface ui) {
		super(totalBet, deck, ui);
		unlimited = totalBet == 0;
	}
	
	/**
	 * Return if the dealer has unlimited bets.
	 * @return
	 */
	@Override
	public boolean isUnlimited() {
		return unlimited;
	}
	
	@Override
	public Boolean isAlive() {
		if(isUnlimited()) return true;
		return super.isAlive();
	}
	
	/**
	 * start a new round game.
	 */
	@Override
	protected void newGame() {
		super.newGame();
		ui.onDealerNewGame(this);
	}
	
	@Override
	protected void onHit(Card card) {
		ui.onDealerHit(card);
	}
	
	void showAllCards() {
		for(Card card : cards)
			card.isSet = false;
		ui.onRefreshDealerCards(this);
	}
}

class Player extends Gamer implements PlayerInfo {
	
	public Player(int id, int totalBet, Deck deck, UserInterface ui) {
		super(totalBet, deck, ui);
		this.id = id;
	}
	
	private int id;
	private int bet;
	
	protected void lose() {
		lose(bet);
	}
	
	protected void win() {
		win(bet);
	}
	
	protected void even() {
		ui.onPlayerEven(this);
	}
	
	@Override
	protected void newGame() {
		super.newGame();
		bet = 0;
		ui.onPlayerNewGame(this);
	}
	
	protected void downBet() {
		bet = ui.onDownBet(this);
		assert(bet > 0);
	}
	
	@Override
	protected void onHit(Card card) {
		ui.onPlayerHit(this, card);
	}
	
	boolean askHit() {
		return ui.onAskPlayerHit(this);
	}
	
	protected void onBust() {
		ui.onPlayerBust(this);
	}
	
	@Override
	protected void onLose(int amount) {
		ui.onPlayerLose(this, amount);
	}
	
	@Override
	protected void onWin(int amount) {
		ui.onPlayerWin(this, amount);
	}
	
	@Override
	public int getId() {
		return id;
	}
	
	public int getBet() {
		return bet;
	}
}

abstract class Gamer implements GamerInfo {
	
	private   int          totalBet;
	protected Vector<Card> cards;
	
	protected final Deck          deck;
	protected final UserInterface ui;
	
	protected Gamer(int totalBet, Deck deck, UserInterface ui) {
		this.totalBet = totalBet;
		this.deck     = deck;
		this.ui       = ui;
	}
	
	protected void newGame() {
		cards = new Vector<Card>();
	}
	
	/**
	 * get a card from deck at specific set formation.
	 * @param isSet if this card is set
	 */
	protected void hit(boolean isSet) {
		Card card = deck.pop(isSet);
		cards.add(card);
		onHit(card);
		if(isBust())onBust();
	}
	
	protected void hit() {
		hit(false);
	}
	
	protected void lose(int amount) {
		totalBet -= amount;
		onLose(amount);
	}
	
	protected void win(int amount) {
		totalBet += amount;
		onWin(amount);
	}
	
	protected void onBust(){}
	protected void onHit(Card card){}
	protected void onLose(int amount){}
	protected void onWin(int amount){}
	
	protected boolean isBust() {
		return getPoints() > 21;
	}
	
	@Override
	public int getPoints() {
		int points = 0;
		boolean hasAce = false;
		for(Card card : cards)
			if(card.rank == Card.RANK_A) {hasAce = true; ++points; }
			else if(card.rank >= Card.RANK_10) points += 10;
			else points += card.rank + 1;
		if(hasAce && points + 10 <= 21) points += 10;
		return points;
	}
	
	@Override
	public Boolean isAlive() {
		return totalBet > 0;
	}
	
	@Override
	public int getTotalBet() {
		return totalBet;
	}
}