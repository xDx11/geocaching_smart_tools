package cz.uhk.fim.soucera.geocatcher.caches;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import cz.uhk.fim.soucera.geocatcher.caches.Fragments.FragmentMainActivityCaches;
import cz.uhk.fim.soucera.geocatcher.R;

public class MainCachesActivity extends AppCompatActivity implements FragmentMainActivityCaches.OnCacheClickedListener, PopupMenu.OnMenuItemClickListener {

    private static final String TAG = MainCachesActivity.class.getName();
    private @Nullable PopupMenu popup;
    private FragmentMainActivityCaches mainFragment;
    private static String[] PERMISSIONS_MAPS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final CharSequence[] FILTER_FIND_ITEMS = {"Všechny keše", "Nalezené keše", "Nenalezené keše"};
    private static final int FILTER_ALL = 0;
    private static final int FILTER_FOUND = 1;
    private static final int FILTER_NOT_FOUND = 2;
    private static final int SORTING_DEFAULT_ADDED = 0;
    private static final int SORTING_NAME = 1;
    private static final int SORTING_CODE = 2;
    private static final int SORTING_TYPE = 3;
    private static final int SORTING_DISTANCE = 4;
    private static final String LIST_STATE = "listState";
    private Parcelable mListState = null;
    private int listPosition;
    private int sortingType;
    private int filterType;
    private String findingString;

