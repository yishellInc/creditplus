package com.creditplus.p2p.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.creditplus.p2p.common.constant.Constant;
import com.creditplus.p2p.common.util.CheckParamUtil;
import com.creditplus.p2p.dao.ApproveLogDao;
import com.creditplus.p2p.service.ApproveLogService;

public class ApproveLogServiceImpl  implements ApproveLogService{

	@Autowired
	private ApproveLogDao approveLogDao;
	
	public final Logger logger = LogManager.getLogger(ApproveLogServiceImpl.class);
	
	public Map getAppLogByLoanId(Map paramMap) throws Exception {
		CheckParamUtil.checkKey(paramMap, Constant.LOAN_ID);
		List applogList=approveLogDao.getAppLogByLoanId(paramMap);
		Map gridMap=new HashMap();
		gridMap.put("griddata", applogList);
		return gridMap;
	}
	
	
	/**
	 * 日志插入
	 */
	public void insertApproveLog(Map paramMap) {
		//判断是否已插入日志
//		CheckParamUtil.checkKey(paramMap, "loan_id","approve_content","apply_state");
		approveLogDao.insertApproveLog(paramMap);
	}


	/* 
	 * @param list
	 * 批量插入
	 */
	public void batchInsertApproveLog(List<Map> list) {
		if(list!=null && list.size()>0){
			Map approveLogMap=new HashMap();
			approveLogMap.put("list", list);
			approveLogDao.batchInsertApproveLog(approveLogMap);
		}
	}

}
