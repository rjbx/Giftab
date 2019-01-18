package com.github.rjbx.givetrack.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.rjbx.givetrack.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;

public class DonorFragment extends Fragment {

    public DonorFragment() {
        super();
    }

    static DonorFragment newInstance(Bundle args) {

        DonorFragment donorFragment = new DonorFragment();
        if (args != null) donorFragment.setArguments(args);
        return donorFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_donor, null);
        ButterKnife.bind(this, rootView);

        return rootView;
    }
}