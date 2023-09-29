package com.myLiabray.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.myLiabray.service.JwtServiceImpl;

import io.micrometer.core.instrument.util.StringUtils;




@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{

	@Autowired
	private JwtServiceImpl jwtService;
	@Autowired
	private UserDetailsService userDetailsService;
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		final String authHeader = request.getHeader("Authorization");
		final String jwt;
        final String email;
        
        if (StringUtils.isEmpty(authHeader) || !startsWith(authHeader, "Bearer ")) {
            filterChain.doFilter(request, response);
            
            return;
        }
        jwt = authHeader.substring(7);
        email = jwtService.extractUserName(jwt);
        if (StringUtils.isNotEmpty(email) && SecurityContextHolder.getContext().getAuthentication() == null)
        {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (jwtService.isTokenValid(jwt, userDetails))
            {
            	request.setAttribute("email", email);
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            }
        }
        
        filterChain.doFilter(request, response);
		
	}
	
	 public boolean startsWith(String str, String prefix) {
	        if (str == null || prefix == null) {
	            return false;
	        }
	        return str.startsWith(prefix);
	    }

}

