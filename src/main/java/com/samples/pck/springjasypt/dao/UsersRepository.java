package com.samples.pck.springjasypt.dao;

import org.springframework.data.repository.CrudRepository;

import com.samples.pck.springjasypt.entity.Users;

public interface UsersRepository extends CrudRepository<Users, Long>{

}
