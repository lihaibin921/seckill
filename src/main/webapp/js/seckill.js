//存放主要交互逻辑的js代码
var seckill = {
    //封装秒杀相关ajax的url
    URL: {
        now: function () {
            return '/seckill/time/now';
        },
        exposer: function (seckillId) {
            return '/seckill/' + seckillId + '/exposer';
        },
        execution: function (seckillId, md5) {
            return '/seckill/' + seckillId + '/' + md5 + '/execution';
        }
    },

    handleSeckillKill: function (seckillId, node) {
        //处理秒杀逻辑
        node.hide()
            .html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');
        $.post(seckill.URL.exposer(seckillId), {}, function (result) {
            //在回调函数中执行交互流程
            if (result && result['success']) {
                var exposer = result['data'];
                if (exposer['exposed']) {
                    //开启秒杀
                    //获取秒杀地址
                    var md5 = exposer['md5'];
                    var killUrl = seckill.URL.execution(seckillId, md5);
                    console.log('killUrl ' + killUrl);

                    //给秒杀按钮绑定事件 绑定一次点击事件
                    $('#killBtn').one('click', function () {
                        //1 直接禁用按钮 你点一次就行了
                        $(this).addClass('disabled');
                        //2 发送秒杀请求
                        $.post(killUrl, {}, function (result) {
                            //alert(result['success']);

                            if (result && result['success']) {
                                var killResult = result['data'];
                                var state = killResult['state'];

                                var stateInfo = killResult['stateInfo'];

                                //alert(stateInfo);
                                node.html('<span class="label label-success">' + stateInfo + '</span>');
                            }
                        });
                    });
                    node.show();
                } else {
                    //未开启秒杀 (可能是用户前端计时与我们服务器有偏差了)
                    var now = exposer['now'];
                    var start = exposer['start'];
                    var end = exposer['end'];
                    //让用户重新计时
                    seckill.countdown(seckillId, now, start, end);
                }
            } else {
                console.log('result ' + result);
            }
        });

    },

    //验证手机号
    validatePhone: function (phone) {
        if (phone && phone.length == 11 && !isNaN(phone)) {
            return true;
        } else {
            return false;
        }
    },

    countdown: function (seckillId, nowTime, startTime, endTime) {
        //console.log('seckillId ' + seckillId + ' nowTime ' + nowTime + ' startTime ' + startTime + ' endTime ' + endTime);
        var seckillBox = $('#seckill-box');

        //时间判断
        if (nowTime > endTime) {
            //秒杀已经结束
            seckillBox.html("秒杀结束!!!");
        } else if (nowTime < startTime) {
            //秒杀未开始 , 计时时间绑定
            var killTime = new Date(startTime + 1000);

            //(这个函数是jquery.countdown中的)
            seckillBox.countdown(killTime, function (event) {
                var format = event.strftime('秒杀计时 : %D天 %H时 %M分 %S秒');

                seckillBox.html(format);
                //完成倒计时后回调事件
            }).on('finish.countdown', function () {
                //获取秒杀地址 , 控制显示逻辑 , 给用户一个点击按钮用来秒杀
                seckill.handleSeckillKill(seckillId, seckillBox);
            });
        } else {
            //秒杀开始
            seckill.handleSeckillKill(seckillId, seckillBox);
        }
    },

    //详情页秒杀逻辑
    detail: {
        //详情页初始化
        init: function (params) {
            //手机验证和登陆 , 计时交互
            //从cookie中查找手机号
            var killPhone = $.cookie('killPhone');

            //验证手机号 即是否登陆
            if (!seckill.validatePhone(killPhone)) {
                //绑定手机号
                //控制输出
                var killPhoneModal = $('#killPhoneModal');
                killPhoneModal.modal({
                    show: true,//显示弹出层
                    backdrop: 'static',//禁止位置关闭 点击其他位置弹出层也不会关闭
                    keyboard: false//关闭键盘事件
                });

                $('#killPhoneBtn').click(function () {
                    var inputPhone = $('#killPhoneKey').val();
                    if (seckill.validatePhone(inputPhone)) {
                        //电话写入cookie
                        $.cookie('killPhone', inputPhone, {expires: 7, path: '/seckill'});
                        //刷新页面
                        window.location.reload();
                    } else {
                        $('#killPhoneMessage').hide().html('<label class="label label-danger">手机号错误!</label>').show(300);
                    }
                });
            }

            //已经登陆 开启计时
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];

            $.get(seckill.URL.now(), {}, function (result) {
                if (result && result['success']) {
                    //alert(result['data']);
                    var nowTime = result['data'];
                    //时间判断
                    seckill.countdown(seckillId, nowTime, startTime, endTime);
                } else {
                    console.log('result:' + result);
                }
            });
        }
    }
}
