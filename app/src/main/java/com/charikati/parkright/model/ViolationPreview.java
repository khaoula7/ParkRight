package com.charikati.parkright.model;

public class ViolationPreview {
    /** the violation type */
    private String mViolationType;
    /** Image resource ID for the violation */
    private final int mImageResourceId;

    /**
     * Create a new Violation object.
     * @param violationType is the violation type
     * @param imageResourceId is the drawable resource ID for the image associated with the violation
     */
    public ViolationPreview(String violationType, int imageResourceId){
        mViolationType = violationType;
        mImageResourceId = imageResourceId;
    }

    /**
     * Get the string resource ID for the violation type
     */
    public String getViolationType() { return mViolationType; }

    /**
     * Get the string resource ID for the violation type
     */
    public int getImageResourceId() {
        return mImageResourceId;
    }
}
