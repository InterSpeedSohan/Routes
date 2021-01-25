 package com.example.routes.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    String isSold = "";


    String imageString = "";
    Boolean photoFlag = false;

    static final int REQUEST_IMAGE_CAPTURE = 99;
    String currentPhotoPath = "";

    SharedPreferences sharedPreferences;

    boolean network = false;

    String[] operatorList = {"017", "013", "019", "014", "016", "018", "015"};
    String[] purposeList = {"Glass of Milk", "Dessert", "Tea", "Others"};
    String[] sexList = {"Male", "Female", "Others"};


    User user;
    String callStatus = "", callDuration = "", callTime = "";
    ArrayAdapter<String> purposeListAdapter, sexListAdapter;
    String diplomaPurpose = "", marksPurpose = "", freshPurpose = "", danoPurpose = "", danishPurpose = "", pranPurpose = "", nidoPurpose = "", othersPurpose = "";
    String sex = "";
    boolean correctNumber = false, successCall = false, isfresh =false, isdiploma=false, ismarks=false, isdano=false, isdanish=false, ispran=false, isnido=false, isothers=false;
    FragmentActivityBinding binding;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentActivityBinding.inflate(getLayoutInflater(),container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if(savedInstanceState!=null)
        {
            correctNumber = savedInstanceState.getBoolean("correctNumber");
            successCall = savedInstanceState.getBoolean("successCall");
            photoFlag = savedInstanceState.getBoolean("photoFlag");
            if(photoFlag)
            {
                imageString = savedInstanceState.getString("imageString");
                binding.imageStatus.setText(R.string.take_image_done);
            }
            binding.textMobileNumber.setText(binding.edtContactNumber.getText().toString());
            setCallVisible();
        }
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

        binding.edtContactNumber.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(!isCorrectPhoneNumber(s.toString()))
                {
                    binding.edtContactNumber.setError("Number must be correct and 11 digits");
                    correctNumber = false;
                    setCallGone();
                }
                else if(!correctNumber)
                {
                    checkNumberValidation(s.toString());
                }
            }
        });



        purposeListAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, purposeList);
        binding.diplomaSpinner.setAdapter(purposeListAdapter);
        binding.marksSpinner.setAdapter(purposeListAdapter);
        binding.freshSpinner.setAdapter(purposeListAdapter);
        binding.danoSpinner.setAdapter(purposeListAdapter);
        binding.danishSpinner.setAdapter(purposeListAdapter);
        binding.pranSpinner.setAdapter(purposeListAdapter);
        binding.nidoSpinner.setAdapter(purposeListAdapter);
        binding.othersSpinner.setAdapter(purposeListAdapter);

        sexListAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, sexList);
        binding.sexSpinner.setAdapter(sexListAdapter);
        binding.sexSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sex = binding.sexSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        binding.diplomaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                diplomaPurpose = binding.diplomaSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        binding.marksSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                marksPurpose = binding.marksSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        binding.freshSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                freshPurpose = binding.freshSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        binding.danoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                danoPurpose = binding.danoSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        binding.danishSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                danishPurpose = binding.danishSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        binding.pranSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                pranPurpose = binding.pranSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        binding.nidoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                nidoPurpose = binding.nidoSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        binding.othersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                othersPurpose = binding.othersSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.diplomaCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    binding.diplomaCardview.setVisibility(View.VISIBLE);
                    isdiploma = true;
                }
                else
                {
                    binding.diplomaCardview.setVisibility(View.GONE);
                    isdiploma = false;
                }
            }
        });
        binding.marksCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    binding.marksCardview.setVisibility(View.VISIBLE);
                    ismarks = true;
                }
                else
                {
                    binding.marksCardview.setVisibility(View.GONE);
                    ismarks = false;
                }
            }
        });
        binding.freshCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    binding.freshCardview.setVisibility(View.VISIBLE);
                    isfresh = true;
                }
                else
                {
                    binding.freshCardview.setVisibility(View.GONE);
                    isfresh = false;
                }
            }
        });
        binding.danoCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    binding.danoCardview.setVisibility(View.VISIBLE);
                    isdano = true;
                }
                else
                {
                    binding.danoCardview.setVisibility(View.GONE);
                    isdano = false;
                }
            }
        });
        binding.danishCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    binding.danishCardview.setVisibility(View.VISIBLE);
                    isdanish = true;
                }
                else
                {
                    binding.danishCardview.setVisibility(View.GONE);
                    isdanish = false;
                }
            }
        });
        binding.pranCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    binding.pranCardview.setVisibility(View.VISIBLE);
                    ispran = true;
                }
                else
                {
                    binding.pranCardview.setVisibility(View.GONE);
                    ispran = false;
                }
            }
        });
        binding.nidoCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    binding.nidoCardview.setVisibility(View.VISIBLE);
                    isnido = true;
                }
                else
                {
                    binding.nidoCardview.setVisibility(View.GONE);
                    isnido = false;
                }
            }
        });
        binding.othersCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    binding.othersCardview.setVisibility(View.VISIBLE);
                    isothers = true;
                }
                else
                {
                    binding.othersCardview.setVisibility(View.GONE);
                    isothers = false;
                }
            }
        });

        binding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == binding.saleDoneYes.getId())
                {
                    isSold = "1";
                }
                else if(checkedId == binding.saleDoneNo.getId())
                {
                    isSold = "0";
                }
            }
        });

        binding.callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "tel:" + binding.textMobileNumber.getText().toString().trim();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                requireActivity().startActivity(intent);
            }
        });

        binding.getDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.callDetailsCardview.setVisibility(View.VISIBLE);
                if(!getCallDetails()){
                    CustomUtility.showError(requireContext(), "Please call this numnber first", "Required fields");
                }
            }
        });

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                String photoName = CustomUtility.getDeviceDate() + "image.jpeg";
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
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

    private boolean getCallDetails() {

        Cursor managedCursor = requireContext().getContentResolver().query( CallLog.Calls.CONTENT_URI,null, null,null, CallLog.Calls.DATE + " DESC");
        int number = managedCursor.getColumnIndex( CallLog.Calls.NUMBER );
        int type = managedCursor.getColumnIndex( CallLog.Calls.TYPE );
        int date = managedCursor.getColumnIndex( CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex( CallLog.Calls.DURATION);

        int i = 0;
        String phNumber, callType, callDate = null, timeString = null;
        Date callDayTime = null;
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd H:mm:ss");
        int dircode, hours, minutes, seconds;
        while ( managedCursor.moveToNext() ) {
            phNumber = managedCursor.getString( number );
            callType = managedCursor.getString( type );
            callDate = managedCursor.getString( date );
            callDayTime = new Date(Long.parseLong(callDate));
            callTime = simpleDateFormat1.format(callDayTime);
            callDuration = managedCursor.getString( duration );
            hours = Integer.parseInt(callDuration) / 3600;
            minutes = (Integer.parseInt(callDuration) % 3600) / 60;
            seconds = Integer.parseInt(callDuration) % 60;
            callDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            callStatus = null;
            dircode = Integer.parseInt( callType );
            switch( dircode ) {
                case CallLog.Calls.OUTGOING_TYPE:
                    callStatus = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    callStatus = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    callStatus = "MISSED";
                    break;
            }
            i++;
            if(phNumber.equals(binding.textMobileNumber.getText().toString()))
            {
                successCall = true;
                break;
            }
            else if(i == 3) {
                successCall = false;
                break;
            }
        }
        managedCursor.close();

        if(successCall){
            binding.callTypes.setText("Call type: "+callStatus);
            binding.callTime.setText("Call date: "+callTime);
            binding.callDuration.setText("Call duration: "+callDuration);
        }
        return successCall;
    }

    private void checkNumberValidation(String number)
    {

        pDialog = new SweetAlertDialog(requireContext(),SweetAlertDialog.PROGRESS_TYPE);
        pDialog.show();
        String upLoadServerUri = "https://fresh.atmdbd.com/api/contact/get_mobile_count.php";
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
                                String percent = jsonObject.getString("duplicatePercentage");
                                if(Float.parseFloat(percent) < 5.0)
                                {
                                    correctNumber = true;
                                    binding.textMobileNumber.setText(number);
                                    setCallVisible();
                                }
                                else
                                {
                                    correctNumber = false;
                                    binding.edtContactNumber.setError("Duplicate number detected");
                                    setCallGone();
                                }
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
                params.put("Mobile",number);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(requireContext()).addToRequestQue(stringRequest);

    }

    private void setCallGone()
    {
        binding.textMobileNumber.setVisibility(View.GONE);
        binding.callBtn.setVisibility(View.GONE);
        binding.getDetailsBtn.setVisibility(View.GONE);
        binding.callDetailsCardview.setVisibility(View.GONE);
    }
    private void setCallVisible()
    {
        binding.textMobileNumber.setVisibility(View.VISIBLE);
        binding.callBtn.setVisibility(View.VISIBLE);
        binding.getDetailsBtn.setVisibility(View.VISIBLE);
        binding.callDetailsCardview.setVisibility(View.VISIBLE);
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
            binding.imageStatus.setText(R.string.take_image_done);
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageString = CustomUtility.imageToString(imageBitmap);
        }
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
        else if (!successCall)
        {
            CustomUtility.showWarning(requireContext(), "Please call the number","Required fields");
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
                params.put("Gender",sex);
                params.put("Age",binding.edtAge.getText().toString());
                params.put("IsSold",isSold);

                if(isfresh){
                    params.put("PriorBrand01Name","Fresh");
                    params.put("PriorBrand01Skew", binding.edtFreshConsumptionSku.getText().toString());
                    params.put("PriorBrand01ConsumptionUnit",binding.freshEdtConsumptionUnit.getText().toString());
                    params.put("PriorBrand01Purpose",freshPurpose);
                }
                if(isdiploma){
                    params.put("PriorBrand02Name","Diploma");
                    params.put("PriorBrand02Skew", binding.edtDiplomaConsumptionSku.getText().toString());
                    params.put("PriorBrand02ConsumptionUnit",binding.diplomaEdtConsumptionUnit.getText().toString());
                    params.put("PriorBrand02Purpose",diplomaPurpose);
                }
                if(ismarks){
                    params.put("PriorBrand03Name","Marks");
                    params.put("PriorBrand03Skew", binding.edtMarksConsumptionSku.getText().toString());
                    params.put("PriorBrand03ConsumptionUnit",binding.marksEdtConsumptionUnit.getText().toString());
                    params.put("PriorBrand03Purpose",marksPurpose);
                }
                if(isdano){
                    params.put("PriorBrand04Name","Dano");
                    params.put("PriorBrand04Skew", binding.edtDanoConsumptionSku.getText().toString());
                    params.put("PriorBrand04ConsumptionUnit",binding.danoEdtConsumptionUnit.getText().toString());
                    params.put("PriorBrand04Purpose",danoPurpose);
                }
                if(isdanish){
                    params.put("PriorBrand05Name","Danish");
                    params.put("PriorBrand05Skew", binding.edtDanishConsumptionSku.getText().toString());
                    params.put("PriorBrand05ConsumptionUnit",binding.danishEdtConsumptionUnit.getText().toString());
                    params.put("PriorBrand05Purpose",danishPurpose);
                }
                if(ispran){
                    params.put("PriorBrand06Name","Pran");
                    params.put("PriorBrand06Skew", binding.edtPranConsumptionSku.getText().toString());
                    params.put("PriorBrand06ConsumptionUnit",binding.pranEdtConsumptionUnit.getText().toString());
                    params.put("PriorBrand06Purpose",pranPurpose);
                }
                if(isnido)
                {
                    params.put("PriorBrand07Name","Nido");
                    params.put("PriorBrand07Skew", binding.edtNidoConsumptionSku.getText().toString());
                    params.put("PriorBrand07ConsumptionUnit",binding.nidoEdtConsumptionUnit.getText().toString());
                    params.put("PriorBrand07Purpose",nidoPurpose);
                }
                if(isothers)
                {
                    params.put("PriorBrand08Name","Others");
                    params.put("PriorBrand08Skew", binding.edtOthersConsumptionSku.getText().toString());
                    params.put("PriorBrand08ConsumptionUnit",binding.othersEdtConsumptionUnit.getText().toString());
                    params.put("PriorBrand08Purpose",othersPurpose);
                }


                params.put("CallStatus",callStatus);
                params.put("CallTime", callTime);
                params.put("CallDuration", callDuration);

                params.put("Latitude", MainActivity.presentLat);
                params.put("Longitude",MainActivity.presentLon);
                params.put("Accuracy",MainActivity.presentAcc);
                params.put("PictureData",imageString);
                params.put("Remark",binding.remark.getText().toString());

                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(requireContext()).addToRequestQue(stringRequest);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("correctNumber",correctNumber);
        outState.putBoolean("successCall",successCall);
        outState.putBoolean("photoFlag",photoFlag);
        outState.putString("imageString",imageString);
        super.onSaveInstanceState(outState);
    }



}