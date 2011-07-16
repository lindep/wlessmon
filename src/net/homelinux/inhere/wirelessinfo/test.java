package net.homelinux.inhere.wirelessinfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class test extends Activity {
	
	String PUBLIC_STATIC_STRING_IDENTIFIER = null;
	private EditText InputFileName;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		InputFileName = (EditText) findViewById(R.id.inputFileName);
		
		Spinner spinner = (Spinner) findViewById(R.id.fileSizeSpinner);
	    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.fileSize_array, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    
	    spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
	}
	
	public void onClickTest(View v) {
		trace("test: onClickStartTest: Start.");
		
		Bundle b = this.getIntent().getExtras();
        String s = b.getString("IDENT");
        String s1 = b.getString("IDENT1");
        trace("From parent intent = "+s+", second string = "+s1);
        
        String FileName = InputFileName.getText().toString();
		
		try{
			Intent resultIntent = new Intent(getApplicationContext(),test.class);
			resultIntent.putExtra(PUBLIC_STATIC_STRING_IDENTIFIER, "data_to_activity");
			resultIntent.putExtra("FILE_NAME", FileName);
			setResult(Activity.RESULT_OK, resultIntent);				
			finish();
		}catch (Exception e){
			trace("test intent eror ."+e.getMessage());
		}
		trace("test intent close.");
	}
	
	public class MyOnItemSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id) {
	      trace("The planet is " +parent.getItemAtPosition(pos).toString());
	    }

	    public void onNothingSelected(AdapterView parent) {
	      // Do nothing.
	    }
	}
	
	public void trace(String msg) {
		Log.d("WirelessInfo", msg);
	}

}
