package com.creditplus.p2p.dao;

import java.util.List;

import com.creditplus.p2p.model.UserVO;

public interface UserDao {
	
	void insertUser(UserVO userVO);
	
	void deleteUserById(int userId);
	
	void updateUser(UserVO userVO);
	
	void changePassword(UserVO userVO);
	
	UserVO getUserById(int userId);
	
	UserVO findByName(String username);
	
	int getUserCount(UserVO userVO);	
	
	List<UserVO> getUserListWithPage(UserVO userVO);
}
