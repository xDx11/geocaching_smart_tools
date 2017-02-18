package cz.uhk.fim.soucera.geocatcher.caches.Fragments;


import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.uhk.fim.soucera.geocatcher.Cache;
import cz.uhk.fim.soucera.geocatcher.Caches_DB;
import cz.uhk.fim.soucera.geocatcher.caches.MainCachesActivity;
import cz.uhk.fim.soucera.geocatcher.R;
import cz.uhk.fim.soucera.geocatcher.utils.Utils;
import cz.uhk.fim.soucera.geocatcher.utils.SignedDecimalKeyListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_Add_Cache extends Fragment {

    private final String TAG = Fragment_Add_Cache.class.getName();
    private View layout;
    private EditText nameEdit;
    private EditText codeEdit;
    private EditText helpEdit;
    private EditText latEdit;
    private EditText lonEdit;
    private EditText urlEdit;
    private EditText descEdit;
    private RadioButton rbTraditional;
    private RadioButton rbMulti;
    private RadioButton rbMystery;
    private Spinner spinnerTerr;
    private Spinner spinnerSize;
    private Spinner spinnerDiff;
    private RadioGroup rb_group_type;

    public Fragment_Add_Cache() {
        // Required empty public constructor
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreatet_setSpinners");
        try {
            spinnerTerr = (Spinner) layout.findViewById(R.id.spinner_cache_terrain);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.terrain_arrays, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTerr.setAdapter(adapter);
            spinnerTerr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View arg1,
                                           int position, long arg3) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ((TextView) spinnerTerr.getSelectedView()).setTextColor(getContext().getColor(R.color.colorWhite));
                    } else {
                        ((TextView) spinnerTerr.getSelectedView()).setTextColor(getResources().getColor(R.color.colorWhite));
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinnerDiff = (Spinner) layout.findViewById(R.id.spinner_cache_diff);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDiff.setAdapter(adapter);
            spinnerDiff.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View arg1,
                                           int position, long arg3) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ((TextView) spinnerDiff.getSelectedView()).setTextColor(getContext().getColor(R.color.colorWhite));
                    } else {
                        ((TextView) spinnerDiff.getSelectedView()).setTextColor(getResources().getColor(R.color.colorWhite));
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            spinnerSize = (Spinner) layout.findViewById(R.id.spinner_cache_size);
            ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(), R.array.size_arrays, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSize.setAdapter(adapter2);
            spinnerSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View arg1,
                                           int position, long arg3) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ((TextView) spinnerSize.getSelectedView()).setTextColor(getContext().getColor(R.color.colorWhite));
                    } else {
                        ((TextView) spinnerSize.getSelectedView()).setTextColor(getResources().getColor(R.color.colorWhite));
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        } catch (Exception e) {
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }


        try {
            descEdit.setOnEditorActionListener(new DoneOnEditorActionListener());
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_LONG).show();
        }

        latEdit.setText("°");
        lonEdit.setText("°");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG,"onCreateView");

        layout = inflater.inflate(R.layout.fragment_add_cache, container, false);

        nameEdit = (EditText) layout.findViewById(R.id.editText_name_cache);
        codeEdit = (EditText) layout.findViewById(R.id.editText_code_cache);

        rbTraditional = (RadioButton) layout.findViewById(R.id.rb_trad);
        rbMulti = (RadioButton) layout.findViewById(R.id.rb_multi);
        rbMystery = (RadioButton) layout.findViewById(R.id.rb_mystery);

        latEdit = (EditText) layout.findViewById(R.id.editText_lat_cache);
        lonEdit = (EditText) layout.findViewById(R.id.editText_lon_cache);

        latEdit.setKeyListener(SignedDecimalKeyListener.getInstance());
        lonEdit.setKeyListener(SignedDecimalKeyListener.getInstance());

        helpEdit = (EditText) layout.findViewById(R.id.editText_help_cache);
        urlEdit = (EditText) layout.findViewById(R.id.editText_url_cache);
        descEdit = (EditText) layout.findViewById(R.id.editText_desc_cache);

        rb_group_type = (RadioGroup) layout.findViewById(R.id.rb_group_type);
        rb_group_type.check(rbTraditional.getId());

        return layout;
    }

    public boolean onAddCache(){
        Log.i(TAG,"onAddCache");
        if(TextUtils.isEmpty(nameEdit.getText().toString())) {
            nameEdit.setError("Prázdná hodnota není povolena!");
            return false;
        }

        if(TextUtils.isEmpty(lonEdit.getText().toString())) {
            lonEdit.setError("Prázdná hodnota není povolena!");
            return false;
        }

        if(TextUtils.isEmpty(latEdit.getText().toString())) {
            latEdit.setError("Prázdná hodnota není povolena!");
            return false;
        }

        //Pattern pMinutes = Pattern.compile("[0-9][0-9][°][0-9][0-9][.][0-9][0-9][0-9][0-9]*");
        //Pattern pMinutesLat = Pattern.compile("[0-9][0-9][°][0-9][0-9][.][0-9][0-9][0-9][0-9]*");
        Pattern pMinutesLatTest = Pattern.compile("^-?[0-9]{1,2}°[0-9]{1,2}\\.[0-9]{1,6}$");
        //Pattern pMinutesLon = Pattern.compile("[0-9][0-9][0-9][°][0-9][0-9][.][0-9][0-9][0-9][0-9]*");
        Pattern pMinutesLonTest = Pattern.compile("^-?[0-9]{1,3}°[0-9]{1,2}\\.[0-9]{1,6}$");
        Matcher mLat = pMinutesLatTest.matcher(latEdit.getText().toString());
        Matcher mLon = pMinutesLonTest.matcher(lonEdit.getText().toString());
        boolean okLatMinutes = mLat.matches();
        boolean okLonMinutes = mLon.matches();
        Pattern pDegreesLat = Pattern.compile("^-?[0-9]{1,2}\\.[0-9]{4,7}$");
        Pattern pDegreesLon = Pattern.compile("^-?[0-9]{1,3}\\.[0-9]{4,7}$");
        mLat = pDegreesLat.matcher(latEdit.getText().toString());
        mLon = pDegreesLon.matcher(lonEdit.getText().toString());
        boolean okLatDegrees = mLat.matches();
        boolean okLonDegrees = mLon.matches();
        if(!okLatMinutes){
            if(!okLatDegrees){
                latEdit.setError("Zadejte souřadnici ve formatu xx°xx.xxx nebo xx.xxxx");
                return false;
            }
        }
        if(!okLonMinutes){
            if(!okLonDegrees){
                lonEdit.setError("Zadejte souřadnici ve formatu xxx°xx.xxx nebo xxx.xxxx");
                return false;
            }

        }
        String lat;
        String lon;

        if(okLatMinutes && okLonMinutes){
            String latM = latEdit.getText().toString().replace("°", ":");//.replace(".", ",");
            String longM = lonEdit.getText().toString().replace("°", ":");//.replace(".", ",");
            //System.out.println("replace lat: " + latM);
            //System.out.println("replace lon: " + longM);
            double coord1;
            double coord2;
            try {
                coord1 = Location.convert(latM);
                coord2 = Location.convert(longM);
            } catch (Exception e){
                latEdit.setError("Hodnota musi byt v intervalu <-90,90>");
                lonEdit.setError("Hodnota musi byt v intervalu <-180,180>");
                return false;
            }
            //System.out.println("coord1: " + latM);
            //System.out.println("coord2: " + longM);
            String latMin = Location.convert(coord1, Location.FORMAT_DEGREES);
            String longMin = Location.convert(coord2, Location.FORMAT_DEGREES);
            latMin = latMin.replace(",",".");
            longMin = longMin.replace(",",".");
            lat = latMin;
            lon = longMin;
            //System.out.println("N " + latMin + " E " + longMin);
        } else if(okLatDegrees && okLonDegrees) {
            lat = latEdit.getText().toString();
            lon = lonEdit.getText().toString();
            try {
                if(!Utils.isValidLatLng(Double.parseDouble(lat.trim()), Double.parseDouble(lon.trim()))){
                    latEdit.setError("Hodnota musi byt v intervalu <-90,90>");
                    lonEdit.setError("Hodnota musi byt v intervalu <-180,180>");
                    return false;
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        } else {
            latEdit.setError("Nesouhlasi formaty souradnic. Zadejte souřadnici ve formatu xx°xx.xxx nebo xx.xxxx");
            return false;
        }
        String typeCache = getTypeValue(rb_group_type.getCheckedRadioButtonId());
        //float diffCache = getDiffValue(rb_group_diff.getCheckedRadioButtonId());

        Cache cache = new Cache();
        cache.setName(nameEdit.getText().toString());
        cache.setCode(codeEdit.getText().toString());
        cache.setType(typeCache);
        cache.setSize(spinnerSize.getSelectedItem().toString());
        cache.setTerrain(Double.parseDouble(spinnerTerr.getSelectedItem().toString()));
        cache.setDifficulty(Double.parseDouble(spinnerDiff.getSelectedItem().toString()));
        cache.setLat(Float.parseFloat(lat.trim()));
        cache.setLon(Float.parseFloat(lon.trim()));
        cache.setDesc(descEdit.getText().toString());
        cache.setHelp(helpEdit.getText().toString());
        cache.setUrl(urlEdit.getText().toString());
        try {

            Caches_DB caches_db = new Caches_DB(getContext());
            long id = caches_db.insertCache(cache);
            caches_db.close();
            if (id > -1) {
                Log.i(TAG, "cacheAdded");
                Intent i = new Intent(getContext(), MainCachesActivity.class);
                //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(i, 0);
                getActivity().finish();
            }else{
                Toast.makeText(getContext(), R.string.cache_not_added, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e){
            e.printStackTrace();
            Log.e(TAG,"Saving problem with cache!");
            Toast.makeText(getContext(), R.string.cache_not_added, Toast.LENGTH_LONG).show();
        }

        return true;
    }

    private String getTypeValue(int id){
        if (id == rbTraditional.getId()){
            return rbTraditional.getText().toString();
        } else if (id == rbMulti.getId()){
            return rbMulti.getText().toString();
        } else if (id == rbMystery.getId()){
            return "Unknown Cache";
        } else {
            return "Unknown Cache";
        }
    }

    private class DoneOnEditorActionListener implements TextView.OnEditorActionListener {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                onAddCache();
                return true;
            }
            return false;
        }
    }

}
