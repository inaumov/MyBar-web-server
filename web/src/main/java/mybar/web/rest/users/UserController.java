package mybar.web.rest.users;

import com.fasterxml.jackson.annotation.JsonView;
import mybar.api.users.IUser;
import mybar.api.users.IUserDetails;
import mybar.app.bean.users.BeanFactory;
import mybar.app.bean.users.RegisterUserBean;
import mybar.app.bean.users.UserBean;
import mybar.app.bean.users.UserDetailsBean;
import mybar.app.bean.users.UserList;
import mybar.app.bean.users.View;
import mybar.service.users.UserService;
import mybar.web.rest.users.exception.PasswordConfirmationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @JsonView(View.UserView.class)
    @RequestMapping(method = RequestMethod.GET, value = "/{username}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UserDetailsBean getUser(@PathVariable("username") String username) {
        IUserDetails user = userService.findByUsername(username);
        return BeanFactory.fromDetails(user);
    }

    @JsonView(View.UserView.class)
    @RequestMapping(method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UserDetailsBean updateUser(@RequestBody UserBean userBean) {
        IUserDetails user = userService.editUserInfo(userBean);
        return BeanFactory.fromDetails(user);
    }

    @JsonView(View.UserView.class)
    @RequestMapping(value = "/register", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UserDetailsBean register(@RequestHeader HttpHeaders headers,
                                    @RequestBody RegisterUserBean registerUserBean) {
        validatePasswordConfirmation(registerUserBean);

        String pwd = new String(Base64.getDecoder().decode(registerUserBean.getPassword()),
                Charset.forName("UTF-8"));
        registerUserBean.setPassword(passwordEncoder.encode(pwd));

        IUserDetails user = userService.createUser(registerUserBean);
        return BeanFactory.fromDetails(user);
    }

    private void validatePasswordConfirmation(RegisterUserBean user) {
        if (user.getPasswordConfirm() == null || !Objects.equals(user.getPassword(), user.getPasswordConfirm())) {
            throw new PasswordConfirmationException();
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable String username) {
        userService.deactivateUser(username);
    }

    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<MappingJacksonValue> getUsers() {
        List<IUser> users = userService.getAllRegisteredUsers();

        UserList userList = new UserList(BeanFactory.toFullUserList(users));

        MappingJacksonValue wrapper = new MappingJacksonValue(userList);
        wrapper.setSerializationView(View.AdminView.class);

        return new ResponseEntity<>(wrapper, HttpStatus.OK);
    }

    private String[] getAutHeader(HttpHeaders headers) {

        final String authorization = headers.getFirst("Authorization");

        if (authorization != null && authorization.startsWith("Basic")) {
            // Authorization: Basic base64credentials
            String base64Credentials = authorization.substring("Basic".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials),
                    Charset.forName("UTF-8"));
            // credentials = username:password
            return credentials.split(":", 2);
        }

        return null;
    }

}