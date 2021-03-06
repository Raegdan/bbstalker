package org.raegdan.bbstalker;

import android.app.Application;
import android.content.Context;

public class BBStalkerApplication extends Application {

	// Global database variable and methods for it
	public BlindbagDB database;
	public Boolean DBLoaded;

	public BBStalkerApplication() {
		super();

		database = new BlindbagDB();
		DBLoaded = false;
	}

	public Boolean LoadDB(Context context) {
		return database.LoadDB(context);
	}

	public BlindbagDB GetDB(Context context) throws CloneNotSupportedException {
		return database.clone(context);
	}
}