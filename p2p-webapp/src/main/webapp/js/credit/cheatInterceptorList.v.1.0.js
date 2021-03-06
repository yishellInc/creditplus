//页面初始化加载函数
$(function(){
	debugger;
	//日期空间format
	datepickerRender("cheatInterceptorConditionDiv");
	//下拉框数据填充
	selectRender("cheatInterceptorConditionDiv");
	//构造grid下拉框需要的数据
	var applyStateSelectObj=gridSelectColRender("","",{"type":"apply_state"},"code","name",true);
	//构造grid
 $("#cheatInterceptorGrid").jqGrid({
	 url:serviceAddress,
		datatype: 'json',
		postData:{"module":"loanOrderService","method":"checkInterceptListWithPage","request_data":{}},
		mtype: 'POST',
		height:205,
	autowidth:true,
	colNames:['操作','申请单编号','申请单编号','申请人姓名','申请人身份证号','金额','期次','时长','申请时间','状态'],
	colModel :[
	    {name:'operate', index:'operate',align:'center',"sortable":false,
	    	formatter:function(cellvalue, options, rowObject){
	    		debugger;
				var loan_id=rowObject.loan_id;
				var renderStr = "<a name='a_joinBlacklist' onclick=\"addInterceptor("+loan_id+")\" style='color:blue;' data-val='"+loan_id+"'>加入黑名单</a>";
				if(rowObject.apply_state == "11") renderStr = '<span style="color: #555562;">加入黑名单</span>';
				return renderStr;
			}
	    },      
	    {
			name:'loan_id', index:'loan_id',align:'center',"sortable":false, hidden:true
		},
		{	
			name:'loan_id_render', index:'loan_id', align:'center', "sortable":false,
			formatter:function(cellvalue, options, rowObject){
			   debugger;
			   var paramsStr=JSON.stringify(rowObject);
			   if(paramsStr){
//				   paramsStr=paramsStr.replace(/"/g,"@#_@#");
				   paramsStr=escape(paramsStr);
			   }
			   return "<a style='color:blue;' onclick=\"addTabItem('tenderTab','cheatInterceptorDetail','拦截欺诈信息','/p2p-webapp/page/cheatInterceptorDetail.html','true','/p2p-webapp/js/credit/cheatInterceptorDetail"+app_verion+".js','"+paramsStr+"');\">"+rowObject.loan_id+"</a>";
			}
		},
		{name:'name', index:'name',align:'center',"sortable":false},
		{name:'id_num', index:'id_num',align:'center',"sortable":false},
		{name:'loan_money', index:'loan_money',align:'center',"sortable":false},
		{name:'dateCount', index:'dateCount',align:'center',"sortable":false},
		{name:'loan_day', index:'loan_day',align:'center',"sortable":false},
		{name:'modifytime', index:'modifytime',align:'center',"sortable":false},
		{name:'apply_state', index:'apply_state',align:'center',
			"sortable":false,
			formatter:'select',
			editoptions:{value:applyStateSelectObj.jsonStr}
		}
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

    },
    onPaging:function(pgButton){
		 debugger;
		 var request_data = getValue("cheatInterceptorConditionDiv");
		 var  grid=$(this).jqGrid();
		 gridOnPaging(pgButton,grid,"cheatInterceptorPager",request_data);
	},
	gridComplete : function(rowId, e, arg3, arg4){
		// 获取所有行序列
		var rowIds = $("#cheatInterceptorGrid").jqGrid("getDataIDs");
		if(rowIds && rowIds.length > 0){
			$.each(rowIds, function(i, rowid){
				// 获取行对象
				var rowData = $("#cheatInterceptorGrid").jqGrid("getRowData", rowid);
				// 如果是黑名单状态， 不显示复选框
				if(rowData.apply_state && rowData.apply_state == '11'){
					// 清空复选框
					$($("#cheatInterceptorGrid #" + rowid + " td")[0]).html('');
				}
			});
		}
	},
	onSelectRow : function(rowid,status){
		debugger;
		var grid = $("#cheatInterceptorGrid");
		var rowData = grid.jqGrid("getRowData", rowid);
		// 如果是黑名单状态， 不显示复选框
		if(rowData.apply_state && rowData.apply_state == '11'){
			$("#cheatInterceptorGrid #" + rowid).removeClass('ui-state-highlight');
		}
		return status;
	},
	onSelectAll : function(aRowids,status){
		debugger;
		if(status){
			var grid = $("#cheatInterceptorGrid");
			$.each(aRowids, function(i, rowid){
				var rowData = grid.jqGrid("getRowData", rowid);
				// 如果是黑名单状态， 不显示复选框
				if(rowData.apply_state && rowData.apply_state == '11'){
					$("#cheatInterceptorGrid #" + rowid).removeClass('ui-state-highlight');
				}
			});
		}
	}
		
});
 
    //加入黑名单
	 $("[name='joinBlacklistBtn']").click(function(){
		  var grid=$("#cheatInterceptorGrid");
		  var selectedIds = grid.jqGrid('getGridParam','selarrrow');
		  //去除黑名单数据
		  var newIds = [];
		  $.each(selectedIds, function(i, rowid){
			  var rowData = grid.jqGrid('getRowData',selectedIds[i]);
			  if(rowData.apply_state && rowData.apply_state == '7'){
				  newIds.push(rowid);
			  }
		  });
		  if(newIds && newIds.length>0){
			  var selectRowDataArray=[];
			  for(var i=0;i<newIds.length;i++){
				  var rowData = grid.jqGrid('getRowData',newIds[i]);
				  delete rowData['loan_id_render'];
				  rowData.apply_state = 11;//黑名单状态
				  rowData.approve_content = "加入黑名单";
				  selectRowDataArray.push(rowData);
			  }
			  //调用撤标服务
			  interceptorCall(selectRowDataArray);
		  }else{
			  messageBox.createMessageDialog("提示","请至少选择一条数据加入黑名单！","","","warning");
		  }
	 });
	 
	 
	//查询按钮
	  $("[name='cheatInterceptorSearchBtn']").click(function(){
		  var request_data = getValue("cheatInterceptorConditionDiv");
	      $("#cheatInterceptorGrid").jqGrid('setGridParam',{  
	          datatype:'json',  
	          postData:{'request_data':JSON.stringify(request_data)}, //发送数据
	          page:1,
	          rowNum:10
	      }).trigger("reloadGrid"); //重新载入
	  	
	  });
		
		//重置按钮
	  $("[name='clearCheatInterceptorConditionBtn']").click(function(){
	      //调用重置函数
	  	clearDomVal("cheatInterceptorConditionDiv");
	  });
	 
});

function addInterceptor(loan_id){
	 var datas = [];
	 var request_data = {};
	 request_data.loan_id = loan_id;
	 request_data.apply_state = 11;//黑名单状态
	 request_data.approve_content = "加入黑名单";
	 datas.push(request_data);
	 //调服务
	 interceptorCall(datas);
}

function interceptorCall(datas){
	//调用撤标服务
	  $.ajax({ 
			url: serviceAddress,
			datatype: 'json',
			method:"post",
			data:{"module":"loanOrderService","method":"updateLoanOrderState","request_data":JSON.stringify(datas)},			
			success: function(data){
				//removeTabItem("userTab","userCreate");
				$("[name=cheatInterceptorSearchBtn]").click();
			},error:function(error){
				messageBox.createMessageDialog("提示","操作失败！","","","warning");
			}
	  });
	  messageBox.createMessageDialog("提示","加入黑名单成功！","","","warning");
	  //刷新数据
	  $("[name='cheatInterceptorSearchBtn']").click();
}