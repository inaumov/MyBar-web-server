package mybar.app.bean.bar;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.Setter;
import mybar.api.bar.IBottle;
import mybar.app.bean.bar.ingredient.BeverageBean;

@Getter
@Setter
public class BottleBean implements IBottle {

    @JsonView(View.Shelf.class)
    private int id;

    @JsonView(View.Shelf.class)
    @JsonProperty("ingredient")
    private BeverageBean beverage;

    @JsonView(View.Shelf.class)
    private String brandName;

    @JsonView(View.Shelf.class)
    private double volume;

    @JsonView(View.Shelf.class)
    private double price;

    @JsonView(View.Shelf.class)
    private InShelf inShelf = InShelf.NO;

    @JsonView(View.Shelf.class)
    private String imageUrl;

    @JsonIgnore
    @Override
    public boolean isInShelf() {
        return InShelf.YES == inShelf;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("beverage", beverage)
                .add("brandName", brandName)
                .add("volume", volume)
                .add("price", price)
                .toString();
    }

}