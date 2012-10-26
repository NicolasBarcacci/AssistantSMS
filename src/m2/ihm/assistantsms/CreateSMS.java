package m2.ihm.assistantsms;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;

import resources.DatePickerFragment;
import resources.TimePickerFragment;
import m2.ihm.assistantsms.base_de_donnees.MaBaseSMSGestion;
import m2.ihm.assistantsms.service.ServiceEnvoieSMS;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.FragmentActivity;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


@SuppressLint("NewApi")
public class CreateSMS extends FragmentActivity {
	
	private TimePickerFragment fragmentTime;
	private DatePickerFragment fragmentDate;

	private Button buttonDate = null;
	private Button buttonTime = null;
	
	private ImageButton buttonMap = null;	
	private ImageButton buttonCalendrier = null;	
	
	private CheckBox checkBoxTime = null;
	private CheckBox checkBoxMap = null;
	
	private EditText editTextContact = null;
	private EditText editTextLocalisation = null;
	private EditText editTextSMS = null;
    private static final int CONTACT_PICKER_RESULT = 1001;  
    final Context context = this;

	private MaBaseSMSGestion  maBaseGestion= new MaBaseSMSGestion(this);
	private AlarmManager alarmManager;
	private static final long MINIMUM_DISTANCECHANGE_FOR_UPDATE = 1; // in Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATE = 1000; // in Milliseconds
	
	private static final long POINT_RADIUS = 1000; // in Meters
	private static final long PROX_ALERT_EXPIRATION = -1; 
	
	private static final String POINT_LATITUDE_KEY = "POINT_LATITUDE_KEY";
	private static final String POINT_LONGITUDE_KEY = "POINT_LONGITUDE_KEY";
	
	private static final String PROX_ALERT_INTENT = "m2.ihm.assistantsms.ProximityAlert";
	
	private static final NumberFormat nf = new DecimalFormat("##.########");
	
