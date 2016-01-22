package mybar.dto.bar;

import mybar.UnitsValue;
import mybar.api.bar.IInside;

public class InsideDto implements IInside {

    private int ingredientId;
    private double volume;
    private UnitsValue unitsValue;
    private boolean missing;

    @Override
    public int getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    @Override
    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    @Override
    public UnitsValue getUnitsValue() {
        return unitsValue;
    }

    public void setUnitsValue(UnitsValue unitsValue) {
        this.unitsValue = unitsValue;
    }

    @Override
    public boolean isMissing() {
        return missing;
    }

    public void setMissing(boolean missing) {
        this.missing = missing;
    }

}