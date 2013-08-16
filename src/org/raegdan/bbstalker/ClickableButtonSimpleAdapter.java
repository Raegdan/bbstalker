package org.raegdan.bbstalker;

/*/
 * Based on the code borrowed here: http://codeplay.hu/en/developer/2010/07/android-onitemclick-problema-listview-es-button-hasznalatakor/
 * /*/

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

public class ClickableButtonSimpleAdapter extends SimpleAdapter {	

	public static final String HASHMAP_ID = "_id";

	public ClickableButtonSimpleAdapter(Context context,
			List<? extends Map<String, ?>> data, 
			int resource, String[] from,
			int[] to) 
	{
		super(context, data, resource, from, to);
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = super.getView(position, convertView, parent);
		setViewTag(view, ((HashMap<String, Integer>) getItem(position)).get(HASHMAP_ID) );
		return view;
	}

	private void setViewTag(View view, Object tag)
	{
		view.setTag(tag);
		if (view instanceof ViewGroup) {
			for (int i=0; i < ((ViewGroup) view).getChildCount(); i++) {
				setViewTag(((ViewGroup) view).getChildAt(i), tag);
			}
		}
	}
}
