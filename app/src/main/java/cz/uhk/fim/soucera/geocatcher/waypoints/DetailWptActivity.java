package cz.uhk.fim.soucera.geocatcher.waypoints;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import cz.uhk.fim.soucera.geocatcher.Cache;
import cz.uhk.fim.soucera.geocatcher.Caches_DB;
import cz.uhk.fim.soucera.geocatcher.R;
import cz.uhk.fim.soucera.geocatcher.caches.SelectCachesActivity;
import cz.uhk.fim.soucera.geocatcher.caches.SelectDetailCacheActivity;

public class DetailWptActivity extends AppCompatActivity {

    private static final String TAG = DetailWptActivity.class.getName();
    private int id;
    private long longId;
    private Waypoint wpt;
    private MenuItem menuItem_Found;
    private MenuItem menuItem_Pairing;
    private static final int LIST_HLEDANE = 0;
    private static final int LIST_NALEZENE = 1;
    private static final int SELECT_CACHE_ACTIVITY = 1000;
    private TextView textName;
    private TextView textType;
    private TextView textCode;
    private ImageView imageType;
    private TextView textSym;
    private TextView textCmt;
    private TextView textLocations;
    private TextView textLocationsMinutes;
    private TextView textCacheName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_wpt);

        try {
            textName = (TextView) findViewById(R.id.textView_name_wpt_detail);
            textType = (TextView) findViewById(R.id.textView_type_wpt_detail);
            textCode = (TextView) findViewById(R.id.textView_code_wpt_detail);
            imageType = (ImageView) findViewById(R.id.imageView_type_wpt_detail);
            textSym = (TextView) findViewById(R.id.textView_sym_wpt_detail);
            textCmt = (TextView) findViewById(R.id.textView_cmt_wpt_detail);
            textLocations = (TextView) findViewById(R.id.textView_location_detail_wpt);
            textLocationsMinutes = (TextView) findViewById(R.id.textView_location_detail_minutes_wpt);
            textCacheName = (TextView) findViewById(R.id.textView_cache_pairing_wpt);

            id = getIntent().getIntExtra("id", 0);
            if(id>(-1)){
                setId(id);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setCacheName(int cache_id){
        Caches_DB caches_db = new Caches_DB(getApplicationContext());
        final Cache cache = caches_db.getCache(cache_id);
        caches_db.close();

        SpannableString content = new SpannableString(cache.getName());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        textCacheName.setTextColor(getResources().getColor(R.color.colorAccent));
        textCacheName.setText(content);
        textCacheName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO onClickListener WPT detail
                Intent intent = new Intent(getApplicationContext(), SelectDetailCacheActivity.class);
                intent.putExtra("id", cache.getId());
                startActivityForResult(intent, 10);
            }
        });
    }

    public void setId(int id) {
        Log.i(TAG, "DetailWpt setID: "+id);
        this.id = id;
        final Context ctx = getApplicationContext();
        Caches_DB caches_db = new Caches_DB(ctx);
        wpt = caches_db.getWpt(id);
        caches_db.close();

        if(wpt.getId_cache()>-1){
            setCacheName(wpt.getId_cache());
        } else {
            textCacheName.setText("Nezparovano!");
        }


        textName.setText(wpt.getDesc());
        textType.setText("Waypoint");
        textCode.setText(wpt.getName());
        textSym.setText(wpt.getSym());
        textCmt.setText(wpt.getCmt());

        if(wpt.getSym().contains("Parking")){
            imageType.setBackgroundResource(R.drawable.ico_parking);
        } else {
            imageType.setBackgroundResource(R.drawable.ico_wpt);
        }

        try {
            DecimalFormat formatter = new DecimalFormat("#0.000000");
            textLocations.setText("N " + formatter.format(wpt.getLat()).replace(",", ".") + " E " + formatter.format(wpt.getLon()).replace(",", "."));
            String latMin = (Location.convert(wpt.getLat(), Location.FORMAT_MINUTES).replace(":", "°").replace(",","."));
            String longMin = Location.convert(wpt.getLon(), Location.FORMAT_MINUTES).replace(":","°").replace(",", ".");
            String[] latMinParts = latMin.split("\\.");
            String[] lonMinParts = longMin.split("\\.");
            if(latMinParts.length==1){
                latMin = latMinParts[0];
            } else {
                if(latMinParts[1].length()<3){
                    latMin = latMinParts[0] + "." + latMinParts[1];
                } else {
                    latMin = latMinParts[0] + "." +latMinParts[1].subSequence(0,3);
                }
            }
            if(lonMinParts.length==1){
                longMin = lonMinParts[0];
            } else {
                if(lonMinParts[1].length()<3){
                    longMin = lonMinParts[0] + "." + lonMinParts[1];
                } else {
                    longMin = lonMinParts[0] + "." + lonMinParts[1].subSequence(0,3);
                }
            }
            textLocationsMinutes.setText("N " + latMin + " E " + longMin);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_wpt_detail, menu);

        menuItem_Found = menu.findItem(R.id.action_is_found);
        Caches_DB wpts_db = new Caches_DB(getApplicationContext());
        try {
            Waypoint wpt = wpts_db.getWpt(id);
            if(wpt.getId_list()== LIST_NALEZENE){
                menuItem_Found.setIcon(R.drawable.ico_found_128);
                Log.i(TAG, "Found true");

            } else{
                menuItem_Found.setIcon(R.drawable.ico_not_found_128);
                Log.i(TAG, "Found NOT");
            }

            menuItem_Pairing = menu.findItem(R.id.action_pairing_wpt_detail);
            if(wpt.getId_cache() > 0){
                menuItem_Pairing.setIcon(R.drawable.ico_pairing_green);
            } else {
                menuItem_Pairing.setIcon(R.drawable.ico_pairing_red);
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            wpts_db.close();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_is_found:
                isFound(id);
                return true;
            case R.id.action_pairing_wpt_detail:
                //TODO select cache to pairing
                Log.i(TAG, "MenuClick_find_one_cache_pairing_action");
                Intent serverSetIntent = new Intent(this, SelectCachesActivity.class);
                startActivityForResult(serverSetIntent, SELECT_CACHE_ACTIVITY);
                return true;
            case android.R.id.home:
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
                return true;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_CACHE_ACTIVITY && resultCode == RESULT_OK && data != null) {
            try {
                //TODO PAIRING!
                System.out.println("TODO PAIRING: " + data.getIntExtra("id", 0));
                int cache_id = data.getIntExtra("id", 0);
                Caches_DB caches_db = new Caches_DB(getApplicationContext());
                String partCode = wpt.getName().substring(Math.max(0, wpt.getName().length() - 5));
                System.out.println("WPT partCode: " + partCode);
                ArrayList<Waypoint> wpts = caches_db.getWptsByCode(partCode);
                boolean status = caches_db.updateWpts(wpts, cache_id);
                System.out.println("STATUS UPDATE: " + status);

                if(status && cache_id>0){
                    menuItem_Pairing.setIcon(R.drawable.ico_pairing_green);
                } else {
                    menuItem_Pairing.setIcon(R.drawable.ico_pairing_red);
                }

                if(status){
                    setCacheName(cache_id);
                }

                if(wpts!=null){
                    System.out.println("WPTS size: " + wpts.size());
                    for(int i = 0; i < wpts.size(); i++){
                        System.out.println("WPTS desc: " + wpts.get(i).getDesc());
                    }
                }
                //findSelectedCache(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onBackPressed(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    private void isFound(int id){
        Caches_DB wpts_db = new Caches_DB(getApplicationContext());
        try {
            Waypoint wpt = wpts_db.getWpt(id);
            if(wpts_db.isWptFound(wpt)){
                menuItem_Found.setIcon(R.drawable.ico_not_found_128);
                Log.i(TAG, "Found NOT");

            } else{
                menuItem_Found.setIcon(R.drawable.ico_found_128);
                Log.i(TAG, "Found true");
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            wpts_db.close();
        }
    }
}
