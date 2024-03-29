package com.samples.pck.springjasypt.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.samples.pck.springjasypt.dao.UsersRepository;
import com.samples.pck.springjasypt.entity.Users;

@Service
public class UsersService {

	@Autowired
	UsersRepository usersRepository;
	
	public Users addUser(Users user) throws IllegalAccessException {
		if(user.getUserId() != null) 
			throw new IllegalAccessException("You cannot add a user with the id, this field is autogenerated!");
		
		return usersRepository.save(user);
	}
	
	public Users updateUser(Users user) throws IllegalAccessException{
		if(user.getUserId() == null) 
			throw new IllegalAccessException("Illegal Access! User id required!!");
		
		return usersRepository.save(user);
	}
	
	public Users getUserById(Long userId) {		
		return usersRepository.findById(userId).get();
	}
	
	public boolean delete(Users user) {
		usersRepository.delete(user);
		return true;
	}
	
}
