package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.view.activity.LicenseListActivity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.local.Library;
import com.bakkenbaeck.token.view.activity.LicenseActivity;
import com.bakkenbaeck.token.view.adapter.LibraryAdapter;
import com.bakkenbaeck.token.view.custom.HorizontalLineDivider;

import java.util.ArrayList;
import java.util.List;

public class LicenseListPresenter implements Presenter<LicenseListActivity> {
    
    private LicenseListActivity activity;
    
    @Override
    public void onViewAttached(LicenseListActivity view) {
        this.activity = view;
        initRecycleView();
        addLibraries();
        initClickListeners();
    }

    private void initRecycleView() {
        final RecyclerView libraryList = this.activity.getBinding().libraryList;
        libraryList.setLayoutManager(new LinearLayoutManager(this.activity));
        libraryList.addItemDecoration(new HorizontalLineDivider(ContextCompat.getColor(this.activity, R.color.divider_amount)));
        final LibraryAdapter adapter = new LibraryAdapter(new ArrayList<>());
        libraryList.setAdapter(adapter);
        adapter.setOnItemClickListener(this::handleItemClicked);
    }

    private void handleItemClicked(final Library library) {
        final Intent intent = new Intent(this.activity, LicenseActivity.class);
        intent.putExtra(LicenseActivity.LIBRARY, library);
        this.activity.startActivity(intent);
    }

    private void addLibraries() {
        final Library retrofit = new Library()
                .setName("Retrofit 2")
                .setLicence("License");
        final Library okHttp = new Library()
                .setName("OkHttp 3")
                .setLicence("License");
        final Library rxJava = new Library()
                .setName("RxJava")
                .setLicence("License");

        final List<Library> libraries = new ArrayList<>();
        libraries.add(retrofit);
        libraries.add(okHttp);
        libraries.add(rxJava);

        final LibraryAdapter adapter = (LibraryAdapter) this.activity.getBinding().libraryList.getAdapter();
        adapter.setLibraries(libraries);
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(this::handleCloseButtonClicked);
    }

    private void handleCloseButtonClicked(final View v) {
        this.activity.finish();
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        this.activity = null;
    }
}
