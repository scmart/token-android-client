package com.bakkenbaeck.token.presenter;

public interface Presenter<V>{
    void onViewAttached(V view);
    void onViewDetached();
    void onViewDestroyed();
}
