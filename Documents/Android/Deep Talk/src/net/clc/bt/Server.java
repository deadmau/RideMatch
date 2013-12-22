
package net.clc.bt;

import net.clc.bt.Connection.OnConnectionLostListener;
import net.clc.bt.Connection.OnConnectionServiceReadyListener;
import net.clc.bt.Connection.OnIncomingConnectionListener;
import net.clc.bt.Connection.OnMaxConnectionsReachedListener;
import net.clc.bt.Connection.OnMessageReceivedListener;



import android.view.ViewGroup.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.WindowManager.BadTokenException;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Server extends Activity implements Callback {

   // public static final String TAG = "AirHockey";
	private ListActivity mClass;
	
	   //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> listItems=new ArrayList<String>();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter<String> adapter,adapter2;

    //RECORDING HOW MANY TIMES THE BUTTON HAS BEEN CLICKED
    int clickCounter=0;
    
    String devicename;
    
    View view;
    
    Server keep;

    private static final int SERVER_LIST_RESULT_CODE = 42;

    public static final int UP = 3;

    public static final int DOWN = 4;

    public static final int FLIPTOP = 5;

    private Server self;

    private int mType; // 0 = server, 1 = client

    private SurfaceView mSurface;

    private SurfaceHolder mHolder;

    private Paint bgPaint;

    private Paint goalPaint;


    //private PhysicsLoop pLoop;

    private ArrayList<Point> mPaddlePoints;

    private ArrayList<Long> mPaddleTimes;

    private int mPaddlePointWindowSize = 5;

   // private int mPaddleRadius = 55;

    private Bitmap mPaddleBmp;


   // private int mBallRadius = 40;

    private Connection mConnection;

    private String rivalDevice = "";

    //private SoundPool mSoundPool;

   // private int tockSound = 0;

    private MediaPlayer mPlayer;

    private int hostScore = 0;

    private int clientScore = 0;

    private OnMessageReceivedListener dataReceivedListener = new OnMessageReceivedListener() {
        public void OnMessageReceived(String device, String message) {
            if (message.indexOf("SCORE") == 0) {
                String[] scoreMessageSplit = message.split(":");
                //hostScore = Integer.parseInt(scoreMessageSplit[1]);
                //clientScore = Integer.parseInt(scoreMessageSplit[2]);
                //showScore();
            } else {
               // mBall.restoreState(message);
            }
        }
    };

    private OnMaxConnectionsReachedListener maxConnectionsListener = new OnMaxConnectionsReachedListener() {
        public void OnMaxConnectionsReached() {

        }
    };

    private OnIncomingConnectionListener connectedListener = new OnIncomingConnectionListener() {
        public void OnIncomingConnection(String device) {
            rivalDevice = device;
            WindowManager w = getWindowManager();
            Display d = w.getDefaultDisplay();

        }
    };

    private OnConnectionLostListener disconnectedListener = new OnConnectionLostListener() {
        public void OnConnectionLost(String device) {
            class displayConnectionLostAlert implements Runnable {
                public void run() {
                    Builder connectionLostAlert = new Builder(self);

                    connectionLostAlert.setTitle("Connection lost");
                    connectionLostAlert
                            .setMessage("Your connection with the other player has been lost.");

                    connectionLostAlert.setPositiveButton("Ok", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    connectionLostAlert.setCancelable(false);
                    try {
                    self.exitList();
                    connectionLostAlert.show();
                    } catch (BadTokenException e){
                        // Something really bad happened here; 
                        // seems like the Activity itself went away before
                        // the runnable finished.
                        // Bail out gracefully here and do nothing.
                    }
                }
            }
            self.runOnUiThread(new displayConnectionLostAlert());
        }
    };

    private OnConnectionServiceReadyListener serviceReadyListener = new OnConnectionServiceReadyListener() {
        public void OnConnectionServiceReady() {
            if (mType == 0) {
                mConnection.startServer(1, connectedListener, maxConnectionsListener,
                        dataReceivedListener, disconnectedListener);
                self.setTitle("방장 : " + mConnection.getName() + "-" + mConnection.getAddress());
   
                
            } else {
            	
                Intent serverListIntent = new Intent(self, ServerListActivity.class);
                startActivityForResult(serverListIntent, SERVER_LIST_RESULT_CODE);
                

            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        self = this;
        ListView list = new ListView(this);
        adapter = new ArrayAdapter<String>(this,
                R.layout.listtextcolor, listItems);
        list.setAdapter(adapter);
  

        TextView tv1 = new TextView(this);
        tv1.setText("방장님이 입장하셨습니다. ");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        params.setMargins(230,80,0,0); // (left, top, right, bottom);
        tv1.setLayoutParams(params);

        LinearLayout ll = new LinearLayout(this);
        ll.setBackgroundResource(R.drawable.serverwait);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(tv1); 
        ll.addView(list);
        setContentView(ll);
        
        Intent startingIntent = getIntent();
        mType = startingIntent.getIntExtra("TYPE", 0);
        
   
        mConnection = new Connection(this, serviceReadyListener);
        //self.addtoList();


    }

    public void exitList() {
        listItems.add( devicename+ "님이 나가셨습니다. ");
        adapter.notifyDataSetChanged();
        Intent lost = new Intent(Server.this, Lost.class);
        startActivity(lost);
		
	}

	@Override
    protected void onDestroy() {
        if (mConnection != null) {
            mConnection.shutdown();
        }
        if (mPlayer != null) {
            mPlayer.release();
        }
        super.onDestroy();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        //pLoop = new PhysicsLoop();
        //pLoop.start();
    }

   /* private void draw() {
        Canvas canvas = null;
        try {
            canvas = mHolder.lockCanvas();
            if (canvas != null) {
                doDraw(canvas);
            }
        } finally {
            if (canvas != null) {
                mHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void doDraw(Canvas c) {
        c.drawRect(0, 0, c.getWidth(), c.getHeight(), bgPaint);
        c.drawRect(0, c.getHeight() - (int) (c.getHeight() * 0.02), c.getWidth(), c.getHeight(),
                goalPaint);

        if (mPaddleTimes.size() > 0) {
            Point p = mPaddlePoints.get(mPaddlePoints.size() - 1);

   
            if (p != null) {
                c.drawBitmap(mPaddleBmp, p.x - 60, p.y - 200, new Paint());
            }}
        }*/


    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
           // pLoop.safeStop();
        } finally {
            //pLoop = null;
        }
    }

    private class PhysicsLoop extends Thread {
        private volatile boolean running = true;
    
        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(5);
                    }

                 catch (InterruptedException ie) {
                    running = false;
                }}
            
        }

        public void safeStop() {
            running = false;
           // interrupt();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((resultCode == Activity.RESULT_OK) && (requestCode == SERVER_LIST_RESULT_CODE)) {
            String device = data.getStringExtra(ServerListActivity.EXTRA_SELECTED_ADDRESS);

            int connectionStatus = mConnection.connect(device, dataReceivedListener,
                    disconnectedListener);
            if (connectionStatus != Connection.SUCCESS) {
                Toast.makeText(self, "Unable to connect; please try again.", 1).show();
                
            } else {
                rivalDevice = device;  
                devicename = mConnection.getName();
                self.addtoList();
                //keep.addtoLists();
				//clickCounter++;
            }
            return;
        }
    }


    private void addtoList() {
    	/*View curr = getWindow().getDecorView().findViewById(android.R.id.content);
        TextView tv1 = new TextView(this);
        tv1.setText("님이 입장하셨습니다. ");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        params.setMargins(10,10,10,10); // (left, top, right, bottom);
        tv1.setLayoutParams(params);
        ((ViewGroup) curr).addView(tv1);	*/
        listItems.add( devicename+ "님이 입장하셨습니다. ");
        adapter.notifyDataSetChanged();

		
	}



	@Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Point p = new Point((int) event.getX(), (int) event.getY());
            mPaddlePoints.add(p);
            mPaddleTimes.add(System.currentTimeMillis());
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            Point p = new Point((int) event.getX(), (int) event.getY());
            mPaddlePoints.add(p);
            mPaddleTimes.add(System.currentTimeMillis());
            if (mPaddleTimes.size() > mPaddlePointWindowSize) {
                mPaddleTimes.remove(0);
                mPaddlePoints.remove(0);
            }
        } else {
            mPaddleTimes = new ArrayList<Long>();
            mPaddlePoints = new ArrayList<Point>();
        }
        return false;
    }


    public Point getPaddleCenter() {
        if (mPaddleTimes.size() > 0) {
            Point p = mPaddlePoints.get(mPaddlePoints.size() - 1);
            int x = p.x + 10;
            int y = p.y - 130;
            return new Point(x, y);
        } else {
            return new Point(-1, -1);
        }
    }

    private void showScore() {
        class showScoreRunnable implements Runnable {
            public void run() {
                String scoreString = "";
                if (mType == 0) {
                    scoreString = hostScore + " - " + clientScore;
                } else {
                    scoreString = clientScore + " - " + hostScore;
                }
                Toast.makeText(self, scoreString, 0).show();
            }
        }
        self.runOnUiThread(new showScoreRunnable());
    }

    private void handleCollision() {
       
        
    }

}
