package com.tokenbrowser.presenter;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tokenbrowser.model.local.Library;
import com.tokenbrowser.R;
import com.tokenbrowser.view.activity.LicenseActivity;
import com.tokenbrowser.view.activity.LicenseListActivity;
import com.tokenbrowser.view.adapter.LibraryAdapter;
import com.tokenbrowser.view.custom.HorizontalLineDivider;

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
        final Library ethereumj = new Library()
                .setName("Ethereumj")
                .setLicence(this.activity.getString(R.string.mit_license));
        final Library spongycastle = new Library()
                .setName("Spongycastle")
                .setLicence(this.activity.getString(R.string.mit_license));

        final Library signal = new Library()
                .setName("Signal")
                .setLicence(this.activity.getString(R.string.gplv3_license));

        final Library glide = new Library()
                .setName("Glide")
                .setLicence(this.activity.getString(R.string.glide_license));

        final Library recyclerView = new Library()
                .setName("Android Support Library RecyclerView")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library gridLayout = new Library()
                .setName("Android Support Library GridLayout")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library appCompat = new Library()
                .setName("Android Support Library Appcompat")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library design = new Library()
                .setName("Android Support Library Design")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library multidex = new Library()
                .setName("Android Support Library Multidex")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library rxjavaProguardRules = new Library()
                .setName("Rxjava Proguard Rules")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library rxAndroid = new Library()
                .setName("RxAndroid")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library rxBinding = new Library()
                .setName("RxBinding")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library ahbottomnavigation = new Library()
                .setName("Ahbottomnavigation")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library circleImageview = new Library()
                .setName("CircleImageview")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library zxingAndroidEmbedded = new Library()
                .setName("ZxingAndroidEmbedded")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library bitcoinjCore = new Library()
                .setName("Bitcoinj-core")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library whisperSystemsLibsignalService = new Library()
                .setName("WhisperSystems Libsignal-service")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library chipsLayoutManager = new Library()
                .setName("ChipsLayoutManager")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library googlePlayServicesGcm = new Library()
                .setName("Google Cloud Messaging Play Services")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library retrofit = new Library()
                .setName("Retrofit")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library retrofitMoshi = new Library()
                .setName("Retrofit Converter Moshi")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library retrofitRxJava = new Library()
                .setName("Retrofit Adapter Rxjava")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library retrofitScalars = new Library()
                .setName("Retrofit Converter Scalars")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library okHttp = new Library()
                .setName("OkHttp 3")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final Library okHttpInterceptor = new Library()
                .setName("Okhttp3 Logging interceptor")
                .setLicence(this.activity.getString(R.string.apache_v2_license));
        final List<Library> libraries = new ArrayList<>();

        libraries.add(retrofit);
        libraries.add(retrofitMoshi);
        libraries.add(retrofitRxJava);
        libraries.add(retrofitScalars);
        libraries.add(okHttp);
        libraries.add(okHttpInterceptor);
        libraries.add(ethereumj);
        libraries.add(spongycastle);
        libraries.add(signal);
        libraries.add(glide);
        libraries.add(recyclerView);
        libraries.add(gridLayout);
        libraries.add(appCompat);
        libraries.add(design);
        libraries.add(multidex);
        libraries.add(rxjavaProguardRules);
        libraries.add(rxAndroid);
        libraries.add(rxBinding);
        libraries.add(ahbottomnavigation);
        libraries.add(circleImageview);
        libraries.add(zxingAndroidEmbedded);
        libraries.add(retrofitMoshi);
        libraries.add(bitcoinjCore);
        libraries.add(whisperSystemsLibsignalService);
        libraries.add(chipsLayoutManager);
        libraries.add(googlePlayServicesGcm);

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
    public void onDestroyed() {}
}
