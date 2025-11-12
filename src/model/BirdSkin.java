package model;

public class BirdSkin {
    private String name;
    private String imagePath;
    private int price;
    private boolean unlocked;

    public BirdSkin(String name, String imagePath, int price) {
        this.name = name;
        this.imagePath = imagePath;
        this.price = price;
        this.unlocked = false;
    }

    public String getName() {
        return name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getPrice() {
        return price;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void unlock() {
        this.unlocked = true;
    }
}