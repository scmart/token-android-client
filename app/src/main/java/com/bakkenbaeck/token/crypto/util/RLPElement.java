package com.bakkenbaeck.token.crypto.util;


import java.io.Serializable;

public interface RLPElement extends Serializable {

    byte[] getRLPData();
}
