package cz.uhk.fim.soucera.geocatcher.caches;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import cz.uhk.fim.soucera.geocatcher.caches.Fragments.Fragment_Edit_Cache;
import cz.uhk.fim.soucera.geocatcher.R;

public class EditCacheActivity extends AppCompatActivity {

    private Fragment_Edit_Cache fragEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cache);

        int id = getIntent().getIntExtra("id", 0);
        System.out.println(getIntent().getIntExtra("id", 0));
        fragEdit = (Fragment_Edit_Cache) getSupportFragmentManager().findFragmentById(R.id.fragEditCache);
        fragEdit.setId(id);

        //fragEdit.setViews(id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cache_edit, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_update_cache_process:
                try {
                    fragEdit.onUpdateCache();
                } catch (Exception e){
                    e.printStackTrace();
                }
                return true;
            case android.R.id.home:
                Intent i = new Intent(this, MainCachesActivity.class);
                startActivityForResult(i, 0);
                finish();
                return true;
        }
        return true;
    }
}
