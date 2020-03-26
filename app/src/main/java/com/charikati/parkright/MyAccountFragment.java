package com.charikati.parkright;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class MyAccountFragment extends Fragment {

    public MyAccountFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_myaccount, container, false);
        //Change the Toolbar title
        TextView heading = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar_title);
        heading.setText(R.string.my_account);
        //Get the user name and e-mail
        TextView userName = v.findViewById(R.id.user_txt);
        TextView email = v.findViewById(R.id.email_txt);
        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        userName.setText(Objects.requireNonNull(mFireBaseAuth.getCurrentUser()).getDisplayName());
        email.setText(mFireBaseAuth.getCurrentUser().getEmail());
        return v;
    }
}
