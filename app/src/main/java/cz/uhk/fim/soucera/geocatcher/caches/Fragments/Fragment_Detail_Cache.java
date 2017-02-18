package cz.uhk.fim.soucera.geocatcher.caches.Fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import cz.uhk.fim.soucera.geocatcher.Cache;
import cz.uhk.fim.soucera.geocatcher.Caches_DB;
import cz.uhk.fim.soucera.geocatcher.logs.Log_Cache;
import cz.uhk.fim.soucera.geocatcher.R;
import cz.uhk.fim.soucera.geocatcher.logs.LogsAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_Detail_Cache extends Fragment {

    private final String TAG = Fragment_Detail_Cache.class.getName();
    private int id;
    private TextView textName;
    private TextView textType;
    private TextView textCode;
    private ImageView imageType;
    private TextView textSize;
    private TextView textLocations;
    private TextView textLocationsMinutes;
    private TextView textHelp;
    private TextView textURL;
    private WebView webViewDesc;
    private RatingBar ratingDiff;
    private RatingBar ratingTerrain;
    private ListView listLogs;
    private TextView noLogs;
    private ScrollView scrollView;

    public Fragment_Detail_Cache() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            id = getArguments().getInt("id",-1);
            Log.i(TAG, "DetailCache id is: "+id);
        } else {
            Log.e(TAG, "DetailCache: NO ID!");
            id = -1;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_detail_cache, container, false);

        textName = (TextView) layout.findViewById(R.id.textView_name_cache_detail);
        textType = (TextView) layout.findViewById(R.id.textView_type_cache_detail);
        textCode = (TextView) layout.findViewById(R.id.textView_code_cache_detail);
        imageType = (ImageView) layout.findViewById(R.id.imageView_type_cache_detail);
        textSize = (TextView) layout.findViewById(R.id.textView_size_cache_detail);
        textHelp = (TextView) layout.findViewById(R.id.textView_help_cache_detail);
        textURL = (TextView) layout.findViewById(R.id.textView_url_detail);
        webViewDesc = (WebView) layout.findViewById(R.id.textView_desc_cache_detail);
        webViewDesc.setWebViewClient(new WebViewClient());
        webViewDesc.setBackgroundColor(Color.TRANSPARENT);
        //WebSettings webSettings=webViewDesc.getSettings();
        //webViewDesc.getSettings().setJavaScriptEnabled(true);
        //webViewDesc.getSettings().setDomStorageEnabled(true);
        //webSettings.setLoadsImagesAutomatically(false);
        //webSettings.setAllowFileAccessFromFileURLs(false);
       // webSettings.setAllowUniversalAccessFromFileURLs(false);
        textLocations = (TextView) layout.findViewById(R.id.textView_location_detail);
        textLocationsMinutes = (TextView) layout.findViewById(R.id.textView_location_detail_minutes);
        ratingDiff = (RatingBar) layout.findViewById(R.id.ratingBar_diff);
        ratingTerrain = (RatingBar) layout.findViewById(R.id.ratingBar_terrain);

        listLogs = (ListView) layout.findViewById(R.id.listView_logs);
        noLogs = (TextView) layout.findViewById(R.id.tv_detail_no_logs);
        scrollView = (ScrollView) layout.findViewById(R.id.scrollView2);
        //Drawable drawable = ratingDiff.getProgressDrawable();
        //drawable.setColorFilter(Color.parseColor("#ff90b6"), PorterDuff.Mode.SRC_ATOP);
        //Drawable drawable2 = ratingTerrain.getProgressDrawable();
        //drawable2.setColorFilter(Color.parseColor("#ff90b6"), PorterDuff.Mode.SRC_ATOP);
        //Drawable progress = ratingDiff.getProgressDrawable();
        //DrawableCompat.setTint(progress, Color.parseColor("#ff90b6"));


        if(id>(-1)){
            setId(id);
        }

        return layout;
    }

    public void setId(int id) {
        Log.i(TAG, "DetailCache setID: "+id);
        this.id = id;
        Context ctx = getContext();
        Caches_DB caches_db = new Caches_DB(ctx);
        Cache cache = caches_db.getCache(id);
        caches_db.close();

        String typeCache = cache.getType();
        String sizeCache = cache.getSize();
        String helpCache = cache.getHelp();
        String codeCache = cache.getCode();
        String urlCache = cache.getUrl();
        /*
        if(typeCache.equals("")) typeCache = "Neuvedeno";
        if(sizeCache.equals("")) sizeCache = "Neuvedeno";
        if(helpCache.equals("")) helpCache = "Neuvedeno";
        if(codeCache.equals("")) codeCache = "Neuvedeno";
        if(urlCache.equals("")) urlCache = "Neuvedeno";
        */

        textName.setText(cache.getName());
        textType.setText(typeCache);
        textCode.setText(codeCache);
        textSize.setText(sizeCache);
        textHelp.setText(helpCache);
        textURL.setText(urlCache);
        String desc = cache.getDesc();
        if(desc==null){
            desc = "";
        }
        String text =      "<html><head><meta charset=\"UTF-8\">"
                         + "<style type=\"text/css\">body{color: #fff;} "
                         + "img{display: inline; height: auto; max-width: 100%;} "
                         + "</style>"
                         + "</head> <body>"
                         + "<p align=\"justify\" color=\"white\">"
                         + desc
                         + "</p> "
                         + "</body></html>";
        WebSettings settings = webViewDesc.getSettings();
        settings.setDefaultTextEncodingName("utf-8");

        SharedPreferences preferenceSettings = getActivity().getSharedPreferences("listingOptions",0);
        boolean isLoadingListingsEnabled = preferenceSettings.getBoolean("isLoadingListingsEnabled", false);
        Log.i(TAG, "IsLoadingListingsEnabled:" + isLoadingListingsEnabled);
        if(isLoadingListingsEnabled){
            settings.setLoadsImagesAutomatically(true);
            settings.setBlockNetworkImage(false);
            settings.setBlockNetworkLoads (false);
        } else {
            settings.setLoadsImagesAutomatically(false);
            settings.setBlockNetworkImage(true);
            settings.setBlockNetworkLoads (true);
        }


        webViewDesc.loadData(text , "text/html; charset=utf-8", "utf-8"); //TODO hazi error, nepada ... BUG= 662040 from chromium / dummy binder

        if(cache.getType().contains("Traditional")){
            imageType.setBackgroundResource(R.drawable.traditional_cache);
        } else if (cache.getType().contains("Multi")){
            imageType.setBackgroundResource(R.drawable.multi_cache);
        } else if (cache.getType().contains("Mystery") || cache.getType().contains("Unknown")){
            imageType.setBackgroundResource(R.drawable.mystery_cache);
        }

        try {
            DecimalFormat formatter = new DecimalFormat("#0.000000");
            textLocations.setText("N " + formatter.format(cache.getLat()).replace(",", ".") + " E " + formatter.format(cache.getLon()).replace(",", "."));
            String latMin = (Location.convert(cache.getLat(), Location.FORMAT_MINUTES).replace(":", "°").replace(",","."));
            String longMin = Location.convert(cache.getLon(), Location.FORMAT_MINUTES).replace(":","°").replace(",", ".");
            String[] latMinParts = latMin.split("\\.");
            String[] lonMinParts = longMin.split("\\.");
            System.out.println(latMinParts.length);
            System.out.println(lonMinParts.length);
            System.out.println(latMin);
            System.out.println(longMin);
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

        ratingDiff.setRating((float) cache.getDifficulty());
        String s = String.valueOf(cache.getTerrain());
        ratingTerrain.setMax(5);
        ratingTerrain.setStepSize(0.5f);
        ratingTerrain.setRating(Float.parseFloat(s));
        setLogs(cache.getId());
    }

    private void setLogs(int cache_id){
        //TODO test LOGS in cache
        try {
            Context ctx = getContext();
            Caches_DB caches_db = new Caches_DB(ctx);
            ArrayList<Log_Cache> logs = caches_db.getLogs(cache_id);
            caches_db.close();

            if(logs != null){
                if(logs.size()>0){
                    noLogs.setVisibility(View.INVISIBLE);
                    noLogs.setHeight(0);
                    LogsAdapter adapter = new LogsAdapter(getActivity(), R.layout.list_item_log, logs);
                    //listLogs.invalidateViews();


                    listLogs.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    setListViewHeightBasedOnChildren4(listLogs);
                    //getListViewSize(listLogs);
                }
            } else {
                listLogs.setVisibility(View.INVISIBLE);
                noLogs.setVisibility(View.VISIBLE);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void setListViewHeightBasedOnChildren4(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null || listAdapter.getCount() < 2) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem instanceof ViewGroup) listItem.setLayoutParams(lp);
            listItem.measure(widthMeasureSpec, heightMeasureSpec);
            totalHeight += listItem.getMeasuredHeight();
            int padding = listView.getPaddingTop()+listView.getPaddingBottom();
            totalHeight += 200;
        }

        //totalHeight += listView.getPaddingTop() + listView.getPaddingBottom();
        //totalHeight += (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

}
