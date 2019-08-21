//存放主要交互逻辑js代码
//javascript 模块化

var seckill = {
    //封装秒杀相关ajax的url
    //否则在js中与后端通信的url就随处可见
    //js 的模块化
    //动态语言 函数也是对象
    URL : {
        now :  function(){
            return '/seckill/time/now';
        },
        exposer : function(seckillId, node){
            return '/seckill/' + seckillId + '/exposer';
        },
        execution : function(seckillId, md5){
            return '/seckill/' + seckillId + '/' + md5 + '/execution';
        }

    },

    handleSeckill : function(seckillId, node){
        //处理秒杀逻辑
        //后台统一把所有的ajax请求封装到了SeckillResult中，方便处理
        node.hide().html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');

        //jquery发起post请求到后台
        $.post(seckill.URL.exposer(seckillId), {}, function(result){
           //在回调函数中，执行交互流程
            if(result && result['success']){
                var exposer = result['data'];
                if(exposer['exposed']){
                    //开启秒杀
                    //获取秒杀地址
                    var md5 = exposer['md5'];
                    var killUrl = seckill.URL.execution(seckillId, md5);
                    console.log("killUrl:" + killUrl);
                    //绑定一次点击事件  在事件的回调函数中执行秒杀
                    $('#killBtn').one('click', function(){
                       //执行秒杀请求
                        //1.先禁用按钮
                        $(this).addClass('disabled');

                        //2.发送秒杀请求执行秒杀
                        $.post(killUrl, {}, function(result){
                           if(result && result['success']){

                               var killResult = result['data'];
                               var state = killResult['state'];
                               var stateInfo = killResult['stateInfo'];

                               //3.显示秒杀结果
                               //当用户点击按钮，发送post请求
                               node.html('<span class="label label-success">' + stateInfo + "</span>");
                           }


                        });
                    });
                    //按钮显示
                    node.show();

                }else{
                    //未开启秒杀 可能是因为客户端服务器时间不一致
                    //重新计算计时逻辑
                    var now = exposer['now'];
                    var start = exposer['start'];
                    var end = exposer['end'];

                    seckill.countdown(seckillId, now ,start, end);
                }
            }else{
                console.log("result" + result);
            }

        });

    },
    //验证手机号

    validatePhone : function(phone){
        if(phone && phone.length == 11 && !isNaN(phone)){
            return true;
        }else{
            return false;
        }
    },

    //时间判断逻辑 单独拿出来
    countdown : function(seckillId, nowTime, startTime, endTime){
        var seckillBox = $('#seckill-box');

        //时间判断
        if(nowTime > endTime){
            //秒杀结束
            seckillBox.html('秒杀结束');
        }else if(nowTime < startTime){
            //秒杀未开始，计时事件绑定
            var killTime = new Date(startTime + 1000);
            seckillBox.countdown(killTime, function(event){
                //时间格式
                //时间变动会触发事件，格式化输出 以killTime作为基准时间
                //插件的使用方式
                var format = event.strftime('秒杀倒计时: %D天 %H时 %M分 %S秒');
                seckillBox.html(format);
                //时间完成后回调事件
                //因为这个插件只负责计时，后面结束会自动停住
            }).on('finish.countdown', function(){

                //获取秒杀地址，控制显示逻辑,显示秒杀按钮,执行秒杀
                seckill.handleSeckill(seckillId, seckillBox);

            });
        }else{
            //秒杀开始
            //也是要执行获取秒杀地址相关的逻辑
            seckill.handleSeckill(seckillId, seckillBox);


        }
    },


    //详情页秒杀逻辑

    detail : {
        //详情页初始化
        init : function(params){
            //手机验证和登录， 计时交互
            //规划我们的交互流程
            //在cookie中查找手机号

            var killPhone = $.cookie('killPhone');
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];

            //验证手机号
            if( !seckill.validatePhone(killPhone)){
                //绑定phone
                //控制输出
                var killPhoneModal = $('#killPhoneModal');
                //显示弹出层
                killPhoneModal.modal({
                    show : true, //显示弹出层
                    backdrop : 'static', //禁止位置关闭
                    keyboard : false //关闭键盘事件
                });
                $('#killPhoneBtn').click(function(){
                    var inputPhone = $('#killPhoneKey').val();
                    console.log('inputPhone=' + inputPhone); //TODO
                    if(seckill.validatePhone(inputPhone)){
                        //电话写入cookie
                        $.cookie('killPhone', inputPhone, {expires:7, path: '/seckill'});
                        //刷新页面
                        window.location.reload();
                    }else{
                        $('#killPhoneMessage').hide().html('<label class="label label-danger">手机号错误!</label>').show(300);
                    }
                });
            }

            //已经登录
            //计时交互
            //首先通过一个ajax请求后端拿到系统时间
            //result 是 ajax请求结果

            $.get(seckill.URL.now(), {}, function(result){
                //判断请求结果是否有问题
                if(result && result['success']){

                    var nowTime = result['data'];
                    //时间判断
                    seckill.countdown(seckillId, nowTime, startTime, endTime);

                }else{
                    console.log('result:' +  result);
                }

            });

        }



    }


}