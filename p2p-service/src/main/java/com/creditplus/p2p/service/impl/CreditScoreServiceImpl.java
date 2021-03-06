/**
 * Administrator
 * 2015年11月5日
 */
package com.creditplus.p2p.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import com.creditplus.p2p.common.constant.Constant;
import com.creditplus.p2p.common.util.CheckParamUtil;
import com.creditplus.p2p.common.util.CommonUtil;
import com.creditplus.p2p.dao.CommonInfoDao;
import com.creditplus.p2p.dao.CreditScoreDao;
import com.creditplus.p2p.model.PageVO;
import com.creditplus.p2p.page.PageUtil;
import com.creditplus.p2p.service.CreditScoreService;

/**
 * @author Administrator
 *
 */
public class CreditScoreServiceImpl implements CreditScoreService{

	public static final Logger logger = LogManager.getLogger(CreditScoreServiceImpl.class);
	@Autowired
	CreditScoreDao creditScoreDao;
	@Autowired
	CommonInfoDao commonInfoDao;

	
	public Map getCreditScore(Integer user_id, Integer loan_id) {
		return getCreditScoreMap(user_id,loan_id);
	}
	
	
	private Map getCreditScoreMap(Integer user_id,Integer loan_id){
		List<Map> creditScores=creditScoreDao.getCreditScoreList(new HashMap());
		Map<String,Object> score1=new HashMap<String,Object>();
		Map<String,Object> score2=new HashMap<String,Object>();
		Double total1=0D,total2=0D;     //信用总分，模型1，模型2
		if(creditScores!=null && creditScores.size()>0){
			
			for(Map creditMap:creditScores){
				String fact_table=(String) creditMap.get("fact_table");
				String fact_column=(String) creditMap.get("fact_column");
				String model_name= (String) creditMap.get("model_name");
				String dimension_name=(String) creditMap.get("dimension_name");
				String proportion=(String) creditMap.get("proportion");
				Double baifenbi=Double.valueOf(proportion.substring(0, proportion.indexOf("%")).trim());
				Integer score_id=(Integer) creditMap.get("score_id");
				List<Map> itemsList=creditScoreDao.getCreditItemById(score_id);
				if(itemsList!=null && itemsList.size()>0){
					
					//查询评分维度字段值
					StringBuilder sbSql=new StringBuilder("select t.").append(fact_column).append(" from ").append(fact_table).append(" t left join loan_list l on t.user_id=l.user_id where l.user_id=#{user_id} and l.loan_id=#{loan_id}");
					Map sqlMap=new HashMap();
					sqlMap.put("user_id", user_id);
					sqlMap.put("loan_id", loan_id);
					sqlMap.put("sql", sbSql.toString());
					List<Map> result=commonInfoDao.executeDonamicSQL(sqlMap);
					System.out.println("=====result:"+result);
					Object value=null;
					if(result!=null && result.size()>0){
						Map resultMap=result.get(0);
						if(resultMap!=null && resultMap.size()>0)
							value=result.iterator().next().get(fact_column);
					}
					Map valueMap=new HashMap();
					valueMap.put(fact_column, value);
					
					Double dimesion_score=0D;
					for(Map itemMap:itemsList){
						String arithmetic=(String) itemMap.get("arithmetic");
						String dimension_value=(String) itemMap.get("dimension_value");
						Integer score=Integer.valueOf(itemMap.get("score")+"");
						StringBuilder expression=new StringBuilder(fact_column);
						if("like".equalsIgnoreCase(arithmetic.trim())){
							expression.append(".indexOf('").append(dimension_value).append("')");
						}else{
							if(CheckParamUtil.isNumber(dimension_value))
								expression.append(arithmetic).append(dimension_value);
							else
								expression.append(arithmetic).append("'").append(dimension_value).append("'");
						}
						
						//执行维度评分逻辑表达式
						boolean flag=CommonUtil.exeExpression(expression.toString(), valueMap);
						if(flag){
							dimesion_score=getDouble(score*baifenbi/100);
							System.out.println("score:"+score+" dimesion_score:"+dimesion_score);
						}
						
						if("1".equals(model_name)){
							score1.put(dimension_name, dimesion_score);
							total1+=dimesion_score;
						}else if("2".equals(model_name)){
							score2.put(dimension_name, dimesion_score);
							total2+=dimesion_score;
						}
						if(flag)
							break;
					}
				}
			}
			
		}
		
		score1.put("credit_total_score", getInteger(total1));
		score2.put("credit_total_score", getInteger(total2));
		Map scoreMap=new HashMap();
		scoreMap.put("score1", score1);
		scoreMap.put("score2", score2);
		scoreMap.put("credit_score1", getInteger(total1));
		scoreMap.put("credit_score2", getInteger(total2));
		scoreMap.put("credit_score_total", getInteger(total1+total2));
		return scoreMap;
	}
	
	
	/**
	 * 按四舍五入保留两位小数
	 * @param d
	 * @return
		double
	 */
	private double getDouble(double d){
		BigDecimal   b   =   new   BigDecimal(d);
		return b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 
	 * 按四舍五入返回整数
	 * @param d
	 * @return
		int
	 */
	private int getInteger(double d){
		BigDecimal   b   =   new   BigDecimal(d);
		return b.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
	}
	
	

	public PageVO getCreditScoreListWithPage(Map paramMap) {
		int currentPage=1,pageSize=10;
		if(paramMap!=null && (paramMap.get(Constant.CURRPAGE)!=null || paramMap.get(Constant.ROWNUM)!=null)){
			currentPage=Integer.valueOf(paramMap.get(Constant.CURRPAGE)+"");
			pageSize=Integer.valueOf(paramMap.get(Constant.ROWNUM)+"");
		}
		//初始化分页信息
		PageUtil.initPageInfo(currentPage, pageSize);
		creditScoreDao.getCreditScoreList(paramMap);
		//得到分页VO
		PageVO pageVo=PageUtil.getPageVO();
		return pageVo;
	}

	/* 
	 * @param idList
	 */
	public void deleteCreditScore(List<Integer> idList) {
		if(idList!=null && idList.size()>0){
			creditScoreDao.deleteCreditScore(idList);
			for(int i=0;i<idList.size();i++){
				creditScoreDao.deleteCreditItemBySid(idList.get(i));
			}
		}
	}

	/* 
	 * @param score_id
	 * @return
	 */
	public List<Map> getCreditItemById(Integer score_id) {
		return creditScoreDao.getCreditItemById(score_id);
	}

	/* 
	 * @param idList
	 */
	public void deleteCreditItem(List<Integer> idList) {
		if(idList!=null && idList.size()>0)
			creditScoreDao.deleteCreditItem(idList);
	}

	/* 
	 * @param dataMap
	 */
	public void insertCreditScore(Map creditMap,List itemsList) throws Exception {
		try{
			if(creditMap!=null && creditMap.size()>0){
				CheckParamUtil.initParamMap(creditMap);
				creditScoreDao.insertCreditScore(creditMap);
				
				if(itemsList!=null && itemsList.size()>0){
					Map searchMap=new HashMap();
					searchMap.put("dimension_name", (String)creditMap.get("dimension_name"));
					searchMap.put("model_name",(String)creditMap.get("model_name"));
					Integer score_id=creditScoreDao.findByName(searchMap);
					saveCreditItems(score_id, itemsList);
				}
			}
		}catch(Exception e){
			logger.error("insertCreditScore exception:"+e);
			if(e.getMessage()!=null && e.getMessage().contains("model_dimension_name_unique"))
				throw new Exception("同一模型同一维度，只能创建一条记录");
			else
				throw new Exception("保存失败");
		}
		
	}

	
	private void saveCreditItems(Integer score_id,List<Map> dataList){
		creditScoreDao.deleteCreditItemBySid(score_id);
		if(dataList!=null && dataList.size()>0){
			String currentUser=CommonUtil.getCurrentUser();
			for(Map dataMap:dataList){
				dataMap.put("last_updated_by",currentUser);
				dataMap.put("score_id", score_id);
			}
			Map creditItemMap=new HashMap();
			creditItemMap.put("list", dataList);
			creditScoreDao.insertCreditItems(creditItemMap);
		}
	}
	
	

	/* 
	 * @param dataMap
	 */
	public void updateCreditScore(Map dataMap,List<Map> itemsList) throws Exception {
		try{
			if(dataMap!=null && dataMap.size()>0){
				CheckParamUtil.checkKey(dataMap, "score_id");
				Integer score_id=Integer.valueOf(dataMap.get("score_id")+"");
				CheckParamUtil.initParamMap(dataMap);
				creditScoreDao.updateCreditScore(dataMap);
				if(itemsList!=null && itemsList.size()>0){
					this.saveCreditItems(score_id, itemsList);
				}
			}
		}catch(Exception e){
			logger.error("updateCreditScore exception:"+e);
			if(e.getMessage()!=null && e.getMessage().contains("model_dimension_name_unique"))
				throw new Exception("同一模型同一维度，只能创建一条记录");
			else
				throw new Exception("保存失败");
		}
	}

	
	/**
	 * 模型名称和维度唯一性校验
	 * 
	 */
	private void uniquenessCheck(Map dataMap,int operation){
		
	}
	
	/* 
	 * @param score_id
	 * @return
	 */
	public Map getCreditScoreById(Integer score_id) {
		return creditScoreDao.getCreditScoreById(score_id);
	}
	
	public static void main(String[] args) {
		Integer i=40;
		String proportion="12%";
		Integer baifenbi=Integer.valueOf(proportion.substring(0, proportion.indexOf("%")).trim());
		System.out.println(i*baifenbi/100);
	}
	

}
