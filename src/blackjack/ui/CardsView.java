package blackjack.ui;

import java.io.IOException;
import java.util.Vector;

import blackjack.core.Deck.Card;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class CardsView extends View {

	public CardsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		synchronized (CardsView.class) {
			if(cardBitmaps == null) {
				cardBitmaps = new Bitmap[Card.SUITS][Card.RANKS];
				for(int suit = 0; suit < Card.SUITS; ++suit) for(int rank = 0; rank < Card.RANKS; ++rank) {
					try {
						cardBitmaps[suit][rank] = BitmapFactory.decodeStream(context.getAssets().open("card_" + suit + "_" + rank + ".bmp"));
					} catch (IOException e) {
						throw new IllegalArgumentException(e);
					}
				}
			}
			if(cardSetBitmap == null) {
				try {
					cardSetBitmap = BitmapFactory.decodeStream(context.getAssets().open("card_set.bmp"));
				} catch (IOException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}
	}
	
	public void addCard(Card card) {
		cards.add(card);
		this.postInvalidate();
	}
	
	/**
	 * Remove all cards.
	 */
	public void clear() {
		cards.removeAllElements();
		this.postInvalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for(int i = 0; i < cards.size(); ++i) {
			Card card = cards.get(i);
			Bitmap cardBitmap = card.isSet ? cardSetBitmap : cardBitmaps[card.suit][card.rank];
			canvas.drawBitmap(cardBitmap, OFFSET_X * i, OFFSET_Y * i, null);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int ASSUME_MAX_CARDS = 7;
		setMeasuredDimension(CARD_WIDTH + OFFSET_X * ASSUME_MAX_CARDS, CARD_HEIGHT + OFFSET_Y * ASSUME_MAX_CARDS);
	}
	
	private final Vector<Card>    cards  = new Vector<Card>();
	
	private static final int OFFSET_X = 10, OFFSET_Y = 2, CARD_WIDTH = 48, CARD_HEIGHT = 64;
	private static Bitmap[][] cardBitmaps; //use cardBitmaps[suit][rank] to get
	private static Bitmap     cardSetBitmap;
}
