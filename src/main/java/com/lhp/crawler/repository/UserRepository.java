package com.lhp.crawler.repository;

import com.lhp.crawler.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {

}
