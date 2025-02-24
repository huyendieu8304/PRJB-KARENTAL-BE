package com.mp.karental.security.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Role;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDetailsImpl implements UserDetails {

    Account account;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsImpl build(Account account) {
        //create Authority for User
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().getName()));

        return new UserDetailsImpl(
                account,
                account.getPassword(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    //this actually return the email of the user
    @Override
    public String getUsername() {
        return account.getEmail();
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Role getRole(){
        return account.getRole();
    }

    public String getAccoutnId(){
        return account.getId();
    }

    public String getEmail(){
        return account.getEmail();
    }

}
