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
		html += "<tr>" +
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
			alert("新增失败：[" + textStatus + "]" + XMLHttpRequest.responseText);
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
			alert("移动失败：[" + textStatus + "]" + XMLHttpRequest.responseText);
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
			alert("删除失败：[" + textStatus + "]" + XMLHttpRequest.responseText);
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
		}
	});
}

function buildPyramid(menuList, panelId) {
	let $panel = $("#" + panelId);
	$panel.html("");
	for (let i = 0; i < menuList.length; i++) {
		let root = menuList[i];
		let $pyramid = $('<div class="pyramid"></div>').width(size * root.length).height(1000);
		addChildNodes([root], $pyramid);

		for (let n = 1; n <= root.length; n++) {
			let $num = $('<div class="num"></div>')
				.css("left", ((n - 1) * size - 1) + "px")
				.html(n);
			$pyramid.append($num);
		}

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
	let $node = $('<div class="node" id="' + menu.id + '" left="' + menu.l + '" right="' + menu.r + '" title="' + menu.name + '">' +
		'<div class="left-num">' + menu.l + '</div>' +
		'<div class="right-num">' + menu.r + '</div>' +
		'</div>');

	$node
		.css("left", (size * (menu.l - 1) - 1) + "px")
		.css("top", (size * menu.level - 1) + "px")
		.width(size * menu.length - 1);

	return $node;
}