function loadTree(callback) {
	callback = callback || window.callback;
	$.ajax({
		url: "./api/v1/sys-menu/tree",
		success: function (menuList) {
			let html = "";
			html += buildTreeHtml(menuList, "");
			$("#treeTableBody").html(html).hide().fadeIn(400);
			if (callback) {
				callback(menuList);
			}
		},
		error: function (XMLHttpRequest, textStatus, errorThrown) {
			let result = JSON.parse(XMLHttpRequest.responseText);
			alert("加载数据失败：[" + XMLHttpRequest.status + "] " + result.message);
		}
	});
}

function buildTreeHtml(menuList, s, parent) {
	if (!menuList || menuList.length === 0) {
		return "";
	}

	let html = "";
	for (let i = 0; i < menuList.length; i++) {
		let menu = menuList[i];
		setWarn(menu, parent); // 设置警告信息（数据正确性检测）
		html += "<tr id='row_" + menu.id + "' menuid='" + menu.id + "'" + (menu.root ? ' class="root_row"' : "") + ">" +
			"	<td>" + menu.id + "</td>" +
			"	<td>" + menu.pid + "</td>" +
			"	<td>" + menu.rootId + "</td>" +
			"	<td class='b' style='text-align: right'>" + menu.l + "</td>" +
			"	<td>" + s + menu.name + "</td>" +
			"	<td class='b'>" + menu.r + "</td>" +
			"	<td>" + menu.childSize + "</td>" +
			"	<td>" + menu.level + "</td>" +
			"	<td>" + (menu.root ? "根节点" : "") + (menu.leaf ? (menu.root ? "、" : "") + "叶节点" : "") + "</td>" +
			"	<td>" +
			"		<a href=\"javascript:del('" + menu.id + "')\">删除</a>" +
			"	</td>" +
			"	<td style='color: red'>" + menu.warn + "</td>" +
			"</tr>";
		html += buildTreeHtml(menu.childList, s + "—&nbsp;", menu);
	}
	return html;
}

function insert() {
	let name = $("#name").val();
	let pid = $("#pid").val();

	if (!name) {
		alert("节点名称必填");
		$("#name").focus();
		return;
	}

	$.ajax({
		url: "./api/v1/sys-menu/insert",
		type: "post",
		data: JSON.stringify({
			"name": name,
			"pid": pid || ""
		}),
		contentType: "application/json",
		success: function (data) {
			alert("新增成功");
			loadTree();
		},
		error: function (XMLHttpRequest, textStatus, errorThrown) {
			let result = JSON.parse(XMLHttpRequest.responseText);
			alert("新增失败：[" + XMLHttpRequest.status + "] " + result.message);
			loadTree();
		}
	});
}

function move() {
	let id = $("#id").val();
	let targetPid = $("#targetPid").val();

	if (!id) {
		alert("节点ID必填");
		$("#id").focus();
		return;
	}

	$.ajax({
		url: "./api/v1/sys-menu/move",
		type: "post",
		data: JSON.stringify({
			"id": id,
			"targetPid": targetPid || ""
		}),
		contentType: "application/json",
		success: function (data) {
			alert("移动成功");
			loadTree();
		},
		error: function (XMLHttpRequest, textStatus, errorThrown) {
			let result = JSON.parse(XMLHttpRequest.responseText);
			alert("移动失败：[" + XMLHttpRequest.status + "] " + result.message);
			loadTree();
		}
	});
}

function del(id) {
	$.ajax({
		url: "./api/v1/sys-menu/delete?id=" + id,
		type: "post",
		success: function (data) {
			alert("删除成功");
			loadTree();
		},
		error: function (XMLHttpRequest, textStatus, errorThrown) {
			let result = JSON.parse(XMLHttpRequest.responseText);
			alert("删除失败：[" + XMLHttpRequest.status + "] " + result.message);
			loadTree();
		}
	});
}

function setWarn(menu, par) {
	menu.warn = menu.warn || "";
	if (menu.l <= 0) menu.warn += "left <= 0；<br/>";
	else if (menu.id === menu.rootId && menu.l !== 1) menu.warn += "根节点的left必须为1，但目前为" + menu.l + "；<br/>";
	if (menu.r <= 0) menu.warn += "right <= 0；<br/>";
	if (menu.l >= menu.r) menu.warn += "left >= right；<br/>"
	if (menu.childList && menu.childList.length > 0) {
		// 判断左右值与所有子节点的左右值是否符合
		let minLeftFromChilds = getMinLeftFromChilds(menu.childList, menu.r);
		if (menu.l !== minLeftFromChilds - 1) {
			menu.warn += "left太" + (menu.l > minLeftFromChilds - 1 ? "大" : "小") + "（应为子节点最小left - 1 = " + (minLeftFromChilds - 1) + "）；<br/>";
		}

		let maxRightFromChjilds = getMaxRightFromChilds(menu.childList, menu.l);
		if (menu.r !== maxRightFromChjilds + 1) {
			menu.warn += "right太" + (menu.r > maxRightFromChjilds + 1 ? "大" : "小") + "（应为子节点最大right + 1 = " + (maxRightFromChjilds + 1) + "）；<br/>";
		}
	} else if (menu.l !== menu.r - 1) {
		menu.warn += "left != right - 1；<br/>";
	}

	if (par) {
		if (menu.rootId !== par.rootId) menu.warn += "rootId != parent.rootId；<br/>";
		if (menu.pid !== par.id) menu.warn += "pid != parent.id；<br/>";
		if (menu.level !== par.level + 1) menu.warn += "level != parent.level + 1；<br/>";
		if (menu.l <= par.l) menu.warn += "left <= parent.left；<br/>";
		if (menu.l >= par.r) menu.warn += "left >= parent.right；<br/>";
		if (menu.r <= par.l) menu.warn += "right <= parent.left；<br/>";
		if (menu.r >= par.r) menu.warn += "right >= parent.right；<br/>";
	}
}

