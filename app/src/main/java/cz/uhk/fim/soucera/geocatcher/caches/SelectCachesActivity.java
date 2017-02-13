package cz.uhk.fim.soucera.geocatcher.caches;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import cz.uhk.fim.soucera.geocatcher.caches.Fragments.FragmentMainActivityCaches;
import cz.uhk.fim.soucera.geocatcher.R;

public class SelectCachesActivity extends AppCompatActivity implements FragmentMainActivityCaches.OnCacheClickedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_caches);
    }

    @Override
    public void onCacheClicked(int id) {
        selectCache(id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_back_only, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Intent returnIntent = new Intent();
                //setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
                return true;
        }
        return true;
    }

    public void onBackPressed(){
        //Intent returnIntent = new Intent();
        //setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    private void selectCache(int id) {
        Intent intent = new Intent();
        intent.putExtra("id", id);
        setResult(RESULT_OK, intent);
        finish();
    }
}
