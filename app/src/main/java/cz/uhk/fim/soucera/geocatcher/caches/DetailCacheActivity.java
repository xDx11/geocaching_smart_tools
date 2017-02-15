package cz.uhk.fim.soucera.geocatcher.caches;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import cz.uhk.fim.soucera.geocatcher.Cache;
import cz.uhk.fim.soucera.geocatcher.MapsActivity;
import cz.uhk.fim.soucera.geocatcher.caches.Fragments.Fragment_Detail_Cache;
import cz.uhk.fim.soucera.geocatcher.Caches_DB;
import cz.uhk.fim.soucera.geocatcher.R;

public class DetailCacheActivity extends AppCompatActivity {
    private int id;
    private long longId;
    private static final String TAG = DetailCacheActivity.class.getName();
    private MenuItem menuItem_;
    private static final int LIST_HLEDANE = 0;
    private static final int LIST_NALEZENE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_cache);

        id = getIntent().getIntExtra("id", 0);
        Fragment_Detail_Cache detailFragment = (Fragment_Detail_Cache) getSupportFragmentManager().findFragmentById(R.id.detail_cache_frag);
        try{
            detailFragment.setId(id);
        } catch (Exception e){
            e.printStackTrace();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cache_detail, menu);
        menuItem_ = menu.findItem(R.id.action_is_found);
        Caches_DB caches = new Caches_DB(DetailCacheActivity.this.getApplicationContext());
        try {
            Cache cache = caches.getCache(id);
            if(cache.getId_list()== LIST_NALEZENE){
                menuItem_.setIcon(R.drawable.ico_found_128);
                Log.i(TAG, "Found true");

            } else{
                menuItem_.setIcon(R.drawable.ico_not_found_128);
                Log.i(TAG, "Found NOT");
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            caches.close();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_cache_detail:
                editCache(id);
                return true;
            case R.id.action_del_cache_detail:
                deleteCache(id);
                return true;
            case R.id.action_map_navigate:
                navigateCache(id);
                return true;
            case R.id.action_is_found:
                isFound(id);
                return true;
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                //Intent i = new Intent(this, MainCachesActivity.class);
                //startActivityForResult(i, 0);
                finish();
                //finishActivity(0);
                return true;
        }
        return true;
    }

    public void onBackPressed(){
        //Intent i = new Intent(this, MainCachesActivity.class);
        //startActivityForResult(i, 0);

        finish();
        //finishActivity(0);
    }

    private void deleteCache(long id){
        this.longId = id;
        new AlertDialog.Builder(DetailCacheActivity.this, R.style.YourAlertDialogTheme)
                .setTitle("Smazat?")
                .setMessage("Opravdu si přejete smazat danou cache?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        Caches_DB caches = new Caches_DB(DetailCacheActivity.this.getApplicationContext());
                        try {
                            if(caches.deleteCache(longId)){
                                Toast.makeText(DetailCacheActivity.this, R.string.cache_deleted, Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(DetailCacheActivity.this, MainCachesActivity.class);
                                startActivityForResult(i, 0);
                                finish();
                            } else{
                                Toast.makeText(DetailCacheActivity.this, R.string.cache_not_deleted, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        } finally {
                            caches.close();
                        }

                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void editCache(int id){
        Intent i = new Intent(this, EditCacheActivity.class);
        i.putExtra("id", id);
        startActivityForResult(i, 0);
        finish();
    }

    private void navigateCache(int id){
        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra("id",id);
        startActivity(i);
        finish();
    }

    private void isFound(int id){
        Caches_DB caches = new Caches_DB(DetailCacheActivity.this.getApplicationContext());
        try {
            Cache cache = caches.getCache(id);
            if(caches.isCacheFound(cache)){
                menuItem_.setIcon(R.drawable.ico_not_found_128);
                Log.i(TAG, "Found true");

            } else{
                menuItem_.setIcon(R.drawable.ico_found_128);
                Log.i(TAG, "Found NOT");
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            caches.close();
        }
    }



}
