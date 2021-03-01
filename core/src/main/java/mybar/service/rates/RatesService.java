package mybar.service.rates;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import mybar.api.rates.IRate;
import mybar.domain.bar.Cocktail;
import mybar.domain.rates.Rate;
import mybar.domain.users.User;
import mybar.dto.RateDto;
import mybar.exception.CocktailNotFoundException;
import mybar.messaging.IMessageProducer;
import mybar.repository.bar.CocktailDao;
import mybar.repository.rates.RatesDao;
import mybar.repository.users.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
@Service
public class RatesService {

    private final RatesDao ratesDao;

    private final UserDao userDao;

    private final CocktailDao cocktailDao;

    private static final Range<Integer> starsRange = new Range<>(1, 10);

    private final IMessageProducer messageProducer;

    private final Gson gson = new Gson();

    @Autowired
    public RatesService(IMessageProducer messageProducer, RatesDao ratesDao, UserDao userDao, CocktailDao cocktailDao) {
        this.messageProducer = messageProducer;
        this.ratesDao = ratesDao;
        this.userDao = userDao;
        this.cocktailDao = cocktailDao;
    }

    public IRate rateCocktail(String username, String cocktailId, Integer stars) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(username), "Username is required.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(cocktailId), "Cocktail id is required.");
        Preconditions.checkArgument(stars != null && starsRange.contains(stars), "Stars number should be from 1 to 10.");
        checkCocktailExists(cocktailId);

        RateDto rateDto = RateDto.ofStars(stars);
        Long send = messageProducer.send(toCacheKey(username, cocktailId), gson.toJson(rateDto));
        rateDto.setCocktailId(cocktailId);
        rateDto.setRatedAt(new Date(send));
        return rateDto;
    }

    public void removeCocktailFromRates(String userId, String cocktailId) {
        Rate rate = ratesDao.findBy(userId, cocktailId);
        ratesDao.delete(rate);
    }

    private String toCacheKey(String userId, String cocktailId) {
        return userId + "@" + cocktailId;
    }

    public Collection<IRate> getRatedCocktails(String userId) {
        List<IRate> userRates = new ArrayList<>();
        User user = userDao.getOne(userId);
        List<Rate> allRatesForUser = ratesDao.findAllRatesForUser(user);
        for (Rate rate : allRatesForUser) {
            RateDto rateDto = new RateDto();
            rateDto.setCocktailId(rate.getCocktail().getId());
            rateDto.setRatedAt(rate.getRatedAt());
            rateDto.setStars(rate.getStars());
            userRates.add(rateDto);
        }
        return Collections.unmodifiableCollection(userRates);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void persistRates(String cacheKey, long timestamp, String object) {
        Gson gson = new Gson();
        IRate rateDto = gson.fromJson(object, RateDto.class);
        String[] strings = StringUtils.split(cacheKey, "@");
        String userId = strings[0];
        User user = userDao.getOne(userId);
        String cocktailId = strings[1];
        Cocktail cocktail = cocktailDao.read(cocktailId);
        if (user != null && cocktail != null) {
            Rate rate = new Rate();
            rate.setCocktail(cocktail);
            rate.setStars(rateDto.getStars());
            rate.setRatedAt(new Date(timestamp));
            rate.setUser(user);
            ratesDao.update(rate);
        } else {
            log.error("Could not persist rate for [{}]. It is either user or cocktail is unknown.", cacheKey);
        }
    }

    private void checkCocktailExists(String cocktailId) {
        Cocktail cocktail = cocktailDao.read(cocktailId);
        if (cocktail == null) {
            throw new CocktailNotFoundException(cocktailId);
        }
    }

    public Map<String, Double> findAllAverageRates() {
        return ratesDao.findAllAverageRates();
    }

}