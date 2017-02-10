package hansha.android.neuroskytest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter for ListView that allows for titles and data values underneath.
 */
public class DynamicDataAdapter extends BaseAdapter{

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<String> titles;
    private ArrayList<String> data;

    public DynamicDataAdapter(Context context, ArrayList<String> titles, ArrayList<String> data) {
        this.context = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.titles = titles;
        this.data = data;
    }

    @Override
    public int getCount() {
        return titles.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowView = inflater.inflate(R.layout.data_list_view, viewGroup, false);
        TextView titleTextView =
                (TextView)rowView.findViewById(R.id.Title);
        TextView dataTextView =
                (TextView)rowView.findViewById(R.id.Data);
        titleTextView.setText(titles.get(i));
        dataTextView.setText(data.get(i));
        return rowView;
    }
}
