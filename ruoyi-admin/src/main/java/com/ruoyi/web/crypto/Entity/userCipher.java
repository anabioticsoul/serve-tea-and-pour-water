package com.ruoyi.web.crypto.Entity;

public class userCipher {
    private String userId;
    private String userPrivateKey;
    private String blindedCertificateSignature;

    public userCipher(String userId, String userPrivateKey, String blindedCertificateSignature) {
        super();
        this.userId = userId;
        this.userPrivateKey = userPrivateKey;
        this.blindedCertificateSignature = blindedCertificateSignature;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPrivateKey() {
        return userPrivateKey;
    }

    public void setUserPrivateKey(String userPrivateKey) {
        this.userPrivateKey = userPrivateKey;
    }

    public String getBlindedCertificateSignature() {
        return blindedCertificateSignature;
    }

    public void setBlindedCertificateSignature(String blindedCertificateSignature) {
        this.blindedCertificateSignature = blindedCertificateSignature;
    }

    @Override
    public String toString() {
        return "userId: " + userId + "\n"
                + "userPrivateKey: " + userPrivateKey + "\n"
                + "blindedCertificateSignature: " + blindedCertificateSignature + "\n";
    }
}
