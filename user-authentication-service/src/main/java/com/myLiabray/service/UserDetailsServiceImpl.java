package com.myLiabray.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.myLiabray.model.User;
import com.myLiabray.model.UserDetailsImpl;
import com.myLiabray.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user=userRepository.findByEmail(username).orElseThrow(()->new UsernameNotFoundException("User not found with email : "+username));
		UserDetailsImpl userDetailsImpl=new UserDetailsImpl();
		userDetailsImpl.setUsername(username);
		userDetailsImpl.setPassword(user.getPassword());
		return userDetailsImpl;
	}

}
