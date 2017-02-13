package cz.uhk.fim.soucera.geocatcher.caches.Fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cz.uhk.fim.soucera.geocatcher.Cache;
import cz.uhk.fim.soucera.geocatcher.Caches_DB;
import cz.uhk.fim.soucera.geocatcher.MapsActivity;
import cz.uhk.fim.soucera.geocatcher.caches.EditCacheActivity;
import cz.uhk.fim.soucera.geocatcher.R;
import cz.uhk.fim.soucera.geocatcher.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentMainActivityCaches extends ListFragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = FragmentMainActivityCaches.class.getName();
    private long longId;
    private ArrayList<Cache> caches;
    private MenuItem menuItem_;

    OnCacheClickedListener listener;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        SharedPreferences preferenceSettings = getActivity().getSharedPreferences("sortingOptions",0);
        int sortingType = preferenceSettings.getInt("sortingType",0);
        SharedPreferences preferenceSettingsFilter = getActivity().getSharedPreferences("filteringOptions",0);
        int filterType = preferenceSettingsFilter.getInt("filterType",0);
        //System.out.println("SortingType_FRAGMENT_CREATE: "+sortingType);
        showCachesByFilter(filterType,sortingType);
        if(sortingType==4){
            sortByDistance();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public interface OnCacheClickedListener {
        void onCacheClicked(int id);
    }

    public FragmentMainActivityCaches() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnCacheClickedListener) context;
            buildGoogleApiClient();

            if(mGoogleApiClient!= null){
                mGoogleApiClient.connect();
            }

        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(getListView());

        //listView = getListView();
        //updateList();

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.popup_menu_caches, menu);
        menuItem_ = menu.findItem(R.id.action_status_find);
        Caches_DB caches = new Caches_DB(getActivity());
        try {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            int index = info.position;
            int cache_id = this.caches.get(index).getId();
            Cache cache = caches.getCache(cache_id);
            if(cache.getId_list()==1){
                menuItem_.setTitle("Zrušit nalezení!");
                Log.i(TAG, "Found true");

            } else{
                menuItem_.setTitle("Nalezeno!");
                Log.i(TAG, "Found NOT");
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            caches.close();
        }

    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        int cache_id = caches.get(index).getId();

        switch (item.getItemId()) {
            case R.id.action_edit_cache_detail:
                editCache(cache_id);
                return true;
            case R.id.action_del_cache_detail:
                deleteCache(cache_id);
                return true;
            case R.id.action_navigate_cache_detail:
                navigateCache(cache_id);
                return true;
            case R.id.action_status_find:
                isFound(cache_id);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void isFound(int id){
        Caches_DB caches = new Caches_DB(getActivity());
        try {
            Cache cache = caches.getCache(id);
            if(caches.isCacheFound(cache)){
                menuItem_.setTitle("Zrušit nalezení!");
                Log.i(TAG, "Found true");

            } else{
                menuItem_.setTitle("Nalezeno!");
                Log.i(TAG, "Found NOT");
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            caches.close();
            updateList();
        }
    }

    private void navigateCache(int cache_id) {
        Intent i = new Intent(getActivity(), MapsActivity.class);
        i.putExtra("id",cache_id);
        startActivity(i);
        getActivity().finish();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_cache, container, false);
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id){
        Log.i(TAG,"onListItemCLick");
        Cache cache = (Cache)list.getItemAtPosition(position);
        listener.onCacheClicked(cache.getId());
    }

    public void updateList(){
        Log.i(TAG, "updateList");
        try {
            Context ctx = getContext();
            Caches_DB caches_db = new Caches_DB(ctx);
            caches = caches_db.getCaches();
            caches_db.close();

            CachesAdapter adapter = new CachesAdapter(getActivity(), R.layout.list_item_cache, caches);
            setListAdapter(adapter);
            adapter.notifyDataSetChanged();
        } catch (Exception e){
            Log.e(TAG, "Problem with loading caches!");
            e.printStackTrace();
        }
    }

    public void getFoundList(int typeFilter, int sortingType, String findString){
        Log.i(TAG, "getFoundList");
        try {
            Context ctx = getContext();
            Caches_DB caches_db = new Caches_DB(ctx);
            caches = caches_db.getCaches(typeFilter, sortingType, findString);
            caches_db.close();

            if(mLastLocation!=null){
                System.out.println("mLastLocation NOT NULL");
                LatLng myLoc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                for(int i = 0; i<caches.size();i++){
                    LatLng cacheLoc = new LatLng(caches.get(i).getLat(),caches.get(i).getLon());
                    caches.get(i).setDistance_to_me(Utils.CalculationByDistance(myLoc, cacheLoc));
                }
            } else {
                System.out.println("mLastLocation NULL");
            }
            CachesAdapter adapter = new CachesAdapter(getActivity(), R.layout.list_item_cache, caches);
            setListAdapter(adapter);
            adapter.notifyDataSetChanged();
        } catch (Exception e){
            Log.e(TAG, "Problem with loading caches!");
            e.printStackTrace();
        }
    }

    private void deleteCache(long id){
        this.longId = id;
        new AlertDialog.Builder(getActivity(), R.style.YourAlertDialogTheme)
                .setTitle("Smazat?")
                .setMessage("Opravdu si přejete smazat danou cache?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        Caches_DB caches = new Caches_DB(getContext());
                        try {
                            if(caches.deleteCache(longId)){
                                Log.i(TAG, "Cache deleted!");
                                Toast.makeText(getContext(), R.string.cache_deleted, Toast.LENGTH_SHORT).show();
                                updateList();
                            } else{
                                Log.e(TAG, "Cache not deleted!");
                                Toast.makeText(getContext(), R.string.cache_not_deleted, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e){
                            Log.e(TAG, "CacheExceptionDeleted");
                            e.printStackTrace();
                        } finally {
                            caches.close();
                        }
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void editCache(int id){
            Log.i(TAG, "EditAcitivity id: "+id);
            Intent i = new Intent(getContext(), EditCacheActivity.class);
            i.putExtra("id", id);
            startActivityForResult(i, 0);
    }

    public void showCachesByFilter(int typeFilter, int sortingType){
        Log.i(TAG, "ShowCacheByFilter by type filter: " + typeFilter);
        try {
            Context ctx = getContext();
            Caches_DB caches_db = new Caches_DB(ctx);
            caches = caches_db.getCaches(typeFilter, sortingType);
            caches_db.close();

            if(mLastLocation!=null){
                System.out.println("mLastLocation NOT NULL");
                LatLng myLoc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                for(int i = 0; i<caches.size();i++){
                    LatLng cacheLoc = new LatLng(caches.get(i).getLat(),caches.get(i).getLon());
                    caches.get(i).setDistance_to_me(Utils.CalculationByDistance(myLoc, cacheLoc));
                }
            } else {
                System.out.println("mLastLocation NULL");
            }
            CachesAdapter adapter = new CachesAdapter(getActivity(), R.layout.list_item_cache, caches);
            setListAdapter(adapter);
            adapter.notifyDataSetChanged();
        } catch (Exception e){
            Log.e(TAG, "Problem with loading caches!");
            e.printStackTrace();
        }
    }

    public void sortByDistance(){
        if(caches!=null){
            Collections.sort(caches, new Comparator<Cache>() {
                @Override
                public int compare(Cache o1, Cache o2) {
                    return Double.compare(o1.getDistance_to_me(), o2.getDistance_to_me());
                }
            });
            CachesAdapter adapter = new CachesAdapter(getActivity(), R.layout.list_item_cache, caches);
            setListAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }
}
