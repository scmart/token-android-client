package com.bakkenbaeck.token.presenter.factory;


import com.bakkenbaeck.token.presenter.VideoPresenter;

public class VideoPresenterFactory implements PresenterFactory<VideoPresenter> {

    @Override
    public VideoPresenter create() {
        return new VideoPresenter();
    }
}
