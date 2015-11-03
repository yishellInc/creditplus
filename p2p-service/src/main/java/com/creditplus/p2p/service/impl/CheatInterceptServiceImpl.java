package com.creditplus.p2p.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.creditplus.p2p.common.constant.Constant;
import com.creditplus.p2p.common.util.CommonUtil;
import com.creditplus.p2p.dao.CheatInterceptDao;
import com.creditplus.p2p.dao.CommonInfoDao;
import com.creditplus.p2p.dao.LoanOrderDao;
import com.creditplus.p2p.dao.RuleDao;
import com.creditplus.p2p.service.ApproveLogService;
import com.creditplus.p2p.service.CheatInterceptService;

public class CheatInterceptServiceImpl implements CheatInterceptService {

	@Autowired
	CommonInfoDao commonInfoDao;
	@Autowired
	RuleDao ruleDao;
	@Autowired
	CheatInterceptDao cheatInterceptDao;
	@Autowired
	LoanOrderDao loanOrderDao;
	@Autowired
	ApproveLogService approveLogService;
	
	
	public boolean intercept(Integer user_id,Integer loan_id) throws Exception {
		boolean checkFlag=false;
		List<Map> ruleList=getRuleList();
		if(ruleList!=null && ruleList.size()>0){
			for(Map ruleMap:ruleList){
				String rule_name=(String) ruleMap.get(Constant.RULE_NAME);
				Integer rule_id=Integer.valueOf(ruleMap.get(Constant.RULE_ID)+"");
				checkFlag=ruleSqlCheat(ruleMap, loan_id);
				if(checkFlag){
					CheatProcess(loan_id, new StringBuilder("检查规则 '").append(rule_name).append("' sql不通过").toString());
					break;
				}
					
				
				List<Map> dimensionList=getDimensionByRuleId(rule_id);
				if(dimensionList!=null && dimensionList.size()>0){
					checkFlag=dimensionCheat(dimensionList, user_id,loan_id);
					if(checkFlag){
						CheatProcess(loan_id, new StringBuilder("检查规则 '").append(rule_name).append("' 维度不通过").toString());
						break;
					}
				}
				checkFlag=false;
			}
		}
		return checkFlag;
	}
	
	
	private boolean dimensionCheat(List<Map> dismensionList,Integer user_id,Integer loan_id){
		boolean flag=false;
		if(dismensionList!=null && dismensionList.size()>0){
			Map<String, Object> sqlMap=new HashMap<String, Object>();
			StringBuilder sbSql=new StringBuilder("select count(1) as total_record from user_info u left join loan_list l on u.user_id=l.user_id left join  customer_info_t c on u.user_id=c.user_id  where  ");
			for(int i=0;i<dismensionList.size();i++){
				Map dismensionMap=dismensionList.get(i);
				String column_name=(String) dismensionMap.get("column_name");
				String semanteme=(String) dismensionMap.get("semanteme");
				String value=(String) dismensionMap.get("dis_value");
				String arithmetic=(String) dismensionMap.get("arithmetic");
				String key="value_"+i;
				sbSql.append(column_name).append(semanteme).append("#{").append(key).append("} ");
				sqlMap.put(key, value);
				if(i<dismensionList.size()-1)
					sbSql.append(arithmetic);
			}
			sqlMap.put("sql", sbSql);
			Integer total_record=executeQuerySql(sqlMap);
			flag=total_record>0?true:false;
		}
		
		System.out.println("=====flag:"+flag);
		return flag;	
	} 
	
	
	private boolean ruleSqlCheat(Map ruleMap,Integer loan_id){
		boolean flag=false;
		if(ruleMap!=null && ruleMap.size()>0){
			String rule_sql=(String) ruleMap.get("rule_sql");
			if(StringUtils.isNotEmpty(rule_sql) && rule_sql.toLowerCase().contains("select")){
				Map<String, Object> sqlMap=new HashMap<String, Object>();
				String sql=new StringBuilder("select count(1) as total_record from (").append(rule_sql).append(") xt").toString();
				sqlMap.put("sql", sql);
				Integer total_record=executeQuerySql(sqlMap);
				flag=total_record>0?true:false;
			}
		}
		return flag;
	}
	