function getMinLeftFromChilds(childList, minLeft) {
	for (let i = 0; i < childList.length; i++) {
		let menu = childList[i];
		if (minLeft > menu.l) {
			minLeft = menu.l;
		}
		let minLeftFromChilds = getMinLeftFromChilds(menu.childList, minLeft);
		if (minLeft > minLeftFromChilds) {
			minLeft = minLeftFromChilds;
		}
	}
	return minLeft;
}

function getMaxRightFromChilds(childList, maxRight) {
	for (let i = 0; i < childList.length; i++) {
		let menu = childList[i];
		if (maxRight < menu.r) {
			maxRight = menu.r;
		}
		let maxRightFromChilds = getMaxRightFromChilds(menu.childList, maxRight);
		if (maxRight < maxRightFromChilds) {
			maxRight = maxRightFromChilds;
		}
	}
	return maxRight;
}


const size = 40;

function loadPyramid(panelId) {
	$.ajax({
		url: "./api/v1/sys-menu/tree",
		success: function (menuList) {
			buildPyramid(menuList, panelId);
		},
		error: function (XMLHttpRequest, textStatus, errorThrown) {
			let result = JSON.parse(XMLHttpRequest.responseText);
			alert("加载数据失败：[" + XMLHttpRequest.status + "] " + result.message);
		}
	});
}

function buildPyramid(menuList, panelId) {
	let $panel = $("#" + panelId);
	$panel.html("");
	for (let i = 0; i < menuList.length; i++) {
		let root = menuList[i];

		// 创建一根树的倒金字塔图
		let length = root.length + root.length % 2;
		let $pyramid = $('<div class="pyramid" id="pyramid_' + root.id + '"></div>').width(size * length);

		// 往倒金字塔图中添加节点
		addChildNodes([root], $pyramid);

		// 给倒金字塔图添加尺码条
		for (let n = 1; n <= length; n++) {
			let $num = $('<div class="num"></div>')
				.css("left", ((n - 1) * size - 1) + "px")
				.html(n);
			$pyramid.append($num);
		}

		// 将倒金字塔展示到页面上
		$panel.append($pyramid);
	}
}

function addChildNodes(menuList, $pyramid) {
	for (let i = 0; i < menuList.length; i++) {
		let menu = menuList[i];
		$pyramid.append(buildNode(menu));
		addChildNodes(menu.childList, $pyramid);
	}
}

function buildNode(menu) {
	let $node = $('<div class="node' + (menu.warn ? ' warn' : "") + '" id="node_' + menu.id + '" menuid="' + menu.id + '" left="' + menu.l + '" right="' + menu.r + '" title="' + menu.name + '">' +
		'<div class="left-num">' + menu.l + '</div>' +
		'<div class="right-num">' + menu.r + '</div>' +
		'</div>');

	// 设置尺寸和位置
	$node
		.css("left", (size * (menu.l - 1) - 1) + "px")
		.css("top", (size * menu.level - 1) + "px")
		.width(size * menu.length - 1);

	// 设置鼠标移进移出事件
	$node
		.mouseover(function () {
			let $this = $(this);
			$this.addClass("highlight");
			let menuid = $this.attr("menuid");
			$("#row_" + menuid).addClass("highlight");
		})
		.mouseout(function () {
			let $this = $(this);
			$this.removeClass("highlight");
			let menuid = $this.attr("menuid");
			$("#row_" + menuid).removeClass("highlight");
		});

	// 为节点对应的数据行设置鼠标移进移出事件
	$("#row_" + menu.id)
		.mouseover(function () {
			let $this = $(this);
			$this.addClass("highlight");
			let menuid = $this.attr("menuid");
			$("#node_" + menuid).addClass("highlight");
		})
		.mouseout(function () {
			let $this = $(this);
			$this.removeClass("highlight");
			let menuid = $this.attr("menuid");
			$("#node_" + menuid).removeClass("highlight");
		});

	return $node;
}