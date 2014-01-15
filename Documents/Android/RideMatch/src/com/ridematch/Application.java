package com.ridematch;

import com.parse.Parse;
import com.parse.ParseInstallation;

public class Application extends android.app.Application {
  // Debugging switch
  public static final boolean APPDEBUG = false;
  
  // Debugging tag for the application
  public static final String APPTAG = "RideMatch";

  public Application() {
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Parse.initialize(this, "rnbaczRVq3ctAKOZURj4JkEL3n2C4mSALH1iu1bU", "UXSgBnFxy0Gadr0kczlJXj7xv9DqnLLLjmGupc8O");
    ParseInstallation.getCurrentInstallation().saveInBackground();
  }
}