    private SharedPreferences preferenceSettingsSorting;
    private SharedPreferences.Editor preferenceEditor;
    private static final int PREFERENCE_MODE_PRIVATE = 0;
    private SharedPreferences preferenceSettingsFiltering;
    private SharedPreferences.Editor preferenceEditorFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_caches);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        mainFragment = (FragmentMainActivityCaches) getSupportFragmentManager().findFragmentById(R.id.mainFragCaches);
        preferenceSettingsSorting = getSharedPreferences("sortingOptions",PREFERENCE_MODE_PRIVATE);
        sortingType = preferenceSettingsSorting.getInt("sortingType", SORTING_DEFAULT_ADDED);
        System.out.println("onCreate_sortingType: "+sortingType);
        preferenceSettingsFiltering = getSharedPreferences("filteringOptions", PREFERENCE_MODE_PRIVATE);
        filterType = preferenceSettingsFiltering.getInt("filterType", FILTER_ALL);
    }

    @Override
    public void onCacheClicked(int id) {
        showCache(id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_caches, menu);

        return true;
    }

    public boolean checkLocationPermission() {
        Log.i(TAG, "checkLocationPermission");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        PERMISSIONS_MAPS,
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        PERMISSIONS_MAPS,
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_cache:
                try {
                    Intent i = new Intent(this, AddCacheActivity.class);
                    startActivityForResult(i, 0);
                    //finish();
                } catch (Exception e){
                    e.printStackTrace();
                }
                return true;
            case R.id.action_main_find_caches:
                showFindDialog();
                return true;
            case R.id.action_filter:
                try {
                    View menuItemView = findViewById(R.id.action_filter); // SAME ID AS MENU ID
                    showMenu(menuItemView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return true;
    }

    private void showFindDialog(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.YourAlertDialogTheme);

        alert.setTitle("Vyhledavani");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alert.setIcon(getResources().getDrawable(R.drawable.ico_cache_128, getTheme()));
        } else {
            alert.setIcon(getResources().getDrawable(R.drawable.ico_cache_128));
        }
        final EditText input = new EditText(this);
        findingString = "";
        input.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null)
                {
                    if(s.length()>0)
                        mainFragment.getFoundList(filterType, sortingType, s.toString());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }


        });
        input.setSingleLine();
        input.setTextColor(getResources().getColor(R.color.colorWhite));
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 40;
        params.rightMargin = 40;
        input.setLayoutParams(params);
        container.addView(input);
        alert.setView(container);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        alert.setNegativeButton("Zrušit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mainFragment.showCachesByFilter(filterType, sortingType);
            }
        });
        alert.show();

    }

    public void showMenu(View v) {
        try {
            Log.i(TAG, "ShowPopUpMenuFind");
            ActionBar actionBar = getSupportActionBar();
            if(actionBar!=null){
                popup = new PopupMenu(actionBar.getThemedContext(), v);
                popup.setOnMenuItemClickListener(this);
                popup.inflate(R.menu.menu_detail_filter);
                popup.show();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_main_filter_caches:
                Log.i(TAG, "MenuClick_main_caches_filter");
                showFilterFindCacheDialog();
                return true;
            case R.id.action_load_complete_listings:
                Log.i(TAG, "MenuClick_load_complete_listings");
                loadListingDialog();
                return true;
            case R.id.action_sorting_listview:
                Log.i(TAG, "MenuClick_sorting_listview");
                sorting();
                return true;
            default:
                return false;
        }
    }

    private void showFilterFindCacheDialog() {
        Log.i(TAG, "showFilterFindCacheDialog");
        final String fDialogTitle = "Jakým způsobem chcete keše vyhledávat: ";
        filterType = preferenceSettingsFiltering.getInt("filterType", FILTER_ALL);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.YourAlertDialogTheme);
        builder.setTitle(fDialogTitle);
        builder.setSingleChoiceItems(
                FILTER_FIND_ITEMS,
                filterType,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        try {
                            switch (item) {
                                case FILTER_ALL:
                                    Log.i(TAG, "MenuClick_find_all_caches_action");
                                    filterType = FILTER_ALL;
                                    filterCaches();
                                    break;
                                case FILTER_FOUND:
                                    Log.i(TAG, "MenuClick_found_caches_filter");
                                    filterType = FILTER_FOUND;
                                    filterCaches();
                                    break;
                                case FILTER_NOT_FOUND:
                                    Log.i(TAG, "MenuClick_not_found_caches_filter");
                                    filterType = FILTER_NOT_FOUND;
                                    filterCaches();
                                    break;
                                default:
                                    Log.i(TAG, "MenuClick_find_all_caches_action");
                                    filterType = FILTER_ALL;
                                    filterCaches();
                                    break;
                            }
                            dialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).show();
    }

    private void sorting() {
        Log.i(TAG, "Sorting");
        final String fDialogTitle = "Kritérium řazení: ";
        final CharSequence[] SORTING_TYPE_ITEMS = {"DEFAULT_DATE", "NAME", "CODE", "TYPE", "DISTANCE" };
        sortingType = preferenceSettingsSorting.getInt("sortingType",0);
        System.out.println("sorting()_sortingType: "+ sortingType);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.YourAlertDialogTheme);
        builder.setTitle(fDialogTitle);
        builder.setSingleChoiceItems(
                SORTING_TYPE_ITEMS,
                sortingType,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case SORTING_NAME:
                                sortingType = SORTING_NAME;
                                sortCaches();
                                break;
                            case SORTING_CODE:
                                sortingType = SORTING_CODE;
                                sortCaches();
                                break;
                            case SORTING_TYPE:
                                sortingType = SORTING_TYPE;
                                sortCaches();
                                break;
                            case SORTING_DISTANCE:
                                sortingType = SORTING_DISTANCE;
                                preferenceEditor = preferenceSettingsSorting.edit();
                                preferenceEditor.putInt("sortingType", sortingType);
                                preferenceEditor.apply();
                                mainFragment.sortByDistance();
                                break;
                            default: //TODO distance sorting variant
                                sortingType = SORTING_DEFAULT_ADDED;
                                preferenceEditor = preferenceSettingsSorting.edit();
                                preferenceEditor.putInt("sortingType", sortingType);
                                preferenceEditor.apply();
                                sortCaches();
                                break;
                        }
                        dialog.dismiss();
                    }
                }
        ).show();
    }

    private void sortCaches(){
        preferenceEditor = preferenceSettingsSorting.edit();
        preferenceEditor.putInt("sortingType", sortingType);
        preferenceEditor.apply();
        mainFragment.showCachesByFilter(filterType, sortingType);
    }

    private void filterCaches(){
        preferenceEditorFilter = preferenceSettingsFiltering.edit();
        preferenceEditorFilter.putInt("filterType", filterType);
        preferenceEditorFilter.apply();
        mainFragment.showCachesByFilter(filterType, sortingType);
        if(sortingType==SORTING_DISTANCE){
            mainFragment.sortByDistance();
        }
    }

    private void loadListingDialog(){
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.YourAlertDialogTheme)
                .setTitle("Nastavení listingů")
                .setIcon(R.drawable.ico_disable_cache)
                .setMessage("Přejete si zapnout kompletní načítání listingů keší? Vyžaduje aktivovaná data!")
                .setPositiveButton("Ano", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(getApplicationContext(), "Načítání kompletních listingů je aktivní!", Toast.LENGTH_SHORT).show();

                        SharedPreferences preferenceSettings = getSharedPreferences("listingOptions",0);
                        SharedPreferences.Editor preferenceEditor;
                        preferenceEditor = preferenceSettings.edit();
                        preferenceEditor.putBoolean("isLoadingListingsEnabled", true);
                        preferenceEditor.apply();
                    }
                })
                .setNegativeButton("Ne", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(getApplicationContext(), "Načítání kompletních listingů je vypnuto!", Toast.LENGTH_SHORT).show();
                        SharedPreferences preferenceSettings = getSharedPreferences("listingOptions",0);
                        SharedPreferences.Editor preferenceEditor;
                        preferenceEditor = preferenceSettings.edit();
                        preferenceEditor.putBoolean("isLoadingListingsEnabled", false);
                        preferenceEditor.apply();
                    }
                })
                .show();
    }

    private void showCache(int id) {
        Intent intent = new Intent(this, DetailCacheActivity.class);
        intent.putExtra("id", id);
        startActivityForResult(intent, 0);
        //finish();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        listPosition = mainFragment.getListView().getFirstVisiblePosition();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        mainFragment.showCachesByFilter(filterType, sortingType);
        mainFragment.getListView().setSelection(listPosition);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //mainFragment.showCachesByFilter(filterType, sortingType);
        mainFragment.showCachesByFilter(filterType, sortingType);
        mainFragment.getListView().setSelection(listPosition);
    }
}
