package blackjack.ui;

import java.util.HashMap;
import java.util.Map;

import blackjack.core.BlackJack;
import blackjack.core.DealerInfo;
import blackjack.core.PlayerInfo;
import blackjack.core.UserInterface;
import blackjack.core.BlackJack.Builder;
import blackjack.core.Deck.Card;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Game extends Activity implements UserInterface {
	
	private BlackJack blackJack;
	
	private View     askHitView; //also use as a lock to wait this operation done
	private TextView askHitPlayerView;
	private boolean  askHitResult;
	
	private View     downBetView; //also use as a lock to wait this operation done
	private View     downBetOkView;
	private TextView downBetPlayerView;
	private TextView downBetBetView;
	private int      downBetResult;
	private int      downBetMax;
	
	private LinearLayout            playersView;
	private Map<Integer, TextView>  playersViewMap = new HashMap<Integer, TextView>();
	private Map<Integer, TextView>  betsViewMap    = new HashMap<Integer, TextView>();
	private Map<Integer, CardsView> cardsViewMap   = new HashMap<Integer, CardsView>();
	
	private TextView  dealerView;
	private CardsView dealerCardsView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
		
		playersView     = (LinearLayout) findViewById(R.id.players);
		
		dealerView      = (TextView) findViewById(R.id.dealer);
		dealerCardsView = (CardsView) findViewById(R.id.dealer_cards_view);
		
		initAskHitView();
		initDownBetView();
		
		Builder builder = new Builder();
		Intent intent = getIntent();
		builder.setDecks(intent.getIntExtra("decks", 0));
		builder.setPlayers(intent.getIntExtra("players", 0));
		builder.setInitialBet(intent.getIntExtra("bet", 0));
		builder.setDealerBet(intent.getIntExtra("dealer_bet", 0));
		builder.setUI(this);
		blackJack = builder.create();
		new Thread() {
			public void run() {
				blackJack.start();
			}
		}.start();
	}
	
	private void initAskHitView() {
		askHitView = findViewById(R.id.ask_hit_view);
		askHitPlayerView = (TextView) askHitView.findViewById(R.id.player);
		View hit = askHitView.findViewById(R.id.hit);
		hit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				askHitResult = true;
				Utils.notify(askHitView);	
			}
		});
		View notHit = askHitView.findViewById(R.id.not_hit);
		notHit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				askHitResult = false;
				askHitView.setVisibility(View.GONE);
				Utils.notify(askHitView);
			}
		});
		askHitView.setVisibility(View.GONE);
	}
	
	private void initDownBetView() {
		downBetView = findViewById(R.id.down_bet_view);
		OnClickListener onBetClicked = new OnClickListener() {
			@Override
			public void onClick(View v) {
				setDownBet(downBetResult + Integer.parseInt((String)v.getTag()));
			}
		};
		downBetView.findViewById(R.id.bet_5).setOnClickListener(onBetClicked);
		downBetView.findViewById(R.id.bet_10).setOnClickListener(onBetClicked);
		downBetView.findViewById(R.id.bet_20).setOnClickListener(onBetClicked);
		downBetView.findViewById(R.id.bet_50).setOnClickListener(onBetClicked);
		downBetView.findViewById(R.id.bet_all).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setDownBet(downBetMax);
			}
		});
		downBetView.findViewById(R.id.bet_clear).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setDownBet(0);
			}
		});
		
		downBetOkView  = downBetView.findViewById(R.id.bet_ok);
		downBetOkView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				downBetView.setVisibility(View.GONE);
				Utils.notify(downBetView);
			}
		});
		
		downBetPlayerView = (TextView) downBetView.findViewById(R.id.player);
		downBetBetView    = (TextView) downBetView.findViewById(R.id.bet);
		
		downBetView.setVisibility(View.GONE);
	}
	
	private void setDownBet(int bet) {
		if(bet < 0) bet = 0;
		else if(bet > downBetMax) bet = downBetMax;
		
		if(bet == 0) {
			downBetOkView.setVisibility(View.INVISIBLE);
			downBetBetView.setText("请选择筹码下注");
		} else {
			downBetOkView.setVisibility(View.VISIBLE);
			downBetBetView.setText("当前下注: " + bet);
		}
		downBetResult = bet;
	}
	
	
	@Override
	public int onDownBet(final PlayerInfo player) {
		downBetMax = player.getTotalBet();
		setDownBet(0);
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				downBetPlayerView.setText("玩家" + (player.getId()+1) +"下注 筹码:" + player.getTotalBet());
				downBetView.setVisibility(View.VISIBLE);
			}
		});
		Utils.wait(downBetView);
		return downBetResult;
	}

	@Override
	public void onDealerHit(Card card) {
		dealerCardsView.addCard(card);
	}

	@Override
	public void onPlayerHit(PlayerInfo player, Card card) {
		cardsViewMap.get(player.getId()).addCard(card);
	}

	@Override
	public void onDealerNewGame(final DealerInfo dealer) {
		runOnUiThread(new Runnable() {
			public void run() {
				dealerView.setText("庄家 筹码:" + (dealer.isUnlimited() ? "无限" : dealer.getTotalBet()));
				dealerCardsView.clear();
			}
		});
	}

	@Override
	public void onPlayerNewGame(final PlayerInfo player) {
		TextView  playerView, betView;
		CardsView cardsView;
		if(!playersViewMap.containsKey(player.getId())) {
			final View playerItem = View.inflate(Game.this, R.layout.player_item, null);
			
			betView    = (TextView) playerItem.findViewById(R.id.bet);
			playerView = (TextView) playerItem.findViewById(R.id.player);
			cardsView  = (CardsView) playerItem.findViewById(R.id.cards);
			
			betsViewMap.put(player.getId(), betView);
			playersViewMap.put(player.getId(), playerView);
			cardsViewMap.put(player.getId(), cardsView);
			
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					playersView.addView(playerItem);
				}
			});
		} else {
			playerView = playersViewMap.get(player.getId());
			betView    = betsViewMap.get(player.getId());
			cardsView  = cardsViewMap.get(player.getId());
		}
		
		cardsView.clear();
		
		final TextView  finalPlayerView = playerView, finalBetView = betView;
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				finalPlayerView.setText("玩家" + (player.getId()+1) + " 筹码:" + player.getTotalBet());
				finalBetView.setText("");
			}
		});
	}

	@Override
	public boolean onAskPlayerHit(final PlayerInfo player) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				askHitView.setVisibility(View.VISIBLE);
				askHitPlayerView.setText("玩家" + (player.getId() + 1) + " 当前点数:" + player.getPoints());
			}
		});
		Utils.wait(askHitView);
		return askHitResult;
	}

	@Override
	public void onPlayerBust(final PlayerInfo player) {
		final Object dialogLock = new Object();
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				new AlertDialog.Builder(Game.this)
				.setTitle(">_<")
				.setMessage("玩家" + (player.getId()+1) + "爆了！")
				.setCancelable(false)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Utils.notify(dialogLock);
					}
				}).show();
			}
		});
		Utils.wait(dialogLock);
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				askHitView.setVisibility(View.GONE);
			}
		});
	}

	@Override
	public void onDealerBust(DealerInfo dealer) {
		final Object dialogLock = new Object();
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				new AlertDialog.Builder(Game.this)
				.setTitle("^_^")
				.setMessage("庄家爆了！")
				.setCancelable(false)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Utils.notify(dialogLock);
					}
				}).show();
			}
		});
		Utils.wait(dialogLock);
	}

	@Override
	public void onPlayerLose(final PlayerInfo player, final int amount) {
		final Object dialogLock = new Object();
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				new AlertDialog.Builder(Game.this)
				.setTitle(">_<")
				.setMessage("玩家" + (player.getId()+1) + "输了" + amount + "点！")
				.setCancelable(false)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Utils.notify(dialogLock);
					}
				}).show();
			}
		});
		Utils.wait(dialogLock);
	}

	@Override
	public void onPlayerWin(final PlayerInfo player, final int amount) {
		final Object dialogLock = new Object();
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				new AlertDialog.Builder(Game.this)
				.setTitle("^_^")
				.setMessage("玩家" + (player.getId()+1) + "赢了" + amount + "点！")
				.setCancelable(false)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Utils.notify(dialogLock);
					}
				}).show();
			}
		});
		Utils.wait(dialogLock);
	}

	@Override
	public void onPlayerEven(final PlayerInfo player) {
		final Object dialogLock = new Object();
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				new AlertDialog.Builder(Game.this)
				.setTitle("-_-|||")
				.setMessage("玩家" + (player.getId()+1) + "平了庄家")
				.setCancelable(false)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Utils.notify(dialogLock);
					}
				}).show();
			}
		});
		Utils.wait(dialogLock);
	}

	@Override
	public void onRefreshDealerCards(DealerInfo dealer) {
		dealerCardsView.postInvalidate();
	}

	@Override
	public void onGameOver(final DealerInfo dealer) {
		final Object dialogLock = new Object();
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				new AlertDialog.Builder(Game.this)
				.setTitle("游戏结束")
				.setMessage(dealer.isAlive() ? "玩家全都输光了..." : "恭喜你们成功战胜了庄家!")
				.setCancelable(false)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Utils.notify(dialogLock);
					}
				}).show();
			}
		});
		Utils.wait(dialogLock);
		finish();
	}
}

class Utils {
	
	/**
	 * Wait a object without any interrupt. (throw IllegalArgumentException instead)
	 * @param object
	 */
	public static void wait(Object object) {
		try {
			synchronized (object) {
				object.wait();
			}
		} catch (InterruptedException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * Notify a object
	 * @param object
	 */
	public static void notify(Object object) {
		synchronized (object) {
			object.notify();
		}
	}
}