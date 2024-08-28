package com.example.sportstimer;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Diese Klasse ist fuer den Startbildschirm verantwortlich, den der Nutzer beim Starten der App
 * zu sehen bekommt. Sie ermoeglicht ihm, einen rundenbasierten Timer zu starten oder die
 * Aktivitaet ueber die BottomNavigationView zu wechseln.
 *
 * @author Heiko Heitgress
 */
public class MainActivity extends AppCompatActivity {

    private TextView textViewMinutes;
    private EditText editTextMinutes;
    private EditText editTextSeconds;
    private EditText editTextRounds;
    private EditText editTextTimeOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisiere die BottomNavigationView
        BottomNavigationView bnv = findViewById(R.id.bottom_navigation);
        bnv.setSelectedItemId(R.id.navigation_bar_destination_timer);
        bnv.setOnItemSelectedListener(item -> {
            Intent intent;
            int id = item.getItemId();

            if(id == R.id.navigation_bar_destination_timer){
                return true;
            }else if(id == R.id.navigation_bar_stopwatch) {
                intent = new Intent(MainActivity.this, StopWatchActivity.class);
                startActivity(intent);
                return true;
            }
            else if(id == R.id.navigation_bar_destination_settings) {
                intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }else {
                return false;
            }
        });

        // Initialisiere TextView-Felder
        textViewMinutes = findViewById(R.id.textViewMinutes);

        // Initialisiere die EditText-Felder
        editTextMinutes = findViewById(R.id.editTextMinutes);
        editTextSeconds = findViewById(R.id.editTextSeconds);
        editTextRounds = findViewById(R.id.editTextRounds);
        editTextTimeOut = findViewById(R.id.editTextTimeOut);

        // Setze den InputType auf Integer
        editTextMinutes.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTextSeconds.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTextRounds.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTextTimeOut.setInputType(InputType.TYPE_CLASS_NUMBER);

        /**
         * Die Methode switchFocusOnInputLen auf die Minuten-, Sekunden-, und Rundeneingabe
         * angewendet. Es wird bewusst in Kauf genommen, dass der Nutzer Werte ueber 99 nicht
         * mehr komfortabel eintippen kann; da es sich um einen Sport-Timer handelt, werden
         * 99 Stunden bzw. 99 Runden nie erreicht werden.
         */
        switchFocusOnInputLen(editTextMinutes,editTextSeconds,2);
        switchFocusOnInputLen(editTextSeconds,editTextRounds,2);
        switchFocusOnInputLen(editTextRounds,editTextTimeOut,2);

