package mybar.web.rest.bar;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import mybar.api.bar.ICocktail;
import mybar.api.bar.IMenu;
import mybar.app.RestBeanConverter;
import mybar.app.bean.bar.CocktailBean;
import mybar.app.bean.bar.MenuBean;
import mybar.app.bean.bar.View;
import mybar.service.bar.CocktailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.MessageFormat;
import java.util.*;

@RestController
@RequestMapping("/cocktails")
public class CocktailsController {

    private Logger logger = LoggerFactory.getLogger(CocktailsController.class);

    @Autowired
    private CocktailsService cocktailsService;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private AvailableCocktailsWrapper cocktailsWrapper;

    public static Function<ICocktail, CocktailBean> toCocktailBeanFunction = new Function<ICocktail, CocktailBean>() {
        @Override
        public CocktailBean apply(ICocktail cocktail) {
            return RestBeanConverter.toCocktailBean(cocktail);
        }
    };

    //-------------------Retrieve Menu List--------------------------------------------------------

    @JsonView(View.Menu.class)
    @RequestMapping(value = "/menu", method = RequestMethod.GET)
    public ResponseEntity<List<MenuBean>> listAllMenuItems() {
        logger.info("Fetching menu items list...");

        List<IMenu> menuList = cocktailsService.getAllMenuItems();
        if (menuList.isEmpty()) {
            logger.info("Menu list is empty.");
            return new ResponseEntity<>(Collections.<MenuBean>emptyList(), HttpStatus.OK);
        }
        List<MenuBean> convertedList = new ArrayList<>();
        Locale locale = LocaleContextHolder.getLocale();
        for (IMenu menu : menuList) {
            MenuBean from = RestBeanConverter.toMenuBean(menu);
            from.setTranslation(messageSource.getMessage(menu.getName(), null, locale));
            convertedList.add(from);
        }
        logger.info(MessageFormat.format("Found {0} items in menu list.", menuList.size()));
        return new ResponseEntity<>(convertedList, HttpStatus.OK);
    }

    //-------------------Retrieve All Cocktails For Menu--------------------------------------------------------

    private ResponseEntity<List<CocktailBean>> findCocktailsForMenu(String menuName) {
        logger.info("Fetching cocktails for menu [{0}]...", menuName);
        List<ICocktail> cocktails = cocktailsService.getAllCocktailsForMenu(menuName);
        if (cocktails.isEmpty()) {
            logger.error(MessageFormat.format("Cocktails list for menu [{0}] does not exist", menuName));
            return new ResponseEntity<>(Collections.<CocktailBean>emptyList(), HttpStatus.OK);
        }

        List<CocktailBean> converted = toCocktailBeans(cocktails);
        cocktailsWrapper.updateWithState(converted);
        logger.info(MessageFormat.format("Found {0} cocktails for menu [{1}]", cocktails.size(), menuName));
        return new ResponseEntity<>(converted, HttpStatus.OK);
    }

    private static List<CocktailBean> toCocktailBeans(List<ICocktail> cocktails) {
        return FluentIterable
                .from(cocktails)
                .transform(toCocktailBeanFunction)
                .toList();
    }

    //-------------------Retrieve All Cocktails--------------------------------------------------------

    @JsonView(View.Cocktail.class)
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity allCocktails(@RequestParam(value = "filter", required = false) String menuNameParam) {
        if (!Strings.isNullOrEmpty(menuNameParam)) {
            return findCocktailsForMenu(menuNameParam);
        }
        Map<String, List<ICocktail>> cocktails = cocktailsService.getAllCocktails();
        if (cocktails.isEmpty()) {
            logger.info("Cocktail list is empty.");
            return new ResponseEntity<>(Collections.<String, List<CocktailBean>>emptyMap(), HttpStatus.OK);
        }
        Map<String, List<CocktailBean>> converted = Maps.newHashMap();
        for (String menuName : cocktails.keySet()) {
            converted.put(menuName, toCocktailBeans(cocktails.get(menuName)));
        }
        cocktailsWrapper.get(converted);
        for (String menuCode : converted.keySet()) {
            logger.info(MessageFormat.format("Found {0} cocktails for menu [{1}]", converted.get(menuCode).size(), menuCode));
        }
        return new ResponseEntity<>(converted, HttpStatus.OK);
    }

    //-------------------Retrieve a cocktail with details--------------------------------------------------------

    @JsonView(View.CocktailWithDetails.class)
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<CocktailBean> getCocktail(@PathVariable("id") Integer id) {
        logger.info("Fetching cocktail with id " + id);

        ICocktail cocktail = cocktailsService.findCocktailById(id);
        return new ResponseEntity<>(RestBeanConverter.toCocktailBean(cocktail), HttpStatus.OK);
    }

    //-------------------Create a Cocktail--------------------------------------------------------

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<CocktailBean> addCocktail(@RequestBody CocktailBean cocktailBean, UriComponentsBuilder ucBuilder) {
        logger.info("Creating a new cocktail item " + cocktailBean);

        ICocktail saved = cocktailsService.saveCocktail(cocktailBean);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/cocktails/{id}").buildAndExpand(cocktailBean.getId()).toUri());
        return new ResponseEntity<>(RestBeanConverter.toCocktailBean(saved), headers, HttpStatus.CREATED);
    }

    //------------------- Update a Cocktail --------------------------------------------------------

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<CocktailBean> updateCocktail(@RequestBody CocktailBean cocktailBean) {
        logger.info("Updating a cocktail " + cocktailBean);

        ICocktail updated = cocktailsService.updateCocktail(cocktailBean);
        return new ResponseEntity<>(RestBeanConverter.toCocktailBean(updated), HttpStatus.OK);
    }

    //------------------- Delete a Cocktail --------------------------------------------------------

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<CocktailBean> deleteCocktail(@PathVariable("id") int id) {
        logger.info("Deleting a cocktail with id " + id);

        cocktailsService.deleteCocktailById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}