package cz.uhk.fim.soucera.geocatcher.logs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cz.uhk.fim.soucera.geocatcher.R;

public class LogsAdapter extends ArrayAdapter<Log_Cache> {


    public LogsAdapter(Context context, int resource, List<Log_Cache> objects) {
        super(context, resource, objects);
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup group){

        if (convertView == null) {
            LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflator.inflate(R.layout.list_item_log, null);
        }

        Log_Cache log_cache = getItem(position);


        TextView date = (TextView) convertView.findViewById(R.id.tv_log_date);
        TextView type = (TextView) convertView.findViewById(R.id.tv_log_type);
        ImageView type_image = (ImageView) convertView.findViewById(R.id.imageView_log_type);
        TextView finder = (TextView) convertView.findViewById(R.id.tv_log_finder);
        TextView text = (TextView) convertView.findViewById(R.id.tv_log_text);


        if(log_cache != null){
            date.setText(log_cache.getDate().subSequence(0,10)); //TODO mozna blbe index
            type.setText(log_cache.getType());
            finder.setText(log_cache.getFinder());
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                text.setText(Html.fromHtml(log_cache.getText(),Html.FROM_HTML_MODE_LEGACY));
            } else {
                text.setText(Html.fromHtml(log_cache.getText()));
            }

            /*
            DecimalFormat formatter = new DecimalFormat("#0.000000");
            String s = formatter.format(log_cache.getLat()).replace(",", ".");
            s = !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
            String t = formatter.format(log_cache.getLon()).replace(",", ".");
            t = !t.contains(".") ? t : t.replaceAll("0*$", "").replaceAll("\\.$", "");
            location.setText(("Lat: " + s + " Long:" + t));
            */

            if(log_cache.getType().contains("Found it")){
                type_image.setBackgroundResource(R.drawable.ico_log_found);
            } else if (log_cache.getType().contains("Didn't fint id")){
                type_image.setBackgroundResource(R.drawable.ico_log_not_found);
            } else if (log_cache.getType().contains("Enable")){
                type_image.setBackgroundResource(R.drawable.ico_enable_cache);
            } else if (log_cache.getType().contains("Disable")){
                type_image.setBackgroundResource(R.drawable.ico_disable_cache);
            } else {
                //write note
                type_image.setBackgroundResource(R.drawable.ico_log_write_note);
            }

        }
        return convertView;
    }
}
