package com.steppschuh.estirator;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class FragmentEstimationResults extends Fragment {

    MobileApp app;

    View contentFragment;
    ListView estimatedItemsList;
    ListAdapter estimatedItemsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contentFragment = inflater.inflate(R.layout.fragment_estimation_results, container, false);

        app = (MobileApp) getActivity().getApplicationContext();
        getActivity().setTitle(getString(R.string.title_ranking));

        setupUi();

        return contentFragment;
    }

    private void setupUi() {
        estimatedItemsList = (ListView) contentFragment.findViewById(R.id.estimatedItemsList);

        ArrayList<EbayItem> estimatedItems = new ArrayList<>();
        for (EbayItem item : app.getItems()) {
            if (item.hasEstimatedPrice() && !item.itemSkipped()) {
                estimatedItems.add(item);
            }
        }

        estimatedItemsAdapter = new EstimationResultsAdapter(getActivity(), R.layout.estimation_result_item, estimatedItems);

        estimatedItemsList.setAdapter(estimatedItemsAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
