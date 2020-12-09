package com.example.routes.ui.profile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.routes.R;
import com.example.routes.databinding.FragmentProfileBinding;
import com.example.routes.login.LoginActivity;
import com.example.routes.user.User;
import com.example.routes.utils.CustomUtility;
import com.example.routes.utils.MySingleton;
import com.hadiidbouk.charts.BarData;
import com.hadiidbouk.charts.ChartProgressBar;
import com.hadiidbouk.charts.OnBarClickedListener;
import com.ramijemli.percentagechartview.PercentageChartView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProfileFragment extends Fragment {

    RecyclerView recyclerView;
    DataAdapter mAdapter;
    SweetAlertDialog sweetAlertDialog;

    FragmentProfileBinding binding;
    User user;
    SweetAlertDialog pDialog;
    JSONObject jsonObject;

    ChartProgressBar mChart;
    ContextCompat contextCompat;

    Map<String, String> monthMap = new HashMap<String, String>(){{put("Jan","01");put("Feb","2"); put("Mar","3"); put("Apr","4");put("May","5");put("Jun","6");
        put("Jul","7");put("Aug","8"); put("Sep","9"); put("Oct","10");put("Nov","11");put("Dec","12");
    }};

    private List<UploadDetails> dataList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize(view);
    }

    private void initialize(View view)
    {

        user = User.getInstance();

        if(user == null)
        {
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE);
            sweetAlertDialog.setTitle("No user found please login");
            sweetAlertDialog.setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                    getActivity().finish();
                }
            });
        }
        binding.textName.setText(user.getName());
        binding.txtTeam.setText("Team: "+user.getTeamName());


        recyclerView = view.findViewById(R.id.recycler_view);
        mAdapter = new DataAdapter(dataList);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

       // getStatus();
        //getList("Dec");
        createChart();


        PercentageChartView todayChart = binding.todayChart;
        todayChart.setProgress((float) (40/50.0)*100,true);
        todayChart.apply();

        PercentageChartView totalChart = binding.totalChart;
        totalChart.setProgress((float) (400/1000.0)*100, true);
        totalChart.apply();
    }

    // data adapter class for showing the list
    public class DataAdapter extends RecyclerView.Adapter<DataAdapter.MyViewHolder> {

        private List<UploadDetails> dataList;

        public DataAdapter(List<UploadDetails> dataList) {
            this.dataList = dataList;
        }

        @NonNull
        @Override
        public DataAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.upload_details_row, parent, false);
            return new DataAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final UploadDetails data = dataList.get(position);

            holder.date.setText(data.getDate());
            holder.time.setText(data.getTime());

        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView date,time;
            ConstraintLayout rowLayout;
            public MyViewHolder(View convertView) {
                super(convertView);
                rowLayout = convertView.findViewById(R.id.rowLayout);
                date = convertView.findViewById(R.id.date);
                time = convertView.findViewById(R.id.time);
            }
        }
    }



    public void getStatus() {
        sweetAlertDialog = new SweetAlertDialog(requireContext(), 5);
        sweetAlertDialog.setTitleText("Loading");
        sweetAlertDialog.show();
        MySingleton.getInstance(requireContext()).addToRequestQue(new StringRequest(1, "https://routes.atmdbd.com/api/consumer/user_status.php", new Response.Listener<String>() {
            public void onResponse(String response) {
                try {
                    sweetAlertDialog.dismiss();
                    Log.e("response", response);
                    jsonObject = new JSONObject(response);
                    String code = jsonObject.getString("success");
                    if (code.equals("true")) {
                        binding.txtTodayCount.setText("Today Count: "+jsonObject.getString("todayCount"));
                        binding.txtTotalCount.setText("Total Count: "+jsonObject.getString("totalCount"));
                        return;
                    }
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

    private void createChart() {
        ArrayList<BarData> dataList = new ArrayList<>();

        BarData data = new BarData("Jul", 1.8f, "10");
        dataList.add(data);

        data = new BarData("Aug", 50.8f, "330");
        dataList.add(data);

        data = new BarData("Sep", 81.8f, "500");
        dataList.add(data);

        data = new BarData("Oct", 40.8f, "310");
        dataList.add(data);

        data = new BarData("Nov", 90.8f, "770");
        dataList.add(data);

        data = new BarData("Dec", 71.8f, "610");
        dataList.add(data);

        mChart = (ChartProgressBar) binding.ChartProgressBar;
        mChart.setDataList(dataList);
        mChart.build();

        //mChart.selectBar(mChart.getData().size()-1);

        mChart.setOnBarClickedListener(new OnBarClickedListener() {
            @Override
            public void onBarClicked(int index) {
                Log.d("bar_selected", String.valueOf(mChart.getData(index).getBarTitle()));
                getList(mChart.getData(index).getBarTitle());
            }
        });


//        Toast.makeText(getApplicationContext(), "date : "+sdf.format(myCalendar.getTime()), Toast.LENGTH_SHORT).show();
    }
    private void getList(String month) {

        dataList.clear();

        /*
        sweetAlertDialog = new SweetAlertDialog(getContext(),SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setTitleText("Loading");
        sweetAlertDialog.show();

        String upLoadServerUri="https://sec.imslpro.com/api/android/get_sales_record.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, upLoadServerUri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            sweetAlertDialog.dismiss();
                            Log.e("response",response);
                            jsonObject = new JSONObject(response);
                            String code = jsonObject.getString("success");
                            String message = jsonObject.getString("message");
                            Integer quantity = 0, total = 0;
                            if (code.equals("true")) {
                                SalesRecordDataModel salesRecordDataModel;
                                jsonArray = jsonObject.getJSONArray("saleItemResult");
                                for (int i = 0; i< jsonArray.length(); i++)
                                {
                                    quantity += Integer.parseInt(jsonArray.getJSONObject(i).getString("quantity"));
                                    total += Integer.parseInt(jsonArray.getJSONObject(i).getString("billed_amount"));
                                    salesRecordDataModel = new SalesRecordDataModel(jsonArray.getJSONObject(i).getString("product_name")+" "+
                                            jsonArray.getJSONObject(i).getString("product_variation_name"),
                                            jsonArray.getJSONObject(i).getString("quantity"),jsonArray.getJSONObject(i).getString("billed_amount"),
                                            jsonArray.getJSONObject(i).getString("sale_date").substring(11,16)); //2020-08-17 20:03:39 creating only 20:03
                                    dataList.add(salesRecordDataModel);
                                }
                                salesRecordDataModel = new SalesRecordDataModel("Total("+ dataList.size()+")",
                                        String.valueOf(quantity),String.valueOf(total), "");
                                dataList.add(salesRecordDataModel);
                                mAdapter.notifyDataSetChanged();
                            }
                            else{
                                Log.e("mess",message);
                                CustomUtility.showError(SalesRecordActivity.this,message,"Failed");
                                return;
                            }

                        } catch (JSONException e) {
                            CustomUtility.showError(SalesRecordActivity.this,
                                    e.getMessage() +". Failed to parse data",
                                    "Response");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                sweetAlertDialog.dismiss();
                CustomUtility.showAlert(SalesRecordActivity.this, "Try again!!", "Network Erorr");
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("UserId",sharedPreferences.getString("id",null));
                //params.put("UserId","178");
                params.put("SaleDateStart",fromDate);
                params.put("SaleDateEnd",toDate);
                return params;
            }
        };

        MySingleton.getInstance(SalesRecordActivity.this).addToRequestQue(stringRequest);
        */
        for(int i = 1; i<30; i++)
        {
            dataList.add(new UploadDetails(String.valueOf(i)+"-"+monthMap.get(month)+"-2020","11.30am"));
        }
        mAdapter.notifyDataSetChanged();
    }

}