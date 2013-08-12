package org.raegdan.bbstalker;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class SocialShare extends Activity {
	static final int SN_TWITTER = 0;
	static final int SN_VK = 1;
	static final int SN_GPLUS = 2;
	
	// Array order must match constants above!
	private String[] PackageNames = {
			"com.twitter.android",
			"com.vkontakte.android",
			"com.google.android.apps.plus",
	};
	
	private Context context;
	
	SocialShare (Context c)
	{
		context = c;
	}
	
	Boolean Share(String text, int SocialNetwork)
	{
		Intent ss = new Intent(Intent.ACTION_SEND);
		ss.putExtra(Intent.EXTRA_TEXT, text);
		ss.setType("text/plain");
		
		List<ResolveInfo> ril = context.getPackageManager().queryIntentActivities(ss, PackageManager.MATCH_DEFAULT_ONLY);
	
		boolean SNAppFound = false;
		ResolveInfo ri;
		for (int i = 0; i < ril.size(); i++)
		{
			ri = ril.get(i);
		    if(ri.activityInfo.packageName.startsWith(PackageNames[SocialNetwork])){
		        ss.setClassName(ri.activityInfo.packageName, ri.activityInfo.name);
		        SNAppFound = true;
		        break;
		    }				
		}
		
		if (SNAppFound)
		{
		    context.startActivity(ss);
		}
		
	    return SNAppFound;
	}
}
