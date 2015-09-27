package mybar.api.bar;

import mybar.State;

import java.sql.Blob;
import java.util.Collection;

public interface ICocktail {

    int getId();

    String getName();

    Collection<? extends IInside> getIngredients();

    <T extends IMenu> T getMenu();

    String getDescription();

    State getState();

    String getCover();

    Blob getPicture();

    double getPrice();

}