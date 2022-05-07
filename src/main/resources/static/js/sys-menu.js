function showError(title, XMLHttpRequest) {
	let message;
	try {
		let result = JSON.parse(XMLHttpRequest.responseText);
		message = result.message;
	} catch (t) {
		message = XMLHttpRequest.responseText;
	}

	alert(title + "：" + message + "  [" + XMLHttpRequest.status + "]");
}

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
			showError("加载数据失败", XMLHttpRequest);
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
			showError("新增失败", XMLHttpRequest);
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
			showError("移动失败", XMLHttpRequest);
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
			showError("删除失败", XMLHttpRequest);
			loadTree();
		}
	});
}

function setWarn(menu, par) {
	if (menu.warn) {
		return;
	}

	menu.warn = "";
	let n = 1;
	if (menu.l <= 0) menu.warn += (n++ + "、") + "left <= 0；<br/>";
	else if (menu.id === menu.rootId && menu.l !== 1) menu.warn += (n++ + "、") + "根节点的left必须为1，但目前为" + menu.l + "；<br/>";
	if (menu.r <= 0) menu.warn += (n++ + "、") + "right <= 0；<br/>";
	if (menu.l >= menu.r) menu.warn += (n++ + "、") + "left >= right；<br/>"
	if (menu.childList && menu.childList.length > 0) {
		// 判断左右值与所有子节点的左右值是否符合
		let minLeftFromChilds = getMinLeftFromChilds(menu.childList, menu.r);
		if (menu.l !== minLeftFromChilds - 1) {
			menu.warn += (n++ + "、") + "left太" + (menu.l > minLeftFromChilds - 1 ? "大" : "小") + "（应为子节点最小left - 1 = " + (minLeftFromChilds - 1) + "）；<br/>";
		}

		let maxRightFromChjilds = getMaxRightFromChilds(menu.childList, menu.l);
		if (menu.r !== maxRightFromChjilds + 1) {
			menu.warn += (n++ + "、") + "right太" + (menu.r > maxRightFromChjilds + 1 ? "大" : "小") + "（应为子节点最大right + 1 = " + (maxRightFromChjilds + 1) + "）；<br/>";
		}
	} else if (menu.l !== menu.r - 1) {
		menu.warn += (n++ + "、") + "left != right - 1；<br/>";
	}
	if (menu.length % 2 !== 0) {
		menu.warn += (n++ + "、") + "length（即：right - left + 1）不是偶数；";
	}

	if (par) {
		if (menu.rootId !== par.rootId) menu.warn += (n++ + "、") + "rootId != parent.rootId；<br/>";
		if (menu.pid !== par.id) menu.warn += (n++ + "、") + "pid != parent.id；<br/>";
		if (menu.level !== par.level + 1) menu.warn += (n++ + "、") + "level != parent.level + 1；<br/>";

		// 上面已经校验过左右值与子节点的大小正确性，以下这四项无需再校验。
		// if (menu.l <= par.l) menu.warn += (n++ + "、") + "left <= parent.left；<br/>";
		// if (menu.l >= par.r) menu.warn += (n++ + "、") + "left >= parent.right；<br/>";
		// if (menu.r <= par.l) menu.warn += (n++ + "、") + "right <= parent.left；<br/>";
		// if (menu.r >= par.r) menu.warn += (n++ + "、") + "right >= parent.right；<br/>";
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
			showError("加载数据失败", XMLHttpRequest);
		}
	});
}

function buildPyramid(menuList, panelId) {
	let $panel = $("#" + panelId);
	$panel.html("");
	for (let i = 0; i < menuList.length; i++) {
		let root = menuList[i];
		setWarn(root);

		// 计算最大的right
		let maxRight = root.r + root.r % 2;
		let maxRightFromChilds = getMaxRightFromChilds(root.childList, root.r);
		if (maxRight < maxRightFromChilds) maxRight = maxRightFromChilds;
		maxRight += maxRight % 2;

		// 创建一根树的倒金字塔图
		let $pyramid = $('<div class="pyramid" id="pyramid_' + root.id + '"></div>').width(size * maxRight + 1);

		// 往倒金字塔图中添加节点
		addChildNodes([root], $pyramid);

		// 给倒金字塔图添加尺码条
		for (let n = 1; n <= maxRight; n++) {
			let $num = $('<div class="num"></div>')
				.css("left", ((n - 1) * size - 1) + "px")
				.html(n);
			$pyramid.append($num);
		}

		// 将倒金字塔展示到页面上
		$panel.append($pyramid);
	}
	return $panel;
}

function addChildNodes(menuList, $pyramid, par) {
	for (let i = 0; i < menuList.length; i++) {
		let menu = menuList[i];
		setWarn(menu, par)

		if (!menu.warn) setWarn(menu,)
		$pyramid.append(buildNode(menu));
		let height = (menu.level + 1) * size + 1;
		if ($pyramid.height() < height) {
			$pyramid.height(height);
		}

		addChildNodes(menu.childList, $pyramid, menu);
	}
}

function buildNode(menu) {
	let title = menu.name;
	if (menu.warn) {
		title += "\r\n\r\n数据异常警告：\r\n" + menu.warn.replaceAll("<br/>", "\r\n");
	}
	let $node = $('<div class="node' + (menu.warn ? ' warn' : "") + '" id="node_' + menu.id + '" menuid="' + menu.id + '" left="' + menu.l + '" right="' + menu.r + '" title="' + title + '">' +
		'<div class="left-num">' + menu.l + '</div>' +
		'<div class="right-num">' + menu.r + '</div>' +
		'</div>');

	// 设置尺寸和位置
	$node
		.css("left", (size * (menu.l - 1) - 1) + "px")
		.css("top", (size * menu.level) + "px")
		.width(size * menu.length - 1);

	// 设置鼠标移进移出事件
	$node
		.mouseover(function () {
			highlight(this, "#row_");
		})
		.mouseout(function () {
			unHighlight(this, "#row_");
		});

	// 为节点对应的数据行设置鼠标移进移出事件
	$("#row_" + menu.id)
		.mouseover(function () {
			highlight(this, "#node_");
		})
		.mouseout(function () {
			unHighlight(this, "#node_");
		});

	return $node;
}

// 高亮显示
function highlight(node, refIdPre) {
	let $node = $(node);
	$node.addClass("highlight");
	let menuid = $node.attr("menuid");
	$(refIdPre + menuid).addClass("highlight");
}

// 取消高亮显示
function unHighlight(node, refIdPre) {
	let $node = $(node);
	$node.removeClass("highlight");
	let menuid = $node.attr("menuid");
	$(refIdPre + menuid).removeClass("highlight");
}