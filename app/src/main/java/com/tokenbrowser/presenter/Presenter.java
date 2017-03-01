package com.tokenbrowser.presenter;

public interface Presenter<V>{
    void onViewAttached(V view);
    void onViewDetached();
    void onViewDestroyed();
}
