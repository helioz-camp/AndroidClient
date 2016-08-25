package com.helioz.androiddrumclient;

public class PlaybackRequestStateBuilder {
    private String clientToken = null;
    private String serverSequence = null;
    private String sampleName = null;

    public PlaybackRequestStateBuilder setClientToken(String clientToken) {
        this.clientToken = clientToken;
        return this;
    }

    public PlaybackRequestStateBuilder setServerSequence(String serverSequence) {
        this.serverSequence = serverSequence;
        return this;
    }

    public PlaybackRequestStateBuilder setSampleName(String sampleName) {
        this.sampleName = sampleName;
        return this;
    }

    public Audiomixclient.PlaybackRequestState build() {
        return new Audiomixclient.PlaybackRequestState(clientToken, serverSequence, sampleName);
    }
}