	private LocationManager locationManager;
	private Geocoder geocoder ;
	double longitude;
	double latitude;
	private static String TREASURE_PROXIMITY_ALERT = "come.trasurealert";
	
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_sms);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        init();
        
    }

    private void init(){

        editTextContact = (EditText) findViewById(R.id.editTextContact);
        editTextLocalisation = (EditText) findViewById(R.id.editTextLocalisation);
        editTextSMS = (EditText) findViewById(R.id.editTextSMS);
		buttonDate = (Button)findViewById(R.id.picker_date);
		buttonTime = (Button)findViewById(R.id.picker_time);

		buttonCalendrier = (ImageButton)findViewById(R.id.button_calendar);
		buttonMap = (ImageButton)findViewById(R.id.button_map);
		buttonMap.setEnabled(false);
		editTextLocalisation.setEnabled(false);
		
        checkBoxTime = (CheckBox) findViewById(R.id.checkbox_time);
        checkBoxTime.setChecked(true);
        
        checkBoxMap= (CheckBox) findViewById(R.id.checkbox_map);
        
        
    	fragmentTime = new TimePickerFragment(buttonTime);
		fragmentDate = new DatePickerFragment(buttonDate);
    	fragmentTime.ModifierButton();
		fragmentDate.ModifierButton();
    }
	
	
    public void doLaunchContactPicker(View view) {  
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,  
                Contacts.CONTENT_URI);  
        contactPickerIntent.setType(Phone.CONTENT_TYPE);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);  
    }  
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONTACT_PICKER_RESULT) {
            if (resultCode == RESULT_OK) {
                Uri contactUri = data.getData();
                String[] projection = {Phone.NUMBER};
                Cursor cursor = getContentResolver()
                        .query(contactUri, projection, null, null, null);
                cursor.moveToFirst();

                int column = cursor.getColumnIndex(Phone.NUMBER);
                String number = cursor.getString(column);
                if(editTextContact.getText().equals(("")))
                	editTextContact.setText(editTextContact.getText()+";"+number);
                else
                	editTextContact.setText(number);
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_create_sms, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        Toast toast;
    	
    	switch (item.getItemId()) {
            case android.R.id.home:
            	Intent parentActivityIntent = new Intent(this, Main.class);
                parentActivityIntent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(parentActivityIntent);
                finish();
                return true;
                
            case R.id.menu_accept:
            	String error = repectCondition();
            	if(error == null){
	            	maBaseGestion.open();
	            	Timestamp timestamp = null;
	            	if(checkBoxTime.isChecked()){
	            		timestamp = new Timestamp(fragmentDate.getYear()-1900,
							            				fragmentDate.getMonth(),
							            				fragmentDate.getDay(),
							            				fragmentTime.getHour(),
							            				fragmentTime.getMinute(),0,0);
	            		
	            	}
	            	
	            	if(checkBoxTime.isChecked() && checkBoxMap.isChecked()){
	            		maBaseGestion.insertSMS(
		            			editTextContact.getText().toString(), 
		            			timestamp,
		            			editTextLocalisation.getText().toString(), 
		            			editTextSMS.getText().toString());
	            				addAlarmDate(timestamp);
	            				addAlarmLocalisation(editTextLocalisation.getText().toString());
            	
	            	}
	            	else if(checkBoxTime.isChecked()){
	            		maBaseGestion.insertSMS(
		            			editTextContact.getText().toString(), 
		            			timestamp,
		            			"null", 
		            			editTextSMS.getText().toString());
	            				addAlarmDate(timestamp);
	            	}
	            	else{
	            		maBaseGestion.insertSMS(
	            			editTextContact.getText().toString(), 
	            			null,
	            			editTextLocalisation.getText().toString(), 
	            			editTextSMS.getText().toString());
        					addAlarmLocalisation(editTextLocalisation.getText().toString());
                    	         
	            	}
	            	maBaseGestion.close();
	            	
	            	toast = Toast.makeText(getApplicationContext(), "Message enregistr�", Toast.LENGTH_SHORT);
	            	toast.show();
	            	
	            	intent = new Intent(this, Main.class);
	                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                startActivity(intent);
            	}
            	else{
            		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
          			alertDialogBuilder.setTitle("Erreur");
        			alertDialogBuilder
        				.setMessage(error)
        				.setCancelable(false)
        				.setPositiveButton("Yes", null);
        			AlertDialog alertDialog = alertDialogBuilder.create();
        			alertDialog.show();
            	}
            	finish();
            	return true;
            	
            case R.id.menu_cancel:

            	toast = Toast.makeText(getApplicationContext(), "Annulation", Toast.LENGTH_SHORT);
            	toast.show();
            	
            	intent = new Intent(this, Main.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            	return true;
            
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	private void addAlarmDate(Timestamp timestamp){
    	alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    	 Intent intent = new Intent(this, ServiceEnvoieSMS.class);
    	  Bundle n = new Bundle();
    	  n.putString("Key", "date");
    	  intent.putExtras(n);

    	  PendingIntent pendingIntent = PendingIntent.getService(this, 0,
    	    intent, PendingIntent.FLAG_ONE_SHOT);
    	  
    	  Calendar cal = Calendar.getInstance();
  		  cal.set(Calendar.SECOND, 0);
  		  cal.set(Calendar.MILLISECOND, 0);
    	  alarmManager.set(AlarmManager.RTC_WAKEUP,
    	    timestamp.getTime(), pendingIntent);
    }
    
    private String repectCondition() {
    	String erreur = null;
		if(editTextContact.getText().toString().equals(""))
			erreur =  "Vous n'avez pas choisi de contact";
		else if(editTextSMS.getText().toString().equals(""))
			erreur =  "Vous n'avez pas �crit de texte";
		else if(checkBoxMap.isChecked() && editTextLocalisation.getText().toString().equals(""))
			erreur =  "vous n'avez pas choisi une localisation";
		else if(checkBoxTime.isChecked()){
			Timestamp timestamp = new Timestamp(fragmentDate.getYear()-1900,
    				fragmentDate.getMonth(),
    				fragmentDate.getDay(),
    				fragmentTime.getHour(),
    				fragmentTime.getMinute(),0,0);
			final Calendar c = Calendar.getInstance();
			if(c.getTimeInMillis() > timestamp.getTime())
				erreur =  "La date ou l'heure est inf�rieur a celle actuel";
		}
		else if (!checkBoxMap.isChecked() && !checkBoxTime.isChecked())
			erreur =  "Vous devez choisir une date ou une localisation";
		if(checkBoxMap.isChecked() && !editTextLocalisation.getText().toString().equals("")){
			geocoder = new Geocoder(this);
	    	List<Address> listadress;
	    	try {
				 listadress = geocoder.getFromLocationName(editTextLocalisation.getText().toString(), 1);
				 if(listadress.size() == 0)
						erreur =  "La localisation n'a pas �tait trouv�e";
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
		return erreur;
	}

	public void acceslocalisation(View view){
    	Intent intent = new Intent(this, Localisation.class);
    	
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    
    public void onCheckboxClicked(View view){
    	boolean checked = ((CheckBox) view).isChecked();
    	switch(view.getId()){
    		case R.id.checkbox_map:
    			if(checked){
    				buttonMap.setEnabled(true);
    				editTextLocalisation.setEnabled(true);
    				editTextLocalisation.setText("");
    			}
    			else{
    				buttonMap.setEnabled(false);
    				editTextLocalisation.setEnabled(false);    				
    			}
    			break;
    		case R.id.checkbox_time:
    			if(checked){
    				buttonDate.setEnabled(true);
    				buttonTime.setEnabled(true);
    				buttonCalendrier.setEnabled(true);
    				fragmentDate.ModifierButton();
    				fragmentTime.ModifierButton();
    			}
    			else{
    				buttonCalendrier.setEnabled(false);
    				buttonDate.setEnabled(false);
    				buttonTime.setEnabled(false);
    				buttonDate.setText("JJ-MM-AAAA");
    				buttonTime.setText("HH:MM");
    			}
    	}
    }
    public void showTimePickerDialog(View v) {
    	fragmentTime.show(getSupportFragmentManager(), "timePicker");
    	showDialog(999);
    }

	public void showDatePickerDialog(View v) {
		fragmentDate.show(getSupportFragmentManager(), "datePicker");
    	showDialog(998);
	}

    
    private void addAlarmLocalisation(String localisation) {
    	geocoder = new Geocoder(this);
    	List<Address> listadress;
    	try {
			 listadress = geocoder.getFromLocationName(localisation, 1);
			 if(listadress.size() > 0){
					longitude = listadress.get(0).getLongitude();
					latitude = listadress.get(0).getLatitude();
			        Toast.makeText(getApplicationContext(), "longitude "+longitude+ " latitude "+latitude, Toast.LENGTH_LONG).show();
					addProximityAlert(latitude, longitude);
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
       Toast.makeText(getApplicationContext(), "alarm localisation", Toast.LENGTH_LONG).show();
       
	}
	private void addProximityAlert(double latitude, double longitude) {
		
        Intent intent = new Intent(this, ServiceEnvoieSMS.class);
  	    Bundle n = new Bundle();
  	    n.putString("Key", "localisation");
  	    intent.putExtras(n);
        PendingIntent proximityIntent = PendingIntent.getService(this, -1,
        	    intent, 0);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationManager.addProximityAlert(
    		latitude, // the latitude of the central point of the alert region
    		longitude, // the longitude of the central point of the alert region
    		POINT_RADIUS, // the radius of the central point of the alert region, in meters
    		PROX_ALERT_EXPIRATION, // time for this proximity alert, in milliseconds, or -1 to indicate no expiration 
    		proximityIntent // will be used to generate an Intent to fire when entry to or exit from the alert region is detected
       );
	}
	
}
