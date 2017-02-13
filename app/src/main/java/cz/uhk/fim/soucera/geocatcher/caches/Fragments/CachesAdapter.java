package cz.uhk.fim.soucera.geocatcher.caches.Fragments;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import cz.uhk.fim.soucera.geocatcher.R;
import cz.uhk.fim.soucera.geocatcher.Cache;

class CachesAdapter extends ArrayAdapter<Cache> {


    CachesAdapter(Context context, int resource, List<Cache> objects) {
        super(context, resource, objects);
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup group){

        if (convertView == null) {
            LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflator.inflate(R.layout.list_item_cache, null);
        }

        Cache cache = getItem(position);

        TextView nazev = (TextView) convertView.findViewById(R.id.textView_item_name);
        ImageView image = (ImageView) convertView.findViewById(R.id.imageView_type_cache);
        TextView location = (TextView) convertView.findViewById(R.id.textView_location_cache);
        ImageView imageStatus = (ImageView) convertView.findViewById(R.id.status_found);

        if(cache != null){
            nazev.setText(cache.getName());
            /*
            DecimalFormat formatter = new DecimalFormat("#0.000000");
            String s = formatter.format(cache.getLat()).replace(",", ".");
            s = !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
            String t = formatter.format(cache.getLon()).replace(",", ".");
            t = !t.contains(".") ? t : t.replaceAll("0*$", "").replaceAll("\\.$", "");
            location.setText(("Lat: " + s + " Long:" + t));
            */
            String latMin = (Location.convert(cache.getLat(), Location.FORMAT_MINUTES).replace(":", "°").replace(",","."));
            String longMin = Location.convert(cache.getLon(), Location.FORMAT_MINUTES).replace(":","°").replace(",", ".");
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
            location.setText("N: " + latMin + "    E: " + longMin + " | Distance: " + cache.getDistance_to_me() + " km");
            if(cache.getType().contains("Traditional")){
                image.setBackgroundResource(R.drawable.traditional_cache);
            } else if (cache.getType().contains("Multi")){
                image.setBackgroundResource(R.drawable.multi_cache);
            } else if (cache.getType().contains("Unknown") || cache.getType().contains("Mystery")){
                image.setBackgroundResource(R.drawable.mystery_cache);
            } else if (cache.getType().contains("Earthcache")){
                image.setBackgroundResource(R.drawable.earthcache);
            }
            if(cache.getId_list()==0){
                imageStatus.setBackgroundResource(R.drawable.red_status);
            } else {
                imageStatus.setBackgroundResource(R.drawable.green_status);
            }
        }
        return convertView;
    }
}
