package com.example.trinity.utilities;

import androidx.annotation.NonNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import okhttp3.Dns;

public class CustomDns implements Dns {

    private ArrayList<String> serversHost = new ArrayList<>();

    @NonNull
    @Override
    public List<InetAddress> lookup(@NonNull String s) throws UnknownHostException {
        return Arrays.asList(InetAddress.getByName("8.8.8.8"),InetAddress.getByName("8.8.4.4"));
    }
}
