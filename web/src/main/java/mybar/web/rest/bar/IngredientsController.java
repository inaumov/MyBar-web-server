package mybar.web.rest.bar;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import mybar.api.bar.ingredient.IAdditive;
import mybar.api.bar.ingredient.IBeverage;
import mybar.api.bar.ingredient.IDrink;
import mybar.api.bar.ingredient.IIngredient;
import mybar.app.RestBeanConverter;
import mybar.service.bar.IngredientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(value = "/ingredients")
public class IngredientsController {

    private Logger logger = LoggerFactory.getLogger(IngredientsController.class);

    @Autowired
    private IngredientService ingredientService;

    //-------------------Retrieve Ingredients--------------------------------------------------------

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity listIngredients(
            @RequestParam(value = "filter", required = false) String groupNameParam) {

        List<IIngredient> ingredients;

        if (Strings.isNullOrEmpty(groupNameParam)) {
            ingredients = ingredientService.findAll();
        } else {
            ingredients = ingredientService.findByGroupName(groupNameParam);
        }

        if (ingredients.isEmpty()) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }

        Iterable<IBeverage> beverages = Iterables.filter(ingredients, IBeverage.class);
        Iterable<IDrink> drinks = Iterables.filter(ingredients, IDrink.class);
        Iterable<IAdditive> additives = Iterables.filter(ingredients, IAdditive.class);

        ImmutableMap.Builder<String, Iterable<IIngredient>> builder = ImmutableMap.builder();
        putIfPresent(builder, IBeverage.GROUP_NAME, beverages);
        putIfPresent(builder, IDrink.GROUP_NAME, drinks);
        putIfPresent(builder, IAdditive.GROUP_NAME, additives);

        ImmutableMap<String, Iterable<IIngredient>> responseMap = builder.build();
        if (responseMap.size() == 1) {
            return new ResponseEntity<>(responseMap.get(groupNameParam), HttpStatus.OK);
        }
        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    }

    private void putIfPresent(ImmutableMap.Builder<String, Iterable<IIngredient>> builder, String groupName, Iterable<? extends IIngredient> filtered) {
        if (!filtered.iterator().hasNext()) {
            return;
        }
        builder.put(groupName, Lists.newArrayList(Iterables.transform(filtered, ingredientFunction())));
    }

    private static <DTO, BEAN extends IIngredient> Function<DTO, BEAN> ingredientFunction() {
        return new Function<DTO, BEAN>() {

            @Override
            public BEAN apply(DTO input) {
                IIngredient from = null;
                if (input instanceof IAdditive) {
                    from = RestBeanConverter.from((IAdditive) input);
                } else if (input instanceof IDrink) {
                    from = RestBeanConverter.from((IDrink) input);
                } else if (input instanceof IBeverage) {
                    from = RestBeanConverter.from((IBeverage) input);
                }
                return uncheckedCast(from);
            }

            @SuppressWarnings({"unchecked"})
            private BEAN uncheckedCast(Object obj) {
                return (BEAN) obj;
            }
        };
    }

}