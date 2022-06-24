layui.config({
    base: '/static/layuiadmin/' //静态资源所在路径
}).extend({
    index: 'lib/index' //主入口模块
}).use(['index', 'element', 'admin'], function () {
    var $ = layui.$
        , setter = layui.setter
        , admin = layui.admin
        , form = layui.form
        , router = layui.router()
        , element = layui.element
        , search = router.search;
    admin.req({
        url: '/system',
        done: function (res) {
            layui.data("system", {key: "name", value: res.data.name});
            layui.data("system", {key: "version", value: res.data.version});
        }
    });
    //获取所有的菜单
    $.ajax({
        type: "GET",
        url: "/menu?userId=" + layui.data('user').userId,
        dataType: "json",
        success: function (data) {
            if (data.code == 0) {
                //先添加所有的主材单
                var content = '';
                $.each(data.data, function (i, obj) {
                        var dataName = '';
                        if (obj.id != undefined) {
                            dataName = obj.id;
                        }
                        content += '<li data-name="' + dataName + '" class="layui-nav-item">';
                        if (obj.list != null && obj.list.length > 0) {
                            content += '<a href="javascript:;"  lay-tips="' + obj.title + '" lay-direction="2">';
                            content += '<i class="layui-icon ' + obj.icon + '"></i>';
                            content += '<cite>' + obj.title + '</cite>';
                            content += '</a>';
                            content += loadChild(dataName, obj);
                        } else {
                            content += '<a href="javascript:;" lay-href="' + obj.href + '" lay-direction="2" lay-tips="' + obj.title + '">';
                            content += '<i class="layui-icon ' + obj.icon + '"></i>';
                            content += '<cite>' + obj.title + '</cite>';
                            content += '</a>';
                        }
                        content += '</li>';
                    }
                );
                $(".layui-nav-tree").html(content);
                element.init();
            } else {
                alert("发生错误：" + data.msg);
            }
        },
        error: function (err) {
            alert("发生错误：" + err.status);
        }
    });

    //组装子菜单的方法
    function loadChild(root, obj) {
        if (obj == null) {
            return;
        }
        var content = '';
        content += '<dl class="layui-nav-child">';
        if (obj.list != null && obj.list.length > 0) {
            $.each(obj.list, function (i, note) {
                var dataName = root;
                if (note.id != undefined) {
                    dataName += "/" + note.id;
                }
                content += '<dd data-name="' + dataName + '">';
                if (note.list != null && note.list.length > 0) {
                    content += '<a href="javascript:;" >' + note.title + '</a>';
                    content += loadChild(dataName, note);
                } else {
                    content += '<a lay-href="' + note.href + '">' + note.title + '</a>'
                }
                content += '</dd>';
            });
        } else {
            var dataName = root;
            if (obj.id != undefined) {
                dataName += "/" + obj.id;
            }
            content += '<dd data-name="' + dataName + '" data-jump="' + obj.href + '">';
            content += '<a href="javascript:;" lay-href="' + obj.href + '">' + obj.title + '</a>'
            content += '</dd>';
        }
        content += '</dl>';
        return content;
    }
});