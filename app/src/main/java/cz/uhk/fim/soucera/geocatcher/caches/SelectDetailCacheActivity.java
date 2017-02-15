package cz.uhk.fim.soucera.geocatcher.caches;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import cz.uhk.fim.soucera.geocatcher.Cache;
import cz.uhk.fim.soucera.geocatcher.Caches_DB;
import cz.uhk.fim.soucera.geocatcher.R;
import cz.uhk.fim.soucera.geocatcher.caches.Fragments.Fragment_Detail_Cache;

public class SelectDetailCacheActivity extends AppCompatActivity {

    private static final String TAG = SelectDetailCacheActivity.class.getName();
    private int id;
    private int requestCode;
    private long longId;
    private MenuItem menuItem_;
    private static final int LIST_HLEDANE = 0;
    private static final int LIST_NALEZENE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_cache);

        id = getIntent().getIntExtra("id", 0);
        requestCode = getIntent().getIntExtra("requestCode", 0);
        Fragment_Detail_Cache detailFragment = (Fragment_Detail_Cache) getSupportFragmentManager().findFragmentById(R.id.detail_cache_frag);
        detailFragment.setId(id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cache_detail, menu);
        menuItem_ = menu.findItem(R.id.action_del_cache_detail);
        menuItem_.setVisible(false);
        menuItem_ = menu.findItem(R.id.action_edit_cache_detail);
        menuItem_.setVisible(false);
        menuItem_ = menu.findItem(R.id.action_map_navigate);
        menuItem_.setVisible(false);

        menuItem_ = menu.findItem(R.id.action_is_found);
        Caches_DB caches = new Caches_DB(SelectDetailCacheActivity.this.getApplicationContext());
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
            case R.id.action_is_found:
                isFound(id);
                return true;
            case android.R.id.home:
                Intent returnIntent = new Intent();
                returnIntent.putExtra("idCache", id);
                setResult(Activity.RESULT_CANCELED, returnIntent);

                finish();
                return true;
        }
        return true;
    }

    public void onBackPressed(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("idCache", id);
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    private void isFound(int id){
        Caches_DB caches = new Caches_DB(SelectDetailCacheActivity.this.getApplicationContext());
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
