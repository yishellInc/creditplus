//页面初始化加载函数
$(function(){
	debugger;
	//日期空间format
	datepickerRender("makeTenderConditionDiv");
	//下拉框数据填充
	selectRender("makeTenderConditionDiv");
	//构造grid
 $("#makeTenderListGrid").jqGrid({
	 url:serviceAddress,
		datatype: 'json',
		postData:{"module":"loanOrderService","method":"touBiaoListWithPage","request_data":{}},
		mtype: 'POST',
		height:205,
	autowidth:true,
	colNames:['操作','申请单编号','申请人姓名','申请人身份证号','信用等级','金额','期次','时长','银行卡开户行','卡号'],
	colModel :[
		{name:'operate', index:'operate',align:'center',"sortable":false,
			formatter:function(cellvalue, options, rowObject){
				   debugger;
				   var loan_id=rowObject.loan_id;
				   return "<a class='a_makeTender' style='color:blue;' data-val='"+loan_id+"'>投标</a>";
			}
			
		},   
		{
			name:'loan_id', index:'loan_id',align:'center',"sortable":false,
		},
		{name:'name', index:'name',align:'center',"sortable":false},
		{name:'id_num', index:'id_num',align:'center',"sortable":false},
		{name:'creditGrade', index:'creditGrade',align:'center',"sortable":false},
		{name:'loan_money', index:'loan_money',align:'center',"sortable":false},
		{name:'dateCount', index:'dateCount',align:'center',"sortable":false},
		{name:'loan_day', index:'loan_day',align:'center',"sortable":false},
		{name:'card_bank', index:'cardAccountBank',align:'center',"sortable":false},
		{name:'card_no', index:'bankCardNum',align:'center',"sortable":false}
	],
	pager: '#makeTenderListPager',
//	multiselect: true,
	rowNum:10,
	rowList:[10,20,30],
	sortname: 'invid',
	sortorder: 'desc',
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
		 var request_data = getValue("makeTenderConditionDiv");
		 var  grid=$(this).jqGrid();
		 gridOnPaging(pgButton,grid,"makeTenderListPager",request_data);
	}	 
		
		
	});
 
//查询按钮
 $("[name='makeTenderSearchBtn']").click(function(){
     var request_data = getValue("makeTenderConditionDiv");
     $("#makeTenderListGrid").jqGrid('setGridParam',{  
         datatype:'json',  
         postData:{'request_data':JSON.stringify(request_data)}, //发送数据
         page:1,
         rowNum:10
     }).trigger("reloadGrid"); //重新载入
 	
 });
 
 
 /**
  * 投标动作
  */
 $("div[tabid='makeTenderList']").on("click",".a_makeTender",function(){
 	debugger;
 	var ofWithdrawalData=[];
 	var rowData={};
 	rowData.loan_id=$(this).attr("data-val");
 	rowData.apply_state = 9; //投标
 	rowData.approve_content = "生成合同!";
 	ofWithdrawalData.push(rowData);
 	//调用撤标服务
 	  $.ajax({ 
 			url: serviceAddress,
 			datatype: 'json',
 			method:"post",
 			data:{"module":"loanOrderService","method":"createTenderService","request_data":JSON.stringify(ofWithdrawalData)},			
 			success: function(data){
 				messageBox.createMessageDialog("提示","投标成功！","","","true");
 				$("[name=makeTenderSearchBtn]").click();
 			},error:function(error){
 				messageBox.createMessageDialog("提示","投标失败！","","","error");
 			}
 	  });
 });
	
	//重置按钮
 $("[name='clearMakeTenderConditionBtn']").click(function(){
     //调用重置函数
 	clearDomVal("makeTenderConditionDiv");
 });
})