package com.wetness.model.service;

import com.wetness.db.entity.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private long id;
    private String email;
    private String password;
    private String nickname;
    private String role;
    private Collection<? extends GrantedAuthority> authorities;


    public UserDetailsImpl(long id, String email,String password, String nickname, String role) {

        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

    public static UserDetailsImpl build(User user){

        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getNickname(),
                user.getRole()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    public long id(){return this.id;}

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }


    public String getRole(){return this.role;}

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

    public String getEmail() {
        return this.email;
    }

    public String getNickname() {
        return this.nickname;
    }

    @Override
    public String toString() {
        return "UserDetailsImpl{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", nickname='" + nickname + '\'' +
                ", role='" + role + '\'' +
                ", authorities=" + authorities.toString() +
                '}';
    }
}
