package com.example.metis.data_types;

public interface GamesObservable {
    void registerObserver();
    void unregisterObserver();
    void run();
}
