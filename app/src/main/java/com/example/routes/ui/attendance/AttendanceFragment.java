package com.example.routes.ui.attendance;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.routes.databinding.FragmentAttendanceBinding;
import com.example.routes.model.User;
import com.example.routes.utils.CustomUtility;
import com.example.routes.utils.MySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class AttendanceFragment extends Fragment {

    FragmentAttendanceBinding binding;
    String photoName = "", imageString = "";
    Boolean photoFlag = false;

    Uri photoURI;
    static final int REQUEST_IMAGE_CAPTURE = 6;
    String currentPhotoPath = "";
    static Bitmap bitmap;

    SharedPreferences sharedPreferences;

    boolean network = false;
    SweetAlertDialog sweetAlertDialog, pDialog;
    User user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAttendanceBinding.inflate(getLayoutInflater(),container,false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inititalize();
    }

    public void inititalize()
    {
        user = User.getInstance();
        if(user.getUserId()==null)
        {
            user.setValuesFromSharedPreference(requireActivity().getSharedPreferences("user",MODE_PRIVATE));
        }
        binding.hideGpsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.gpsText.setText(MainActivity.presentAcc);
            }
        });
        binding.camera.setOnClickListener(new View.OnClickListener() {
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
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                network = CustomUtility.haveNetworkConnection(requireContext());
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
    }
    //after finishing camera intent whether the picture was save or not
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            photoFlag = true;
            binding.takeSelfieText.setText(R.string.take_image_done);
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
        else if(MainActivity.presentAcc.equals(""))
        {
            CustomUtility.showWarning(requireContext(),"Please wait for the gps","Required fields");
            return false;
        }
        else if (!photoFlag)
        {
            CustomUtility.showWarning(requireContext(),"Please take a selfie","Required fields");
            return false;
        }
        return true;
    }



    private void upload()
    {
        pDialog = new SweetAlertDialog(requireContext(),SweetAlertDialog.PROGRESS_TYPE);
        pDialog.show();

        String upLoadServerUri = "https://fresh.atmdbd.com/api/android/insert_attendance.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, upLoadServerUri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pDialog.dismiss();
                        Log.e("response",response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
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
                params.put("InTimeLat", MainActivity.presentLat);
                params.put("InTimeLon",MainActivity.presentLon);
                params.put("InTimeAccuracy",MainActivity.presentAcc);
                params.put("InPictureData",imageString);
                params.put("Remark",binding.lateRemark.getText().toString());
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(requireContext()).addToRequestQue(stringRequest);
    }
}
