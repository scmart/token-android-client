package com.bakkenbaeck.toshi.crypto;


import java.io.Serializable;

public interface RLPElement extends Serializable {

    byte[] getRLPData();
}
