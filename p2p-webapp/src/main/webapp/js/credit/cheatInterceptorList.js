//页面初始化加载函数
$(function(){
	debugger;

	var serviceAddress="http://"+window.location.host+"/p2p-webapp/services/process";
	//构造grid
 $("#cheatInterceptorGrid").jqGrid({
	 url:serviceAddress,
		datatype: 'json',
		postData:{"module":"loanOrderService","method":"getCreditFirstTrialListWithPage","request_data":{}},
		mtype: 'POST',
	autowidth:true,
	colNames:['申请单编号','申请人姓名','申请人身份证号','金额','期次','时长','申请时间','申请单状态','初审人','复审人'],
	colModel :[
		{
			name:'loan_id', index:'loan_id',align:'center',"sortable":false,
			formatter:function(cellvalue, options, rowObject){
				   debugger;
				   var paramsStr=JSON.stringify(rowObject);
				   if(paramsStr){
					   paramsStr=paramsStr.replace(/"/g,"@#_@#");
				   }
				   return "<a style='color:blue;' onclick=\"addTabItem('tenderTab','cheatInterceptorDetail','防欺诈详细信息','/p2p-webapp/page/cheatInterceptorDetail.html','false','/p2p-webapp/js/credit/cheatInterceptorDetail.js','"+paramsStr+"');\">"+cellvalue+"</a>";
			}
		},
		{name:'name', index:'name',align:'center',"sortable":false},
		{name:'id_num', index:'id_num',align:'center',"sortable":false},
		{name:'loan_money', index:'loan_money',align:'center',"sortable":false},
		{name:'dateCount', index:'dateCount',align:'center',"sortable":false},
		{name:'loan_day', index:'loan_day',align:'center',"sortable":false},
		{name:'modifytime', index:'modifytime',align:'center',"sortable":false},
		{name:'apply_state', index:'apply_state',align:'center',"sortable":false},
		{name:'first_assign_user', index:'first_assign_user',align:'center',"sortable":false},
		{name:'review_assign_user', index:'review_assign_user',align:'center',"sortable":false}
	],
	pager: '#cheatInterceptorPager',
	multiselect: true,
	rowNum:10,
	rowList:[10,20,30],
	viewrecords: true,
	emptyrecords:"没有数据！",
	jsonReader : {  

         root:"griddata",  

         page: "currpage",  

         total: "totalpages",  

         records: "totalrecords"

    }
	});
 
    //加入黑名单
	 $("[name='joinBlacklistBtn']").click(function(){
		  var grid=$("#cheatInterceptorGrid");
		  var selectedIds = grid.jqGrid('getGridParam','selarrrow');
		  if(selectedIds && selectedIds.length>0){
			  var selectRowDataArray=[];
			  for(var i=0;i<selectedIds.length;i++){
				  selectRowDataArray.push(grid.jqGrid('getRowData',selectedIds[i]));
			  }
			  //调用发标服务
			  var serviceAddress="http://"+window.location.host+"/p2p-webapp/services/process";		
			 /** $.ajax({ 
					url: serviceAddress,
					datatype: 'json',
					method:"post",
					data:{"module":"userService","method":"addUser","request_data":JSON.stringify(request_data)},			
					success: function(data){
						removeTabItem("userTab","userCreate");
						$("#searchUserListBtn").click();
					},error:function(error){
						alert(error);
					}
			  });*/
			  alert("加入黑名单！");
			  
		  }else{
			  alert("请至少选择一条数据加入黑名单！");
		  }
		  
	 });
	//查询按钮
	  $("[name='cheatInterceptorSearchBtn']").click(function(){
	      var request_data={};
	      $("#cheatInterceptorGrid").jqGrid('setGridParam',{  
	          datatype:'json',  
	          postData:{'request_data':JSON.stringify(request_data)}, //发送数据
	          page:1,
	          rowNum:10
	      }).trigger("reloadGrid"); //重新载入
	  	
	  });
		
		//清除按钮
	  $("[name='clearCheatInterceptorConditionBtn']").click(function(){
	      //调用清除函数
	  	clearDomVal("clearCheatInterceptorConditionDiv");
	  });
	 
})