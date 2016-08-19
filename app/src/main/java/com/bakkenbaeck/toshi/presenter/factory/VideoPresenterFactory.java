package com.bakkenbaeck.toshi.presenter.factory;


import com.bakkenbaeck.toshi.presenter.VideoPresenter;

public class VideoPresenterFactory implements PresenterFactory<VideoPresenter> {

    @Override
    public VideoPresenter create() {
        return new VideoPresenter();
    }
}
