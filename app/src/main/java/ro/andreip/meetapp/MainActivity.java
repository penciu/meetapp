package ro.andreip.meetapp;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.plus.Plus;

public class MainActivity extends ActionBarActivity
		implements OnMapReadyCallback,
		GoogleMap.OnMapLoadedCallback,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		LocationListener {

	// Global variables.
	boolean requestingLocationUpdates;
	private GoogleApiClient googleApiClient;
	private boolean signInClicked, intentInProgress;
	private ConnectionResult connectionResult;
	private GoogleMap map;
	/* Request code used to invoke sign in user interactions. */
	private static final int RC_SIGN_IN = 0;
	private static final String TAG = "INFO";
	final LatLngBounds ROMANIA = new LatLngBounds( new LatLng( 40, 20 ), new LatLng( 50, 30 ) );

	@Override
	protected void onCreate( Bundle savedInstanceState ){
		super.onCreate( savedInstanceState );
		setContentView( R.layout.main_activity );

		// Declarations.
		FragmentManager fragmentManager;
		MapFragment mapFragment;

		Log.i( TAG, "MainActivity.onCreate" );

		mapFragment = MapFragment.newInstance();

		fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().add(R.id.map, mapFragment).commit();
		mapFragment.getMapAsync(this);

		googleApiClient = new GoogleApiClient.Builder( this )
				.addApi(LocationServices.API)
				.addConnectionCallbacks( this )
				.addOnConnectionFailedListener( this )
				.build();
		googleApiClient.connect();

		// Setting global variables.
		requestingLocationUpdates = true;
	}
//
	@Override
	protected void onStart(){
		super.onStart();
		Log.i( TAG, "MainActivity.onStart" );
		googleApiClient.connect();
	}
//*/
	@Override
	protected void onStop(){
		super.onStop();
		Log.i( TAG, "MainActivity.onStop" );
		if( googleApiClient.isConnected() ){
			googleApiClient.disconnect();
		}
	}

	@Override
	protected void onRestart(){
		Log.i( TAG, "MainActivity.onRestart" );

	}

	@Override
	protected void onResume(){
		super.onResume();
		Log.i( TAG, "MainActivity.onResume" );
		// Logs 'install' and 'app activate' App Events.
		// AppEventsLogger.activateApp(this);
	}

	@Override
	protected void onPause(){
		super.onPause();
		Log.i( TAG, "MainActivity.onPause" );
		// Logs 'app deactivate' App Event.
		// AppEventsLogger.deactivateApp(this);
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		Log.i( TAG, "MainActivity.onDestroy" );
	}


	public void onConnectionSuspended(int cause) {
		googleApiClient.connect();
		Log.i( TAG, "MainActivity.onConnectionSuspended" );
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ){
		// Declarations.
		MenuInflater inflater;
		ActionBar actionBar;
		Log.i( TAG, "MainActivity.onCreateOptionsMenu" );

		inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		super.onCreateOptionsMenu(menu);

		actionBar = getSupportActionBar();
		// actionBar.setDisplayShowCustomEnabled(true);
		if( actionBar == null ){
			throw new AssertionError();
		}
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		return true;
	}

	public void onConnectionFailed( ConnectionResult result ){
		Log.i( TAG, "MainActivity.onConnectionFailed" );
		if( !intentInProgress && result.hasResolution() ){
			try {
				intentInProgress = true;
				startIntentSenderForResult( result.getResolution().getIntentSender(),
						RC_SIGN_IN, null, 0, 0, 0 );
				Log.i( TAG, result.getResolution().toString() );
			} catch( IntentSender.SendIntentException e ){
				// The intent was canceled before it was sent.  Return to the default
				// state and attempt to connect to get an updated ConnectionResult.
				intentInProgress = false;
				googleApiClient.connect();
				Log.i( TAG, "MainActivity.onConnectionFailed:catch_connect" );
			}
		}
	}

	public void signIntoFacebook(){
		Log.i( TAG, "MainActivity.signIntoFacebook" );
		Toast.makeText(getApplicationContext(), "Signing into Facebook", Toast.LENGTH_SHORT).show();
		goToActivity( LoginActivity.class );
	}

	public void signIntoGooglePlus(){
		Log.i( TAG, "MainActivity.signIntoGooglePlus" );
		Toast.makeText(getApplicationContext(), "Signing into Google+", Toast.LENGTH_SHORT).show();
		googleApiClient = new GoogleApiClient.Builder( this )
				.addApi( Plus.API ).build();
		googleApiClient.connect();
		goToActivity( LoginActivity.class );
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i( TAG, "MainActivity.onOptionsItemSelected" );
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.facebook_login_option:
				signIntoFacebook();
				return true;
			case R.id.googleplus_login_option:
				signIntoGooglePlus();
				return true;
			case R.id.googleplus_sign_in_button:
				signIntoGooglePlus();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void transactToFragment( int viewId, Fragment fragment, String tag ){
		Log.i( TAG, "MainActivity.transactToFragment" );
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace( viewId, fragment, tag );
		fragmentTransaction.addToBackStack( tag );
		fragmentTransaction.setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN );
		fragmentTransaction.commit();
	}

	// @Override
	public void onConnected( Bundle connectionHint ){
		Log.i( TAG, "MainActivity.onConnected" );
		if(requestingLocationUpdates){
			startLocationUpdates();
		}
	}

	protected void startLocationUpdates(){
		Log.i( TAG, "MainActivity.startLocationUpdates" );
		LocationRequest mLocationRequest;
		mLocationRequest = LocationRequest.create();
		LocationServices.FusedLocationApi.requestLocationUpdates( googleApiClient, mLocationRequest, this);
		// Check LocationListener to be added from the right library.
		// LocationListener.onLocationChanged(){
		// }
	}

	public void goToActivity( Class<?> c  ){
		Log.i( TAG, "MainActivity.goToActivity" );
		Intent intent = new Intent( this, c );
		startActivity( intent );
	}

	public void onMapLoaded(){
		Log.i( TAG, "MainActivity.onMapLoaded" );
		map.moveCamera( CameraUpdateFactory.newLatLngBounds( ROMANIA, 0 ) );
	}

	@Override
	public void onMapReady( final GoogleMap mMap ){
		Log.i( TAG, "MainActivity.onMapReady" );
		map = mMap;
		map.setMyLocationEnabled( true );
		map.setOnMapLoadedCallback( this );
	}

	@Override
	public void onLocationChanged( Location location ) {
		Log.i( TAG, "MainActivity.onLocationChanged" );
		double latitude = location.getLatitude();
		double longitude = location.getLatitude();
		double offset = 2.0;
		map.addCircle(new CircleOptions()
				.center(new LatLng(latitude, longitude))
				.radius(1000)
				.fillColor(Color.BLUE)
				.strokeColor(Color.BLUE));
		/*
		new MarkerOptions()
				.position( new LatLng( latitude, longitude ) )
				.title( "Me" )
				.icon( BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW ) ) );*/
		map.moveCamera(CameraUpdateFactory.newLatLngBounds(
				new LatLngBounds(new LatLng(latitude - offset, longitude - offset),
						new LatLng(latitude + offset, longitude + offset)), 0));
	}
}
