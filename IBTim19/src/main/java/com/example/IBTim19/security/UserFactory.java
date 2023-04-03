//package com.example.IBTim19.security;
//
//
//import com.example.IBTim19.model.User;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.AuthorityUtils;
//
//import java.util.Collection;
//
//public class UserFactory {
//
//    public static SecurityUser create(User user) {
//        Collection<? extends GrantedAuthority> authorities;
//        try {
//            authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(user.getAuthorities());
//        } catch (Exception e) {
//            authorities = null;
//        }
//
//        SecurityUser su = new SecurityUser(
//                user.getId(),
//                user.getUsername(),
//                user.getPassword(),
//                authorities
//        );
//
//        return su;
//    }
//}
