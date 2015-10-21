package com.creditplus.p2p.service.impl;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.creditplus.p2p.common.util.IDCardUtil;
import com.creditplus.p2p.dao.CommonInfoDao;
import com.creditplus.p2p.service.CommonInfoService;

public class CommonInfoServiceImpl implements CommonInfoService{

	@Autowired
	CommonInfoDao commonInfoDao;

	public Map getCardInfoById(String cardNo) {
		Map cardMap=IDCardUtil.getCardInfo(cardNo);
		System.out.println("cardMap:"+cardMap);
		if("1".equals(cardMap.get("id_state")+"")){
			System.out.println("========true=======");
			String id_first_6num=(String) cardMap.get("id_6");
			System.out.println(id_first_6num);
			Map dbCardInfo=commonInfoDao.getCardInfoById(id_first_6num);
			System.out.println("dbCardInfo:"+dbCardInfo);
			if(dbCardInfo!=null && dbCardInfo.size()>0)
				cardMap.putAll(dbCardInfo);
		}
		System.out.println("cardMap:"+cardMap);
		return cardMap;
	}

	public Map getPhoneInfoById(String phoneNum) {
		Map phoneMap=null;
		if(StringUtils.isNotEmpty(phoneNum)){
			String area=null;
			phoneNum=phoneNum.trim();
			if(phoneNum.length()==11){
				area=phoneNum.substring(3, 7);
			}else if(phoneNum.substring(0, 1).equals(0)){
				area=phoneNum.substring(0,4);
			}
			phoneMap=commonInfoDao.getPhoneInfoById(area);
			System.out.println("phoneMap:"+phoneMap);
		}
		return phoneMap;
	}
	

	public static void main(String[] args) {
		Map map=new CommonInfoServiceImpl().getCardInfoById("511702198506123795");
		System.out.println(map);
	}
}
