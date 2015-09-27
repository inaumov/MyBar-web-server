package mybar.domain.users;

import mybar.api.users.IRole;
import mybar.api.users.IUser;

import java.util.Collection;

public class UmEntityFactory {

    public static final Role from(final IRole role) {
        Role entity = new Role();
        entity.setId(role.getId());
        entity.setWebRole(role.getWebRole());
        return entity;
    }

    public static final User from (final IUser user) {
        User entity = new User();
        entity.setId(user.getId());
        entity.setLogin(user.getLogin());
        entity.setPassword(user.getPassword());
        entity.setAddress(user.getAddress());
        entity.setEmail(user.getEmail());
        entity.setName(user.getName());
        entity.setSurname(user.getSurname());
        Collection<? extends IRole> roles = user.getRoles();
        for(IRole r : roles) {
            entity.addRole(from(r));
        }
        entity.setState(user.getState());
        return entity;
    }

}