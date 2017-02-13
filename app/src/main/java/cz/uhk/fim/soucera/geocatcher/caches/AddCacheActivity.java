package cz.uhk.fim.soucera.geocatcher.caches;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import cz.uhk.fim.soucera.geocatcher.caches.Fragments.Fragment_Add_Cache;
import cz.uhk.fim.soucera.geocatcher.R;

public class AddCacheActivity extends AppCompatActivity {

    private Fragment_Add_Cache frag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cache);
        try {
            frag = (Fragment_Add_Cache) getSupportFragmentManager().findFragmentById(R.id.add_cache_frag);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cache_add, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_cache_process:
                frag.onAddCache();
                return true;
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                Intent i = new Intent(this, MainCachesActivity.class);
                startActivityForResult(i, 0);
                finish();
                return true;
        }
        return true;
    }
}
