package com.example.routes.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.routes.R;
import com.example.routes.databinding.FragmentActivityBinding;
import com.example.routes.user.User;
import com.example.routes.utils.CustomUtility;
import com.example.routes.utils.MySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class ActivityFragment extends Fragment {
    public static String presentLat = "", presentLon = "", presentAcc = "";
    private static final int MY_PERMISSIONS_REQUEST = 0;
    public LocationManager locationManager;
    public GPSLocationListener listener;
    public Location previousBestLocation = null;
    private static final int TWO_MINUTES = 1000 * 60;
    public static final String BROADCAST_ACTION = "gps_data";
    String code = "", message = "";
    Intent intent;
    static Bitmap bitmap;
    SweetAlertDialog pDialog;
    JSONObject jsonObject;

    TextView txtName, txtTeam, txtTodayCount, txtTotalCount, imageStatus;

    ImageButton logoutBtn, imageBtn;
    Button submitBtn;

    EditText edtName,edtNumber,edtAddress;

    String name = "", number = "", address = "";

    String area = "", userId = "";

    String photoName = "", imageString = "";
    Boolean photoFlag = false;

    Uri photoURI;
    static final int REQUEST_IMAGE_CAPTURE = 99;
    String currentPhotoPath = "";

    SharedPreferences sharedPreferences;

    boolean network = false;

    String[] operatorList = {"017", "013", "019", "014", "016", "018", "015"};


    SweetAlertDialog sweetAlertDialog;

    User user;
    List<String> priorBrandList = new ArrayList<>();
    ArrayAdapter<String> priorBrandAdapter;
    FragmentActivityBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentActivityBinding.inflate(getLayoutInflater(),container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();
    }

    private void initialize() {

        user = User.getInstance();
        if(user.getUserId()==null)
        {
            user.setValuesFromSharedPreference(requireActivity().getSharedPreferences("user",MODE_PRIVATE));
        }

        submitBtn = binding.submitBtn;
        imageBtn = binding.imageBtn;

        imageStatus = binding.imageStatus;

        edtName = binding.edtName;
        edtNumber = binding.edtContactNumber;
        edtAddress = binding.edtAddress;

        getBrandList();

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                photoName = CustomUtility.getDeviceDate()+"image.jpeg";
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        CustomUtility.showAlert(requireContext(), ex.getMessage(), "Creating Image");
                        return;
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        photoURI = FileProvider.getUriForFile(requireActivity().getApplicationContext(),
                                "com.example.routes.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                network = CustomUtility.haveNetworkConnection(requireContext());
                name = edtName.getText().toString();
                number = edtNumber.getText().toString();
                address = edtAddress.getText().toString();

                boolean flag = chekFeilds();
                if(flag)
                {
                    SweetAlertDialog confirm = new SweetAlertDialog(requireContext(),SweetAlertDialog.WARNING_TYPE);
                    confirm.setTitle("Are you sure?");
                    confirm.setConfirmButton("Yes", new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            confirm.dismissWithAnimation();
                            upload();
                        }
                    });
                    confirm.setCancelButton("No", new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            confirm.dismissWithAnimation();
                        }
                    });
                    confirm.show();
                }
            }
        });


        // for getting gps value
        //intent = new Intent(BROADCAST_ACTION);
        GPS_Start();
    }

    private void getBrandList() {
        priorBrandList.add("Dano");
        priorBrandList.add("Fresh");
        priorBrandList.add("Nestle");
        priorBrandList.add("Others");
        priorBrandAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, priorBrandList);
        binding.priorBrandSpinner.setAdapter(priorBrandAdapter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //requireContext().stopService(intent);
    }

    private boolean isCorrectPhoneNumber(String phone) {
        if (phone.equals("") || (phone.length() != 11)) {
            return false;
        }
        String code2 = phone.substring(0, 3);
        for (String op : operatorList) {
            if (op.equals(code2)) {
                return true;
            }
        }
        return false;
    }




    //after finishing camera intent whether the picture was save or not
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            photoFlag = true;
            imageStatus.setText(R.string.take_image_done);
        }
    }

    private File createImageFile() throws IOException {

        File storageDir = requireActivity().getExternalFilesDir("Routes/Photos");

        File image = new File(storageDir.getAbsolutePath() + File.separator + photoName);
        try {
            image.createNewFile();
        } catch (IOException e) {
            CustomUtility.showAlert(requireContext(), "Image Creation Failed. Please contact administrator", "Error");
        }
        currentPhotoPath = image.getAbsolutePath();
        Log.e("image path",currentPhotoPath);
        return image;
    }


    private boolean chekFeilds()
    {
        if (!network)
        {
            CustomUtility.showWarning(requireContext(),"Please turn on internet connection","No inerternet connection!");
            return false;
        }
        else if (number.length()>0 & !isCorrectPhoneNumber(number))
        {
            CustomUtility.showWarning(requireContext(), "Please insert correct contact number","Required fields");
            return false;
        }
        else if(!photoFlag)
        {
            CustomUtility.showWarning(requireContext(),"Please take a selfie","Required fields");
            return false;
        }
        else if(presentAcc.equals(""))
        {
            CustomUtility.showWarning(requireContext(),"Please wait for the gps","Required fields");
            return false;
        }
        return true;
    }



    private void upload()
    {

        pDialog = new SweetAlertDialog(requireContext(),SweetAlertDialog.PROGRESS_TYPE);
        pDialog.show();
        Uri uri = Uri.fromFile(new File(currentPhotoPath));
        try{
            bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
        } catch (IOException e) {
            pDialog.dismiss();
            String err = e.getMessage() + " May be storage full please uninstall then install the app again";
            CustomUtility.showAlert(requireContext(), e.getMessage(), "Problem Creating Bitmap at Submit");
            return;
        }
        imageString = CustomUtility.imageToString(bitmap);
        String upLoadServerUri = "https://routes.atmdbd.com/api/consumer/insert_consumer.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, upLoadServerUri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pDialog.dismiss();
                        Log.e("response",response);
                        try {
                            jsonObject = new JSONObject(response);
                            String code = jsonObject.getString("success");
                            String message = jsonObject.getString("message");
                            if(code.equals("true"))
                            {
                                code = "Successful";
                                File fdelete = new File(currentPhotoPath);
                                if (fdelete.exists()) {
                                    if (fdelete.delete()) {
                                        System.out.println("file Deleted :" + currentPhotoPath);
                                    } else {
                                        System.out.println("file not Deleted :" + currentPhotoPath);
                                    }
                                }
                                new SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Successful")
                                        .setContentText("")
                                        .setConfirmText("Ok")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sDialog) {
                                                sDialog.dismissWithAnimation();
                                                startActivity(requireActivity().getIntent());
                                                requireActivity().finish();
                                            }
                                        })
                                        .show();
                            }
                            else
                            {
                                code = "Failed";
                                CustomUtility.showError(requireContext(),message,code);
                                //CustomUtility.showError(AttendanceActivity.this,"You allready submitted in",code);
                            }


                        } catch (JSONException e) {
                            CustomUtility.showError(requireContext(), e.getMessage(), "Failed");
                        }
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                Log.e("response",error.toString());
                CustomUtility.showError(requireContext(), "Network slow, try again", "Failed");

            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("UserId",user.getUserId());
                params.put("Name",name);
                params.put("Mobile",number);
                params.put("Latitude",presentLat);
                params.put("Longitude",presentLon);
                params.put("Accuracy",presentAcc);
                params.put("PictureData",imageString);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(requireContext()).addToRequestQue(stringRequest);
    }



    private void GPS_Start() {
        try {
            locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
            listener = new GPSLocationListener();
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listener);
        } catch (Exception ex) {

        }
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public class GPSLocationListener implements LocationListener {
        public void onLocationChanged(final Location loc) {
            Log.i("**********", "Location changed");
            if (isBetterLocation(loc, previousBestLocation)) {


                loc.getAccuracy();
                //location.setText(" " + loc.getAccuracy());

                presentLat = String.valueOf(loc.getLatitude());
                presentLon = String.valueOf(loc.getLongitude());
                presentAcc = String.valueOf(loc.getAccuracy());


//                Toast.makeText(context, "Latitude" + loc.getLatitude() + "\nLongitude" + loc.getLongitude(), Toast.LENGTH_SHORT).show();
//                intent.putExtra("Latitude", loc.getLatitude());
               // intent.putExtra("Longitude", loc.getLongitude());
               // intent.putExtra("Provider", loc.getProvider());
                //requireActivity().sendBroadcast(intent);
            }
        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(requireActivity().getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }

        public void onProviderEnabled(String provider) {
            Toast.makeText(requireActivity().getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            //Toast.makeText(getApplicationContext(), "Status Changed", Toast.LENGTH_SHORT).show();
        }
    }
}