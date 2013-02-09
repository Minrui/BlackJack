package blackjack.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Spinner;

public class Start extends Activity {
	
	private Spinner playersView;
	private Spinner playerBetView;
	private Spinner dealerBetView;
	private Spinner decksView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        
        playersView   = (Spinner) findViewById(R.id.players);
        playerBetView = (Spinner) findViewById(R.id.bet);
        dealerBetView = (Spinner) findViewById(R.id.dealer_bet);
        decksView     = (Spinner) findViewById(R.id.decks);
        
        findViewById(R.id.start).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Start.this, Game.class);
				intent.putExtra("players", Integer.parseInt((String) playersView.getSelectedItem()));
				intent.putExtra("bet", Integer.parseInt((String) playerBetView.getSelectedItem()));
				try {
					intent.putExtra("dealer_bet", Integer.parseInt((String) dealerBetView.getSelectedItem()));
				} catch (NumberFormatException e) {
					intent.putExtra("dealer_bet", 0);
				}
				intent.putExtra("decks", Integer.parseInt((String) decksView.getSelectedItem()));
				startActivity(intent);
			}
		});
    }
}