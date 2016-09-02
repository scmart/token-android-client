package com.bakkenbaeck.toshi.crypto.util;


import java.io.Serializable;

public interface RLPElement extends Serializable {

    byte[] getRLPData();
}