	private Integer executeQuerySql(Map sqlMap){
		Integer total_record=0;
		List<Map> result=commonInfoDao.executeDonamicSQL(sqlMap);
		if(result!=null && result.size()>0)
			total_record=Integer.valueOf(result.iterator().next().get("total_record")+"");
		return total_record;
	}

	
	/**
	 * 校验单个维度
	 * @param dismensionMap
	 * @param user_id
	 * @param loan_id
	 * @return
		boolean
	 * @throws Exception 
	 *//*
	private boolean executeChectSqlxxx(Map dismensionMap,Integer user_id,Integer loan_id) throws Exception{
		String tableName=(String) dismensionMap.get("table_name");
		String column_name=(String) dismensionMap.get("column_name");
		String semanteme=(String) dismensionMap.get("semanteme");
		String value=(String) dismensionMap.get("value");
		StringBuilder sbSql=new StringBuilder("select count(1) as total_record from ").append(tableName).append(" where ")
				.append(column_name).append(semanteme).append("#{value} and ").append("user_id = #{user_id}");
		
		Map sqlMap=new HashMap();
		sqlMap.put(sqlMap, sbSql.toString());
		sqlMap.put("sql", sbSql.toString());
		sqlMap.put("value", value);
		sqlMap.put("user_id", user_id);
		
		System.out.println("rule check sql"+sbSql);
		List<Map> list=commonInfoDao.executeDonamicSQL(sqlMap);
		Integer total_record=0;
		if(list!=null && list.size()>0)
			total_record=Integer.valueOf(list.iterator().next().get("total_record")+"");
		
		System.out.println("total_record===="+total_record);
		boolean checkFlag=false;
		if(semanteme.contains("!=")){
			checkFlag=total_record==0?true:false;
		}else if(semanteme.contains(">")){
			checkFlag=total_record==0?true:false;
		}else if(semanteme.contains("<")){
			checkFlag=total_record==0?false:true;
		}
		
		if(checkFlag){
			System.out.println("有欺诈=============");
			
			//更新申请单状态
			String userName=CommonUtil.getCurrentUser();
			Map loanOrderMap=new HashMap();
			loanOrderMap.put("loan_id", loan_id);
			loanOrderMap.put("apply_state", 6);
			loanOrderMap.put("last_updated_by", userName);
			loanOrderDao.updateLoanOrderByLoanId(loanOrderMap);
			
			//插入拦截日志
			Map cheatMap=new HashMap();
			cheatMap.put("loan_id", loan_id);
			cheatMap.put("intercept_source", "system");
			cheatMap.put("check_item", dismensionMap.get("rule_name"));
			cheatMap.put("intercept_cause", "检查 "+dismensionMap.get("column_name")+" 不通过");
			cheatMap.put("last_updated_by", userName);
			cheatInterceptDao.insertBatch(cheatMap);
			
			//插入审批日志
			Map approveMap=new HashMap();
			approveMap.put("loan_id", loan_id);
			approveMap.put("apply_state", 6);
			approveMap.put("approve_content", dismensionMap.get("rule_name")+"检查 "+dismensionMap.get("column_name")+" 不通过");
			approveMap.put("last_updated_by", userName);
			approveLogService.insertApproveLog(approveMap, false);
		}
		return checkFlag;
	}*/
	
	
	private void CheatProcess(Integer loan_id,String intercept_cause) throws Exception{
		String userName=CommonUtil.getCurrentUser();
		//更新申请单状态
		Map<String, Object> loanOrderMap=new HashMap<String, Object>();
		loanOrderMap.put(Constant.LOAN_ID, loan_id);
		loanOrderMap.put(Constant.APPLY_STATE, Constant.S_STOP);
		loanOrderMap.put(Constant.LAST_UPDATED_BY, userName);
		loanOrderDao.updateLoanOrderByLoanId(loanOrderMap);
		
		//插入拦截日志
		Map<String, Object> cheatMap=new HashMap<String, Object>();
		cheatMap.put(Constant.LOAN_ID, loan_id);
		cheatMap.put("intercept_source", "system");
		cheatMap.put("check_item", intercept_cause);
		cheatMap.put("intercept_cause", intercept_cause);
		cheatMap.put(Constant.LAST_UPDATED_BY, userName);
		cheatInterceptDao.insertBatch(cheatMap);
		
		//插入审批日志
		Map<String, Object> approveMap=new HashMap<String, Object>();
		approveMap.put(Constant.LOAN_ID, loan_id);
		approveMap.put(Constant.APPLY_STATE, Constant.S_STOP);
		approveMap.put(Constant.APPROVE_CONTENT, intercept_cause);
		approveMap.put(Constant.LAST_UPDATED_BY, userName);
		try {
			approveLogService.insertApproveLog(approveMap);
		} catch (Exception e) {
			throw new Exception(e);
			//写异常日志表
		}
	}
	
	
	private List<Map> getRuleList(){
		return ruleDao.getRulesList(new HashMap());
	}
	
	private List<Map> getDimensionByRuleId(Integer rule_id){
		return ruleDao.getDimensionListByRuleId(rule_id);
	}

}
