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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.routes.MainActivity;
import com.example.routes.R;
import com.example.routes.databinding.FragmentActivityBinding;
import com.example.routes.model.User;
import com.example.routes.utils.CustomUtility;
import com.example.routes.utils.MySingleton;
import com.ramijemli.percentagechartview.PercentageChartView;

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
    static Bitmap bitmap;
    SweetAlertDialog pDialog;
    JSONObject jsonObject;

    TextView imageStatus;

    ImageButton  imageBtn;
    Button submitBtn;

    EditText edtName,edtNumber,edtAddress;

    String name = "", number = "", address = "";
    String isSold = "", priorBrandName = "", priorBrandId = "";

    String area = "", userId = "";

    String photoName = "", imageString = "";
    Boolean photoFlag = false;

    Uri photoURI;
    static final int REQUEST_IMAGE_CAPTURE = 99;
    String currentPhotoPath = "";

    SharedPreferences sharedPreferences;

    boolean network = false;

    String[] operatorList = {"017", "013", "019", "014", "016", "018", "015"};
    String[] purposeList = {"Glass of Milk", "Desert", "Tea", "Others"};
    Map<Integer, String> brandMap = new HashMap<>();

    SweetAlertDialog sweetAlertDialog;

    User user;
    List<String> priorBrandList = new ArrayList<>();
    ArrayAdapter<String> purposeListAdapter;
    String diplomaPurpose = "", marksPurpose = "", freshPurpose = "", danoPurpose = "", danishPurpose = "", pranPurpose = "", nidoPurpose = "", othersPurpose = "";
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

       /* getBrandList();
        binding.priorBrandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                priorBrandName = priorBrandList.get(position);
                priorBrandId = brandMap.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        */

        purposeListAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, purposeList);
        binding.diplomaSpinner.setAdapter(purposeListAdapter);
        binding.marksSpinner.setAdapter(purposeListAdapter);
        binding.freshSpinner.setAdapter(purposeListAdapter);
        binding.danoSpinner.setAdapter(purposeListAdapter);
        binding.danishSpinner.setAdapter(purposeListAdapter);
        binding.pranSpinner.setAdapter(purposeListAdapter);
        binding.nidoSpinner.setAdapter(purposeListAdapter);
        binding.othersSpinner.setAdapter(purposeListAdapter);

        binding.diplomaCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    binding.diplomaCardview.setVisibility(View.VISIBLE);
                }
                else
                {
                    binding.diplomaCardview.setVisibility(View.GONE);
                }
            }
        });
        binding.marksCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    binding.marksCardview.setVisibility(View.VISIBLE);
                }
                else
                {
                    binding.marksCardview.setVisibility(View.GONE);
                }
            }
        });
        binding.freshCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    binding.freshCardview.setVisibility(View.VISIBLE);
                }
                else
                {
                    binding.freshCardview.setVisibility(View.GONE);
                }
            }
        });
        binding.danoCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    binding.danoCardview.setVisibility(View.VISIBLE);
                }
                else
                {
                    binding.danoCardview.setVisibility(View.GONE);
                }
            }
        });
        binding.danishCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    binding.danishCardview.setVisibility(View.VISIBLE);
                }
                else
                {
                    binding.danishCardview.setVisibility(View.GONE);
                }
            }
        });
        binding.pranCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    binding.pranCardview.setVisibility(View.VISIBLE);
                }
                else
                {
                    binding.pranCardview.setVisibility(View.GONE);
                }
            }
        });
        binding.nidoCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    binding.nidoCardview.setVisibility(View.VISIBLE);
                }
                else
                {
                    binding.nidoCardview.setVisibility(View.GONE);
                }
            }
        });
        binding.othersCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    binding.othersCardview.setVisibility(View.VISIBLE);
                }
                else
                {
                    binding.othersCardview.setVisibility(View.GONE);
                }
            }
        });

        binding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == binding.saleDoneYes.getId())
                {
                    isSold = "True";
                }
                else if(checkedId == binding.saleDoneNo.getId())
                {
                    isSold = "False";
                }
            }
        });

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
    }
/*
    private void getBrandList() {
        sweetAlertDialog = new SweetAlertDialog(requireContext(), 5);
        sweetAlertDialog.setTitleText("Loading");
        sweetAlertDialog.show();
        MySingleton.getInstance(requireContext()).addToRequestQue(new StringRequest(1, "https://fresh.atmdbd.com/api/android/get_brand_list.php", new Response.Listener<String>() {
            public void onResponse(String response) {
                try {
                    sweetAlertDialog.dismiss();
                    Log.e("response", response);
                    jsonObject = new JSONObject(response);
                    String code = jsonObject.getString("success");
                    if (code.equals("true")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("brandList");
                        for (int i = 0; i<jsonArray.length();i++)
                        {
                            priorBrandList.add(jsonArray.getJSONObject(i).getString("name"));
                            brandMap.put(i,jsonArray.getJSONObject(i).getString("id"));
                        }
                        priorBrandAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, priorBrandList);
                        binding.priorBrandSpinner.setAdapter(priorBrandAdapter);
                    }
                    else
                        CustomUtility.showError(requireContext(), "No data found", "Failed");
                } catch (JSONException e) {
                    CustomUtility.showError(requireContext(), e.getMessage(), "Getting Response");
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                sweetAlertDialog.dismiss();
                final SweetAlertDialog s = new SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE);
                s.setConfirmText("Ok");
                s.setTitleText("Network Error, try again!");
                s.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        s.dismissWithAnimation();
                        startActivity(requireActivity().getIntent());
                        requireActivity().finish();
                    }
                });
                s.show();
            }
        }) {
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("UserId", user.getUserId());
                return params;
            }
        });
    }

 */

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
        else if (name.equals(""))
        {
            CustomUtility.showWarning(requireContext(), "Please enter consumer name","Required fields");
            return false;
        }
        else if (!isCorrectPhoneNumber(number))
        {
            CustomUtility.showWarning(requireContext(), "Please insert correct contact number","Required fields");
            return false;
        }
        else if (isSold.equals(""))
        {
            CustomUtility.showWarning(requireContext(), "Please select Fresh was sold or not","Required fields");
            return false;
        }

        else if(MainActivity.presentAcc.equals(""))
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
        if(!currentPhotoPath.equals(""))
        {
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
        }
        String upLoadServerUri = "https://fresh.atmdbd.com/api/contact/insert_contact.php";
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
                params.put("Address",address);
                params.put("IsSold",isSold);
                params.put("Remark",binding.remark.getText().toString());
                params.put("PriorBrandName",priorBrandName);
                params.put("PriorBrandId", priorBrandId);
                params.put("Latitude", MainActivity.presentLat);
                params.put("Longitude",MainActivity.presentLon);
                params.put("Accuracy",MainActivity.presentAcc);
                params.put("PictureData",imageString);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(requireContext()).addToRequestQue(stringRequest);
    }


}