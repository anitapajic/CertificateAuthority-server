//package com.example.IBTim19.security;
//
//import com.example.IBTim19.exceptions.NotFoundException;
//import com.example.IBTim19.service.UserDetailsServiceImpl;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.ServletRequest;
//import jakarta.servlet.ServletResponse;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//
//import java.io.IOException;
//
//public class AuthenticationTokenFilter extends UsernamePasswordAuthenticationFilter {
//
//    @Value("${token.header}")
//    private String tokenHeader;
//
//    @Autowired
//    private TokenUtils tokenUtils;
//
//    @Autowired
//    private UserDetailsServiceImpl userDetailsService;
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//        String authToken = httpRequest.getHeader(this.tokenHeader);
//        String username = this.tokenUtils.getUsernameFromToken(authToken);
//
//        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            try {
//                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
//                if (this.tokenUtils.validateToken(authToken, userDetails)) {
//                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
//                    SecurityContextHolder.getContext().setAuthentication(authentication);
//                }
//            } catch (UsernameNotFoundException e) {
//                throw new NotFoundException("Username not found!");
//            }
//        }
//
//        chain.doFilter(request, response);
//    }
//
//
//}