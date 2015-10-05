package com.creditplus.p2p.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import com.creditplus.p2p.dao.LoanAppDao;
import com.creditplus.p2p.service.LoanAppService;

public class LoanAppServiceImpl implements LoanAppService{

	@Autowired 
	private LoanAppDao loanAppDao;

	public List<Map> getLoappVoList(Map paramMap) {
		return loanAppDao.getLoappVoList(paramMap);
	}

}