        // Initialisiere die Buttons
        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(view -> {

            if (isEmpty(editTextMinutes) || isEmpty(editTextSeconds) || isEmpty(editTextRounds)
                    || isEmpty(editTextTimeOut)) {
                Toast.makeText(getApplicationContext(), "Please Fill Out all Fields",
                        Toast.LENGTH_SHORT).show();
            } else {
                int enteredMinutes = Integer.parseInt(editTextMinutes.getText().toString());
                int enteredSeconds = Integer.parseInt(editTextSeconds.getText().toString());
                int enteredRounds = Integer.parseInt(editTextRounds.getText().toString());
                int enteredTimeOut = Integer.parseInt((editTextTimeOut.getText().toString()));

                int totalSeconds = enteredSeconds + enteredMinutes*60;
                String confirmation = "";
                if(totalSeconds > 0 && enteredRounds > 0) {
                    // Sende die Nutzereingaben an TimerProcess.java
                    Intent intent = new Intent(MainActivity.this, TimerActivity.class);
                    intent.putExtra("totalSeconds", totalSeconds);
                    intent.putExtra("enteredRounds", enteredRounds);
                    intent.putExtra("enteredTimeOut", enteredTimeOut);
                    if(enteredRounds!= 1) {
                        confirmation = "Starting " + enteredRounds + " Rounds @" +
                                String.format("%d:%02d", enteredMinutes, enteredSeconds);
                    }else {
                        confirmation = "Starting " + enteredRounds + " Round @" +
                                String.format("%d:%02d", enteredMinutes, enteredSeconds);
                    }
                    startActivity(intent);

                }else {
                    if(totalSeconds <= 0 && enteredRounds > 0) {
                        confirmation = "You need to enter a valid time value";
                    } else if (enteredRounds <= 0 && totalSeconds > 0) {
                        confirmation = "You need to enter at least 1 round.";
                    } else if (totalSeconds <= 0 && enteredRounds <= 0) {
                        confirmation = "You need to enter at least 1 round.";
                        Toast.makeText(getApplicationContext(),
                                "You need to enter a valid time value",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                Toast.makeText(getApplicationContext(), confirmation, Toast.LENGTH_SHORT).show();
            }
        });

        Button incMinutes = findViewById(R.id.incrementMinutes);
        Button decMinutes = findViewById(R.id.decrementMinutes);
        Button incSeconds = findViewById(R.id.incrementSeconds);
        Button decSeconds = findViewById(R.id.decrementSeconds);
        Button incRounds = findViewById(R.id.incrementRounds);
        Button decRounds = findViewById(R.id.decrementRounds);
        Button incTimeOut = findViewById(R.id.incrementTimeOut);
        Button decTimeOut = findViewById(R.id.decrementTimeOut);

        initButton(incMinutes, editTextMinutes, true);
        initButton(decMinutes, editTextMinutes, false);
        initButton(incSeconds,editTextSeconds,true);
        initButton(decSeconds,editTextSeconds,false);
        initButton(incRounds,editTextRounds,true);
        initButton(decRounds,editTextRounds,false);
        initButton(incTimeOut,editTextTimeOut,true);
        initButton(decTimeOut,editTextTimeOut,false);
    }

    /**
     * Diese Methode verringert den Code, der für die Initialisierung der Increment- und
     * Decrement-Buttons erforderlich ist. Es wird zunächst überprüft, ob der jeweilige
     * Wert leer ist, damit dieser ggf. mit 1 oder 0 initialisiert werden kann. Es wird
     * außerdem verhindert, dass ein Nutzer mit dem Decrement-Knopf negative Werte eingibt.
     * @param button soll initialisiert werden
     * @param et den Wert dieses EditText-Feldes soll der Button manipulieren
     * @param inc legt fest, ob es sich um einen increment- oder decrement-Knopf handelt
     */
    public void initButton(Button button, EditText et, boolean inc) {
        button.setOnClickListener(view -> {
            if(!isEmpty(et)){
                int current = Integer.parseInt(et.getText().toString());
                if(inc == true){
                    current++;
                }else {
                    if(current > 0){
                        current--;
                    }else{
                        current = 0;
                        Toast.makeText(getApplicationContext(),
                                "Can't go any lower than zero.", Toast.LENGTH_SHORT).show();
                    }
                }
                et.setText(String.valueOf(current));
            }else{
                if(inc == true){
                    et.setText("1");
                }else {
                    et.setText("0");
                }
            }
        });
    }

    public boolean isEmpty(EditText et) {
        return et.getText().toString().trim().isEmpty();
    }

    /**
     * Diese Methode verschiebt den Fokus von einem EditText auf ein anderes, sobald
     * die Bedingung erfuellt ist, dass der Eingabe-String >= 2 Zeichen lang ist. Das
     * soll die Navigation durch das UI beschleunigen.
     * Die ungenutzen abstrakten Methoden beforeTextChanged und afterTextChanged
     * muessen trotz ihrer leeren Bodies implementiert werden.
     * @param from ist das EditText-Objekt, von dem aus der Fokus gewechselt wird
     * @param to ist das EditText-Objekt, auf das der Fokus gesetzt wird
     * @param length ist die Anzahl an Schriftzeichen, ab der gewechselt wird
     */
    public void switchFocusOnInputLen(EditText from, EditText to, int length) {
        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String input = from.getText().toString();
                if(input.length() >= length){
                    to.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        from.addTextChangedListener(tw);
    }
}

