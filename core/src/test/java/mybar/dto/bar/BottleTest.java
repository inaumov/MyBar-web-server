package mybar.dto.bar;

import mybar.domain.bar.Bottle;
import mybar.domain.bar.ingredient.Beverage;
import mybar.dto.DtoFactory;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class BottleTest {

    public static final String TEST_REFERENCE = "cocktail-123";
    public static final String BRAND_NAME = "brand name";
    public static final BigDecimal PRICE = new BigDecimal(195.00);
    public static final double VOLUME = 1;
    public static final String IMAGE_URL = "http://bottle-image.jpg";

    @Test
    public void testConvertBottleToDto() throws Exception {
        Bottle bottleEntity = new Bottle();
        bottleEntity.setId(TEST_REFERENCE);
        Beverage beverageEntity = new Beverage();
        beverageEntity.setId(1);
        bottleEntity.setBeverage(beverageEntity);
        bottleEntity.setBrandName(BRAND_NAME);
        bottleEntity.setInShelf(true);
        bottleEntity.setPrice(PRICE);
        bottleEntity.setVolume(VOLUME);
        bottleEntity.setImageUrl(IMAGE_URL);

        BottleDto dto = DtoFactory.toDto(bottleEntity);
        assertEquals(TEST_REFERENCE, dto.getId());
        assertEquals(1, dto.getBeverage().getId().intValue());
        assertEquals(BRAND_NAME, dto.getBrandName());
        assertTrue(dto.isInShelf());
        assertThat(PRICE,  Matchers.comparesEqualTo(dto.getPrice()));
        assertEquals(VOLUME, dto.getVolume());
        assertEquals(IMAGE_URL, dto.getImageUrl());
    }

}