package com.example.routes.ui.logout;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.routes.R;
import com.example.routes.user.User;

import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.content.Context.MODE_PRIVATE;

public class LogoutFragment extends Fragment {

    private LogoutViewModel logoutViewModel;
    User user;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        logoutViewModel =
                new ViewModelProvider(this).get(LogoutViewModel.class);
        View root = inflater.inflate(R.layout.fragment_logout, container, false);
        logoutViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user = User.getInstance();
        final SweetAlertDialog log = new SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE);
        log.setTitleText("Are you sure to Sign Out?");
        log.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("user",MODE_PRIVATE).edit();
                //editor.clear();
                //editor.apply();
                user.clear(editor);
                log.dismissWithAnimation();
                getActivity().finish();
            }
        });
        log.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {

            }
        });
        log.setCancelText("No");
        log.setConfirmText("Ok");
        log.show();

    }
}