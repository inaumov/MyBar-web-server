package mybar.web.rest.users.exception;

import lombok.extern.slf4j.Slf4j;
import mybar.exception.users.EmailDuplicatedException;
import mybar.exception.users.UnknownUserException;
import mybar.exception.users.UserExistsException;
import mybar.web.rest.bar.exception.ErrorInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Locale;

@Slf4j
@ControllerAdvice(basePackages = "mybar.web.rest.users")
public class UsersRestExceptionProcessor {

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(EmailDuplicatedException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorInfo duplicatedEmail(HttpServletRequest req, EmailDuplicatedException ex) {
        log.error("Duplicated email thrown", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("error.duplicated.email", null, locale);
        errorMessage = MessageFormat.format(errorMessage, ex.getEmail());
        String errorURL = req.getRequestURL().toString();

        return new ErrorInfo(errorURL, errorMessage);
    }

    @ExceptionHandler(UserExistsException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorInfo userExists(HttpServletRequest req, UserExistsException ex) {
        log.error("User exists thrown", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("error.user.exists", null, locale);
        errorMessage = MessageFormat.format(errorMessage, ex.getUsername());
        String errorURL = req.getRequestURL().toString();

        return new ErrorInfo(errorURL, errorMessage);
    }

    @ExceptionHandler(UnknownUserException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorInfo userUnknown(HttpServletRequest req, UnknownUserException ex) {
        log.error("User unknown thrown", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("error.unknown.user", null, locale);
        errorMessage = MessageFormat.format(errorMessage, ex.getUsername());
        String errorURL = req.getRequestURL().toString();

        return new ErrorInfo(errorURL, errorMessage);
    }

    @ExceptionHandler(PasswordConfirmationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorInfo pwdConfirmation(HttpServletRequest req, PasswordConfirmationException ex) {
        log.error("Password confirmation thrown", ex);

        Locale locale = LocaleContextHolder.getLocale();
        String errorMessage = messageSource.getMessage("error.pwd.confirmation", null, locale);
        String errorURL = req.getRequestURL().toString();

        return new ErrorInfo(errorURL, errorMessage);
    }

}