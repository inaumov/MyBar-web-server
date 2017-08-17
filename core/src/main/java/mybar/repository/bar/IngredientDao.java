package mybar.repository.bar;

import mybar.api.bar.ingredient.IAdditive;
import mybar.api.bar.ingredient.IBeverage;
import mybar.api.bar.ingredient.IDrink;
import mybar.domain.bar.ingredient.Additive;
import mybar.domain.bar.ingredient.Beverage;
import mybar.domain.bar.ingredient.Drink;
import mybar.domain.bar.ingredient.Ingredient;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;

@Repository
public class IngredientDao {

    @PersistenceContext
    protected EntityManager em;

    /**
     * Find ingredients by one group name ordered by group name, kind.
     */
    public List<Ingredient> findByGroupName(String groupName) throws Exception {
        Class<? extends Ingredient> clazz;
        switch (groupName) {
            case IBeverage.GROUP_NAME: {
                clazz = Beverage.class;
                break;
            }
            case IDrink.GROUP_NAME: {
                clazz = Drink.class;
                break;
            }
            case IAdditive.GROUP_NAME: {
                clazz = Additive.class;
                break;
            }
            default: {
                return Collections.emptyList();
            }
        }
        TypedQuery<Ingredient> q = em.createNamedQuery("findByGroupName", Ingredient.class);
        q.setParameter("type", clazz);
        return q.getResultList();
    }

    /**
     * Find all ingredients ordered by group name, kind.
     */
    public List<Ingredient> findAll() {
        TypedQuery<Ingredient> q = em.createNamedQuery("findAll", Ingredient.class);
        return q.getResultList();
    }

    public Beverage findBeverageById(int id) {
        return em.find(Beverage.class, id);
    }

    public List<Ingredient> findIn(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        TypedQuery<Ingredient> q = em.createNamedQuery("findIn", Ingredient.class);
        q.setParameter("ids", ids);
        return q.getResultList();
    }
}