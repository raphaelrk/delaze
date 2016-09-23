package net.qwuke.unblyopia;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.Switch;
import android.widget.TextView;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaSquare;
import yuku.ambilwarna.widget.AmbilWarnaPreference;

import static net.qwuke.unblyopia.TetrisModel.fallenColour;

public class MenuActivity extends AppCompatActivity {
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        prefs = getApplicationContext().getSharedPreferences(getPackageName(),MODE_PRIVATE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Resources res = getResources();
        String text = res.getString(R.string.title_name);
        CharSequence styledText = Html.fromHtml(text);
        toolbar.setTitle(styledText);
        setSupportActionBar(toolbar);
        if(prefs.getBoolean("BackgroundMusic", true)) {
            Intent svc = new Intent(this, MusicService.class);
            startService(svc);
        }
        final Button button = (Button) findViewById(R.id.button_tetris);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent("android.intent.action.GAME"));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);
        switch(item.getItemId()){
            case R.id.settings:
                showSettingsDialog();
                break;
            case R.id.about:
                showAboutDialog();
                break;
        }
        return true;
    }


    private void showSettingsDialog(){

        final SharedPreferences.Editor editor = prefs.edit();

        AlertDialog.Builder bldr = new AlertDialog.Builder(this);
        bldr.setTitle(R.string.settings);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30,20,30,20);

        final Switch headTrackSwitch = new Switch(this);
        headTrackSwitch.setText(R.string.headTracking);
        headTrackSwitch.setChecked(prefs.getBoolean("HeadTracking", true));
        layout.addView(headTrackSwitch);

        final Switch musicSwitch = new Switch(this);
        musicSwitch.setText(R.string.bgMusic);
        musicSwitch.setChecked(prefs.getBoolean("BackgroundMusic", true));
        layout.addView(musicSwitch);

        final Button activeColourButt = new Button(this);
        activeColourButt.setText("Select Active Block Colour");
        activeColourButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCustomColour(prefs.getInt("activeEyeBlockColour", Color.CYAN), "activeEyeBlockColour");
            }
        });
        layout.addView(activeColourButt);

        final Button bgColourButt = new Button(this);
        bgColourButt.setText("Select Background Colour");
        bgColourButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCustomColour(prefs.getInt("bgColour", Color.LTGRAY), "bgColour");
            }
        });
        layout.addView(bgColourButt);

        final Button fallenColourButt = new Button(this);
        fallenColourButt.setText("Select Fallen Block Colour");
        fallenColourButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCustomColour(prefs.getInt("fallenColour", Color.BLUE), "fallenColour");
            }
        });
        layout.addView(fallenColourButt);

        bldr.setView(layout);

        bldr.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(!musicSwitch.isChecked()){
                    stopService(new Intent(MenuActivity.this, MusicService.class));
                }

                editor.putBoolean("HeadTracking", headTrackSwitch.isChecked());
                editor.putBoolean("BackgroundMusic", musicSwitch.isChecked());

                editor.commit();
            }
        });
        bldr.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Just close the dialog
            }
        });

        bldr.create().show();
    }

    private void getCustomColour(int defaultColour, final String key){

        AmbilWarnaDialog dialog = new AmbilWarnaDialog(MenuActivity.this, defaultColour, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                //Do Nothing
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                prefs.edit().putInt(key, color).commit();
            }
        });
        dialog.show();
    }

    private void showAboutDialog(){
        AlertDialog.Builder bldr = new AlertDialog.Builder(this);
        bldr.setTitle(R.string.about_title);
        TextView aboutTextView = new TextView(this);
        //Most of this straight out of the README.md with controls added
        aboutTextView.setText(R.string.about_text);
        aboutTextView.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Medium);
        aboutTextView.setPadding(30,20,30,20);
        aboutTextView.setScroller(new Scroller(this));
        aboutTextView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
        aboutTextView.setMovementMethod(new ScrollingMovementMethod());
        bldr.setView(aboutTextView);
        bldr.setPositiveButton(R.string.ok, null);
        bldr.create().show();
    }
}
