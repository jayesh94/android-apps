package app.mugup.mugup.helper;

public class SubjectCard
{
    private String mTextResource;
    private String mTitleResource;
    private String mTitleImage;
    private String mTitleId;
    private String mCourseId;

    public SubjectCard(String title, String text, String image, String id, String courseId) {
        mTitleResource = title;
        mTextResource = text;
        mTitleImage = image;
        mTitleId = id;
        mCourseId = courseId;
    }

    public String getText() {
        return mTextResource;
    }
    public String getTitle() {
        return mTitleResource;
    }
    public String getSubjectId() {
        return mTitleId;
    }
    public String getCourseId() {
        return mCourseId;
    }
    public String getImage() {
        return mTitleImage;
    }
}