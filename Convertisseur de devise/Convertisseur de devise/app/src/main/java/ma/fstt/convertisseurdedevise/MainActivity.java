package ma.fstt.convertisseurdedevise;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private String currencyFrom = "EUR";
    private String currencyTo = "USD";
    private float conversionRate = 0f;

    private EditText inputText;
    private EditText resultText;
    private Button convertBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputText = findViewById(R.id.input_text);
        resultText = findViewById(R.id.result_text);
        convertBtn = findViewById(R.id.btn_convert);

        spinnerSetup();
        onButton();

    }

    private void getApiResult(){

        if (inputText != null && !inputText.getText().toString().isEmpty() && !inputText.getText().toString().trim().isEmpty()) {

            final String API_KEY = "521f754eefb1c61fbaa85459";
            final String API = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/" + currencyFrom;

            if (Objects.equals(currencyFrom, currencyTo)) {
                Toast.makeText(
                        getApplicationContext(),
                        "Please pick a currency to convert",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(() -> {

                    try {

                        URL url = new URL(API);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                        int responseCode = connection.getResponseCode();
                        if (responseCode >= 200 && responseCode < 300) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuilder stringBuilder = new StringBuilder();
                            String line;

                            while ((line = reader.readLine()) != null) {
                                stringBuilder.append(line);
                            }

                            reader.close();
                            connection.disconnect();

                            String apiResult = stringBuilder.toString();

                            JSONObject jsonObject = new JSONObject(apiResult);
                            String rateString = jsonObject.getJSONObject("conversion_rates").getString(currencyTo);
                            conversionRate = Float.parseFloat(rateString);

                            new Handler(Looper.getMainLooper()).post(() -> {
                                float value = Float.parseFloat(inputText.getText().toString());
                                String text = Float.toString(value * conversionRate);
                                if (resultText != null) {
                                    resultText.setText(text);
                                }
                            });
                        } else {
                            Log.e("MainActivity", "HTTP error code: " + responseCode);
                        }
                    } catch (Exception err) {
                        Log.e("MainActivity", "Error during API call", err);
                    }

                });
            }
        }

    }

    public void spinnerSetup(){

        Spinner spinnerFrom = findViewById(R.id.spinner_from);
        Spinner spinnerTo = findViewById(R.id.spinner_to);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.currencies2, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTo.setAdapter(adapter2);

        spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO: Handle the scenario when nothing is selected
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currencyFrom = parent.getItemAtPosition(position).toString();
                getApiResult();
            }
        });

        spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO: Handle the scenario when nothing is selected
            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currencyTo = parent.getItemAtPosition(position).toString();
                getApiResult();
            }
        });

    }

    public void onButton(){

        convertBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            getApiResult();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Type a value", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

    }

}