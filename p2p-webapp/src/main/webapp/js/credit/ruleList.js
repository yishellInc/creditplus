//页面初始化加载函数
$(function(){
	debugger;
	//构造grid
    $("#ruleListGrid").jqGrid({
			url:serviceAddress,
			datatype: 'json',
			postData:{"module":"roleService","method":"getRoleListWithPage"},
			mtype: 'POST',
			autowidth:true,
			
			colNames:["操作","规则名称","是否可用","创建人","创建时间","最后修改人","最后修改时间","备注"],
			colModel :[
				{name:'ruleId', index:'operate_col',align:'center',"sortable":false,width:"100px",
					formatter:function(cellvalue, options, rowObject){
					   debugger;
					   if(!rowObject.dictId){
						   return "<span name='ruleEditSpan' class='ui-icon-edit' data-val='' data-name=''></span>";
					   }else{
						   return "<span name='ruleEditSpan' class='ui-icon-edit' data-val='"+rowObject.id+ "' data-name='" + rowObject.name +"'></span>";
					   }
					}
				},
				{name:'ruleName', index:'ruleName',align:'center',"sortable":false},
				{name:'enable', index:'enable',align:'center',"sortable":false,formatter:"select", editoptions:{value:"0:不可用;1:可用"}},
				{name:'created_by', index:'created_by',align:'center',"sortable":false},
				{name:'created_date', index:'created_date',align:'center',"sortable":false},
				{name:'last_updated_by', index:'last_updated_by',align:'center',"sortable":false},
				{name:'last_updated_date', index:'last_updated_date',align:'center',"sortable":false},
				{name:'remark', index:'remark',align:'center',"sortable":false}
			],
			pager: '#ruleListPager',
			multiselect: true,
			rowNum:10,
			rowList:[10,20,30],
			viewrecords: true,
			sortable:false,
			emptyrecords:"没有数据！",
			jsonReader : {  

		         root:"griddata",  

		         page: "currpage",  

		         total: "totalpages",  

		         records: "totalrecords"

		     }
	});
    
    //输入用户名称，点击按钮进行过滤
    $("#searchRuleListBtn").click(function(){
        var rulename = $("input[name='rulename']").val();
        var request_data={};
        if(rolename){
        	request_data.rulename=rulename;
        }
        $("#ruleListGrid").jqGrid('setGridParam',{  
            datatype:'json',  
            postData:{'request_data':JSON.stringify(request_data)}, //发送数据
            page:1,
            rowNum:10
        }).trigger("reloadGrid"); //重新载入
    	
    });
    
    //点击用户列表中的新增按钮
    $("[name='addRuleBtn']").click(function(){
    	addTabItem("ruleTab","ruleCreate","规则新增","/p2p-webapp/page/systemmng/ruleCreate.html",true,"/p2p-webapp/js/credit/ruleCreate.js");   	
    });
       
    //点击用户列表中的删除按钮
    $("[name='delRuleBtn']").click(function(){
        var request_data=[];
    	var rowids = $("#ruleListGrid").jqGrid('getDataIDs');
    	for(var i=0;i<rowids.length;i++){
    		var isChecked = $("#ruleListGrid").find("tr[id='"+rowids[i]+"']").find("input[type='checkbox']").is(':checked');
    		if(isChecked){
          	    var dataVal = $.trim($("#ruleListGrid").find("tr[id='"+rowids[i]+"']").find("a").text());
          	    request_data.push(dataVal*1);    			
    		}
      	}
    	
    	if(request_data.length <=0){
    		return;
    	}
		//调用服务
		publicSaveAjax("roleService","deleteRoleById",JSON.stringify(request_data),null,null,"#searchRoleListBtn");    	
    });
})