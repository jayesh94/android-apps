package app.mugup.mugup.helper;

public class ProfileList {
    public int image;
    public String name;
    public String credit;

    public ProfileList(int image, String name, String credit) {
        super();
        this.image = image;
        this.name = name;
        this.credit = credit;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }
}
